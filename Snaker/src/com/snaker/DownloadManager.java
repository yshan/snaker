/**
   Copyright [Shan Yin]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.snaker;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.snaker.Downloader.Status;
import com.snaker.Setting.Proxy;
import com.snaker.ssl.EasySSLProtocolSocketFactory;

public class DownloadManager {
	private static Log logger = LogFactory.getLog(DownloadManager.class);
	private int maxDownloadedCount;
	private Map<String, HttpClient> clients = new ConcurrentHashMap<String, HttpClient>();
	private MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	private ExecutorService defaultPool = null;
	private Collection<Downloader> downloadings = new LinkedBlockingQueue<Downloader>();
	private LinkedBlockingDeque<Downloader> downloaded = new LinkedBlockingDeque<Downloader>();
	private DownloadQueue queue = new DownloadQueue();
	
	static{
		@SuppressWarnings("deprecation")
		Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(),443);
		Protocol.registerProtocol("https", easyhttps); 
	}

	public DownloadManager(Setting s) {
		defaultPool = Executors.newFixedThreadPool(s.getMaxConcurrentDownload());
		maxDownloadedCount = s.getMaxDownloadedCount();
		startQueueConsumeThread();
	}

	public Collection<Downloader> getDownloaded() {
		return downloaded;
	}

	public Collection<Downloader> getDownloading() {
		return downloadings;
	}

	public Collection<Downloader> threadNum() {
		return downloaded;
	}

	public void prepare(Task task) {
		HttpClient client = new HttpClient(connectionManager);
		client.getParams().setParameter("http.protocol.single-cookie-header",
				true);
		client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		Proxy p = task.getProxy();
		if (p != null) {
			client.getHostConfiguration().setProxy(p.getHost(), p.getPort());
		}
		clients.put(task.getId(), client);
	}

	public void clean(Task task) {
		clients.remove(task.getId());
	}

	private Runnable createRunnable(final Downloader d) {
		final Task task = d.getTask();
		return new Runnable() {
			@Override
			public void run() {
				logger.info("start download:" + d.getUrl());
				HttpClient client = clients.get(task.getId());
				DownloadHandler handler = d.getHandler();
				HttpMethodBase m = null;
				d.setStatus(Status.STARTED);
				d.setStartTime(System.currentTimeMillis());
				try {
					String url = d.getUrl();
					if (d.isGet()) {
						GetMethod get = new GetMethod(url);
						m = get;
					} else {
						final String requestCharset = d.getRequestCharset();
						PostMethod post = new PostMethod(url){
							public String getRequestCharSet() {
								if(requestCharset!=null)
									return requestCharset;
								else
									return super.getRequestCharSet();
							}
							public boolean getFollowRedirects() {
				                return true;
				            }
						};
						if(requestCharset!=null){
							post.setRequestHeader("ContentType",
									"application/x-www-form-urlencoded;charset="+requestCharset);
						}
						DownloadParams parms = d.getParms();
						if (parms != null)
							post.setRequestBody(parms.toNVP());
						m = post;
					}
					{ // set the headers
						m.setRequestHeader("User-Agent",
								"Mozilla/5.0 (Windows NT 5.1; rv:8.0.1) Gecko/20100101 Firefox/8.0.1");
						m.setRequestHeader("Accept",
								"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
						m.setRequestHeader("Accept-Language",
								"en-us,zh-cn;q=0.5");
						m.setRequestHeader("Accept-Charset",
								"ISO-8859-1,utf-8;q=0.7,*;q=0.7");
						m.setRequestHeader("Referer", url);
					}
					client.executeMethod(m);
					//check status
					int sc = m.getStatusCode();
					d.setStatusCode(sc);
					
					if(isBadStatusCode(sc)){
						logger.error("download failed,url:" + d.getUrl()+",Status Code:"+sc);
						d.setStatus(Status.FAILED);
						d.setDescription(m.getStatusText());
						return;
					}
					else if(sc == 404 ||sc == 410){
						d.setStatus(Status.FINISHED);
						d.setDescription("NOT FOUND");
						return;
					}
					
					long size = m.getResponseContentLength();
					d.setFileSize(size);
					
					// get File Name
					if (d.getFileName() == null) {
						Header h = m.getResponseHeader("Content-Disposition");
						String fileName = null;
						if (h != null) {
							String f = h.getValue();
							int tag = f.indexOf("filename=");
							if (tag != -1 && tag != f.length() - 1)
								fileName = f.substring(tag + 1);
						}

						if (fileName == null || fileName.length() == 0) {
							int tag1 = url.lastIndexOf("/");
							int tag2 = url.lastIndexOf("?");
							if (tag1 != -1 && tag1 != url.length() - 1) {
								if (tag2 > tag1) {
									fileName = url.substring(tag1 + 1, tag2);
								} else {
									fileName = url.substring(tag1 + 1);
								}
							}
						}
						d.setFileName(fileName);
					}
					
					// set the all headers
					Header[] headers = m.getResponseHeaders();
					if (headers != null) {
						for (Header header : headers) {
							d.addResponseHeader(header.getName(),
									header.getValue());
						}
					}
					d.setStatus(Status.RUNNING);
					// recv the body
					if (handler == null) {
						byte[] content = m.getResponseBody();
						int len = content.length;
						d.setFileSize(len);
						d.setReceived(len);
						d.setResponseCharset(m.getResponseCharSet());
						d.setResponseBody(content);
					} else {
						InputStream is = m.getResponseBodyAsStream();
						handler.start(d);
						byte[] buffer = new byte[102400];
						long count = 0;
						while (true) {
							int r = is.read(buffer);
							if (r > 0) {
								count += r;
								d.setReceived(count);
								handler.handle(buffer, r);
							} else {
								break;
							}
						}
						is.close();
					}
					d.setStatus(Status.FINISHED);
				} catch (Exception e) {
					logger.error("download failed,url:" + d.getUrl(), e);
					d.setStatus(Status.FAILED);
					d.setDescription(e.getMessage());
				} finally {
					m.releaseConnection();
					if (handler != null) {
						handler.stop();
					}
					downloadings.remove(d);
					d.setEndTime(System.currentTimeMillis());
					while (downloaded.size() >= maxDownloadedCount) {
						downloaded.poll();
					}
					downloaded.offer(d);
					task.downloadFininshed(d);
				}
			}
		};
	}

	private static boolean isBadStatusCode(int sc) {
		if(sc<400) return false;
		if(sc==404 || sc==410) return false;
		return true;
	}

	public void start(final Downloader d) throws IOException {
		String host = null;
		try {
			URI uri = new URI(d.getUrl());
			host = uri.getHost();
			if(host!=null){
				host = host.toLowerCase();
			}
			else{
				throw new URISyntaxException(d.getUrl(),"Bad url");
			}
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
		d.setHost(host);

		downloadings.add(d);
		if (d.getHandler() == null) {
			createRunnable(d).run();
		} else {
			queue.put(d);
		}
	}
	
	private void startQueueConsumeThread(){
		new Thread(new Runnable(){
			@Override
			public void run() {
				while(true){
					try {
						Downloader d = queue.poll();
						defaultPool.execute(createRunnable(d));
					} catch (InterruptedException e) {
						logger.error("interrputed",e);
					}
				}
			}
		}).start();
	}
}

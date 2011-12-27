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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Setting {
	private static final String CONF_FILE="snaker.conf.xml";
	private static Log logger = LogFactory.getLog(Setting.class);
	public static Setting load(){
		File f = new File(CONF_FILE);
		if(f.exists()){
			XStream xs = createXStream();
			return (Setting) xs.fromXML(f);
		}
		else{
			return new Setting();
		}
	}
	
	public void save(){
		XStream xs = createXStream();
		String result = xs.toXML(this);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(CONF_FILE);
			fos.write(result.getBytes());
		} catch (IOException e) {
			logger.error("write log failed",e);
		} finally{
			if(fos!=null){
				try {
					fos.close();
				} catch (IOException e) {
					logger.error("close log failed",e);
				}
			}
		}
		
	}
	
	private static XStream createXStream(){
		XStream xs = new XStream(new DomDriver());
		xs.alias("setting", Setting.class);
		xs.alias("proxy", Proxy.class);
		return xs;
	}
	
	//#####proxy######
	private List<Proxy> proxies = new ArrayList<Proxy>();
	public static class Proxy{
		private String name;
		private String host;
		private int port;
		public void setHost(String host) {
			this.host = host;
		}
		public String getHost() {
			return host;
		}
		public void setPort(int port) {
			this.port = port;
		}
		public int getPort() {
			return port;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		
		public Proxy(String name){
			this.name = name;
		}
	}
	
	public void newProxy(Proxy proxy) {
		this.proxies.add(proxy);
	}
	
	public Proxy findProxy(String proxy){
		for(Proxy p:proxies){
			if(p.getName().equals(proxy)){
				return p;
			}
		}
		return null;
	}
	
	public void removeProxy(Proxy proxy){
		removeProxy(proxy.getName());
	}
	
	public void removeProxy(String proxyName){
		int size = proxies.size();
		for(int i=0;i<size;++i){
			Proxy p = proxies.get(i);
			if(p.getName().equals(proxyName)){
				proxies.remove(i);
				break;
			}
		}
	}
	
	public void updateProxy(Proxy proxy){
		int size = proxies.size();
		for(int i=0;i<size;++i){
			Proxy p = proxies.get(i);
			if(p.getName().equals(proxy.getName())){
				proxies.set(i, proxy);
				return;
			}
		}
		newProxy(proxy);
	}

	public List<Proxy> getProxies() {
		return proxies;
	}
	
	//#####Default save folder ####
	private String defaultPath="./";

	public void setDefaultPath(String defaultPath) {
		this.defaultPath = defaultPath;
	}

	public String getDefaultPath() {
		return defaultPath;
	}
	
	//######Download Setting#####
	private static final int MAX_DOWNLOADED_COUNT = 1024; 
	private static final int MAX_CONCURRENT_DOWNLOAD = 50;
	
	private int maxDownloadedCount = MAX_DOWNLOADED_COUNT;
	private int maxConcurrentDownload = MAX_CONCURRENT_DOWNLOAD;
	public void setMaxDownloadedCount(int maxDownloadedCount) {
		this.maxDownloadedCount = maxDownloadedCount;
	}

	public int getMaxDownloadedCount() {
		return maxDownloadedCount;
	}

	public int getMaxConcurrentDownload() {
		return maxConcurrentDownload;
	}

	public void setMaxConcurrentDownload(int maxConcurrentDownload) {
		this.maxConcurrentDownload = maxConcurrentDownload;
	}
}

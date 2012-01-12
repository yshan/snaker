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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Downloader {

	enum Status {
		RUNNING, PENDING,STARTED,FINISHED, CANCELLED, FAILED
	};

	public static class Header {
		public String key;
		public String value;
	}

	private boolean get = true;
	private int statusCode;
	private Status status = Status.PENDING;
	private String url;
	private String description = "";
	private DownloadHandler handler;
	private long fileSize;
	private long received;
	private String fileName;
	private String subFolder;
	private String responseCharset = DEFAULT_RESPONSE_CHARSET;
	private byte[] responseBody;
	private DownloadParams parms;
	private long startTime;
	private long endTime;
	private Task task;
	private String host;
	private String requestCharset;
	// the total speed Statistics time , 10s
	public static long SPEED_WINDOW_SIZE = 10 * 1000L;
	public static long SPEED_UPDATE_FREQ = 1 * 1000L;
	private static final String DEFAULT_RESPONSE_CHARSET="utf8";
	private LinkedList<SpeedRecord> speeds = new LinkedList<SpeedRecord>();
	private DownloadObserver observer = null;

	public DownloadObserver getObserver() {
		return observer;
	}

	public void setObserver(DownloadObserver observer) {
		this.observer = observer;
	}
	public void removeObserver() {
		this.observer = null;
	}

	private static class SpeedRecord {
		long tick;
		long received;

		SpeedRecord(long tick, long received) {
			this.tick = tick;
			this.received = received;
		}
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	private List<Header> responseHeaders = new ArrayList<Header>();

	public int getStatusCode() {
		return statusCode;
	}

	public void addResponseHeader(String key, String value) {
		Header h = new Header();
		h.key = key;
		h.value = value;
		responseHeaders.add(h);
	}

	public String getResponseHeader(String key) {
		for (Header h : responseHeaders) {
			if (key.equals(h.key)) {
				return h.value;
			}
		}
		return null;
	}

	public List<Header> getResponseHeaders() {
		return responseHeaders;
	}
	
	public byte[] getResponseBody(boolean clean){
		byte[] result = responseBody;
		if(clean){
			responseBody = null;
		}
		return result;
	}

	public String getResponseBodyAsString(boolean clean){
		String result;
		try {
			result = new String(responseBody,responseCharset);
		} catch (UnsupportedEncodingException e) {
			result = new String(responseBody);
		}
		if(clean){
			responseBody = null;
		}
		return result;
	}

	public List<String> findInBody(Pattern p) {
		List<String> result = new ArrayList<String>();
		if (responseBody != null) {
			String body = getResponseBodyAsString(true);
			Matcher ms = p.matcher(body);
			while (ms.find()) {
				String s = ms.group();
				result.add(s);
			}
		}
		return result;
	}

	public void setGet(boolean get) {
		this.get = get;
	}

	public boolean isGet() {
		return get;
	}

	public String getUrl() {
		return url;
	}

	public void setStatus(Status status) {
		if(observer!=null){
			observer.statusChanged(this, this.status, status);
		}
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setHandler(DownloadHandler handler) {
		this.handler = handler;
	}

	public DownloadHandler getHandler() {
		return handler;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setResponseBody(byte[] content) {
		this.responseBody = content;
	}

	private synchronized boolean newRecord(long tick, long r) {
		boolean result = false;
		if (speeds.isEmpty()) {
			speeds.add(new SpeedRecord(tick, r));
			result = true;
		} else {
			SpeedRecord last = speeds.getLast();
			if (tick - last.tick >= SPEED_UPDATE_FREQ) {
				speeds.add(new SpeedRecord(tick, r));
				result = true;
			}
		}
		return result;
	}

	private synchronized void removeOutdatedRecords(long tick) {
		while (speeds.size() > 1) {
			SpeedRecord first = speeds.getFirst();
			if (tick - first.tick > SPEED_WINDOW_SIZE) {
				speeds.removeFirst();
			} else {
				break;
			}
		}
	}

	public void setReceived(long received) {
		this.received = received;
		long tick = System.currentTimeMillis();
		if (newRecord(tick, received))
			removeOutdatedRecords(tick);
	}

	public long getReceived() {
		return received;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setParms(DownloadParams parms) {
		this.parms = parms;
	}

	public DownloadParams getParms() {
		return parms;
	}

	public float getProgress() {
		if (fileSize != 0) {
			return ((float) received) / fileSize;
		}
		return -1;
	}

	// UNIT: Bytes/second
	public synchronized long getSpeed() {
		removeOutdatedRecords(System.currentTimeMillis());
		if (speeds.size() > 1) {
			SpeedRecord first = speeds.getFirst();
			SpeedRecord last = speeds.getLast();
			if (first != null && last != null && first != last) {
				return ((last.received - first.received) * 1000L)
						/ (last.tick - first.tick);
			}
		}
		return 0;
	}

	// UNIT:milesSeconds
	public long getTimeLeft() {
		if (fileSize != 0 && startTime != 0 && received != 0) {
			return ((fileSize - received) * (System.currentTimeMillis() - startTime))
					/ received;
		}
		return -1;
	}
	
	// UNIT:milesSeconds
	public long getTimeCost() {
		if (startTime != 0) {
			if(endTime!=0){
				return endTime-startTime;
			}
			else{
				return System.currentTimeMillis()-startTime;
			}
		}
		return -1;
	}

	/**
	 * @param subFolder the subFolder to set
	 */
	public void setSubFolder(String subFolder) {
		if(!subFolder.endsWith(File.separator)){
			subFolder += File.separator;
		}
		this.subFolder = subFolder;
	}

	/**
	 * @return the subFolder
	 */
	public String getSubFolder() {
		return subFolder;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getResponseCharset() {
		return responseCharset;
	}

	public void setResponseCharset(String responseCharset) {
		if(responseCharset!=null && !responseCharset.isEmpty())
			this.responseCharset = responseCharset;
	}

	public String getRequestCharset() {
		return requestCharset;
	}

	public void setRequestCharset(String requestCharset) {
		this.requestCharset = requestCharset;
	}
}

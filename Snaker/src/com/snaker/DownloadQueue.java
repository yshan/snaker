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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.snaker.Downloader.Status;

public class DownloadQueue  implements DownloadObserver  {
	private static final long RETRY_INTERVAL = 600 * 1000L;
	private static class HostQueue{
		int maxConcurrent = 0;
		long nextRetry = 0;
		LinkedList<Downloader> pendings = new LinkedList<Downloader>();
		List<Downloader> started = new ArrayList<Downloader>();
		List<Downloader> running = new ArrayList<Downloader>();
	}

	private Map<String, HostQueue> downloadQueue = new HashMap<String, HostQueue>();
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.snaker.DownloadObserver#statusChanged(com.snaker.Downloader,
	 * com.snaker.Downloader.Status, com.snaker.Downloader.Status)
	 */
	@Override
	public synchronized void statusChanged(Downloader d, Status oldStatus,
			Status newStatus) {
		HostQueue hq = downloadQueue.get(d.getHost());
		if (hq.started.contains(d)) {
			if (newStatus == Status.RUNNING) {
				hq.started.remove(d);
				hq.running.add(d);
				int size = hq.running.size();
				if (size > hq.maxConcurrent) {
					hq.maxConcurrent = size;
				}
			} else if (newStatus == Status.FAILED) {
				hq.nextRetry = System.currentTimeMillis() + RETRY_INTERVAL;
				hq.started.remove(d);
				d.removeObserver();
			} else if (newStatus == Status.FINISHED) {
				hq.started.remove(d);
				d.removeObserver();
			} else if (newStatus == Status.CANCELLED) {
				hq.started.remove(d);
				d.removeObserver();
			}
		} else if (hq.running.contains(d)) {
			if (newStatus == Status.FAILED || newStatus == Status.FINISHED
					|| newStatus == Status.CANCELLED) {
				hq.running.remove(d);
				d.removeObserver();
			}
		}
		this.notifyAll();
	}

	public synchronized void put(Downloader d) {
		HostQueue hq = null;
		String host = d.getHost();
		if (downloadQueue.containsKey(host)) {
			hq = downloadQueue.get(host);
		} else {
			hq = new HostQueue();
			downloadQueue.put(host, hq);
		}
		hq.pendings.add(d);
		this.notifyAll();
	}
	
	synchronized void  dumpQueue(){
		for (Entry<String, HostQueue> q : downloadQueue.entrySet()){
			System.out.println("Host:"+q.getKey());
			HostQueue hq = q.getValue();
			System.out.println("Pending:"+hq.pendings.size());
			System.out.println("Started:"+hq.started.size());
			System.out.println("Running:"+hq.running.size());
			System.out.println("maxConcurrent:"+hq.maxConcurrent);
			System.out.println("nextRetry:"+hq.nextRetry);
		}
	}

	public synchronized Downloader poll() throws InterruptedException {
		Downloader result = null;
		Random r = new Random();
		while (result == null) {
			if(!downloadQueue.isEmpty()){
				HostQueue[] hqs = downloadQueue.values().toArray(new HostQueue[0]);
				int size = hqs.length;
				int tag = r.nextInt(size);
				for(int i=0;i<size;++i,++tag){
					HostQueue hq = hqs[tag%size];
					if (hq.pendings.isEmpty()) {
						continue;
					}
					if (!hq.started.isEmpty()) {
						continue;
					}
					if (hq.maxConcurrent != 0){
						int cz = hq.running.size();
						if(cz > hq.maxConcurrent){
							continue;
						}
						else if(cz == hq.maxConcurrent && System.currentTimeMillis()<hq.nextRetry){
							continue;
						}
					}
					Downloader d = hq.pendings.removeFirst();
					hq.started.add(d);
					result = d;
					result.setObserver(this);
					break;
				}
			}
			if (result == null) {
				this.wait();
			}
		}

		return result;
	}
}

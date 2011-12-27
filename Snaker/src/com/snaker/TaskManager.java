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
import java.util.List;

public class TaskManager {
	private List<Task> tasks = new ArrayList<Task>();
	private DownloadManager downloadManager;
	
	public List<Task> getTasks(){
		return tasks;
	}

	
	public void startTask(Task t){
		long now = System.currentTimeMillis();
		if(t.getId() == null){
			t.setId(now+"");
		}
		t.setStartTime(now);
		t.setDownloadManager(downloadManager);
		t.setStatus(Task.Status.RUNNING);
		tasks.add(t);
		new Thread(t).start();
	}

	public void setDownloadManager(DownloadManager downloadManager) {
		this.downloadManager = downloadManager;
	}

	public DownloadManager getDownloadManager() {
		return downloadManager;
	}
}

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
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class Factory implements ApplicationContextAware {
	private static Factory instance = null;
	private ApplicationContext context;
	private WebServer webServer;
	private Setting setting;
	private TaskManager taskManager;
	private EngineManager engineManager;
	private DownloadManager downloadManager;
	private RecognizerManager recognizerManager;
	
	/**
	 * constructor.
	 */
	public Factory() {
		if (instance != null) {
			throw new RuntimeException("the Factory must be singleton!");
		}
		instance = this;
	}

	/**
	 * Get job factory instance.
	 * 
	 * @return GmsJobFactory
	 */
	public static Factory getInstance() {
		return instance;
	}

	public static Object getBean(String name) {
		return instance.getContext().getBean(name);
	}

	public ApplicationContext getContext() {
		return context;
	}

	/**
	 * Initialize configuration.
	 */
	public static void initConfiguration() {
		/* ApplicationContext applicationContext = */new FileSystemXmlApplicationContext(
				new String[] { "classpath:context.xml", });
	}

	/**
	 * Set application context.
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		context = applicationContext;
	}

	public void setWebServer(WebServer webServer) {
		this.webServer = webServer;
	}

	public WebServer getWebServer() {
		return webServer;
	}

	public void setSetting(Setting setting) {
		this.setting = setting;
	}

	public Setting getSetting() {
		return setting;
	}

	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}

	public void setDownloadManager(DownloadManager downloadManager) {
		this.downloadManager = downloadManager;
	}

	public DownloadManager getDownloadManager() {
		return downloadManager;
	}

	public EngineManager getEngineManager() {
		return engineManager;
	}

	public void setEngineManager(EngineManager engineManager) {
		this.engineManager = engineManager;
	}

	public RecognizerManager getRecognizerManager() {
		return recognizerManager;
	}

	public void setRecognizerManager(RecognizerManager recognizerManager) {
		this.recognizerManager = recognizerManager;
	}
}

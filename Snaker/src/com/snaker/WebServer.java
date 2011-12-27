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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

public class WebServer implements Runnable {
	private static final int DEFAULT_PORT = 9527;
	private Log logger = LogFactory.getLog(getClass());
	private int port = DEFAULT_PORT;
	private Server server = null;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// set up Jetty and run the embedded server
		server = new Server(port);
		server.setSendServerVersion(false);
		server.setSendDateHeader(false);
		server.setStopAtShutdown(true);

		WebAppContext wac = new WebAppContext();
		wac.setContextPath("/");
		wac.setWar("./webapps");
		server.setHandler(wac);
		server.setStopAtShutdown(true);

		try {
			server.start();
		} catch (Exception e) {
			logger.error("start server failed", e);
		}
	}
}


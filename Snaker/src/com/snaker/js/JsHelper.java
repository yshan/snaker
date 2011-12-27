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
package com.snaker.js;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import com.snaker.DownloadParams;
import com.snaker.Downloader;
import com.snaker.Downloader.Header;
import com.snaker.Engine;
import com.snaker.Engine.EngineProperty;

public class JsHelper extends NativeObject {
	private static final long serialVersionUID = 8096759901322275080L;
	private JsTask task;
	private Log logger = LogFactory.getLog(getClass());

	public JsHelper() {
		this.task = JsTask.theTask.get();
		Engine engine = task.getEngine();
		List<EngineProperty> props = engine.getProperties();
		Map<String, String[]> parms = task.getParams();
		for (EngineProperty p : props) {
			this.defineProperty(p.getName(), parms.get(p.getName())[0],
					READONLY);
		}
	}

	@Override
	public String getClassName() {
		return "JsHelper";
	}

	public void jsFunction_save(String url,String subFolder,String fileName) throws IOException {
		if("undefined".equalsIgnoreCase(subFolder)) 
			subFolder = null;
		if("undefined".equalsIgnoreCase(fileName)) 
			fileName = null;
		task.save(url,subFolder,fileName);
	}

	public void jsFunction_print(String s) {
		logger.debug(s);
	}

	public Scriptable jsFunction_post(String url, NativeObject parms)
			throws IOException, IllegalAccessException, InstantiationException,
			InvocationTargetException {
		Set<Entry<Object, Object>> parmsSet = parms.entrySet();
		DownloadParams downloadParams = new DownloadParams();
		for (Entry<Object, Object> pp : parmsSet) {
			downloadParams.addParm(pp.getKey().toString(), pp.getValue()
					.toString());
		}
		Downloader d = task.sendPost(url, downloadParams);
		return createResponse(d);
	}

	public Scriptable jsFunction_get(String url) throws IOException,
			IllegalAccessException, InstantiationException,
			InvocationTargetException {
		Downloader d = task.sendGet(url);
		return createResponse(d);
	}

	private NativeObject createResponse(Downloader d) {
		NativeObject response = new NativeObject();
		response.defineProperty("statusCode", new Integer(d.getStatusCode()),
				READONLY);
		NativeObject headers = new NativeObject();
		List<Header> responseHeaders = d.getResponseHeaders();
		if (responseHeaders != null) {
			for (Header h : responseHeaders) {
				headers.defineProperty(h.key, h.value, READONLY);
			}
		}
		response.defineProperty("headers", headers, READONLY);
		response.defineProperty("body", d.getResponseBodyAsString(true), READONLY);
		return response;
	}
}

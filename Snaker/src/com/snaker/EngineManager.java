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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.snaker.js.JsTask;

public class EngineManager {
	private static final String ENGINE_FOLDER = "engines";
	private static final String ENGINE_CHARSET="utf8";
	List<Engine> engines = new ArrayList<Engine>();
	private static Log logger = LogFactory.getLog(EngineManager.class);

	public void load() {
		File f = new File(ENGINE_FOLDER);
		File[] files = f.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".js"))
					return true;
				else
					return false;
			}

		});
		if (files != null && files.length > 0) {
			for (File ff : files) {
				Engine def = reloadEngine(ff);
				if (def != null) {
					engines.add(def);
				} else {
					logger.error("uncognized engine file:" + ff.getName());
				}
			}
		}
	}

	private static final Pattern NAME_PATTERN = Pattern.compile(
			"^//\\s*@name\\s*(.*)\\s*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern TITLE_PATTERN = Pattern.compile(
			"^//\\s*@title\\s*(.*)\\s*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern DESC_PATTERN = Pattern.compile(
			"^//\\s*@description\\s*(.*)\\s*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern PARAM_PATTERN = Pattern.compile(
			"^//\\s*@parameter\\s*(\\S*)\\s*(.*)\\s*$", Pattern.CASE_INSENSITIVE);

	private Engine reloadEngine(File f) {
		BufferedReader br = null;
		Engine def = new Engine();
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f),ENGINE_CHARSET));
			do {
				String line = br.readLine();
				if (line == null)
					break;
				line = line.trim();
				if (!line.startsWith("//")) {
					break;
				}
				Matcher nameMatcher = NAME_PATTERN.matcher(line);
				if (nameMatcher.matches()) {
					String name = nameMatcher.group(1);
					if (name != null && !name.isEmpty()){
						def.setName(name);
					}
					continue;
				}

				Matcher titleMatcher = TITLE_PATTERN.matcher(line);
				if (titleMatcher.matches()) {
					String title = titleMatcher.group(1);
					if (title != null && !title.isEmpty())
						def.setTitle(title);
					continue;
				}

				Matcher descMatcher = DESC_PATTERN.matcher(line);
				if (descMatcher.matches()) {
					String desc = descMatcher.group(1);
					if (desc != null && !desc.isEmpty())
						def.setDescription(desc);
					continue;
				}

				Matcher paramMatcher = PARAM_PATTERN.matcher(line);
				if (paramMatcher.matches()) {
					String name = paramMatcher.group(1);
					String title = paramMatcher.group(2);
					boolean optional = true;
					if(name.startsWith("*")){ //not optional
						name = name.substring(1);
						optional = false;
					}
					def.addProperty(name, title,optional);
					continue;
				}
			} while (true);

		} catch (Exception ioe) {
			logger.error("parse file failed", ioe);
			return null;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("close file failed", e);
				}
			}
		}
		if(def!=null && def.getName()!=null){ //read successfully
			def.setLastModified(f.lastModified());
			def.setSourceFile(f.getPath());
			return def;
		}
		return null;
	}

	public Task createTask(String engineName, Map<String, String[]> params) {
		Engine engine = getEngine(engineName);
		if(engine!=null){
			return new JsTask(engine,params);
		}
		return null;
	}

	public Engine getEngine(String engineName) {
		Engine result =  null;
		int index = 0;
		for(Engine def:engines){
			if(def.getName().equalsIgnoreCase(engineName)){
				result = def;
				break;
			}
			++index;
		}
		
		if(result!=null){
			File f = new File(result.getSourceFile());
			if(f.exists()){
				if(f.lastModified()>result.getLastModified()){
					result = reloadEngine(f);
					engines.set(index, result);
				}
			}
			else{
				engines.remove(index);
				result = null;
			}
		}
		
		return result;
	}

	public List<Engine> getEngines() {
		return engines;
	}
}

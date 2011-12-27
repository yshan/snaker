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

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.snaker.Engine;
import com.snaker.Task;

public class JsTask extends Task {
	static ThreadLocal<JsTask> theTask = new ThreadLocal<JsTask>();
	private Engine engine;
	public Engine getEngine() {
		return engine;
	}

	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	private Map<String, String[]> params;

	public JsTask(Engine engine, Map<String, String[]> params) {
		this.engine = engine;
		this.setParams(params);
	}
	
	@Override
	protected void execute() throws Exception{
		String sourceCode = engine.readSourceCode();
		Context cx = Context.enter();
		try {
			theTask.set(this);
			Scriptable scope = cx.initStandardObjects();
			ScriptableObject.defineClass(scope, JsHelper.class);
			Scriptable tx = cx.newObject(scope, "JsHelper");
			scope.put("$", scope, tx);
			Script script = cx.compileString(sourceCode, engine.getName(), 1, null);
			script.exec(cx, scope);
		} finally {
			Context.exit();
			theTask.remove();
		}
	}
	
	public Map<String, String[]> getParams() {
		return params;
	}

	public void setParams(Map<String, String[]> params) {
		this.params = params;
	}
}

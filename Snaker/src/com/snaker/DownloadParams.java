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

import org.apache.commons.httpclient.NameValuePair;

public class DownloadParams {
	private static class DownloadParam{
		String key,value;
		DownloadParam(String key,String value){
			this.key = key;
			this.value = value;
		}
	}
	List<DownloadParam> parms = new ArrayList<DownloadParam>();
	public DownloadParams(){
		
	}
	public DownloadParams(String parms){
		String[] kvs = parms.split("&");
		if(kvs!=null){
			for(String kv:kvs){
				int tag = kv.indexOf("=");
				String key = null,value = null;
				if(tag!=-1){
					key = kv.substring(0,tag);
					if(tag!=key.length()-1){
						value=kv.substring(tag+1);
					}
				}
				else{
					key = kv;
				}
				this.parms.add(new DownloadParam(key,value));
			}
		}
	}
	
	public DownloadParams addParm(String key,String value){
		this.parms.add(new DownloadParam(key,value));
		return this;
	}
	
	NameValuePair[] toNVP(){
		NameValuePair[] result = new NameValuePair[parms.size()];
		int index = 0;
		for(DownloadParam parm:parms){
			result[index++]=new NameValuePair(parm.key,parm.value);
		}
		return result;
	}
}

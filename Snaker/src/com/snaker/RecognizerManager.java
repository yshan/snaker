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

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.snaker.ocr.OCR;
import com.snaker.ocr.OCRException;

/**
 * @author sandy_yin
 *
 */
public class RecognizerManager {
	private static Log logger = LogFactory.getLog(RecognizerManager.class);
	private OCR ocr;
	
	public static class RecognizeItem{
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public byte[] getImage() {
			return image;
		}
		public void setImage(byte[] image) {
			this.image = image;
		}
		public String getResult() {
			return result;
		}
		public void setResult(String result) {
			this.result = result;
		}
		public boolean isCompleted() {
			return completed;
		}
		public void setCompleted(boolean completed) {
			this.completed = completed;
		}
		private long id;
		private String url;
		private byte[] image;
		private String result;
		private boolean completed;
	}
	private LinkedList<RecognizeItem> items = new LinkedList<RecognizeItem>();
	
	public String recognize(String url,byte[] image, boolean manual){
		String result = null;
		if(ocr!=null && !manual){
			try {
				result = ocr.recognize(image);
			} catch (OCRException e) {
				logger.error("recognize failed",e);
			}
		}
		
		if(result==null || result.isEmpty()){
			RecognizeItem item = new RecognizeItem();
			item.id = System.nanoTime();
			item.url = url;
			item.image = image;
			item.completed = false;
			synchronized(items){
				items.add(item);
			}
			synchronized(item){ 
				while(!item.completed){
					try {
						item.wait();
					} catch (InterruptedException e) {
						logger.error("wait error",e);
					}
				}
			}
			result=item.result;
		}
		
		return result;
	}
	
	public RecognizeItem peek(){
		synchronized(items){
				return items.peek();
		}
	}
	
	public RecognizeItem findItem(long id){
		synchronized(items){
			for(RecognizeItem item:items){
				if(id == item.id){
					return item;
				}
			}
			return null;
		}
	}
	
	public void complete(long id,String result){
		RecognizeItem ri = null;
		synchronized(items){
			for(RecognizeItem item:items){
				if(id == item.id){
					ri = item;
					item.completed = true;
					item.result = result;
					break;
				}
			}
		}
		
		if(ri!=null){
			synchronized(ri){
				items.remove(ri);
				ri.notifyAll();
			}
		}
	}

	public OCR getOcr() {
		return ocr;
	}

	public void setOcr(OCR ocr) {
		this.ocr = ocr;
	}
}

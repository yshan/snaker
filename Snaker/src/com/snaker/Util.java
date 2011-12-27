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

import java.util.Date;

public class Util {
	public static String formatDate(long t) {
		if(t<=0) return "";
		java.text.DateFormat format1 = new java.text.SimpleDateFormat(
				"hh:mm:ss");
		return format1.format(new Date(t));
	}
	
	public static String formatPeroid(long time) {
		long seconds = time/1000;
		
		long s = seconds % 60;
		long m = (seconds/60) % 60;
		long h = seconds/60/60;
		
		return String.format("%02d:%02d:%02d", h,m,s);
	}
	
	public static String formatSize(long size){
		if(size == -1){
			return "UNKNOWN";
		}
		long kb = size/1024;
		if(kb==0){
			return size+"B";
		}
		long mb = kb/1024;
		if(mb == 0){
			return kb+"KB";
		}
		long gb = mb/1024;
		if(gb == 0){
			return mb+"MB";
		}
		return gb + "GB";
	}
}

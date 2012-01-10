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
package com.snaker.ocr;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Sandy_Yin
 * 
 */
public class TesseractOCR implements OCR {
	private String tessExecutable;
	private Log logger = LogFactory.getLog(getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.snaker.ocr.OCR#recognize(byte[])
	 */
	@Override
	public String recognize(byte[] image) throws OCRException {
		if(tessExecutable == null){
			tessExecutable = getDefaultTessExecutable();
		}
		
		File source = null;
		File dest = null;
		try {
			String prefix = System.nanoTime() + "";
			source = File.createTempFile(prefix, "tess");
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(
					source));
			dos.write(image);
			dos.flush();
			dos.close();
			String sourceFileName = source.getAbsolutePath();
			Process p = Runtime.getRuntime().exec(
					String.format("%s %s %s -l eng", tessExecutable,
							sourceFileName, sourceFileName));
			String destFileName = sourceFileName + ".txt";
			dest = new File(destFileName);
			int result = p.waitFor();
			if (result == 0) {
				BufferedReader in = new BufferedReader(new FileReader(dest));
				StringBuilder sb = new StringBuilder();
				String str;
                while ((str = in.readLine()) != null) {
                	sb.append(str).append("\n");
                }
                in.close();
                return sb.toString().trim();
			} else {
				String msg;
				switch (result) {
				case 1:
					msg = "Errors accessing files. There may be spaces in your image's filename.";
					break;
				case 29:
					msg = "Cannot recognize the image or its selected region.";
					break;
				case 31:
					msg = "Unsupported image format.";
					break;
				default:
					msg = "Errors occurred.";
				}
				throw new OCRException(msg);
			}
		} catch (IOException e) {
			throw new OCRException("recognize failed", e);
		} catch (InterruptedException e) {
			logger.error("interrupted", e);
		} finally {
			if (source != null) {
				source.delete();
			}
			if (dest != null) {
				dest.delete();
			}
		}
		return null;
	}

	private String getDefaultTessExecutable() {
		String result =  null;
		String os = System.getProperty("os.name").toLowerCase();
		if(os.startsWith("win")){
			result = ".\\ocr\\tesseract\\win\\tesseract.exe";
		}
		return result;
	}

	public String getTessExecutable() {
		return tessExecutable;
	}

	public void setTessExecutable(String tessExecutable) {
		this.tessExecutable = tessExecutable;
	}
}

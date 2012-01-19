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
package com.snaker.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

/** For unicode output on windows platform
 * @author Sandy_Yin
 * 
 */
public class Console {
	private static Kernel32 INSTANCE = null;

	public interface Kernel32 extends StdCallLibrary {
		public Pointer GetStdHandle(int nStdHandle);

		public boolean WriteConsoleW(Pointer hConsoleOutput, char[] lpBuffer,
				int nNumberOfCharsToWrite,
				IntByReference lpNumberOfCharsWritten, Pointer lpReserved);
	}

	static {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("win")) {
			INSTANCE = (Kernel32) Native
					.loadLibrary("kernel32", Kernel32.class);
		}
	}

	public static void println(String message) {
		boolean successful = false;
		if (INSTANCE != null) {
			Pointer handle = INSTANCE.GetStdHandle(-11);
			char[] buffer = message.toCharArray();
			IntByReference lpNumberOfCharsWritten = new IntByReference();
			successful = INSTANCE.WriteConsoleW(handle, buffer, buffer.length,
					lpNumberOfCharsWritten, null);
			if(successful){
				System.out.println();
			}
		}
		if (!successful) {
			System.out.println(message);
		}
	}
}

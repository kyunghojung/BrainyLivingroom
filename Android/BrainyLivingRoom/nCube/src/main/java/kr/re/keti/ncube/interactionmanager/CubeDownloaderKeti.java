/*
 * ------------------------------------------------------------------------
 * Copyright 2014 Korea Electronics Technology Institute
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ------------------------------------------------------------------------
 */

package kr.re.keti.ncube.interactionmanager;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * HTTP 기반 다운로드를 제공하기 위한 Class
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class CubeDownloaderKeti {
	
	/**
	 * download Method
	 * @param sourceUrl
	 * @param targetFilename
	 * @param dKey
	 * @throws Exception
	 * HTTP 기반 다운로드 Method로서 /nCube 폴더에 파일 다운로드 기능 제공
	 */
	public static void download(String sourceUrl, String targetFilename, String dKey) throws Exception {
		FileOutputStream fos = null;
		InputStream is = null;
		
		// Windows only
		//fos = new FileOutputStream("c:\\" + targetFilename);
		
		// Linux only
		fos = new FileOutputStream("/nCube/" + targetFilename);
			
		URL url = new URL(sourceUrl);
		URLConnection urlConnection = url.openConnection();
		urlConnection.setRequestProperty("d_key", dKey);
		urlConnection.setDoOutput(true);
		
		is = urlConnection.getInputStream();
		byte[] buffer = new byte[2048];
		int readBytes;
		
		while ((readBytes = is.read(buffer)) != -1) {
			fos.write(buffer, 0, readBytes);
		}
		
		fos.close();
		is.close();
	}
}
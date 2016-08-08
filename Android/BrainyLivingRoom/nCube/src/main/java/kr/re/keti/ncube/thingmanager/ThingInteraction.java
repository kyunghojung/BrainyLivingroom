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

package kr.re.keti.ncube.thingmanager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

/**
 * Thing Adaptation Software로부터 데이터를 수신하고 처리를 위해 ThingInteractionProcess를 Thread로 동작시키는 기능을 수행함
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class ThingInteraction extends Thread {
	
	private BlockingQueue<ArrayList<Object>> resourceManagerQueue;
	private BlockingQueue<ArrayList<Object>> thingManagerResponseQueue;
	
	private ServerSocket thingManagerSocket;
	private Socket thingInteractionSocket;
	
	private byte[] commBuffer = new byte[4096];
	int receiveDataSize = 0;
	private String receiveDataString;
	
	public ThingInteraction(
			BlockingQueue<ArrayList<Object>> resourceQueue,
			BlockingQueue<ArrayList<Object>> myResponseQueue) throws Exception {
		
		this.resourceManagerQueue = resourceQueue;
		this.thingManagerResponseQueue = myResponseQueue;
		this.thingManagerSocket = new ServerSocket(7579);
	}
	
	public void run() {
		while(true) {
			try {
				thingInteractionSocket = thingManagerSocket.accept();
				receiveDataSize = thingInteractionSocket.getInputStream().read(commBuffer);
				receiveDataString = new String(commBuffer, 0, receiveDataSize);
				
				Thread thingInteractionProcess = new ThingInteractionProcess(thingInteractionSocket,
																			receiveDataString,
																			resourceManagerQueue,
																			thingManagerResponseQueue);
				thingInteractionProcess.run();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
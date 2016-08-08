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

package kr.re.keti.ncube.securitymanager;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

/**
 * (Next version 적용 예정) Access Token 발급 및 데이터 암/복호화 Method를 제공함
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class SecurityManager extends Thread {
	
	private BlockingQueue<ArrayList<Object>> securityManagerQueue;
	@SuppressWarnings("unused")
	private BlockingQueue<ArrayList<Object>> resourceManagerQueue;
	
	private ArrayList<Object> securityManagerArrayList;
	
	private static final boolean debugPrint = true;
	
	public SecurityManager(
			BlockingQueue<ArrayList<Object>> myQueue,
			BlockingQueue<ArrayList<Object>> resourceQueue) {
		
		this.securityManagerQueue = myQueue;
		this.resourceManagerQueue = resourceQueue;
	}
	
	public void run() {
		
		if (debugPrint) {
			System.out.println("[SecurityManager] Start");
			System.out.println("[SecurityManager] BlockingQueue wait");
			System.out.println();
		}
		
		while(true) {
			try {
				securityManagerArrayList = securityManagerQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				eventProcess(securityManagerArrayList);
			}
		}
	}
	
	/**
	 * eventProcess Method
	 * @param receivedArrayList
	 * BlockingQueue에서 가져온 데이터를 파싱하고 동작을 수행하기 위한 Method
	 */
	private void eventProcess(ArrayList<Object> receivedArrayList) {
		String msgHeader = (String) receivedArrayList.get(0);
		
		switch(msgHeader) {
		
		case "requestGetPSecurityCode":
			break;
			
		default:
			if (debugPrint) {
				System.out.println("[SecurityManager] Message not found");
			}
			break;
		}
	}
}
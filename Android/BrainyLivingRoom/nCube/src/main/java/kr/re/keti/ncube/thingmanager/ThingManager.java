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

import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import kr.re.keti.ncube.MgmtCmd;

/**
 * Device에 연결되는 사물을 관리하기 위한 Manager Class
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class ThingManager extends Thread {
	
	private BlockingQueue<ArrayList<Object>> thingManagerQueue;
	private BlockingQueue<ArrayList<Object>> thingManagerResponseQueue;
	private BlockingQueue<ArrayList<Object>> resourceManagerQueue;
	
	private ArrayList<Object> thingManagerArrayList;
	private Thread thingInteractionThread;
	
	public static ArrayList<Object> thingASProfiles;
	public static ArrayList<Object> thingProfiles;
	public static ArrayList<Object> thingControls;
	
	private static final boolean debugPrint = true;
	
	public ThingManager(
			BlockingQueue<ArrayList<Object>> myQueue,
			BlockingQueue<ArrayList<Object>> myResponseQueue,
			BlockingQueue<ArrayList<Object>> resourceQueue) throws Exception {
		
		this.thingManagerQueue = myQueue;
		this.thingManagerResponseQueue = myResponseQueue;
		this.resourceManagerQueue = resourceQueue;
		
		thingASProfiles = new ArrayList<Object>();
		thingProfiles = new ArrayList<Object>();
		thingControls = new ArrayList<Object>();
		
		this.thingInteractionThread = new ThingInteraction(resourceManagerQueue,
															thingManagerResponseQueue);  
		this.thingInteractionThread.start();
	}
	
	public void run() {

		if (debugPrint) {
			System.out.println("[ThingManager] Start");
			System.out.println("[ThingManager] BlockingQueue wait");
			System.out.println();
		}
		
		while(true) {
			try {
				thingManagerArrayList = thingManagerQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				eventProcess(thingManagerArrayList);
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
		
		case "requestMgmtCmdControl":
			if (debugPrint) {
				System.out.println("[ThingManager] Receive from Resource Manager - MgmtCmd Control request");
			}
			MgmtCmd receivedControlData = (MgmtCmd) receivedArrayList.get(1);
			thingControl(receivedControlData);

			break;
		
		default:
			if (debugPrint) {
				System.out.println("[ThingManager] Message not found");
			}
			break;
		}
	}
	
	/**
	 * thingControl Method
	 * @param receivedData
	 * Thing Adaptation Software로 Thing Control 요청을 보내는 Method
	 */
	private void thingControl(MgmtCmd receivedData) {
		
		String sendData = "thingControl," + receivedData.execReqArgs;
		
		try {
			if (debugPrint) {
				System.out.println("[ThingManager] Send to Thing Adaptation Software (port : " + receivedData.linkedThingAdaptationSoftware + ", " + sendData + ")");
			}
			Socket thingAdaptationSoftwareSocket = new Socket("localhost",
																Integer.parseInt(receivedData.linkedThingAdaptationSoftware));
			BufferedOutputStream bos = new BufferedOutputStream(thingAdaptationSoftwareSocket.getOutputStream());
			bos.write(sendData.getBytes());
			bos.flush();
			bos.close();
			thingAdaptationSoftwareSocket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
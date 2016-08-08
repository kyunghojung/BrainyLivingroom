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

package kr.re.keti.ncube.devicemanager;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import kr.re.keti.ncube.MgmtCmd;

/**
 * (Next version 보강 예정) Device Management를 위한 Class로서 실제 Device의 reset 등의 동작에 관여함 
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class DeviceManagement extends Thread {
	
	private BlockingQueue<ArrayList<Object>> deviceManagerQueue;
	private BlockingQueue<ArrayList<Object>> resourceManagerQueue;
	
	private ArrayList<Object> deviceManagementArrayList;
	
	@SuppressWarnings("unused")
	private MgmtCmd deviceMgmtCmd;
	@SuppressWarnings("unused")
	private MgmtCmd firmwareUpgradeMgmtCmd;
	@SuppressWarnings("unused")
	private MgmtCmd appInstallMgmtCmd;
	
	private static final boolean debugPrint = true;
	
	public DeviceManagement(
			BlockingQueue<ArrayList<Object>> myQueue,
			BlockingQueue<ArrayList<Object>> resourceQueue,
			MgmtCmd dMgmtCmd,
			MgmtCmd fMgmtCmd,
			MgmtCmd appMgmtCmd) {
		
		this.deviceManagerQueue = myQueue;
		this.resourceManagerQueue = resourceQueue;
		this.deviceMgmtCmd = dMgmtCmd;
		this.firmwareUpgradeMgmtCmd = fMgmtCmd;
		this.appInstallMgmtCmd = appMgmtCmd;
	}
	
	public void run() {
		if (debugPrint) {
			System.out.println("[DeviceManager] Start");
			System.out.println("[DeviceManager] BlockingQueue wait");
			System.out.println();
		}
		
		while(true) {
			try {
				deviceManagementArrayList = deviceManagerQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				eventProcess(deviceManagementArrayList);
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
		String labels;
		MgmtCmd receivedControlData;
		
		switch(msgHeader) {
		
		case "registMgmtCmd":
			if (debugPrint) {
				System.out.println("[DeviceManager] Receive from Resource Manager - MgmtCmd regist request");
			}
			labels = (String) receivedArrayList.get(1);
			receivedControlData = (MgmtCmd) receivedArrayList.get(2);
			
			System.out.println("[DeviceManager] Received labels : " + labels);
			System.out.println("[DeviceManager] Received body : " + receivedControlData.resourceID);
			//thingControl(receivedControlData);

			break;
		
		case "requestMgmtCmdControl":
			if (debugPrint) {
				System.out.println("[DeviceManager] Receive from Resource Manager - MgmtCmd Control request");
			}
			labels = (String) receivedArrayList.get(1);
			receivedControlData = (MgmtCmd) receivedArrayList.get(2);
			
			System.out.println("[DeviceManager] Received labels : " + labels);
			System.out.println("[DeviceManager] Received body : " + receivedControlData.resourceID + "," + receivedControlData.execReqArgs);
			//thingControl(receivedControlData);
			
			if (labels.equals("firmwareUpgrade")) {
				ArrayList<Object> resourceSendArrayList = new ArrayList<Object>();
				resourceSendArrayList.add("requestFirmwareUpgrade");
				resourceSendArrayList.add(receivedControlData.execReqArgs);
				
				try {
					resourceManagerQueue.put(resourceSendArrayList);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
			
		case "requestFirmwareSet":
			// firmware config modify
			// device reset
			if (debugPrint) {
				System.out.println("[DeviceManager] Receive Set firmware information...");
			}
			
			ArrayList<Object> resourceSendArrayList = new ArrayList<Object>();
			resourceSendArrayList.add("requestFirmwareUpdate");

			try {
				if (debugPrint) {
					System.out.println("[DeviceManager] Send to Resource Man ager - Firmware Update request");
					System.out.println();
				}
				
				resourceManagerQueue.put(resourceSendArrayList);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		
		default:
			if (debugPrint) {
				System.out.println("[DeviceManager] Message not found");
			}
			break;
		}
	}
}
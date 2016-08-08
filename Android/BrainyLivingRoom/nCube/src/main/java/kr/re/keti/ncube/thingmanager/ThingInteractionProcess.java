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
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;

import kr.re.keti.ncube.Container;
import kr.re.keti.ncube.ContentInstance;
import kr.re.keti.ncube.MgmtCmd;
import kr.re.keti.ncube.ThingASProfile;

/**
 * Thing Adaptation Software�κ��� �����͸� �����Ͽ� ó���ϴ� ����� �����ϸ� ThingInteraction Class�� ���� ȣ���
 * @author NakMyoung Sung (nmsung@keti.re.kr) 
 */
public class ThingInteractionProcess extends Thread {
	
	private final String receiveDataString;
	private BlockingQueue<ArrayList<Object>> resourceManagerQueue;
	private BlockingQueue<ArrayList<Object>> thingManagerResponseQueue;
	private Socket thingInteractionSocket;
	
	private static final boolean debugPrint = true;
	
	public ThingInteractionProcess(Socket mySocket,
								String receiveData,
								BlockingQueue<ArrayList<Object>> resourceQueue,
								BlockingQueue<ArrayList<Object>> myResponseQueue) {
		this.thingInteractionSocket = mySocket;
		this.receiveDataString = receiveData;
		this.resourceManagerQueue = resourceQueue;
		this.thingManagerResponseQueue = myResponseQueue;
	}
	
	public void run() {
		try {
			receiveDataInterpreter(receiveDataString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * receiveDataInterpreter Method
	 * @param receiveData
	 * @throws Exception
	 * Thing Adaptation Software�κ��� ���ŵ� �����͸� �ؼ��ϴ� ����� ������
	 */
	private void receiveDataInterpreter(String receiveData) throws Exception {
		StringTokenizer st = new StringTokenizer(receiveData, ";");
		String msgHeader = st.nextToken();
		String msgBody = st.nextToken();
		
		switch(msgHeader) {

		case "requestTASRegistration":
			if (debugPrint) {
				System.out.println("[ThingManager] Receive to Thing Adaptation Software - TAS Registration request");
			}
			ThingASProfile registTASProfile = new ThingASProfile();
			registTASProfile = ThingInteractionRequestParser.thingASRegistration(msgBody);
			
			addTASprofile(registTASProfile);
			break;
		
		case "requestThingRegistration":
			if (debugPrint) {
				System.out.println("[ThingManager] Receive to Thing Adaptation Software - Thing Registration request");
			}
			Container registThingProfile = new Container();
			registThingProfile = ThingInteractionRequestParser.thingRegistration(msgBody);
			
			requestThingRegistration(registThingProfile);
			break;
		
		case "requestThingDataUpload":
			if (debugPrint) {
				System.out.println("[ThingManager] Receive to Thing Adaptation Software - Thing Data Upload request");
			}
			ContentInstance uploadThingData = new ContentInstance();
			uploadThingData = ThingInteractionRequestParser.uploadThingData(msgBody);
			
			requestThingDataUpload(uploadThingData);
			break;
			
		case "requestThingControlRegistration":
			if (debugPrint) {
				System.out.println("[ThingManager] Receive to Thing Adaptation Software - Thing Control Registration request");
			}
			MgmtCmd registThingControl = new MgmtCmd();
			registThingControl = ThingInteractionRequestParser.thingControlRegistration(msgBody);
			
			requestThingControlRegistration(registThingControl);
			break;
		
		default:
			if (debugPrint) {
				System.out.println("[ThingManager] Message not found");
			}
			break;
		}
	}
	
	/**
	 * addTASProfile Method
	 * @param addTASProfile
	 * @throws Exception
	 * Thing Adaptation Software�� ��ϰ����� ���� Method
	 */
	public void addTASprofile(ThingASProfile addTASProfile) throws Exception {
		int numberOfTAS = ThingManager.thingASProfiles.size();
		boolean registTAS = false;
		String responseString = null;
		
		for (int i = 0; i < numberOfTAS; i++) {
			ThingASProfile tempTASProfile = (ThingASProfile) ThingManager.thingASProfiles.get(i);
			
			if (tempTASProfile.name.equals(addTASProfile.name))	{
				registTAS = true;
				ThingManager.thingASProfiles.set(i, addTASProfile);
				break;
			}
			else registTAS = false;
		}
		
		if (!registTAS) {
			ThingManager.thingASProfiles.add(addTASProfile);
			responseString = "registSuccess;ThingAdaptationSoftware";
			
			if (debugPrint) System.out.println("[ThingManager] Add the Thing Adaptation Software Profile");
		}
		else {
			System.out.println("[ThingManager] Thing Adaptation Software already registerd");
			responseString = "registFailed;AlreadyRegisterd";
		}
		
		if (debugPrint) System.out.println("[ThingManager] Send to Thing Adaptation Software - TAS Registration Result\n");
		
		responseThingAdaptationSoftware(thingInteractionSocket, responseString);
	}
	
	/**
	 * requestThingRegistration Method
	 * @param receivedThingProfile
	 * Resource Manager�� Thing Registration�� ��û�ϴ� Method
	 * @throws Exception 
	 */
	public void requestThingRegistration(Container receivedThingProfile) throws Exception {
		int numberOfThings = ThingManager.thingProfiles.size();
		boolean registThing = false;
		String responseString = null;
		
		for (int i = 0; i < numberOfThings; i++) {
			Container tempThingProfile = (Container) ThingManager.thingProfiles.get(i);
			
			if (tempThingProfile.labels.equals(receivedThingProfile.labels)) {
				registThing = true;
				break;
			}
			else registThing = false;
		}
		
		if (!registThing) {
			Container tempProfile = new Container();
			ArrayList<Object> resourceSendArrayList = new ArrayList<Object>();
			ArrayList<Object> resourceReceiveArrayList = new ArrayList<Object>();
			resourceSendArrayList.add("requestThingRegistration");
			resourceSendArrayList.add(receivedThingProfile);
			
			if (debugPrint) System.out.println("[ThihngManager] Send to Resource Manager - Request Thing Registration\n");
			
			resourceManagerQueue.put(resourceSendArrayList);
			
			resourceReceiveArrayList = thingManagerResponseQueue.take();
			tempProfile = (Container) resourceReceiveArrayList.get(0);
		
			if (debugPrint) System.out.println("[ThingManager] Receive to Resource Manager - Response Thing Registration");
			
			ThingManager.thingProfiles.add(tempProfile);
			
			responseString = "registSuccess;" + receivedThingProfile.labels +"," + receivedThingProfile.resourceID;
		}
		else {
			System.out.println("[ThingManager] Thing Profile is already registerd");
			responseString = "registFailed;" + receivedThingProfile.labels +"," + receivedThingProfile.resourceID + ";AlreadyRegisterd";
		}
		
		if (debugPrint) System.out.println("[ThingManager] Send to Thing Adaptation Software - Thing Registration Result\n");
		
		responseThingAdaptationSoftware(thingInteractionSocket, responseString);
	}
	
	/**
	 * requestThingDataUpload Method
	 * @param receivedThingData
	 * Resource Manager�� Thing Data Upload�� ��û�ϴ� Method
	 * @throws Exception 
	 */
	public void requestThingDataUpload(ContentInstance receivedThingData) throws Exception {
		ArrayList<Object> resourceSendArrayList = new ArrayList<Object>();
		resourceSendArrayList.add("requestThingDataUpload");
		resourceSendArrayList.add(receivedThingData);
		
		if (debugPrint) System.out.println("[ThihngManager] Send to Resource Manager - Request Thing Registration\n");
		
		resourceManagerQueue.put(resourceSendArrayList);
	}
	
	/**
	 * requestThingControlRegistration Method
	 * @param receivedThingControl
	 * Resource Manager�� Thing Control Object ����� ��û�ϴ� Method
	 * @throws Exception 
	 */
	public void requestThingControlRegistration(MgmtCmd receivedThingControl) throws Exception {
		int numberOfThingControls = ThingManager.thingControls.size();
		boolean registThingControls = false;
		String responseString = null;
		
		for (int i = 0; i < numberOfThingControls; i++) {
			MgmtCmd tempThingControl = (MgmtCmd) ThingManager.thingControls.get(i);
			
			if (tempThingControl.labels.equals(receivedThingControl.labels)) {
				registThingControls = true;
				ArrayList<Object> resourceSendArrayList = new ArrayList<Object>();
				ArrayList<Object> resourceReceiveArrayList = new ArrayList<Object>();
				resourceSendArrayList.add("requestMgmtCmdCreate");
				resourceSendArrayList.add(receivedThingControl);
				
				if (debugPrint) System.out.println("[ThihngManager] Send to Resource Manager - Request Thing Control Registration\n");
				
				resourceManagerQueue.put(resourceSendArrayList);
				resourceReceiveArrayList = thingManagerResponseQueue.take();
				tempThingControl = (MgmtCmd) resourceReceiveArrayList.get(0);
				
				if (debugPrint) System.out.println("[ThingManager] Receive to Resource Manager - Response Thing Control Registration");
				
				ThingManager.thingControls.set(i, tempThingControl);
				break;
			}
			
			else registThingControls = false;
		}
		
		if (!registThingControls) {
			MgmtCmd tempThingControl = new MgmtCmd();
			ArrayList<Object> resourceSendArrayList = new ArrayList<Object>();
			ArrayList<Object> resourceReceiveArrayList = new ArrayList<Object>();
			resourceSendArrayList.add("requestMgmtCmdCreate");
			resourceSendArrayList.add(receivedThingControl);
			
			if (debugPrint) System.out.println("[ThingManager] Send to Resource Manager - Request Thing Control Registration\n");
			
			resourceManagerQueue.put(resourceSendArrayList);
			resourceReceiveArrayList = thingManagerResponseQueue.take();
			tempThingControl = (MgmtCmd) resourceReceiveArrayList.get(0);
			
			if (debugPrint) System.out.println("[ThingManager] Receive to Resource Manager - Response Thing Control Registration");
			
			ThingManager.thingControls.add(tempThingControl);
			
			responseString = "registSuccess;" + tempThingControl.labels;
		}
		else {
			if (debugPrint) System.out.println("[ThingManager] Thing Control is already registerd");
			responseString = "registFailed;" + receivedThingControl.labels + ";AlreadyRegisterd";
		}
		
		if (debugPrint) System.out.println("[ThingManager] Send to Thing Adaptation Software - Thing Control Registration Result\n");
		
		responseThingAdaptationSoftware(thingInteractionSocket, responseString);
	}
	
	/**
	 * responseThingAdaptationSoftware Method
	 * @param responseString
	 * @throws Exception
	 * Thing Adaptation Software�� ��û�� ���� ������ ������ Method
	 */
	public void responseThingAdaptationSoftware(Socket responseSocket, String responseString) throws Exception {
		BufferedOutputStream bos = new BufferedOutputStream(responseSocket.getOutputStream());
		bos.write(responseString.getBytes());
		bos.flush();
		bos.close();
		responseSocket.close();
	}
}
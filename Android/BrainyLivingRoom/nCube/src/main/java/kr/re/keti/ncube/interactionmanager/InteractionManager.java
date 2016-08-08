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

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;

import kr.re.keti.ncube.AE;
import kr.re.keti.ncube.Base64;
import kr.re.keti.ncube.CSEBase;
import kr.re.keti.ncube.Container;
import kr.re.keti.ncube.ContentInstance;
import kr.re.keti.ncube.DeviceInfo;
import kr.re.keti.ncube.Firmware;
import kr.re.keti.ncube.MgmtCmd;
import kr.re.keti.ncube.Software;

/**
 * 외부에서 Device에 대해 접근하는 요청을 관리하는 Manager
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class InteractionManager extends Thread {
	
	private BlockingQueue<ArrayList<Object>> interactionManagerQueue;
	private BlockingQueue<ArrayList<Object>> interactionManqgerResponseQueue;
	private BlockingQueue<ArrayList<Object>> resourceManagerQueue;
	private BlockingQueue<ArrayList<Object>> resourceManagerResponseQueue;
	
	private ArrayList<Object> interactionManagerArrayList;
	
	// MQTT client initialize...
	private String mqttServerUrl = "tcp://";
	private MqttClientKeti mqttClient;
	private String requestFrom = null;
	private String inCSEAddress = null;
	
	private static final boolean debugPrint = true;
	
	private InteractionRequest interactionRequest;
	private InteractionResponseParser interactionResponseParser;
	
	public InteractionManager(
			BlockingQueue<ArrayList<Object>> myQueue,
			BlockingQueue<ArrayList<Object>> myResponseQueue,
			BlockingQueue<ArrayList<Object>> resourceQueue,
			BlockingQueue<ArrayList<Object>> resourceResponseQueue,
			String requestFromUrl,
			String inCSEIP,
			String mqttBrokerIP) {
		
		this.interactionManagerQueue = myQueue;
		this.interactionManqgerResponseQueue = myResponseQueue;
		this.resourceManagerQueue = resourceQueue;
		this.resourceManagerResponseQueue = resourceResponseQueue;
		this.requestFrom = requestFromUrl;
		this.inCSEAddress = inCSEIP;
		this.mqttServerUrl = this.mqttServerUrl + mqttBrokerIP + ":1883";
		
		this.mqttClient = new MqttClientKeti(mqttServerUrl, interactionManagerQueue);
		this.interactionRequest = new InteractionRequest(inCSEAddress);
		this.interactionResponseParser = new InteractionResponseParser();
	}
	
	public void run() {

		if (debugPrint) {
			System.out.println("[InteractionManager] Start");
			System.out.println("[InteractionManager] BlockingQueue wait");
			System.out.println();
		}
		
		while(true) {
			try {
				interactionManagerArrayList = interactionManagerQueue.take();
				eventProcess(interactionManagerArrayList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	/**
	 * eventProcess Method
	 * @param receivedArrayList
	 * @throws Exception
	 * BlockingQueue에서 가져온 데이터를 파싱하고 동작을 수행하기 위한 Method
	 */
	private void eventProcess(ArrayList<Object> receivedArrayList) throws Exception {
		String msgHeader = (String) receivedArrayList.get(0);
		
		switch(msgHeader) {
		
		case "requestCSERegistration":
			if (debugPrint) {
				System.out.println("[InteractionManager] CSE Registration start...");
			}
			setCSERegistration((CSEBase) receivedArrayList.get(1));

			break;

		case "requestFirmwareCreate":
			if (debugPrint) {
				System.out.println("[InteractionManager] Firmware Create start...");
			}
			setFirmwareCreate((Firmware) receivedArrayList.get(1),
								(CSEBase) receivedArrayList.get(2));

			break;
			
		case "requestFirmwareUpdate":
			if (debugPrint) {
				System.out.println("[InteractionManager] Firmware Update start...");
			}
			setFirmwareUpdate((Firmware) receivedArrayList.get(1),
								(CSEBase) receivedArrayList.get(2));

			break;
			
		case "requestDeviceInfoCreate":
			if (debugPrint) {
				System.out.println("[InteractionManager] DeviceInfo Create start...");
			}
			setDeviceInfoCreate((DeviceInfo) receivedArrayList.get(1),
								(CSEBase) receivedArrayList.get(2));

			break;
			
		case "requestThingRegistration":
			if (debugPrint) {
				System.out.println("[InteractionManager] Thing Registration start...");
			}
			setContainerRegistration((Container) receivedArrayList.get(1),
								(CSEBase) receivedArrayList.get(2));

			break;
			
		case "requestMgmtCmdCreate":
			if (debugPrint) {
				System.out.println("[InteractionManager] MgmtCmd Registration start...");
			}
			setMgmtCmdRegistration((MgmtCmd) receivedArrayList.get(1),
										(CSEBase) receivedArrayList.get(2));

			break;
			
		case "requestThingDataUpload":
			if (debugPrint) {
				System.out.println("[InteractionManager] Thing Data Upload start...");
			}
			setThingDataUpload((ContentInstance) receivedArrayList.get(1),
								(CSEBase) receivedArrayList.get(2));

			break;
			
		case "requestMgmtCmdControl":
			if (debugPrint) {
				System.out.println("[InteractionManager] Receive mgmtCmd Control request...");
			}
			setMgmtCmdControl((String) receivedArrayList.get(1));

			break;
			
		case "requestFirmwareUpgrade":
			if (debugPrint) {
				System.out.println("[InteractionManager] Receive Firmware Upgrade request...");
			}
			requestFirmwareUpgrade((String) receivedArrayList.get(1));
			
			break;
			
		case "requestAECreate":
			if (debugPrint) {
				System.out.println("[InteractionManager] Receive AE Create request...");
			}
			setAECreate((AE) receivedArrayList.get(1),
					(CSEBase) receivedArrayList.get(2));
			
			break;
			
		case "requestAEContainerCreate":
			if (debugPrint) {
				System.out.println("[InteractionManager] Receive AE Container Create request...");
			}
			setAEContainerCreate((Container) receivedArrayList.get(1),
							(CSEBase) receivedArrayList.get(2));
			
			break;
			
		case "requestAEContentInstanceCreate":
			if (debugPrint) {
				System.out.println("[InteractionManager] Receive AE ContentInstance Create request...");
			}
			setAEContentInstanceCreate((ContentInstance) receivedArrayList.get(1),
								(CSEBase) receivedArrayList.get(2));
			
			break;
			
		case "requestAppDownload":
			if (debugPrint) {
				System.out.println("[InteractionManager] Receive Application Download request...");
			}
			requestApplicationDownload((String) receivedArrayList.get(1));
			
			break;
			
		case "requestSoftwareCreate":
			if (debugPrint) {
				System.out.println("[InteractionManager] Receive Software Create request...");
			}
			setSoftwareCreate((Software) receivedArrayList.get(1),
							(CSEBase) receivedArrayList.get(2));
			
			break;
			
		default:
			if (debugPrint) {
				System.out.println("[InteractionManager] Message not found");
			}
			break;
		}
	}
	
	/**
	 * setCSERegistration Method
	 * @param receivedProfile
	 * @throws Exception
	 * Mobius 플랫폼으로 CSE Registration 요청을 수행하는 Method
	 */
	public void setCSERegistration(CSEBase receivedProfile) throws Exception {

		String registrationEntity = interactionRequest.CSERegistrationMessage(receivedProfile);
		
		System.out.println(registrationEntity);
		
		CSEBase updateProfile = interactionResponseParser.CSERegistration(receivedProfile, registrationEntity);
		
		ArrayList<Object> responseArrayList = new ArrayList<Object>();
		responseArrayList.add(updateProfile);
		
		if (debugPrint) {
			System.out.println("[InteractionManager] CSE Registration Success");
		
			System.out.println("[InteractionManager] MQTT Client Subscribe : " + updateProfile.CSEID);
		}
		mqttClient.subscribe(updateProfile.CSEID);
		
		if (debugPrint) {
			System.out.println("[InteractionManager] Send to Resource Manager - CSE Registration Result");
			System.out.println();
		}
		
		resourceManagerResponseQueue.put(responseArrayList);

	}
	
	/**
	 * setFirmwareCreeate Method
	 * @param receivedInfo
	 * @param receivedCSEProfile
	 * @throws Exception
	 * Mobius 플랫폼으로 Firmware Create 요청을 수행하는 Method
	 */
	public void setFirmwareCreate(Firmware receivedInfo,
								CSEBase receivedCSEProfile) throws Exception {

		String registrationEntity = interactionRequest.firmwareCreateMessage(receivedInfo, receivedCSEProfile);
		Firmware updateInfo = interactionResponseParser.firmwareCreateParse(receivedInfo, registrationEntity);
		
		ArrayList<Object> responseArrayList = new ArrayList<Object>();
		responseArrayList.add(updateInfo);
		
		if (debugPrint) {
			System.out.println("[InteractionManager] Firmware Create Success");

			System.out.println("[InteractionManager] Send to Resource Manager - Firmware Create Result");
			System.out.println();
		}
		
		resourceManagerResponseQueue.put(responseArrayList);

	}
	
	/**
	 * setFirmwareUpdate Method
	 * @param receivedInfo
	 * @param receivedCSEProfile
	 * @throws Exception
	 * Mobius 플랫폼으로 Firmware Update 결과를 전송하는 Method
	 */
	public void setFirmwareUpdate(Firmware receivedInfo,
			CSEBase receivedCSEProfile) throws Exception {

		String registrationEntity = interactionRequest.firmwareUpdateMessage(receivedInfo, receivedCSEProfile);
		Firmware updateInfo = interactionResponseParser.firmwareCreateParse(receivedInfo, registrationEntity);
		
		ArrayList<Object> responseArrayList = new ArrayList<Object>();
		responseArrayList.add(updateInfo);
		
		if (debugPrint) {
			System.out.println("[InteractionManager] Firmware Update Success");
		
			System.out.println("[InteractionManager] Send to Resource Manager - Firmware Update Result");
			System.out.println();
		}

}
	
	/**
	 * setDeviceInfoCreate
	 * @param receivedInfo
	 * @param receivedCSEProfile
	 * @throws Exception
	 * Mobius 플랫폼으로 DeviceInfo Create 요청을 수행하는 Method
	 */
	public void setDeviceInfoCreate(DeviceInfo receivedInfo,
									CSEBase receivedCSEProfile) throws Exception {

		String registrationEntity = interactionRequest.deviceInfoCreateMessage(receivedInfo, receivedCSEProfile);
		DeviceInfo updateInfo = interactionResponseParser.deviceInfoCreateParse(receivedInfo, registrationEntity);
		
		ArrayList<Object> responseArrayList = new ArrayList<Object>();
		responseArrayList.add(updateInfo);
		
		if (debugPrint) {
		System.out.println("[InteractionManager] DeviceInfo Create Success");
		
		System.out.println("[InteractionManager] Send to Resource Manager - DeviceInfo Create Result");
		System.out.println();
		}
		
		resourceManagerResponseQueue.put(responseArrayList);
	
	}
	
	/**
	 * setContainerRegistration Method
	 * @param receivedContainerProfile
	 * @param receivedCSEProfile
	 * @throws Exception
	 * Mobius 플랫폼으로 Container Registration 요청을 수행하는 Method
	 */
	public void setContainerRegistration(Container receivedContainerProfile,
									CSEBase receivedCSEProfile) throws Exception {

		String registrationEntity = interactionRequest.containerCreateMessage(receivedContainerProfile, receivedCSEProfile);
		
		receivedContainerProfile = interactionResponseParser.containerCreateParse(registrationEntity);
		
		ArrayList<Object> responseArrayList = new ArrayList<Object>();
		responseArrayList.add(receivedContainerProfile);
		
		if (debugPrint) {
			System.out.println("[InteractionManager] Container Registration Success");
		
			System.out.println("[InteractionManager] Send to Resource Manager - Container Registration Result");
			System.out.println();
		}
		
		resourceManagerResponseQueue.put(responseArrayList);

	}
	
	/**
	 * setMgmtCmdRegistration Method
	 * @param receivedThingControl
	 * @param receivedCSEProfile
	 * @throws Exception
	 * Mobius 플랫폼으로 mgmtCmd Registration 요청을 수행하는 Method
	 */
	public void setMgmtCmdRegistration(MgmtCmd receivedThingControl,
											CSEBase receivedCSEProfile) throws Exception {

		String registrationEntity = interactionRequest.mgmtCmdRegistrationMessage(receivedThingControl, receivedCSEProfile);
		receivedThingControl = interactionResponseParser.mgmtCmdCreateParse(registrationEntity);
		
		ArrayList<Object> responseArrayList = new ArrayList<Object>();
		responseArrayList.add(receivedThingControl);
		
		if (debugPrint) {
			System.out.println("[InteractionManager] MgmtCmd Registration Success");
		
			System.out.println("[InteractionManager] Send to Resource Manager - mgmtCmd Registration Result");
			System.out.println();
		}
		
		resourceManagerResponseQueue.put(responseArrayList);

	}
	
	/**
	 * setThingDataUpload Method
	 * @param receivedThingData
	 * @param receivedCSEProfile
	 * @throws Exception
	 * Mobius Mashup 플랫폼으로 Thing Data Upload를 요청하는 Method
	 */
	public void setThingDataUpload(ContentInstance receivedThingData, CSEBase receivedCSEProfile) throws Exception {

		String registrationEntity = interactionRequest.contentInstanceCreate(receivedThingData, receivedCSEProfile);

		System.out.println(registrationEntity);
		
		if (debugPrint) {
			System.out.println("[InteractionManager] Thing Data Upload Success");
		}
	}
	
	/**
	 * setMgmtCmdControl Method
	 * @param receivedControlData
	 * @throws Exception
	 * Resource Manager로 MgmtCmd Control Data를 전송하는 Method
	 */
	public void setMgmtCmdControl(String receivedControlData) throws Exception {
		
		StringTokenizer st = new StringTokenizer(receivedControlData, ",");
		String labels = st.nextToken();
		String mgmtCmdBody = st.nextToken();
		
		while (st.hasMoreTokens()) {
			mgmtCmdBody = mgmtCmdBody + "," + st.nextToken();
		}
		
		mgmtCmdBody = mgmtCmdBody.substring(55);
		
		MgmtCmd mgmtCmdUpdateRequest = interactionResponseParser.mgmtCmdRequestParse(mgmtCmdBody);
		mgmtCmdUpdateRequest.labels = labels;
		
		String deviceKey = setDeviceKeyRequest(mgmtCmdUpdateRequest.execTarget);
		
		setThingControlStatus(mgmtCmdUpdateRequest, deviceKey);
		
		ArrayList<Object> resourceManagerArrayList = new ArrayList<Object>();
		resourceManagerArrayList.add("requestMgmtCmdControl");
		resourceManagerArrayList.add(mgmtCmdUpdateRequest);
		
		resourceManagerQueue.put(resourceManagerArrayList);

	}
	
	/**
	 * setThingControlStatus Method
	 * @param receivedThingControl
	 * @param deviceKey
	 * @throws Exception
	 * Thing Control 요청에 대한 성공 여부를 업로드 하는 Method
	 */
	public void setThingControlStatus(MgmtCmd receivedThingControl,
			String deviceKey) throws Exception {

		String registrationEntity = interactionRequest.mgmtCmdUpdateMessage(receivedThingControl, deviceKey, requestFrom);
		
		if (debugPrint) {
			System.out.println("[InteractionManager] Thing Control Status Upload Success");
			System.out.println(registrationEntity);
		}
	}
	
	/**
	 * setDeviceKeyRequest Method
	 * @param deviceId
	 * @return deviceKey
	 * @throws Exception
	 * Resource Manager로 Device Key 정보를 요청하는 Method
	 */
	public String setDeviceKeyRequest(String deviceId) throws Exception {
		if (debugPrint) {
			System.out.println("[InteractionManager] Send to Resource Manager - request device key");
			System.out.println();
		}
		ArrayList<Object> resourceManagerArrayList = new ArrayList<Object>();
		resourceManagerArrayList.add("requestDeviceKey");
		resourceManagerArrayList.add(deviceId);
		
		resourceManagerQueue.put(resourceManagerArrayList);
		
		ArrayList<Object> interactionManagerResponseArrayList;
		
		interactionManagerResponseArrayList = interactionManqgerResponseQueue.take();
		
		return (String) interactionManagerResponseArrayList.get(0);
	}
	
	/**
	 * requestFirmwareUpgrade Method
	 * @param firmwareInfo
	 * @throws Exception
	 * Mobius로 Firmware 다운로드를 수행하는 Method
	 */
	@SuppressWarnings("static-access")
	public void requestFirmwareUpgrade(String firmwareInfo) throws Exception {
		Firmware requestInfo = new Firmware();
		requestInfo = interactionResponseParser.firmwareCreateParse(requestInfo, firmwareInfo);
		
		requestInfo.url = Base64.decode(requestInfo.url);
		
		int fileNameIndex = requestInfo.url.lastIndexOf("=") + 1;
		String fileName = requestInfo.url.substring(fileNameIndex);
		String dKey = setDeviceKeyRequest(requestInfo.parentID);

		if (debugPrint) {
			System.out.println("[InteractionManager] Firmware Download start : " + fileName);
			System.out.println();
		}
		
		CubeDownloaderKeti firmwareDownloader = new CubeDownloaderKeti();
		firmwareDownloader.download(requestInfo.url, fileName, dKey);
		
		if (debugPrint) {
			System.out.println("[InteractionManager] Firmware Download success");
			System.out.println();
		}
		
		requestInfo.fileName = fileName;
		
		ArrayList<Object> resourceManagerArrayList = new ArrayList<Object>();
		resourceManagerArrayList.add("setFirmwareInfo");
		resourceManagerArrayList.add(requestInfo);
		
		resourceManagerQueue.put(resourceManagerArrayList);
	}
	
	/**
	 * setAECreate Method
	 * @param receivedProfile
	 * @param receivedCSEProfile
	 * @throws Exception
	 * oneM2M AE resource create를 지원하기 위한 Method 
	 */
	public void setAECreate(AE receivedProfile,
			CSEBase receivedCSEProfile) throws Exception {

		String registrationEntity = interactionRequest.aeCreateMessage(receivedProfile, receivedCSEProfile);
		receivedProfile = interactionResponseParser.aeCreateParse(registrationEntity);
		
		ArrayList<Object> responseArrayList = new ArrayList<Object>();
		responseArrayList.add(receivedProfile);
		responseArrayList.add(registrationEntity);
		
		if (debugPrint) {
		System.out.println("[InteractionManager] AE Create Success");
		
		System.out.println("[InteractionManager] Send to Resource Manager - AE Create Result");
		System.out.println();
		}
		
		resourceManagerResponseQueue.put(responseArrayList);

	}
	
	/**
	 * setAEContainerCreate Method
	 * @param receivedProfile
	 * @param receivedCSEProfile
	 * @throws Exception
	 * oneM2M AE Container resource create를 지원하기 위한 Method
	 */
	public void setAEContainerCreate(Container receivedProfile,
			CSEBase receivedCSEProfile) throws Exception {
		
		String registrationEntity = interactionRequest.aeContainerCreateMessage(receivedProfile, receivedCSEProfile);
		receivedProfile = interactionResponseParser.containerCreateParse(registrationEntity);
		
		ArrayList<Object> responseArrayList = new ArrayList<Object>();
		responseArrayList.add(receivedProfile);
		responseArrayList.add(registrationEntity);
		
		if (debugPrint) {
		System.out.println("[InteractionManager] AE container Create Success");
		
		System.out.println("[InteractionManager] Send to Resource Manager - AE container Create Result");
		System.out.println();
		}
		
		resourceManagerResponseQueue.put(responseArrayList);
	}
	
	/**
	 * setAEContentInstanceCreate Method
	 * @param receivedInstance
	 * @param receivedCSEProfile
	 * @throws Exception
	 * oneM2M AE contentInstance resource create를 지원하기 위한 Method
	 */
	public void setAEContentInstanceCreate(ContentInstance receivedInstance,
			CSEBase receivedCSEProfile) throws Exception {
		
		int registrationCode = interactionRequest.aeContentInstanceCreateMessage(receivedInstance, receivedCSEProfile);
		
		if (registrationCode == 201) {
			ArrayList<Object> responseArrayList = new ArrayList<Object>();
			responseArrayList.add("create");
			
			if (debugPrint) {
			System.out.println("[InteractionManager] AE contentInstance Create Success");
			
			System.out.println("[InteractionManager] Send to Resource Manager - AE contentInstance Create Result");
			System.out.println();
			}
			
			resourceManagerResponseQueue.put(responseArrayList);
		}
		
		else {
			ArrayList<Object> responseArrayList = new ArrayList<Object>();
			responseArrayList.add("failed");
			
			if (debugPrint) {
				System.out.println("[InteractionManager] AE contentInstance Create Failed");
				
				System.out.println("[InteractionManager] Send to Resource Manager - AE contentInstance Create Result");
				System.out.println();
				}
				
			resourceManagerResponseQueue.put(responseArrayList);
		}
	}
	
	/**
	 * requestApplicationDownload Method
	 * @param applicationInfo
	 * @throws Exception
	 * Device Application (AE)를 다운로드하기 위한 Method
	 */
	@SuppressWarnings("static-access")
	public void requestApplicationDownload(String applicationInfo) throws Exception {
		Software requestInfo = new Software();
		requestInfo = interactionResponseParser.applicationDownloadParse(applicationInfo);
		
		requestInfo.url = Base64.decode(requestInfo.url);
		
		int fileNameIndex = requestInfo.url.lastIndexOf("=") + 1;
		String fileName = requestInfo.url.substring(fileNameIndex);
		String dKey = setDeviceKeyRequest(requestInfo.parentID);

		if (debugPrint) {
			System.out.println("[InteractionManager] Application Download start : " + fileName);
			System.out.println();
		}
		
		CubeDownloaderKeti applicationDownloader = new CubeDownloaderKeti();
		applicationDownloader.download(requestInfo.url, fileName, dKey);
		
		if (debugPrint) {
			System.out.println("[InteractionManager] Application Download success");
			System.out.println();
		}
		
		requestInfo.fileName = fileName;
		
		ArrayList<Object> resourceManagerArrayList = new ArrayList<Object>();
		resourceManagerArrayList.add("setAppInfo");
		resourceManagerArrayList.add(requestInfo);
		
		resourceManagerQueue.put(resourceManagerArrayList);
	}
	
	/**
	 * setSoftwareCreate Method
	 * @param receivedInfo
	 * @param receivedCSEProfile
	 * @throws Exception
	 * oneM2M Software resource create를 지원하기 위한 Method
	 */
	public void setSoftwareCreate(Software receivedInfo,
					CSEBase receivedCSEProfile) throws Exception {

		String registrationEntity = interactionRequest.softwareCreateMessage(receivedInfo, receivedCSEProfile);
		Software updateInfo = interactionResponseParser.softwareCreateParse(registrationEntity);
		
		ArrayList<Object> responseArrayList = new ArrayList<Object>();
		responseArrayList.add(updateInfo);
		
		if (debugPrint) {
		System.out.println("[InteractionManager] Firmware Create Success");
		
		System.out.println("[InteractionManager] Send to Resource Manager - Firmware Create Result");
		System.out.println();
		}
		
		resourceManagerResponseQueue.put(responseArrayList);
		
	}
}
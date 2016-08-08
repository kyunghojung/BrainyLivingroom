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

package kr.re.keti.ncube.resourcemanager;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import kr.re.keti.ncube.*;


/**
 * Device 내의 Resource에 대한 관장을 담당하는 Manager
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class ResourceManager extends Thread {
	
	private BlockingQueue<ArrayList<Object>> deviceManagerQueue;
	private BlockingQueue<ArrayList<Object>> resourceManagerQueue;
	private BlockingQueue<ArrayList<Object>> resourceManagerResponseQueue;
	private BlockingQueue<ArrayList<Object>> interactionManagerQueue;
	private BlockingQueue<ArrayList<Object>> interactionManagerResponseQueue;
	private BlockingQueue<ArrayList<Object>> thingManagerQueue;
	private BlockingQueue<ArrayList<Object>> thingManagerResponseQueue;
	private BlockingQueue<ArrayList<Object>> applicationManagerQueue;
	@SuppressWarnings("unused")
	private BlockingQueue<ArrayList<Object>> securityManagerQueue;
	private BlockingQueue<ArrayList<Object>> httpServerQueue;
	
	private ArrayList<Object> resourceManagerArrayList;
	
	private ResourceAccess resourceDB;
	
	private static final boolean debugPrint = true;
	
	public ResourceManager(
			BlockingQueue<ArrayList<Object>> deviceQueue,
			BlockingQueue<ArrayList<Object>> myQueue,
			BlockingQueue<ArrayList<Object>> myResponseQueue,
			BlockingQueue<ArrayList<Object>> interactionQueue,
			BlockingQueue<ArrayList<Object>> interactionResponseQueue,
			BlockingQueue<ArrayList<Object>> thingQueue,
			BlockingQueue<ArrayList<Object>> thingResponseQueue,
			BlockingQueue<ArrayList<Object>> appQueue,
			BlockingQueue<ArrayList<Object>> securityQueue,
			BlockingQueue<ArrayList<Object>> httpQueue) {
		
		this.deviceManagerQueue = deviceQueue;
		this.resourceManagerQueue = myQueue;
		this.resourceManagerResponseQueue = myResponseQueue;
		this.interactionManagerQueue = interactionQueue;
		this.interactionManagerResponseQueue = interactionResponseQueue;
		this.thingManagerQueue = thingQueue;
		this.thingManagerResponseQueue = thingResponseQueue;
		this.applicationManagerQueue = appQueue;
		this.securityManagerQueue = securityQueue;
		this.httpServerQueue = httpQueue;
		
		resourceDB = new ResourceAccess();
	}
	
	
	/**
	 * Resource Manager run Method
	 * Resource Manager Thread Run 시 불리는 함수로 Resource Manager의 동작을 수행함
	 */
	public void run() {
		if (debugPrint) {
			System.out.println("[ResourceManager] Start");
			System.out.println("[ResourceManager] BlockingQueue wait");
			System.out.println();
		}
		
		while(true) {
			try {
				resourceManagerArrayList = resourceManagerQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				eventProcess(resourceManagerArrayList);
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
		
		case "requestCSERegistration":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive Device Registration request");
			}
			requestCSERegistration((CSEBase) receivedArrayList.get(1));
			break;
			
		case "requestFirmwareCreate":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive Firmware Create request");
			}
			requestFirmwareCreate((Firmware) receivedArrayList.get(1));
			break;
			
		case "requestFirmwareUpdate":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive Firmware Update request");
			}
			requestFirmwareUpdate();
			break;
		
		case "requestDeviceInfoCreate":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive DeviceInfo Create request");
			}
			requestDeviceInfoCreate((DeviceInfo) receivedArrayList.get(1));
			break;
			
		case "requestThingRegistration":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive Thing Registration request");
			}
			thingRegistration((Container) receivedArrayList.get(1));
			break;
			
		case "requestMgmtCmdCreate":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive mgmtCmd Registration request");
			}
			mgmtCmdRegistration((MgmtCmd) receivedArrayList.get(1));
			break;
			
		case "requestThingDataUpload":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive Thing Data Upload request");
			}
			requestThingDataUpload((ContentInstance) receivedArrayList.get(1));
			break;
			
		case "requestMgmtCmdControl":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive MgmtCmd Control request");
			}
			requestMgmtCmdControl((MgmtCmd) receivedArrayList.get(1));
			break;
			
		case "requestDeviceKey":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive Device Key request");
			}
			requestDeviceKey();
			break;
			
		case "requestAECreate":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive AE Create request...");
			}
			requestAECreate((AE) receivedArrayList.get(1));
			break;
			
		case "requestAEContainerCreate":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive AE Container Create request...");
			}
			requestAEContainerCreate((Container) receivedArrayList.get(1));
			break;
			
		case "requestAEContentInstanceCreate":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive AE ContentInstance Create request...");
			}
			requestAEContentInstanceCreate((ContentInstance) receivedArrayList.get(1));
			break;
			
		case "requestFirmwareUpgrade":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive Firmware Upgrade request...");
			}
			 requestFirmwareUpgrade((String) receivedArrayList.get(1));
			
			break;
			
		case "setFirmwareInfo":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive Set firmware information...");
			}
			setFirmwareInfo((Firmware) receivedArrayList.get(1));
			
			break;
			
		case "requestAppDownload":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive Application Download request...");
			}
			requestApplicationDownload((String) receivedArrayList.get(1));
			
			break;
			
		case "setAppInfo":
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive Set Application information...");
			}
			setAppInfo((Software) receivedArrayList.get(1));
			
			break;
			
		default:
			if (debugPrint) {
				System.out.println("[ResourceManager] Message not found");
			}
			break;
		}
	}
	
	
	/**
	 * requestCSERegistration Method
	 * @return receiveProfile
	 * Interaction Manager로 CSE 등록을 요청하는 Method로서 Mobius로부터 수신한 CSE Profile을 리턴함
	 */
	public CSEBase requestCSERegistration(CSEBase receivedProfile) {
		ArrayList<Object> interactionSendArrayList = new ArrayList<Object>();
		ArrayList<Object> interactionReceiveArrayList = new ArrayList<Object>();
		CSEBase receiveProfile = new CSEBase();
		interactionSendArrayList.add("requestCSERegistration");
		interactionSendArrayList.add(receivedProfile);
		
		try {
			if (debugPrint) {
				System.out.println("[ResourceManager] Send to Interaction Manager - CSE Registration");
				System.out.println();
			}
			
			interactionManagerQueue.put(interactionSendArrayList);
			
			interactionReceiveArrayList = resourceManagerResponseQueue.take();
			receiveProfile = (CSEBase) interactionReceiveArrayList.get(0);
			
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive from Interaction Manager - CSE Registration Result");
			}
			
			resourceDB.setCSEProfile(receiveProfile);
			
			if (debugPrint) {
				System.out.println("[ResourceManager] CSE Registration... OK");
				System.out.println();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return receiveProfile;
	}
	
	/**
	 * requestFirmwareCreate Method
	 * @param receivedFirmwareInfo
	 * @return receiveInfo
	 * Interaction Manager로 Firmware Create를 요청하는 Method로서 Mobius로부터 수신한 Firmware Create 정보를 리턴함
	 */
	public Firmware requestFirmwareCreate(Firmware receivedFirmwareInfo) {
		CSEBase tempCSEProfile = resourceDB.getCSEProfile();
		ArrayList<Object> interactionSendArrayList = new ArrayList<Object>();
		ArrayList<Object> interactionReceiveArrayList = new ArrayList<Object>();
		Firmware receiveInfo = new Firmware();
		interactionSendArrayList.add("requestFirmwareCreate");
		interactionSendArrayList.add(receivedFirmwareInfo);
		interactionSendArrayList.add(tempCSEProfile);
		
		try {
			if (debugPrint) {
				System.out.println("[ResourceManager] Send to Interaction Manager - Firmware Create");
				System.out.println();
			}
			
			interactionManagerQueue.put(interactionSendArrayList);
			
			interactionReceiveArrayList = resourceManagerResponseQueue.take();
			receiveInfo = (Firmware) interactionReceiveArrayList.get(0);
			
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive from Interaction Manager - Firmware Create Result");
			}
			
			resourceDB.setFirmwareInformation(receiveInfo);
			
			if (debugPrint) {
				System.out.println("[ResourceManager] Firmware Create... OK");
				System.out.println();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return receiveInfo;
	}
	
	/**
	 * requestFirmwareUpdate Method
	 * Device Firmware 정보를 업데이트 하기 위한 Method
	 */
	public void requestFirmwareUpdate() {
		CSEBase tempCSEProfile = resourceDB.getCSEProfile();
		Firmware tempFirmwareInfo = resourceDB.getFirmwareInformation();
		ArrayList<Object> interactionSendArrayList = new ArrayList<Object>();
		interactionSendArrayList.add("requestFirmwareUpdate");
		interactionSendArrayList.add(tempFirmwareInfo);
		interactionSendArrayList.add(tempCSEProfile);
		
		try {
			if (debugPrint) {
				System.out.println("[ResourceManager] Send to Interaction Manager - Firmware Update");
				System.out.println();
			}
			
			interactionManagerQueue.put(interactionSendArrayList);
			
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive from Interaction Manager - Firmware Update Result");
			}
			
			if (debugPrint) {
				System.out.println("[ResourceManager] Firmware Create... OK");
				System.out.println();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * requestDeviceInfoCreate Method
	 * @param receivedDeviceInfo
	 * @return receiveInfo
	 * Interaction Manager로 DeviceInfo Create를 요청하는 Method로서 Mobius로부터 수신한 DeviceInfo Create 정보를 리턴함
	 */
	public DeviceInfo requestDeviceInfoCreate(DeviceInfo receivedDeviceInfo) {
		CSEBase tempCSEProfile = resourceDB.getCSEProfile();
		ArrayList<Object> interactionSendArrayList = new ArrayList<Object>();
		ArrayList<Object> interactionReceiveArrayList = new ArrayList<Object>();
		DeviceInfo receiveInfo = new DeviceInfo();
		interactionSendArrayList.add("requestDeviceInfoCreate");
		interactionSendArrayList.add(receivedDeviceInfo);
		interactionSendArrayList.add(tempCSEProfile);
		
		try {
			if (debugPrint) {
				System.out.println("[ResourceManager] Send to Interaction Manager - DeviceInfo Create");
				System.out.println();
			}
			
			interactionManagerQueue.put(interactionSendArrayList);
			
			interactionReceiveArrayList = resourceManagerResponseQueue.take();
			receiveInfo = (DeviceInfo) interactionReceiveArrayList.get(0);
			
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive from Interaction Manager - DeviceInfo Create Result");
			}
			
			resourceDB.setDeviceInformation(receiveInfo);
			
			if (debugPrint) {
				System.out.println("[ResourceManager] DeviceInfo Create... OK");
				System.out.println();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return receiveInfo;
	}
	
	
	/**
	 * thingRegistration Method 
	 * @param receivedProfile
	 * Thing Registration 요청이 등어올 때 사용하는 Method
	 */
	private void thingRegistration(Container receivedProfile) {
		CSEBase tempCSEProfile = resourceDB.getCSEProfile();
		Container tempThingProfile = requestThingRegistration(receivedProfile, tempCSEProfile);
		
		resourceDB.setThingProfile(tempThingProfile); 
		
		if (debugPrint) {
			System.out.println("[ResourceManager] Thing Registration... OK");
		}
		
		ArrayList<Object> registThingProfile = new ArrayList<Object>();
		registThingProfile.add(tempThingProfile);
		
		if (debugPrint) {
			System.out.println("[ResourceManager] Return to Thing Registration Result");
			System.out.println();
		}
		
		try {
			thingManagerResponseQueue.put(registThingProfile);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void mgmtCmdRegistration(MgmtCmd receivedThingControl) {
		CSEBase tempDeviceProfile = resourceDB.getCSEProfile();
		
		MgmtCmd tempMgmtCmd = requestMgmtCmdRegistration(receivedThingControl, tempDeviceProfile);
		
		resourceDB.setMgmtCmd(tempMgmtCmd);
		
		if (debugPrint) {
			System.out.println("[ResourceManager] mgmtCmd Registration... OK");
		}
		
		ArrayList<Object> registMgmtCmd = new ArrayList<Object>();
		
		if (debugPrint) {
			System.out.println("[ResourceManager] Return to mgmtCmd Result");
			System.out.println();
		}
		
		if (tempMgmtCmd.labels.equals("deviceManagement") ||
			tempMgmtCmd.labels.equals("firmwareUpgrade") ||
			tempMgmtCmd.labels.equals("appInstall")) {
			
			registMgmtCmd.add("registMgmtCmd");
			registMgmtCmd.add(tempMgmtCmd.labels);
			registMgmtCmd.add(tempMgmtCmd);
			
			try {
				deviceManagerQueue.put(registMgmtCmd);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else {
			try {
				registMgmtCmd.add(tempMgmtCmd);
				thingManagerResponseQueue.put(registMgmtCmd);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * requestThingRegistration Method
	 * @param receivedProfile
	 * @param deviceProfile
	 * @return receiveProfile
	 * Interaction Manager로 Thing 등록을 요청하는 Method로서 Mobius로부터 수신한 Thing Profile을 리턴함.
	 * 다음 버전에서 수정될 예정
	 */
	public Container requestThingRegistration(Container receivedProfile, CSEBase deviceProfile) {
		ArrayList<Object> interactionSendArrayList = new ArrayList<Object>();
		ArrayList<Object> interactionReceiveArrayList = new ArrayList<Object>();
		Container tempProfile = new Container();
		
		interactionSendArrayList.add("requestThingRegistration");
		
		Container registrationProfile = receivedProfile;
		
		interactionSendArrayList.add(registrationProfile);

		interactionSendArrayList.add(deviceProfile);
		
		try {
			if (debugPrint) {
				System.out.println("[ResourceManager] Send to Interaction Manager - Thing Registration");
				System.out.println();
			}
			
			interactionManagerQueue.put(interactionSendArrayList);
			
			interactionReceiveArrayList = resourceManagerResponseQueue.take();
			tempProfile = (Container) interactionReceiveArrayList.get(0);
			
			receivedProfile = tempProfile;

			if (debugPrint) {
				System.out.println("[ResourceManager] Receive from Interaction Manager - Thing Registration Result");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return receivedProfile;
	}
	
	
	/**
	 * requestMgmtCmdRegistration Method
	 * @param receivedThingControl
	 * @param deviceProfile
	 * @return tempThingControl
	 * Interaction Manager로 MgmtCmd 등록을 요청하는 Method로서 Mobius로부터 수신한 MgmtCmd를 리턴함
	 */
	public MgmtCmd requestMgmtCmdRegistration(MgmtCmd receivedThingControl, CSEBase deviceProfile) {
		ArrayList<Object> interactionSendArrayList = new ArrayList<Object>();
		ArrayList<Object> interactionReceiveArrayList = new ArrayList<Object>();
		MgmtCmd tempMgmtCmd = new MgmtCmd();
		
		interactionSendArrayList.add("requestMgmtCmdCreate");
		
		MgmtCmd registrationMgmtCmd = receivedThingControl;
		
		interactionSendArrayList.add(registrationMgmtCmd);

		interactionSendArrayList.add(deviceProfile);
		
		try {
			if (debugPrint) {
				System.out.println("[ResourceManager] Send to Interaction Manager - MgmtCmd Registration");
				System.out.println();
			}
			
			interactionManagerQueue.put(interactionSendArrayList);
			
			interactionReceiveArrayList = resourceManagerResponseQueue.take();
			tempMgmtCmd = (MgmtCmd) interactionReceiveArrayList.get(0);
			tempMgmtCmd.linkedThingAdaptationSoftware = registrationMgmtCmd.linkedThingAdaptationSoftware;
			
			if (debugPrint) {
				System.out.println("[ResourceManager] Receive from Interaction Manager - MgmtCmd Registration Result");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tempMgmtCmd;
	}	
	
	/**
	 * requestThingDataUpload Method
	 * @param receivedData
	 * Interaction Manager로 Thing Data Upload를 요청하는 Method로서 리턴값은 없음
	 */
	private void requestThingDataUpload(ContentInstance receivedData) {
		resourceDB.setThingData(receivedData);
		if (debugPrint) {
			System.out.println("[ResourceManager] Thing Data set... OK");
		}
		
		ArrayList<Object> interactionSendArrayList = new ArrayList<Object>();
		interactionSendArrayList.add("requestThingDataUpload");
		interactionSendArrayList.add(receivedData);

		interactionSendArrayList.add(resourceDB.getCSEProfile());
		
		try {
			if (debugPrint) {
				System.out.println("[ResourceManager] Send to Interaction Manager - Upload Thing Data request");
				System.out.println();
			}
			
			interactionManagerQueue.put(interactionSendArrayList);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * requestMgmtCmdControl Method
	 * @param receivedMgmtCmdControl
	 * Thing Manager로 MgmtCmd Control을 요청하는 Method로서 리턴값은 없음
	 */
	private void requestMgmtCmdControl(MgmtCmd receivedMgmtCmdControl) {
		
		int numberOfMgmtCmd = resourceDB.getNumberOfMgmtCmd();
		
		ArrayList<Object> thingControlArrayList = resourceDB.getMgmtCmd();
		
		MgmtCmd tempMgmtCmd = new MgmtCmd();
		
		if (numberOfMgmtCmd > 0) {
			for (int i = 0; i < numberOfMgmtCmd; i++) {
				tempMgmtCmd = (MgmtCmd) thingControlArrayList.get(i);

				if (receivedMgmtCmdControl.labels.equals("deviceManagement") ||
					receivedMgmtCmdControl.labels.equals("firmwareUpgrade")) {
					
					ArrayList<Object> deviceSendArrayList = new ArrayList<Object>();
					deviceSendArrayList.add("requestMgmtCmdControl");
					deviceSendArrayList.add(receivedMgmtCmdControl.labels);
					deviceSendArrayList.add(receivedMgmtCmdControl);
					
					try {
						if (debugPrint) {
							System.out.println("[ResourceManager] Send to Device Manager - MgmtCmd Control request");
							System.out.println();
						}
						
						deviceManagerQueue.put(deviceSendArrayList);
						break;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				else if (receivedMgmtCmdControl.labels.equals("appInstall")) {
					ArrayList<Object> applicationSendArrayList = new ArrayList<Object>();
					applicationSendArrayList.add("requestMgmtCmdControl");
					applicationSendArrayList.add(receivedMgmtCmdControl.labels);
					applicationSendArrayList.add(receivedMgmtCmdControl);
					
					try {
						if (debugPrint) {
							System.out.println("[ResourceManager] Send to Application Manager - MgmtCmd Control request");
							System.out.println();
						}
						
						applicationManagerQueue.put(applicationSendArrayList);
						break;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				else if (receivedMgmtCmdControl.labels.equals(tempMgmtCmd.labels)) {
					receivedMgmtCmdControl.linkedThingAdaptationSoftware = tempMgmtCmd.linkedThingAdaptationSoftware;
					
					ArrayList<Object> thingSendArrayList = new ArrayList<Object>();
					thingSendArrayList.add("requestMgmtCmdControl");
					thingSendArrayList.add(receivedMgmtCmdControl);
					
					try {
						if (debugPrint) {
							System.out.println("[ResourceManager] Send to Thing Manager - MgmtCmd Control request");
							System.out.println();
						}
						
						thingManagerQueue.put(thingSendArrayList);
						break;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				else {
					if (debugPrint) {
						System.out.println("[ResourceManager] MgmtCmd not matched");
					}
				}
			}
		}
		
		else {
			if (debugPrint) {
				System.out.println("[ResourceManager] MgmtCmd Control profile not found");
				System.out.println();
			}
		}
	}
	
	/**
	 * requestDeviceKey Method
	 * Device Key request 요청을 처리하는 Method
	 */
	private void requestDeviceKey() {
		CSEBase CSEProfile = resourceDB.getCSEProfile(); 
		String deviceKey = CSEProfile.dKey;
		
		ArrayList<Object> interactionSendArrayList = new ArrayList<Object>();
		interactionSendArrayList.add(deviceKey);
		
		try {
			interactionManagerResponseQueue.put(interactionSendArrayList);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * requestFirmwareUpgrade Method
	 * @param firmwareInfo
	 * Device Firmware upgrade 요청을 처리하는 Method
	 */
	private void requestFirmwareUpgrade(String firmwareInfo) {
		ArrayList<Object> interactionSendArrayList = new ArrayList<Object>();
		interactionSendArrayList.add("requestFirmwareUpgrade");
		interactionSendArrayList.add(firmwareInfo);
		
		try {
			if (debugPrint) {
				System.out.println("[ResourceManager] Send to Interaction Manager - Firmware Upgrade request");
				System.out.println();
			}
			
			interactionManagerQueue.put(interactionSendArrayList);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * setFirmwareInfo Method
	 * @param firmwareInfo
	 * Device Firmware 설정 정보를 저장하고 Device Manager로 알리는 Method
	 */
	private void setFirmwareInfo(Firmware firmwareInfo) {
		resourceDB.setFirmwareInformation(firmwareInfo);
		firmwareInfo = resourceDB.getFirmwareInformation();
		
		ArrayList<Object> deviceSendArrayList = new ArrayList<Object>();
		deviceSendArrayList.add("requestFirmwareSet");
		deviceSendArrayList.add(firmwareInfo);

		try {
			if (debugPrint) {
				System.out.println("[ResourceManager] Send to Device Manager - Firmware Set request");
				System.out.println();
			}
			
			deviceManagerQueue.put(deviceSendArrayList);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * requestAECreate Method
	 * @param receivedAE
	 * oneM2M AE resource create를 지원하기 위한 Method 
	 */
	private void requestAECreate(AE receivedAE) {
		String aeState = resourceDB.setAEProfile(receivedAE);
		
		if (aeState.equals("create")) {
			ArrayList<Object> httpServerSendArrayList = new ArrayList<Object>();
			ArrayList<Object> interactionSendArrayList = new ArrayList<Object>();
			
			interactionSendArrayList.add("requestAECreate");
			interactionSendArrayList.add(receivedAE);
			interactionSendArrayList.add(resourceDB.getCSEProfile());
			
			try {
				if (debugPrint) {
					System.out.println("[ResourceManager] Send to Interaction Manager - AE Create request");
					System.out.println();
				}
				
				interactionManagerQueue.put(interactionSendArrayList);

				resourceManagerResponseQueue.take();
				
				if (debugPrint) {
					System.out.println("[ResourceManager] Send to HttpServer - AE Create response");
					System.out.println();
				}
				
				httpServerSendArrayList.add("create");
				httpServerQueue.put(httpServerSendArrayList);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else if (aeState.equals("registered")) {
			ArrayList<Object> httpServerSendArrayList = new ArrayList<Object>();
			httpServerSendArrayList.add("registered");
			try {
				httpServerQueue.put(httpServerSendArrayList);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * requestAEContainerCreate Method
	 * @param receivedContainer
	 * oneM2M AE Container resource create를 지원하기 위한 Method 
	 */
	private void requestAEContainerCreate(Container receivedContainer) {
		String aeContainerState = resourceDB.setAEContainerProfile(receivedContainer);
		
		if (aeContainerState.equals("create")) {
			ArrayList<Object> httpServerSendArrayList = new ArrayList<Object>();
			ArrayList<Object> interactionSendArrayList = new ArrayList<Object>();
			
			interactionSendArrayList.add("requestAEContainerCreate");
			interactionSendArrayList.add(receivedContainer);
			interactionSendArrayList.add(resourceDB.getCSEProfile());
			
			try {
				if (debugPrint) {
					System.out.println("[ResourceManager] Send to Interaction Manager - AE container Create request");
					System.out.println();
				}
				
				interactionManagerQueue.put(interactionSendArrayList);

				resourceManagerResponseQueue.take();
				
				if (debugPrint) {
					System.out.println("[ResourceManager] Send to HttpServer - AE container Create response");
					System.out.println();
				}
				
				httpServerSendArrayList.add("create");
				httpServerQueue.put(httpServerSendArrayList);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else if (aeContainerState.equals("registered")) {
			ArrayList<Object> httpServerSendArrayList = new ArrayList<Object>();
			httpServerSendArrayList.add("registered");
			try {
				httpServerQueue.put(httpServerSendArrayList);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else {
			ArrayList<Object> httpServerSendArrayList = new ArrayList<Object>();
			httpServerSendArrayList.add("failed");
			try {
				httpServerQueue.put(httpServerSendArrayList);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * requestAEContentInstanceCreate Method
	 * @param receivedInstance
	 * oneM2M AE contentInstance resource create를 지원하기 위한 Method 
	 */
	private void requestAEContentInstanceCreate(ContentInstance receivedInstance) {
		String aeContentInstanceState = resourceDB.setAEContentInstanceData(receivedInstance);
		
		if (aeContentInstanceState.equals("create")) {
			ArrayList<Object> httpServerSendArrayList = new ArrayList<Object>();
			ArrayList<Object> interactionSendArrayList = new ArrayList<Object>();
			
			interactionSendArrayList.add("requestAEContentInstanceCreate");
			interactionSendArrayList.add(receivedInstance);
			interactionSendArrayList.add(resourceDB.getCSEProfile());
			
			try {
				if (debugPrint) {
					System.out.println("[ResourceManager] Send to Interaction Manager - AE contentInstance Create request");
					System.out.println();
				}
				
				interactionManagerQueue.put(interactionSendArrayList);

				httpServerSendArrayList = resourceManagerResponseQueue.take();
				
				if (debugPrint) {
					System.out.println("[ResourceManager] Send to HttpServer - AE contentInstance Create response");
					System.out.println();
				}
				
				httpServerQueue.put(httpServerSendArrayList);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else {
			ArrayList<Object> httpServerSendArrayList = new ArrayList<Object>();
			httpServerSendArrayList.add("failed");
			try {
				httpServerQueue.put(httpServerSendArrayList);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * requestApplicationDownload Method
	 * @param applicationInfo
	 * @throws Exception
	 * Device Application (AE) 다운로드를 지원하기 위한 Method
	 */
	private void requestApplicationDownload(String applicationInfo) {
		ArrayList<Object> interactionSendArrayList = new ArrayList<Object>();
		interactionSendArrayList.add("requestAppDownload");
		interactionSendArrayList.add(applicationInfo);
		
		try {
			if (debugPrint) {
				System.out.println("[ResourceManager] Send to Interaction Manager - Application Download request");
				System.out.println();
			}
			
			interactionManagerQueue.put(interactionSendArrayList);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * setAppInfo Method
	 * @param SoftwareInfo
	 * @throws Exception
	 * oneM2M Software resource create 및 Application Manager로 실행설정을 위한 Method
	 */
	private void setAppInfo(Software SoftwareInfo) {
		
		ArrayList<Object> interactionSendArrayList = new ArrayList<Object>();
		ArrayList<Object> interactionReceiveArrayList = new ArrayList<Object>();
		interactionSendArrayList.add("requestSoftwareCreate");
		interactionSendArrayList.add(SoftwareInfo);
		interactionSendArrayList.add(resourceDB.getCSEProfile());
		
		try {
			if (debugPrint) {
				System.out.println("[ResourceManager] Send to Interaction Manager - Software create request");
				System.out.println();
			}
			
			interactionManagerQueue.put(interactionSendArrayList);
			
			interactionReceiveArrayList = resourceManagerResponseQueue.take();
			
			resourceDB.setSoftwareInformation((Software) interactionReceiveArrayList.get(0));
			
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ArrayList<Object> applicationSendArrayList = new ArrayList<Object>();
		applicationSendArrayList.add("requestApplicationSet");
		applicationSendArrayList.add(SoftwareInfo);

		try {
			if (debugPrint) {
				System.out.println("[ResourceManager] Send to Application Manager - Application Set request");
				System.out.println();
			}
			
			applicationManagerQueue.put(applicationSendArrayList);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
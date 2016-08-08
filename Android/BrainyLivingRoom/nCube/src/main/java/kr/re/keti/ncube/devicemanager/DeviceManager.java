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

import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import kr.re.keti.ncube.*;
import kr.re.keti.ncube.resourcemanager.ResourceManager;
import kr.re.keti.ncube.applicationmanager.ApplicationManager;
import kr.re.keti.ncube.httpserver.HttpServerKeti;
import kr.re.keti.ncube.interactionmanager.InteractionManager;
import kr.re.keti.ncube.thingmanager.ThingManager;
import kr.re.keti.ncube.securitymanager.SecurityManager;

/**
 * &Cube�� Device Manager�μ� ���α׷� ���� �� ���� ���� ����Ǿ� �ٸ� Manager���� �����Ŵ
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class DeviceManager {
	
	// manager thread ����
	private static Thread deviceManagementThread;
	private static Thread resourceManagerThread;
	private static Thread interactionManagerThread;
	private static Thread thingManagerThread;
	private static Thread securityManagerThread;
	private static Thread applicationManagerThread;
	private static Thread httpServer;
	
	// blocking queue ����
	private static BlockingQueue<ArrayList<Object>> resourceManagerQueue = new ArrayBlockingQueue<>(16);
	private static BlockingQueue<ArrayList<Object>> resourceManagerResponseQueue = new ArrayBlockingQueue<>(8);
	private static BlockingQueue<ArrayList<Object>> interactionManagerQueue = new ArrayBlockingQueue<>(16);
	private static BlockingQueue<ArrayList<Object>> interactionManagerResponseQueue = new ArrayBlockingQueue<>(8);
	private static BlockingQueue<ArrayList<Object>> thingManagerQueue = new ArrayBlockingQueue<>(16);
	private static BlockingQueue<ArrayList<Object>> thingManagerResponseQueue = new ArrayBlockingQueue<>(8);
	private static BlockingQueue<ArrayList<Object>> securityManagerQueue = new ArrayBlockingQueue<>(16);
	private static BlockingQueue<ArrayList<Object>> applicationManagerQueue = new ArrayBlockingQueue<>(16);
	private static BlockingQueue<ArrayList<Object>> deviceManagerQueue = new ArrayBlockingQueue<>(16);
	private static BlockingQueue<ArrayList<Object>> httpServerQueue = new ArrayBlockingQueue<>(16);
	
	// initialize
	private static CSEBase CSEBase = new CSEBase();
	private static Firmware firmwareInformation = new Firmware();
	private static DeviceInfo deviceInformation = new DeviceInfo();
	private static MgmtCmd deviceMgmtCmd = new MgmtCmd();
	private static MgmtCmd firmwareUpgradeMgmtCmd = new MgmtCmd();
	private static MgmtCmd appInstallMgmtCmd = new MgmtCmd();
	private static String inCSEAddress = null;
	private static String mqttBrokerAddress = null;
	
	private static final boolean debugPrint = true;
	
	/**
	 * getCSEProfile Method
	 * ���� CSE Profile ������ ���� Local Storage���� Device �⺻ ������ �о���� Method
	 * @throws Exception
	 */
	private static void getTotalInformation() throws Exception {
		String configString;
		
		CSEBase.from = "http://localhost";
		// CSEBase Information

		CSEBase.CSEID = Resources.getSystem().getString(R.string.CSEid);
		if (debugPrint) {
			System.out.println("[DeviceManager] CSEProfile - CSE-ID = " + CSEBase.CSEID);
		}

		CSEBase.passcode = Resources.getSystem().getString(R.string.CSEpasscode);
		if (debugPrint) {
			System.out.println("[DeviceManager] CSEProfile - passcode = " + CSEBase.passcode);
		}

		CSEBase.labels = Resources.getSystem().getString(R.string.CSElabels);
		if (debugPrint) {
			System.out.println("[DeviceManager] CSEProfile - labels = " + CSEBase.labels);
		}

		CSEBase.pointOfAccess = Resources.getSystem().getString(R.string.CSEPointOfAccess);
		if (debugPrint) {
			System.out.println("[DeviceManager] CSEProfile - pointOfAccess = " + CSEBase.pointOfAccess);
		}

		// Firmware Information
		firmwareInformation.name = Resources.getSystem().getString(R.string.firmwareName);
		if (debugPrint) {
			System.out.println("[DeviceManager] Firmware - name = " + firmwareInformation.name);
		}

		firmwareInformation.description = Resources.getSystem().getString(R.string.firmwareDescription);
		if (debugPrint) {
			System.out.println("[DeviceManager] Firmware - description = " + firmwareInformation.description);
		}

		firmwareInformation.version = Resources.getSystem().getString(R.string.firmwareVersion);
		if (debugPrint) {
			System.out.println("[DeviceManager] Firmware - version = " + firmwareInformation.version);
		}

		firmwareInformation.url = Resources.getSystem().getString(R.string.firmwareURL);
		if (debugPrint) {
			System.out.println("[DeviceManager] Firmware - URL = " + firmwareInformation.url);
		}

		firmwareInformation.updateStatus = Resources.getSystem().getString(R.string.firmwareUpdateStatus);
		if (debugPrint) {
			System.out.println("[DeviceManager] Firmware - updateStatus = " + firmwareInformation.updateStatus);
		}

		// Device Information
		deviceInformation.labels = Resources.getSystem().getString(R.string.deviceName);
		if (debugPrint) {
			System.out.println("[DeviceManager] DeviceInfo - name = " + deviceInformation.labels);
		}

		deviceInformation.description = Resources.getSystem().getString(R.string.deviceDescription);
		if (debugPrint) {
			System.out.println("[DeviceManager] DeviceInfo - description = " + deviceInformation.description);
		}

		deviceInformation.manufacturer = Resources.getSystem().getString(R.string.deviceManufacturer);
		if (debugPrint) {
			System.out.println("[DeviceManager] DeviceInfo - manufacturer = " + deviceInformation.manufacturer);
		}

		deviceInformation.model = Resources.getSystem().getString(R.string.deviceModel);
		if (debugPrint) {
			System.out.println("[DeviceManager] DeviceInfo - model = " + deviceInformation.model);
		}

		deviceInformation.deviceType = Resources.getSystem().getString(R.string.deviceType);
		if (debugPrint) {
			System.out.println("[DeviceManager] DeviceInfo - deviceType = " + deviceInformation.deviceType);
		}

		deviceInformation.fwVersion = Resources.getSystem().getString(R.string.deviceFwVersion);
		if (debugPrint) {
			System.out.println("[DeviceManager] DeviceInfo - deviceFwVersion = " + deviceInformation.fwVersion);
		}

		deviceInformation.hwVersion = Resources.getSystem().getString(R.string.deviceHwVersion);
		if (debugPrint) {
			System.out.println("[DeviceManager] DeviceInfo - deviceHwVersion = " + deviceInformation.hwVersion);
		}

		// IN-CSE Address
		inCSEAddress = Resources.getSystem().getString(R.string.INCSEAddress);
		if (debugPrint) {
			System.out.println("[DeviceManager] IN-CSE Address = " + inCSEAddress);
		}

		// MQTT Broker Address
		mqttBrokerAddress = Resources.getSystem().getString(R.string.MQTTBrokerAddress);
		if (debugPrint) {
			System.out.println("[DeviceManager] MQTT Broker Address = " + mqttBrokerAddress);
		}

		/*
		// Windows only
		//BufferedReader in = new BufferedReader(new FileReader("c:\\reg.conf"));
		
		// Linux only
		BufferedReader in = new BufferedReader(new FileReader("/nCube/reg.conf"));
		while ((configString = in.readLine()) != null) {
			
			// CSEBase Information
			if (configString.matches(".*CS Eid.*")) {
				CSEBase.CSEID = configString.substring(6, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] CSEProfile - CSE-ID = " + CSEBase.CSEID);
				}
			}
			else if (configString.matches(".*CSEpasscode.*")) {
				CSEBase.passcode = configString.substring(12, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] CSEProfile - passcode = " + CSEBase.passcode);
				}
			}
			else if (configString.matches(".*CSElabels.*")) {
				CSEBase.labels = configString.substring(10, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] CSEProfile - labels = " + CSEBase.labels);
				}
			}
			else if (configString.matches(".*CSEPointOfAccess.*")) {
				CSEBase.pointOfAccess = configString.substring(17, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] CSEProfile - pointOfAccess = " + CSEBase.pointOfAccess);
				}
			}
			
			// Firmware Information
			else if (configString.matches(".*firmwareName.*")) {
				firmwareInformation.name = configString.substring(13, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] Firmware - name = " + firmwareInformation.name);
				}
			}
			else if (configString.matches(".*firmwareDescription.*")) {
				firmwareInformation.description = configString.substring(20, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] Firmware - description = " + firmwareInformation.description);
				}
			}
			else if (configString.matches(".*firmwareVersion.*")) {
				firmwareInformation.version = configString.substring(16, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] Firmware - version = " + firmwareInformation.version);
				}
			}
			else if (configString.matches(".*firmwareURL.*")) {
				firmwareInformation.url = configString.substring(12, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] Firmware - URL = " + firmwareInformation.url);
				}
			}
			else if (configString.matches(".*firmwareUpdateStatus.*")) {
				firmwareInformation.updateStatus = configString.substring(21, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] Firmware - updateStatus = " + firmwareInformation.updateStatus);
				}
			}
			
			// Device Information
			else if (configString.matches(".*deviceName.*")) {
				deviceInformation.labels = configString.substring(11, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] DeviceInfo - name = " + deviceInformation.labels);
				}
			}
			else if (configString.matches(".*deviceDescription.*")) {
				deviceInformation.description = configString.substring(18, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] DeviceInfo - description = " + deviceInformation.description);
				}
			}
			else if (configString.matches(".*deviceManufacturer.*")) {
				deviceInformation.manufacturer = configString.substring(19, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] DeviceInfo - manufacturer = " + deviceInformation.manufacturer);
				}
			}
			else if (configString.matches(".*deviceModel.*")) {
				deviceInformation.model = configString.substring(12, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] DeviceInfo - model = " + deviceInformation.model);
				}
			}
			else if (configString.matches(".*deviceType.*")) {
				deviceInformation.deviceType = configString.substring(11, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] DeviceInfo - deviceType = " + deviceInformation.deviceType);
				}
			}
			else if (configString.matches(".*deviceFwVersion.*")) {
				deviceInformation.fwVersion = configString.substring(16, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] DeviceInfo - deviceFwVersion = " + deviceInformation.fwVersion);
				}
			}
			else if (configString.matches(".*deviceHwVersion.*")) {
				deviceInformation.hwVersion = configString.substring(16, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] DeviceInfo - deviceHwVersion = " + deviceInformation.hwVersion);
				}
			}
			
			// IN-CSE Address
			else if (configString.matches(".*INCSEAddress.*")) {
				inCSEAddress = configString.substring(13, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] IN-CSE Address = " + inCSEAddress);
				}
			}
			
			// MQTT Broker Address
			else if (configString.matches(".*MQTTBrokerAddress.*")) {
				mqttBrokerAddress = configString.substring(18, configString.length());
				
				if (debugPrint) {
					System.out.println("[DeviceManager] MQTT Broker Address = " + mqttBrokerAddress);
				}
			}
			
			// error
			else {
				if (debugPrint) {
					System.out.println("[DeviceManager] Config tap is not defined");
				}
			}
		}
		in.close();
		*/
		
		if (debugPrint) {
			System.out.println("[DeviceManager] CSE Profile / Firmware Info / Device Info load... OK");
			System.out.println();
		}
	}
	
	/**
	 * requestCSERegistration Method 
	 * @param CSEProfile
	 * ���� CSE�� ����ϱ� ���� Resource Manager�� ��û�ϴ� Method
	 */
	private static void requestCSERegistration(CSEBase CSEProfile) {
		ArrayList<Object> resourceSendArrayList = new ArrayList<Object>();
		resourceSendArrayList.add("requestCSERegistration");
		resourceSendArrayList.add(CSEProfile);
		
		if (debugPrint) {
			System.out.println("[DeviceManager] CSE Registration start...");
		}
		
		try {
			if (debugPrint) {
				System.out.println("[DeviceManager] Send to Resource Manager - CSE Registration");
				System.out.println();
			}
			
			resourceManagerQueue.put(resourceSendArrayList);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * requestFirmwareCreate Method
	 * @param firmwareInfo
	 * ���� Firmware ������ ����ϱ� ���� Resource Manager�� ��û�ϴ� Method
	 */
	private static void requestFirmwareCreate(Firmware firmwareInfo) {
		ArrayList<Object> resourceSendArrayList = new ArrayList<Object>();
		resourceSendArrayList.add("requestFirmwareCreate");
		resourceSendArrayList.add(firmwareInfo);
		
		if (debugPrint) {
			System.out.println("[DeviceManager] Firmware Create start...");
		}
		
		try {
			if (debugPrint) {
				System.out.println("[DeviceManager] Send to Resource Manager - Firmware Create");
				System.out.println();
			}
			
			resourceManagerQueue.put(resourceSendArrayList);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * requestDeviceInfoCreate Method
	 * @param deviceInfo
	 * ���� Device ������ ����ϱ� ���� Resource Manager�� ��û�ϴ� Method
	 */
	private static void requestDeviceInfoCreate(DeviceInfo deviceInfo) {
		ArrayList<Object> resourceSendArrayList = new ArrayList<Object>();
		resourceSendArrayList.add("requestDeviceInfoCreate");
		resourceSendArrayList.add(deviceInfo);
		
		if (debugPrint) {
			System.out.println("[DeviceManager] DeviceInfo Create start...");
		}
		
		try {
			if (debugPrint) {
				System.out.println("[DeviceManager] Send to Resource Manager - DeviceInfo Create");
				System.out.println();
			}
			
			resourceManagerQueue.put(resourceSendArrayList);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * requestDeviceManagementCmdCreate Method
	 * ���� Device �� ���� ���� ��ü�� ���ϱ� ���� Resource Manager�� ��û�ϴ� Method
	 */
	private static void requestDeviceManagementCmdCreate() {
		
		deviceMgmtCmd.CSEID = CSEBase.CSEID;
		deviceMgmtCmd.labels = "deviceManagement";
		deviceMgmtCmd.description = "Device Management resource";
		deviceMgmtCmd.cmdType = "remoteCSEUpdate";
		deviceMgmtCmd.execReqArgs = "1";
		deviceMgmtCmd.execMode = "1";
		deviceMgmtCmd.execFrequency = "0";
		deviceMgmtCmd.execDelay = "0";
		deviceMgmtCmd.execNumber = "0";
		
		ArrayList<Object> resourceSendArrayList = new ArrayList<Object>();
		resourceSendArrayList.add("requestMgmtCmdCreate");
		resourceSendArrayList.add(deviceMgmtCmd);
		
		if (debugPrint) {
			System.out.println("[DeviceManager] Device mgmtCmd Create start...");
		}
		
		try {
			if (debugPrint) {
				System.out.println("[DeviceManager] Send to Resource Manager - Device mgmtCmd Create");
				System.out.println();
			}
			
			resourceManagerQueue.put(resourceSendArrayList);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * requestFirmwareUpgradeCmdCreate Method
	 * ���� Firmware Upgrade�� ���� ���� ��ü�� ���ϱ� ���� Resource Manager�� ��û�ϴ� Method
	 */
	private static void requestFirmwareUpgradeCmdCreate() {
		
		firmwareUpgradeMgmtCmd.CSEID = CSEBase.CSEID;
		firmwareUpgradeMgmtCmd.labels = "firmwareUpgrade";
		firmwareUpgradeMgmtCmd.description = "Firmware Upgrade resource";
		firmwareUpgradeMgmtCmd.cmdType = "firmwareUpgrade";
		firmwareUpgradeMgmtCmd.execReqArgs = "1";
		firmwareUpgradeMgmtCmd.execMode = "1";
		firmwareUpgradeMgmtCmd.execFrequency = "0";
		firmwareUpgradeMgmtCmd.execDelay = "0";
		firmwareUpgradeMgmtCmd.execNumber = "0";
		
		ArrayList<Object> resourceSendArrayList = new ArrayList<Object>();
		resourceSendArrayList.add("requestMgmtCmdCreate");
		resourceSendArrayList.add(firmwareUpgradeMgmtCmd);
		
		if (debugPrint) {
			System.out.println("[DeviceManager] Firmware Upgrade Create start...");
		}
		
		try {
			if (debugPrint) {
				System.out.println("[DeviceManager] Send to Resource Manager - Firmware Upgrade Create");
				System.out.println();
			}
			
			resourceManagerQueue.put(resourceSendArrayList);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * requestAppInstallCmdCreate Method
	 * ���� Device Application�� ���� ���� ��ü�� ���ϱ� ���� Resource Manager�� ��û�ϴ� Method
	 */
	private static void requestAppInstallCmdCreate() {
		
		appInstallMgmtCmd.CSEID = CSEBase.CSEID;
		appInstallMgmtCmd.labels = "appInstall";
		appInstallMgmtCmd.description = "Application Install cmd resource";
		appInstallMgmtCmd.cmdType = "appInstall";
		appInstallMgmtCmd.execReqArgs = "1";
		appInstallMgmtCmd.execMode = "1";
		appInstallMgmtCmd.execFrequency = "0";
		appInstallMgmtCmd.execDelay = "0";
		appInstallMgmtCmd.execNumber = "0";
		
		ArrayList<Object> resourceSendArrayList = new ArrayList<Object>();
		resourceSendArrayList.add("requestMgmtCmdCreate");
		resourceSendArrayList.add(appInstallMgmtCmd);
		
		if (debugPrint) {
			System.out.println("[DeviceManager] Application Install cmd Create start...");
		}
		
		try {
			if (debugPrint) {
				System.out.println("[DeviceManager] Send to Resource Manager - Application Install cmd Create");
				System.out.println();
			}
			
			resourceManagerQueue.put(resourceSendArrayList);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		// CSE profile �ε�
		if (debugPrint) {
			System.out.println("[DeviceManager] &CUBE Software Platform loading...");
		}
		getTotalInformation();
		
		// �� Manager thread initialize
		deviceManagementThread = new DeviceManagement(
				deviceManagerQueue,
				resourceManagerQueue,
				deviceMgmtCmd,
				firmwareUpgradeMgmtCmd,
				appInstallMgmtCmd);
		resourceManagerThread = new ResourceManager(
				deviceManagerQueue,
				resourceManagerQueue,
				resourceManagerResponseQueue,
				interactionManagerQueue,
				interactionManagerResponseQueue,
				thingManagerQueue,
				thingManagerResponseQueue,
				applicationManagerQueue,
				securityManagerQueue,
				httpServerQueue);
		interactionManagerThread = new InteractionManager(
				interactionManagerQueue,
				interactionManagerResponseQueue,
				resourceManagerQueue,
				resourceManagerResponseQueue,
				CSEBase.from,
				inCSEAddress,
				mqttBrokerAddress);
		thingManagerThread = new ThingManager(
				thingManagerQueue,
				thingManagerResponseQueue,
				resourceManagerQueue);
		securityManagerThread = new SecurityManager(
				securityManagerQueue,
				resourceManagerResponseQueue);
		applicationManagerThread = new ApplicationManager(
				applicationManagerQueue,
				resourceManagerQueue);
		httpServer = new HttpServerKeti(
				resourceManagerQueue,
				httpServerQueue, 80, 10);
		
		// �� Manager thread start
		deviceManagementThread.start();
		resourceManagerThread.start();
		interactionManagerThread.start();
		thingManagerThread.start();
		securityManagerThread.start();
		applicationManagerThread.start();
		httpServer.start();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (debugPrint) {
			System.out.println("[DeviceManager] &CUBE Software Platform loading... OK");
		}
		
		// Resource Manager�� CSE registration ��û
		requestCSERegistration(CSEBase);
		
		// Firmware Create ��û
		requestFirmwareCreate(firmwareInformation);
		
		// DeviceInfo Create ��û
		requestDeviceInfoCreate(deviceInformation);
		
		// ���� MgmtCmd Create ��û
		requestDeviceManagementCmdCreate();
		requestFirmwareUpgradeCmdCreate();
		requestAppInstallCmdCreate();
		
		// �� Manager thread �� Monitoring thread ���� ���
		try {
			resourceManagerThread.join();
			interactionManagerThread.join();
			thingManagerThread.join();
			securityManagerThread.join();
			applicationManagerThread.join();
			deviceManagementThread.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
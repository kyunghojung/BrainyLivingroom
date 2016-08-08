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

import kr.re.keti.ncube.*;

/**
 * Resource Manager가 내부에서 관리하는 Resource에 접근하기 위한 Class
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class ResourceAccess {
	
	// Resource를 내부에 저장하기 위한 변수 집함
	private static CSEBase CSEProfile;
	private static Firmware firmwareInformation = new Firmware();
	@SuppressWarnings("unused")
	private static DeviceInfo deviceInformation;
	
	private static ArrayList<Object> thingProfile = new ArrayList<Object>();
	private static Container tempThingProfile;
	private static int numberOfThings = 0;
	
	private static ArrayList<Object> mgmtCmd = new ArrayList<Object>();
	private static MgmtCmd tempMgmtCmd;
	private static int numberOfMgmtCmd = 0;
	
	private static ArrayList<Object> aeProfile = new ArrayList<Object>();
	private static AE tempAEProfile;
	private static int numberOfAE = 0;
	
	private static ArrayList<Object> softwareProfile = new ArrayList<Object>();
	
	private static final boolean debugPrint = true;
	
	/**
	 * setCSEProfile Method
	 * @param receivedProfile
	 * @return
	 * CSE Profile을 내부에 저장하기 위해 호출하는 Method
	 */
	public boolean setCSEProfile(CSEBase receivedProfile) {
		CSEProfile = receivedProfile;
		
		if (debugPrint) {
			System.out.println("[ResourceManager] CSE Profile set... OK");
		}
		
		return true;
	}
	
	/**
	 * setFirmwareInformation Method
	 * @param receivedInfo
	 * @return
	 * Firmware 정보를 내부에 저장하기 위해 호출하는 Method
	 */
	public boolean setFirmwareInformation(Firmware receivedInfo) {
		
		if (firmwareInformation.resourceType == null) {
			firmwareInformation = receivedInfo;
		}
		
		else {
			firmwareInformation.lastModifiedTime = receivedInfo.lastModifiedTime;
			firmwareInformation.description = receivedInfo.description;
			firmwareInformation.version = receivedInfo.version;
			firmwareInformation.fileName = receivedInfo.fileName;
		}
		
		if (debugPrint) {
			System.out.println("[ResourceManager] Firmware Information set... OK");
		}
		
		return true;
	}
	
	/**
	 * setDeviceInformation Method
	 * @param receivedInfo
	 * @return
	 * DeviceInfo 정보를 내부에 저장하기 위해 호출하는 Method
	 */
	public boolean setDeviceInformation(DeviceInfo receivedInfo) {
		deviceInformation = receivedInfo;
		
		if (debugPrint) {
			System.out.println("[ResourceManager] Device Information set... OK");
		}
		
		return true;
	}
	
	/**
	 * setThingProfile Method
	 * @param receivedProfile
	 * @return
	 * Thing Profile을 내부에 저장하기 위해 호출하는 Method
	 */
	public boolean setThingProfile(Container receivedProfile) {		
		
		boolean setProfile = false; 
		
		for (int i = 0; i < numberOfThings; i++) {
			tempThingProfile = (Container) thingProfile.get(i);
			if (receivedProfile.labels.equals(tempThingProfile.labels)) {
				thingProfile.set(i, receivedProfile);
				setProfile = true;
				break;
			}
		}
		
		if (!setProfile) {
			thingProfile.add(receivedProfile);
			numberOfThings++;
		}
		
		if (debugPrint) {
			System.out.println("[ResourceManager] Thing Profile set... OK");
		}
		
		return setProfile;
	}
	
	/**
	 * setMgmtCmd Method
	 * @param receivedMgmtCmd
	 * @return
	 * MgmtCmd를 내부에 저장하기 위해 호출하는 Method
	 */
	public boolean setMgmtCmd(MgmtCmd receivedMgmtCmd) {
		
		boolean setCmd = false; 
		
		for (int i = 0; i < numberOfMgmtCmd; i++) {
			tempMgmtCmd = (MgmtCmd) mgmtCmd.get(i);
			if (receivedMgmtCmd.labels.equals(tempMgmtCmd.labels)) {
				mgmtCmd.set(i, receivedMgmtCmd);
				setCmd = true;
				break;
			}
		}
		
		if (!setCmd) {
			mgmtCmd.add(receivedMgmtCmd);
			numberOfMgmtCmd++;
		}
		
		if (debugPrint) {
			System.out.println("[ResourceManager] MgmtCmd set... OK");
		}
		
		return setCmd;
	}
	
	/**
	 * setThingData Method
	 * @param receiveData
	 * Thing Data를 내부에 저장하기 위해 호출하는 Method
	 */
	public void setThingData(ContentInstance receiveData) {
		
		for (int i = 0; i < numberOfThings; i++) {
			tempThingProfile = (Container) thingProfile.get(i);
			
			if (receiveData.containerName.equals(tempThingProfile.labels)) {
				if (tempThingProfile.contentInstance.size()<10) {
					tempThingProfile.contentInstance.add(receiveData);
				}
				else {
					tempThingProfile.contentInstance.remove(0);
					tempThingProfile.contentInstance.add(receiveData);
				}
				thingProfile.set(i, tempThingProfile);
			}
		}
	}
	
	/**
	 * setAEProfile Method
	 * @param receivedProfile
	 * @return reponse
	 * AE Profile을 내부에 저장하기 위해 호출하는 Method
	 */
	public String setAEProfile(AE receivedProfile) {
		
		boolean setAE = false;
		String response = null;
		
		for (int i = 0; i < numberOfAE; i++) {
			tempAEProfile = (AE) aeProfile.get(i);
			if (receivedProfile.name.equals(tempAEProfile.name)) {
				receivedProfile.CSEID = CSEProfile.CSEID;
				//aeProfile.set(i, receivedProfile);
				setAE = true;
				response = "registered";
				break;
			}
		}
		
		if (!setAE) {
			aeProfile.add(receivedProfile);
			numberOfAE++;
			response = "create";
		}
		
		if (debugPrint) {
			System.out.println("[ResourceManager] AE Profile set... OK");
		}
		
		return response;
	}
	
	/**
	 * setAEContainerProfile Method
	 * @param receivedProfile
	 * @return response
	 * AE Container Profile을 내부에 저장하기 위해 호출하는 Method
	 */
	public String setAEContainerProfile(Container receivedProfile) {		
		
		boolean setProfile = false;
		String response = "failed";
		
		for (int i = 0; i < numberOfAE; i++) {
			tempAEProfile = (AE) aeProfile.get(i);
			if (receivedProfile.parentID.equals(tempAEProfile.AppID)) {

				for (int j = 0; j < tempAEProfile.container.size(); j++) {
					tempThingProfile = (Container) tempAEProfile.container.get(j);
					if (receivedProfile.labels.equals(tempThingProfile.labels)) {
						//thingProfile.set(j, receivedProfile);
						setProfile = true;
						response = "registered";
						break;
					}
				}
				
				if (!setProfile) {
					tempAEProfile.container.add(receivedProfile);
					aeProfile.set(i, tempAEProfile);
					response = "create";
				}
			}
		}
		
		if (debugPrint) {
			System.out.println("[ResourceManager] AE container Profile set... OK");
		}
		
		return response;
	}
	
	/**
	 * setAEContentInstanceData Method
	 * @param receiveData
	 * @return response
	 * AE contentInstance Data를 내부에 저장하기 위해 호출하는 Method
	 */
	public String setAEContentInstanceData(ContentInstance receiveData) {
		
		String response = "failed";
		
		for (int i = 0; i < numberOfAE; i++) {
			tempAEProfile = (AE) aeProfile.get(i);
			if (receiveData.appId.equals(tempAEProfile.AppID)) {

				for (int j = 0; j < tempAEProfile.container.size(); j++) {
					tempThingProfile = (Container) tempAEProfile.container.get(j);
					
					if (receiveData.containerName.equals(tempThingProfile.labels)) {
						
						if (tempThingProfile.contentInstance.size()<10) {
							tempThingProfile.contentInstance.add(receiveData);
						}
						else {
							tempThingProfile.contentInstance.remove(0);
							tempThingProfile.contentInstance.add(receiveData);
						}
						
						tempAEProfile.container.set(j, tempThingProfile);
						aeProfile.set(i, tempAEProfile);
						
						response = "create";
						break;
					}
				}
			}
		}
		
		return response;
	}
	
	/**
	 * setSoftwareInformation Method
	 * @param receiveInfo
	 * Software 정보를 내부에 저장하기 위해 호출하는 Method
	 */
	public void setSoftwareInformation(Software receiveInfo) {
		if (debugPrint) {
			System.out.println("[ResourceManager] Software Profile set... OK");
		}
		
		softwareProfile.add(receiveInfo);
	}
	
	/**
	 * getCSEProfile Method
	 * @return CSEProfile
	 * 현재 CSE의 Profile 정보를 리턴하는 Method
	 */
	public CSEBase getCSEProfile() {
		if (debugPrint) {
			System.out.println("[ResourceManager] CSE Profile get... OK");		
		}
		return CSEProfile;
	}
	
	public Firmware getFirmwareInformation() {
		if (debugPrint) {
			System.out.println("[ResourceManager] Firmware Information get... OK");
		}
		
		return firmwareInformation;
	}
	
	/**
	 * getNumberOfThingProfile Method
	 * @return numberOfThings
	 * 현재 Device에 연결된 Thing의 개수를 리턴하는 Method
	 */
	public int getNumberOfThingProfile() {
		if (debugPrint) {
			System.out.println("[ResourceManager] Number of Thing Profile... OK");
		}
		return numberOfThings;
	}
	
	/**
	 * getThingProfile Method
	 * @return thingProfile
	 * 현재 Device의 Thing Profile 정보를 리턴하는 Method
	 */
	public ArrayList<Object> getThingProfile() {
		if (debugPrint) {
			System.out.println("[ResourceManager] Thing Profiles get... OK");		
		}
		return thingProfile;
	}
	
	/**
	 * getNumberOfMgmtCmd Method
	 * @return numberOfThings
	 * 현재 Device에 등록된 MgmtCmd의 개수를 리턴하는 Method
	 */
	public int getNumberOfMgmtCmd() {
		if (debugPrint) {
			System.out.println("[ResourceManager] Number of MgmtCmd... OK");
		}
		return numberOfMgmtCmd;
	}
	
	/**
	 * getMgmtCmd Method
	 * @return thingProfile
	 * 현재 Device의 MgmtCmd 정보를 리턴하는 Method
	 */
	public ArrayList<Object> getMgmtCmd() {
		if (debugPrint) {
			System.out.println("[ResourceManager] MgmtCmd get... OK");		
		}
		return mgmtCmd;
	}
	
	/**
	 * getAEProfile Method
	 * @return aeProfile
	 * 현재 Device의 AE Profile 정보를 리턴하는 Method
	 */
	public ArrayList<Object> getAEProfile() {
		if (debugPrint) {
			System.out.println("[ResourceManager] Application Profiles get... OK");
		}
		return aeProfile;
	}
}
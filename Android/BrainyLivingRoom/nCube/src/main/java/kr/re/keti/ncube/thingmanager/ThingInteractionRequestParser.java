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

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import kr.re.keti.ncube.Container;
import kr.re.keti.ncube.ContentInstance;
import kr.re.keti.ncube.MgmtCmd;
import kr.re.keti.ncube.ThingASProfile;

/**
 * Thing Adaptation Software와 Interaction을 통해 수신한 메시지를 파싱하기 위한 Method를 모아놓은 Class로서 각 Method는 Dom Parser로 구현됨
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class ThingInteractionRequestParser {
	
	/**
	 * thingRegistration Method
	 * @param receiveData
	 * @return thingProfile
	 * @throws Exception
	 * Thing registration 요청을 Parsing하기 위한 Method
	 */
	public static Container thingRegistration(String receiveData) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(receiveData));
		Document document = builder.parse(xmlSource);
		
		Container thingProfile = new Container();
		
		NodeList nameNodeList = document.getElementsByTagName("name");
		Node nameNode = nameNodeList.item(0).getChildNodes().item(0);
		thingProfile.labels = nameNode.getNodeValue();
		
		NodeList containerTypeNodeList = document.getElementsByTagName("containerType");
		Node containerTypeNode = containerTypeNodeList.item(0).getChildNodes().item(0);
		thingProfile.containerType = containerTypeNode.getNodeValue();
		
		NodeList uploadConditionNodeList = document.getElementsByTagName("uploadCondition");
		Node uploadConditionNode = uploadConditionNodeList.item(0).getChildNodes().item(0);
		thingProfile.uploadCondition = uploadConditionNode.getNodeValue();
		
		NodeList uploadConditionValueNodeList = document.getElementsByTagName("uploadConditionValue");
		Node uploadConditionValueNode = uploadConditionValueNodeList.item(0).getChildNodes().item(0);
		thingProfile.uploadConditionValue = uploadConditionValueNode.getNodeValue();
		
		return thingProfile;
	}
	
	/**
	 * thingASRegistration Method
	 * @param receiveData
	 * @return thingASProfile
	 * @throws Exception
	 * Thing Adaptation Software Registration 요청을 Parsing하기 위한 Method
	 */
	public static ThingASProfile thingASRegistration(String receiveData) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(receiveData));
		Document document = builder.parse(xmlSource);
		
		ThingASProfile thingASProfile = new ThingASProfile();
		
		NodeList pocNodeList = document.getElementsByTagName("Poc");
		Node pocNode = pocNodeList.item(0).getChildNodes().item(0);
		thingASProfile.poc = pocNode.getNodeValue();
		
		NodeList nameNodeList = document.getElementsByTagName("Name");
		Node nameNode = nameNodeList.item(0).getChildNodes().item(0);
		thingASProfile.name = nameNode.getNodeValue();
		
		NodeList descriptionNodeList = document.getElementsByTagName("Description");
		Node descriptionNode = descriptionNodeList.item(0).getChildNodes().item(0);
		thingASProfile.description = descriptionNode.getNodeValue();
		
		return thingASProfile;
	}
	
	/**
	 * uploadThingData Method
	 * @param receiveData
	 * @return uploadThingData
	 * @throws Exception
	 * Thing Data Upload 요청을 Parsing하기 위한 Method
	 */
	public static ContentInstance uploadThingData(String receiveData) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(receiveData));
		Document document = builder.parse(xmlSource);
		
		ContentInstance uploadThingData = new ContentInstance();
		
		NodeList containerIdNodeList = document.getElementsByTagName("containerName");
		Node containerIdNode = containerIdNodeList.item(0).getChildNodes().item(0);
		uploadThingData.containerName = containerIdNode.getNodeValue();
		
		NodeList typeOfContentNodeList = document.getElementsByTagName("typeOfContent");
		Node typeOfContentNode = typeOfContentNodeList.item(0).getChildNodes().item(0);
		uploadThingData.typeOfContent = typeOfContentNode.getNodeValue();
		
		NodeList contentNodeList = document.getElementsByTagName("content");
		Node contentNode = contentNodeList.item(0).getChildNodes().item(0);
		uploadThingData.content = contentNode.getNodeValue();
		
		NodeList linkTypeNodeList = document.getElementsByTagName("linkType");
		Node linkTypeNode = linkTypeNodeList.item(0).getChildNodes().item(0);
		uploadThingData.linkType = linkTypeNode.getNodeValue();
		
		return uploadThingData;
	}
	
	/**
	 * thingControlRegistration Method
	 * @param receiveData
	 * @return registThingControl
	 * @throws Exception
	 * Thing Control 등록 요청을 Parsing하기 위한 Method
	 */
	public static MgmtCmd thingControlRegistration(String receiveData) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(receiveData));
		Document document = builder.parse(xmlSource);
		
		MgmtCmd registThingControl = new MgmtCmd();
		
		NodeList nameNodeList = document.getElementsByTagName("name");
		Node nameNode = nameNodeList.item(0).getChildNodes().item(0);
		registThingControl.labels = nameNode.getNodeValue();
		
		NodeList descriptionNodeList = document.getElementsByTagName("description");
		Node descriptionNode = descriptionNodeList.item(0).getChildNodes().item(0);
		registThingControl.description = descriptionNode.getNodeValue();
		
		NodeList cmdTypeNodeList = document.getElementsByTagName("cmdType");
		Node cmdTypeNode = cmdTypeNodeList.item(0).getChildNodes().item(0);
		registThingControl.cmdType = cmdTypeNode.getNodeValue();
		
		NodeList execReqArgsNodeList = document.getElementsByTagName("execReqArgs");
		Node execReqArgsNode = execReqArgsNodeList.item(0).getChildNodes().item(0);
		registThingControl.execReqArgs = execReqArgsNode.getNodeValue();
		
		NodeList execModeNodeList = document.getElementsByTagName("execMode");
		Node execModeNode = execModeNodeList.item(0).getChildNodes().item(0);
		registThingControl.execMode = execModeNode.getNodeValue();
		  
		NodeList execFrequencyNodeList = document.getElementsByTagName("execFrequency");
		Node execFrequencyNode = execFrequencyNodeList.item(0).getChildNodes().item(0);
		registThingControl.execFrequency = execFrequencyNode.getNodeValue();
		
		NodeList execDelayNodeList = document.getElementsByTagName("execDelay");
		Node execDelayNode = execDelayNodeList.item(0).getChildNodes().item(0);
		registThingControl.execDelay = execDelayNode.getNodeValue();
		
		NodeList execNumberNodeList = document.getElementsByTagName("execNumber");
		Node execNumberNode = execNumberNodeList.item(0).getChildNodes().item(0);
		registThingControl.execNumber = execNumberNode.getNodeValue();
		
		NodeList tasPocNodeList = document.getElementsByTagName("tasPoc");
		Node tasPocNode = tasPocNodeList.item(0).getChildNodes().item(0);
		registThingControl.linkedThingAdaptationSoftware = tasPocNode.getNodeValue();
		
		return registThingControl;
	}
}
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

package kr.re.keti.ncube.httpserver;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import kr.re.keti.ncube.AE;
import kr.re.keti.ncube.Container;
import kr.re.keti.ncube.ContentInstance;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * (Next version 보완 예정) oneM2M AE 관련 resource 및 P2P 기능을 제공하기 위한 HTTP parser Class
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class HttpParserKeti {
	
	/**
	 * httpParse Method
	 * @param request
	 * @return requestArray
	 * @throws Exception
	 * HTTP request에 대한 파싱을 수행하기 위한 Method
	 */
	public static ArrayList<Object> httpParse(String request) throws Exception {
		String requestString = request;
		String[] lineArray = requestString.split("\n");
		@SuppressWarnings("unused")
		int headerEndIndex = 0;
		int bodyStartIndex = 0;
		
		for (int i = 0; i < lineArray.length; i++) {
			if (lineArray[i].equals("\r")) {
				headerEndIndex = i;
				bodyStartIndex = i + 1;
				break;
			}
		}
		
		String bodyString = "";
		for (int i = bodyStartIndex; i < lineArray.length; i++) {
			if (!lineArray[i].equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
				bodyString = bodyString + lineArray[i];
			}
		}
		
		System.out.println("bodyString : \n" + bodyString);
		
		ArrayList<String> urlArray = urlParse(lineArray[0]);
		ArrayList<Object> requestArray = new ArrayList<Object>();
		
		switch(urlArray.get(0)) {
			case "AECreate":
				AE requestAE = new AE();
				requestAE.AppID = aeCreateParse(bodyString);
				requestAE.name = urlArray.get(1);
				
				requestArray.add("requestAECreate");
				requestArray.add(requestAE);
				break;
			
			case "containerCreate":
				Container requestAEContainer = new Container();
				requestAEContainer = aeContainerCreateParse(bodyString);
				requestAEContainer.parentID = urlArray.get(1).substring(3);
				requestAEContainer.labels = urlArray.get(2);
				 
				requestArray.add("requestAEContainerCreate");
				requestArray.add(requestAEContainer);
				break;
				
			case "contentInstanceCreate":
				ContentInstance requestAEContentInstance = new ContentInstance();
				requestAEContentInstance = aeContentInstanceCreateParse(bodyString);
				requestAEContentInstance.appId = urlArray.get(1).substring(3);
				requestAEContentInstance.containerName = urlArray.get(2).substring(10);
				
				requestArray.add("requestAEContentInstanceCreate");
				requestArray.add(requestAEContentInstance);
				break;
				
			case "Object Not Found":
				System.out.println("requestType : 404 Not Found");
				break;
		}

		return requestArray;
	}
	
	/**
	 * urlParse Method
	 * @param request
	 * @return ArryList<String>
	 * HTTP request 중 oneM2M 표준 리소스 url에 대한 분석을 수행하기 위한 Method
	 */
	private static ArrayList<String> urlParse(String request) {
		String resourceType = null;
		String resourceName = null;
		int startIndex = 0;
		int endIndex = 0;
		ArrayList<String> returnArrayList = new ArrayList<String>();
		
		String[] requestString = request.split(" ");
		String httpMethod = requestString[0];
		String url = requestString[1];
		
		startIndex = url.indexOf("?") + 1;
		endIndex = url.length();

		String urlStrings = url.substring(1, startIndex - 1);
		System.out.println(urlStrings);
		
		String[] urlString = urlStrings.split("/");
		System.out.println("urlString num : " + urlString.length);
		
		String queryStrings = url.substring(startIndex, endIndex);
		System.out.println(queryStrings);
		
		String[] queryString = queryStrings.split("&");
		
		if (queryString.length == 2) {
			resourceType = queryString[0].substring(3);
			resourceName = queryString[1].substring(3);
		}
		else {
			resourceType = queryString[0].substring(3);
		}
		
		if (httpMethod.equals("POST") && urlString.length == 1 && resourceType.equals("AE")) {
			returnArrayList.add("AECreate");
			returnArrayList.add(resourceName);
			
			System.out.println("requestType : AECreate");
			System.out.println("resourceName : " + resourceName);
		}
		
		else if (httpMethod.equals("POST") && urlString.length == 2 && resourceType.equals("container")) {
			returnArrayList.add("containerCreate");
			returnArrayList.add(urlString[1]);
			returnArrayList.add(resourceName);
			
			System.out.println("requestType : containerCreate");
			System.out.println("AppId : " + urlString[1].substring(3));
			System.out.println("resourceName : " + resourceName);
		}
		
		else if (httpMethod.equals("POST") && urlString.length == 3 && resourceType.endsWith("contentInstance")) {
			returnArrayList.add("contentInstanceCreate");
			returnArrayList.add(urlString[1]);
			returnArrayList.add(urlString[2]);
			
			System.out.println("requestType : contentInstanceCreate");
			System.out.println("AppId : " + urlString[1].substring(3));
			System.out.println("containerName : " + urlString[2].substring(10));
		}
		
		else {
			returnArrayList.add("Object Not Found");
			System.out.println("requestType : 404 Not Found");
		}
		
		return returnArrayList;
	}
	
	/**
	 * aeCreateParse Method
	 * @param request
	 * @return response
	 * @throws Exception
	 * oneM2M 표준 AE Create 요청을 파싱하기 위한 Method
	 */
	private static String aeCreateParse(String request) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(request));
		Document document = builder.parse(xmlSource);
		
		String response = null;
		
		NodeList appIDNodeList = document.getElementsByTagName("App-ID");
		
		if (appIDNodeList.getLength() > 0) {
			Node appIDNode = appIDNodeList.item(0).getChildNodes().item(0);
			response = appIDNode.getNodeValue();
		}
		
		return response;
	}
	
	/**
	 * aeContainerCreateParse
	 * @param request
	 * @return response
	 * @throws Exception
	 * oneM2M 표준 AE의 Container Create 요청을 파싱하기 위한 Method
	 */
	private static Container aeContainerCreateParse(String request) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(request));
		Document document = builder.parse(xmlSource);
		
		Container response = new Container();
				
		NodeList containerTypeNodeList = document.getElementsByTagName("containerType");
		Node containerTypeNode = containerTypeNodeList.item(0).getChildNodes().item(0);
		response.containerType = containerTypeNode.getNodeValue();
		
		return response;
	}

	/**
	 * aeContentInstanceCreateParse Method
	 * @param request
	 * @return response
	 * @throws Exception
	 * oneM2M 표준 AE의 Container 하부의 contentInstance Create 요청을 파싱하기 위한 Method
	 */
	private static ContentInstance aeContentInstanceCreateParse(String request) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(request));
		Document document = builder.parse(xmlSource);
		
		ContentInstance response = new ContentInstance();
				
		NodeList typeOfContentNodeList = document.getElementsByTagName("typeOfContent");
		if (typeOfContentNodeList.getLength() > 0) {
			Node typeOfContentNode = typeOfContentNodeList.item(0).getChildNodes().item(0);
			response.typeOfContent = typeOfContentNode.getNodeValue();
		}
				
		NodeList contentNodeList = document.getElementsByTagName("content");
		if (contentNodeList.getLength() > 0) {
			Node contentNode = contentNodeList.item(0).getChildNodes().item(0);
			response.content = contentNode.getNodeValue();
		}

		return response;
	}
}
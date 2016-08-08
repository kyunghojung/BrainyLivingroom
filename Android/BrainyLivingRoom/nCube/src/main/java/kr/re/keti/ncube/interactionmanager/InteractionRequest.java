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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

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
 * Mobius와 Interaction을 하기 위한 Method를 모아놓은 Class로서 각 Method는 HTTP Client로 구현됨
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class InteractionRequest {
	
	private static int requestIndex = 0;
	private String INCSEAddress = null;
	
	private static final boolean debugPrint = true;
	
	public InteractionRequest (String inCSEIP) {
		this.INCSEAddress = inCSEIP;
	}
	
	/**
	 * CSERegistrationMessage Method
	 * @param CSEProfile
	 * @return responseString
	 * @throws Exception
	 * Mobius로 CSE 등록을 하기 위한 Method로서 HTTP POST를 사용함
	 */
	public String CSERegistrationMessage(CSEBase CSEProfile) throws Exception {
		String requestBody = 
				"<m2m:remoteCSE\n" +
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
						"<CSE-ID>" + CSEProfile.CSEID + "</CSE-ID>\n" + 
						"<pointOfAccess>" + CSEProfile.pointOfAccess + "</pointOfAccess>\n" + 
				"</m2m:remoteCSE>";

		StringEntity entity = new StringEntity(
				new String(requestBody.getBytes()));

		URI uri = new URIBuilder()
				.setScheme("http")
				.setHost(INCSEAddress)
				.setPath("/Mobius")
				.setParameter("ty", "remoteCSE")
				.setParameter("nm", CSEProfile.labels)
				.build();
		
		HttpPost post = new HttpPost(uri);
				post.setHeader("Accept", "application/onem2m-resource+xml");
				post.setHeader("Content-Type", "application/onem2m-resource+xml");
				post.setHeader("locale", "ko");
				post.setHeader("passCode", CSEProfile.passcode);
				post.setHeader("From", CSEProfile.from);
				post.setHeader("X-M2M-RI", Integer.toString(requestIndex));
				post.setEntity(entity);
				requestIndex++;
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		HttpResponse response = httpClient.execute(post);
		
		HttpEntity responseEntity = response.getEntity();
		
		String responseString = EntityUtils.toString(responseEntity);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if (debugPrint) {
			System.out.println("HTTP Response Code : " + responseCode);
			System.out.println("HTTP Response String : " + responseString);
		}
		
		httpClient.close();
		
		return responseString;
	}
	
	/**
	 * CSERegistrationHttpsMessage Method
	 * @param CSEProfile
	 * @return responseString
	 * @throws Exception
	 * Mobius로 CSE 등록을 하기 위한 Method로서 HTTPS POST를 사용함
	 */
	public String CSERegistrationHttpsMessage(CSEBase CSEProfile) throws Exception {

		URL url = new URL(INCSEAddress + "/Mobius");
		
		HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
		
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("ty", "remoteCSE");
		conn.setRequestProperty("deviceID", CSEProfile.CSEID);
		conn.setRequestProperty("Accept", "application/xml");
		conn.setRequestProperty("Content-Type", "application/xml");
		conn.setRequestProperty("locale", "ko");
		conn.setRequestProperty("passcode", CSEProfile.passcode);
		
		String requestBody = 
				"<CSEBase>\n" + 
						"<labels>" + CSEProfile.labels + "</labels>\n" + 
						"<pointOfAccess>" + CSEProfile.pointOfAccess + "</pointOfAccess>\n" + 
				"</CSEBase>";
		
		OutputStream os = conn.getOutputStream();
		os.write(requestBody.getBytes());
		os.flush();
		os.close();
		
		BufferedReader br = new BufferedReader
							(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		
		String responseString = null;
		String receivedData = null;
		
		while((receivedData = br.readLine()) != null) {
			responseString = responseString + receivedData;
		}
		
		System.out.println(responseString);
		
		return responseString;
	}
	
	/**
	 * firmwareCreateMessage Method
	 * @param firmwareInfo
	 * @param CSEProfile
	 * @return responseString
	 * @throws Exception
	 * Mobius로 Firmware Create를 하기 위한 Method로서 HTTP POST를 사용함
	 */
	public String firmwareCreateMessage(Firmware firmwareInfo, CSEBase CSEProfile) throws Exception {
		String requestBody = 
				"<m2m:firmware\n" + 
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
						"<description>" + firmwareInfo.description + "</description>\n" + 
						"<version>" + firmwareInfo.version + "</version>\n" +
						"<URL>" + firmwareInfo.url + "</URL>\n" +
						"<updateStatus>" + firmwareInfo.updateStatus + "</updateStatus>\n" +
				"</m2m:firmware>";

		StringEntity entity = new StringEntity(
				new String(requestBody.getBytes()));

		URI uri = new URIBuilder()
				.setScheme("http")
				.setHost(INCSEAddress)
				.setPath("/Mobius/node-" + CSEProfile.nodeLink)
				.setParameter("ty", "firmware")
				.setParameter("nm", firmwareInfo.name)
				.build();
		
		HttpPost post = new HttpPost(uri);
				post.setHeader("Accept", "application/onem2m-resource+xml");
				post.setHeader("Content-Type", "application/onem2m-resource+xml");
				post.setHeader("locale", "ko");
				post.setHeader("From", CSEProfile.from);
				post.setHeader("X-M2M-RI", Integer.toString(requestIndex));
				post.setHeader("dKey", CSEProfile.dKey);
				post.setEntity(entity);
				requestIndex++;
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		HttpResponse response = httpClient.execute(post);
		
		HttpEntity responseEntity = response.getEntity();
		
		String responseString = EntityUtils.toString(responseEntity);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if (debugPrint) {
			System.out.println("HTTP Response Code : " + responseCode);
			System.out.println("HTTP Response String : " + responseString);
		}
		
		httpClient.close();
		
		return responseString;
	}
	
	/**
	 * firmwareUpdateMessage Method
	 * @param firmwareInfo
	 * @param CSEProfile
	 * @return responseString
	 * @throws Exception
	 * Mobius로 Firmware Update 결과를 전송하기 위한 Method로서 HTTP PUT을 사용함
	 */
	public String firmwareUpdateMessage(Firmware firmwareInfo, CSEBase CSEProfile) throws Exception {
		String requestBody = 
				"<m2m:firmware\n" + 
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
						"<description>" + firmwareInfo.description + "</description>\n" + 
						"<version>" + firmwareInfo.version + "</version>\n" +
						"<URL>" + firmwareInfo.url + "</URL>\n" +
						"<updateStatus>" + firmwareInfo.updateStatus + "</updateStatus>\n" +
				"</m2m:firmware>";

		StringEntity entity = new StringEntity(
				new String(requestBody.getBytes()));

		URI uri = new URIBuilder()
				.setScheme("http")
				.setHost(INCSEAddress)
				.setPath("/Mobius/node-" + CSEProfile.nodeLink + "/firmware-" + firmwareInfo.resourceID)
				.setParameter("ty", "firmware")
				.build();
		
		HttpPut put = new HttpPut(uri);
				put.setHeader("Accept", "application/onem2m-resource+xml");
				put.setHeader("Content-Type", "application/onem2m-resource+xml");
				put.setHeader("locale", "ko");
				put.setHeader("From", CSEProfile.from);
				put.setHeader("X-M2M-RI", Integer.toString(requestIndex));
				put.setHeader("dKey", CSEProfile.dKey);
				put.setEntity(entity);
				requestIndex++;
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		HttpResponse response = httpClient.execute(put);
		
		HttpEntity responseEntity = response.getEntity();
		
		String responseString = EntityUtils.toString(responseEntity);
		
		int responseCode = response.getStatusLine().getStatusCode();

		if (debugPrint) {
			System.out.println("HTTP Response Code : " + responseCode);
			System.out.println("HTTP Response String : " + responseString);
		}
		
		httpClient.close();
		
		return responseString;
	}
	
	/**
	 * softwareCreateMessage Method
	 * @param softwareInfo
	 * @param CSEProfile
	 * @return responseString
	 * @throws Exception
	 * Mobius로 software Create를 하기 위한 Method로서 HTTP POST를 사용함
	 */
	public String softwareCreateMessage(Software softwareInfo, CSEBase CSEProfile) throws Exception {
		String requestBody = 
				"<m2m:software\n" + 
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
						"<description>" + softwareInfo.description + "</description>\n" + 
						"<version>" + softwareInfo.version + "</version>\n" +
						"<install>" + softwareInfo.install + "</install>\n" +
						"<uninstall>" + softwareInfo.uninstall + "</uninstall>\n" +
						"<installStatus>" + softwareInfo.installStatus + "</installStatus>\n" +
				"</m2m:software>";

		StringEntity entity = new StringEntity(
				new String(requestBody.getBytes()));

		URI uri = new URIBuilder()
				.setScheme("http")
				.setHost(INCSEAddress)
				.setPath("/Mobius/node-" + CSEProfile.nodeLink)
				.setParameter("ty", "software")
				.setParameter("nm", softwareInfo.name)
				.build();
		
		HttpPost post = new HttpPost(uri);
				post.setHeader("Accept", "application/onem2m-resource+xml");
				post.setHeader("Content-Type", "application/onem2m-resource+xml");
				post.setHeader("locale", "ko");
				post.setHeader("From", CSEProfile.from);
				post.setHeader("X-M2M-RI", Integer.toString(requestIndex));
				post.setHeader("dKey", CSEProfile.dKey);
				post.setEntity(entity);
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		HttpResponse response = httpClient.execute(post);
		
		HttpEntity responseEntity = response.getEntity();
		
		String responseString = EntityUtils.toString(responseEntity);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if (debugPrint) {
			System.out.println("HTTP Response Code : " + responseCode);
			System.out.println("HTTP Response String : " + responseString);
		}
		
		httpClient.close();
		
		return responseString;
	}
	
	/**
	 * deviceInfoCreateMessage Method
	 * @param deviceInfo
	 * @param CSEProfile
	 * @return responseString
	 * @throws Exception
	 * Mobius로 DeviceInfo Create를 하기 위한 Method로서 HTTP POST를 사용함
	 */
	public String deviceInfoCreateMessage(DeviceInfo deviceInfo, CSEBase CSEProfile) throws Exception {
		String requestBody = 
				"<m2m:deviceInfo\n" + 
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
						"<description>" + deviceInfo.description + "</description>\n" + 
						"<manufacturer>" + deviceInfo.manufacturer + "</manufacturer>\n" +
						"<model>" + deviceInfo.model + "</model>\n" +
						"<deviceType>" + deviceInfo.deviceType + "</deviceType>\n" +
						"<fwVersion>" + deviceInfo.fwVersion + "</fwVersion>\n" +
						"<hwVersion>" + deviceInfo.hwVersion + "</hwVersion>\n" +
				"</m2m:deviceInfo>";

		StringEntity entity = new StringEntity(
				new String(requestBody.getBytes()));

		URI uri = new URIBuilder()
				.setScheme("http")
				.setHost(INCSEAddress)
				.setPath("/Mobius/node-" + CSEProfile.nodeLink)
				.setParameter("ty", "deviceInfo")
				.setParameter("nm", deviceInfo.labels)
				.build();
		
		HttpPost post = new HttpPost(uri);
				post.setHeader("Accept", "application/onem2m-resource+xml");
				post.setHeader("Content-Type", "application/onem2m-resource+xml");
				post.setHeader("locale", "ko");
				post.setHeader("From", CSEProfile.from);
				post.setHeader("X-M2M-RI", Integer.toString(requestIndex));
				post.setHeader("dKey", CSEProfile.dKey);
				post.setEntity(entity);
				
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		HttpResponse response = httpClient.execute(post);
		
		HttpEntity responseEntity = response.getEntity();
		
		String responseString = EntityUtils.toString(responseEntity);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if (debugPrint) {
			System.out.println("HTTP Response Code : " + responseCode);
			System.out.println("HTTP Response String : " + responseString);
		}
		
		httpClient.close();
		
		return responseString;
	}
	
	/**
	 * locationPolicyCreateMessage Method
	 * @param deviceInfo
	 * @param CSEProfile
	 * @return responseString
	 * @throws Exception
	 * (Next version 적용 예정) oneM2M locationPolicy resource create를 지원하기 위한 Method 
	 */
	public String locationPolicyCreateMessage(DeviceInfo deviceInfo, CSEBase CSEProfile) throws Exception {
		String requestBody = 
				"<deviceInfo>\n" + 
						"<description>" + deviceInfo.description + "</description>\n" + 
						"<manufacturer>" + deviceInfo.manufacturer + "</manufacturer>\n" +
						"<model>" + deviceInfo.model + "</model>\n" +
						"<deviceType>" + deviceInfo.deviceType + "</deviceType>\n" +
						"<fwVersion>" + deviceInfo.fwVersion + "</fwVersion>\n" +
						"<hwVersion>" + deviceInfo.hwVersion + "</hwVersion>\n" +
				"</deviceInfo>";

		StringEntity entity = new StringEntity(
				new String(requestBody.getBytes()));

		URI uri = new URIBuilder()
				.setScheme("http")
				.setHost(INCSEAddress)
				.setPath("/Mobius/locationPolicy-" + CSEProfile.nodeLink)
				.setParameter("ty", "deviceInfo")
				.setParameter("nm", deviceInfo.labels)
				.build();
		
		HttpPost post = new HttpPost(uri);
				post.setHeader("Accept", "application/onem2m-resource+xml");
				post.setHeader("Content-Type", "application/onem2m-resource+xml");
				post.setHeader("locale", "ko");
				post.setHeader("From", CSEProfile.from);
				post.setHeader("X-M2M-RI", Integer.toString(requestIndex));
				post.setHeader("dKey", CSEProfile.dKey);
				post.setEntity(entity);
		
		System.out.println(uri);
		System.out.println(requestBody);
				
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		HttpResponse response = httpClient.execute(post);
		
		HttpEntity responseEntity = response.getEntity();
		
		String responseString = EntityUtils.toString(responseEntity);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if (debugPrint) {
			System.out.println("HTTP Response Code : " + responseCode);
			System.out.println("HTTP Response String : " + responseString);
		}
		
		httpClient.close();
		
		return responseString;
	}
	
	/**
	 * containerCreateMessage Method
	 * @param registrationProfile
	 * @param CSEProfile
	 * @return responseString
	 * @throws Exception
	 * Mobius Mashup으로 Container 생성 요청을 하기 위한 Method로서 HTTP POST를 사용함
	 */
	public String containerCreateMessage(Container registrationProfile, CSEBase CSEProfile) throws Exception {
	
		String requestBody = 
				"<m2m:container\n" +
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
						"<uploadCondition>" + registrationProfile.uploadCondition + "</uploadCondition>\n" + 
						"<uploadConditionValue>" + registrationProfile.uploadConditionValue + "</uploadConditionValue>\n" +
						"<containerType>" + registrationProfile.containerType + "</containerType>\n" +
				"</m2m:container>";
		
		StringEntity entity = new StringEntity(
							new String(requestBody.getBytes()));
		
		URI uri = new URIBuilder()
				.setScheme("http")
				.setHost(INCSEAddress)
				.setPath("/Mobius/remoteCSE-" + CSEProfile.CSEID)
				.setParameter("ty", "container")
				.setParameter("nm", registrationProfile.labels)
				.build();
		
		HttpPost post = new HttpPost(uri);
				post.setHeader("Accept", "application/onem2m-resource+xml");
				post.setHeader("Content-Type", "application/onem2m-resource+xml");
				post.setHeader("locale", "ko");
				post.setHeader("From", CSEProfile.from);
				post.setHeader("X-M2M-RI", Integer.toString(requestIndex));
				post.setHeader("dKey", CSEProfile.dKey);
				post.setEntity(entity);
				
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		HttpResponse response = httpClient.execute(post);
	
		HttpEntity responseEntity = response.getEntity();
			
		String responseString = EntityUtils.toString(responseEntity);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if (debugPrint) {
			System.out.println("HTTP Response Code : " + responseCode);
			System.out.println("HTTP Response String : " + responseString);
		}
		
		httpClient.close();
		
		return responseString;
	}
	
	/**
	 * contentInstanceCreate Method
	 * @param uploadThingData
	 * @param CSEProfile
	 * @return responseString
	 * @throws Exception
	 * Mobius Mashup으로 Thing 데이터 업로드 요청을 하기 위한 Method로서 HTTP POST를 사용함
	 */
	public String contentInstanceCreate(ContentInstance uploadThingData, CSEBase CSEProfile) throws Exception {
		
		String requestBody = 
				"<m2m:contentInstance\n" +
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
						"<typeOfContent>" + uploadThingData.typeOfContent + "</typeOfContent>\n" + 
						"<content>" + Base64.encode(uploadThingData.content.getBytes()) + "</content>\n" +
				"</m2m:contentInstance>";
		
		StringEntity entity = new StringEntity(
							new String(requestBody.getBytes()));
		
		URI uri = new URIBuilder()
				.setScheme("http")
				.setHost(INCSEAddress)
				.setPath("/Mobius/remoteCSE-" + CSEProfile.CSEID + "/container-" + uploadThingData.containerName)
				.setParameter("ty", "contentInstance")
				.build();
		
		HttpPost post = new HttpPost(uri);
				post.setHeader("Accept", "application/onem2m-resource+xml");
				post.setHeader("Content-Type", "application/onem2m-resource+xml");
				post.setHeader("locale", "ko");
				post.setHeader("From", CSEProfile.from);
				post.setHeader("X-M2M-RI", Integer.toString(requestIndex));
				post.setHeader("dKey", CSEProfile.dKey);
				post.setEntity(entity);
				
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		HttpResponse response = httpClient.execute(post);
	
		HttpEntity responseEntity = response.getEntity();
			
		String responseString = EntityUtils.toString(responseEntity);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if (debugPrint) {
			System.out.println("HTTP Response Code : " + responseCode);
			System.out.println("HTTP Response String : " + responseString);
		}
		
		httpClient.close();
		
		return responseString;
	}
	
	/**
	 * mgmtCmdRegistrationMessage Method
	 * @param controlProfile
	 * @param CSEProfile
	 * @return responseString
	 * @throws Exception
	 * Mobius Mashup으로 mgmtCmd 등록 요청을 하기 위한 Method로서 HTTP POST를 사용함
	 */
	public String mgmtCmdRegistrationMessage(MgmtCmd controlProfile, CSEBase CSEProfile) throws Exception {
		
		String requestBody = 
				"<m2m:mgmtCmd\n" +
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
						"<description>" + controlProfile.description + "</description>\n" + 
						"<cmdType>" + controlProfile.cmdType + "</cmdType>\n" +
						"<execRegArgs>" + controlProfile.execReqArgs + "</execRegArgs>\n" +
						"<execMode>" + controlProfile.execMode + "</execMode>\n" +
						"<execFrequency>" + controlProfile.execFrequency + "</execFrequency>\n" +
						"<execDelay>" + controlProfile.execDelay + "</execDelay>\n" +
						"<execNumber>" + controlProfile.execNumber + "</execNumber>\n" +
				"</m2m:mgmtCmd>";
		
		StringEntity entity = new StringEntity(
							new String(requestBody.getBytes()));
		
		URI uri = new URIBuilder()
				.setScheme("http")
				.setHost(INCSEAddress)
				.setPath("/Mobius/remoteCSE-" + CSEProfile.CSEID)
				.setParameter("ty", "mgmtCmd")
				.setParameter("nm", controlProfile.labels)
				.build();
		
		HttpPost post = new HttpPost(uri);
				post.setHeader("Accept", "application/onem2m-resource+xml");
				post.setHeader("Content-Type", "application/onem2m-resource+xml");
				post.setHeader("locale", "ko");
				post.setHeader("From", CSEProfile.from);
				post.setHeader("X-M2M-RI", Integer.toString(requestIndex));
				post.setHeader("dKey", CSEProfile.dKey);
				post.setEntity(entity);
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		HttpResponse response = httpClient.execute(post);
	
		HttpEntity responseEntity = response.getEntity();
			
		String responseString = EntityUtils.toString(responseEntity);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if (debugPrint) {
			System.out.println("HTTP Response Code : " + responseCode);
			System.out.println("HTTP Response String : " + responseString);
		}
		
		httpClient.close();
		
		return responseString;
	}
	
	/**e
	 * mgmtCmdUpdateMessage Method
	 * @param controlProfile
	 * @param deviceKey
	 * @return responseString
	 * @throws Exception
	 */
	public String mgmtCmdUpdateMessage(MgmtCmd controlProfile, String deviceKey, String requestFrom) throws Exception {
		
		String requestBody = 
				"<m2m:execInstance\n" +
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"</m2m:execInstance>";
		
		StringEntity entity = new StringEntity(
							new String(requestBody.getBytes()));
		
		URI uri = new URIBuilder()
				.setScheme("http")
				.setHost(INCSEAddress)
				.setPath("/Mobius/remoteCSE-" + controlProfile.execTarget + "/mgmtCmd-" +
										controlProfile.labels + "/execInstance-" + controlProfile.resourceID)
				.setParameter("ty", "execInstance")
				.build();
		
		HttpPut put = new HttpPut(uri);
				put.setHeader("Accept", "application/onem2m-resource+xml");
				put.setHeader("Content-Type", "application/onem2m-resource+xml");
				put.setHeader("locale", "ko");
				put.setHeader("From", requestFrom);
				put.setHeader("X-M2M-RI", Integer.toString(requestIndex));
				put.setHeader("dKey", deviceKey);
				put.setEntity(entity);
		
		System.out.println("requestURI : " + uri);
		System.out.println("deviceKey : " + deviceKey);
		System.out.println("requestBody : " + requestBody);
				
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		HttpResponse response = httpClient.execute(put);
	
		HttpEntity responseEntity = response.getEntity();
			
		String responseString = EntityUtils.toString(responseEntity);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if (debugPrint) {
			System.out.println("HTTP Response Code : " + responseCode);
			System.out.println("HTTP Response String : " + responseString);
		}
		
		httpClient.close();
		
		return responseString;
	}
	
	/**
	 * aeCreateMessage Method
	 * @param aeProfile
	 * @param CSEProfile
	 * @return responseString
	 * @throws Exception
	 * Mobius로 oneM2M AE resource Create 요청을 위한  Method로서 HTTP POST를 사용함
	 */
	public String aeCreateMessage(AE aeProfile, CSEBase CSEProfile) throws Exception {
		
		String requestBody = 
				"<m2m:AE\n" +
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
						"<App-ID>" + aeProfile.AppID + "</App-ID>\n" +
						"<AE-ID>" + aeProfile.AppID + "</AE-ID>\n" +
						"<pointOfAccess>" + "MQTT|" + aeProfile.AppID + "</pointOfAccess>\n" +
						"<nodeLink>" + CSEProfile.nodeLink + "</nodeLink>\n" +
				"</m2m:AE>";

		StringEntity entity = new StringEntity(
						new String(requestBody.getBytes()));
		
		URI uri = new URIBuilder()
			.setScheme("http")
			.setHost(INCSEAddress)
			.setPath("/Mobius/remoteCSE-" + CSEProfile.CSEID)
			.setParameter("ty", "AE")
			.setParameter("nm", aeProfile.name)
			.build();
		
		HttpPost post = new HttpPost(uri);
			post.setHeader("Content-Type", "application/onem2m-resource+xml");
			post.setHeader("Accept", "application/onem2m-resource+xml");
			post.setHeader("locale", "ko");
			post.setHeader("From", CSEProfile.from);
			post.setHeader("X-M2M-RI", Integer.toString(requestIndex));
			post.setHeader("dKey", CSEProfile.dKey);
			post.setEntity(entity);
		
		System.out.println("requestURI : " + uri);
		System.out.println("deviceKey : " + CSEProfile.dKey);
		System.out.println("requestBody : " + requestBody);
			
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		HttpResponse response = httpClient.execute(post);
		
		HttpEntity responseEntity = response.getEntity();
		
		String responseString = EntityUtils.toString(responseEntity);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if (debugPrint) {
			System.out.println("HTTP Response Code : " + responseCode);
			System.out.println("HTTP Response String : " + responseString);
		}
		
		httpClient.close();
		
		return responseString;
	}
	
	/**
	 * aeContainerCreateMessage Method
	 * @param registrationProfile
	 * @param CSEProfile
	 * @return responseString
	 * @throws Exception
	 * Mobius로 oneM2M AE Container resource Create 요청을 위한  Method로서 HTTP POST를 사용함
	 */
	public String aeContainerCreateMessage(Container registrationProfile, CSEBase CSEProfile) throws Exception {
		
		String requestBody = 
				"<m2m:container\n" +
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
						"<uploadCondition>nothing</uploadCondition>\n" + 
						"<uploadConditionValue>nothing</uploadConditionValue>\n" +
						"<containerType>" + registrationProfile.containerType + "</containerType>\n" +
				"</m2m:container>";
		
		StringEntity entity = new StringEntity(
							new String(requestBody.getBytes()));
		
		URI uri = new URIBuilder()
				.setScheme("http")
				.setHost(INCSEAddress)
				.setPath("/Mobius/remoteCSE-" + CSEProfile.CSEID + "/AE-" + registrationProfile.parentID)
				.setParameter("ty", "container")
				.setParameter("nm", registrationProfile.labels)
				.build();
		
		HttpPost post = new HttpPost(uri);
				post.setHeader("Content-Type", "application/onem2m-resource+xml");
				post.setHeader("Accept", "application/onem2m-resource+xml");
				post.setHeader("locale", "ko");
				post.setHeader("From", CSEProfile.from);
				post.setHeader("X-M2M-RI", Integer.toString(requestIndex));
				post.setHeader("dKey", CSEProfile.dKey);
				post.setEntity(entity);
				
				System.out.println("requestURI : " + uri);
				System.out.println("deviceKey : " + CSEProfile.dKey);
				System.out.println("requestBody : " + requestBody);
				
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		HttpResponse response = httpClient.execute(post);
	
		HttpEntity responseEntity = response.getEntity();
			
		String responseString = EntityUtils.toString(responseEntity);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if (debugPrint) {
			System.out.println("HTTP Response Code : " + responseCode);
			System.out.println("HTTP Response String : " + responseString);
		}
		
		httpClient.close();
		
		return responseString;
	}
	
	/**
	 * aeContentInstanceCreate Method
	 * @param uploadThingData
	 * @param CSEProfile
	 * @return responseCode
	 * @throws Exception
	 * Mobius로 oneM2M AE contentInstance resource Create 요청을 위한  Method로서 HTTP POST를 사용함
	 */
	public int aeContentInstanceCreateMessage(ContentInstance uploadThingData, CSEBase CSEProfile) throws Exception {
		
		String requestBody = 
				"<m2m:contentInstance\n" +
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
						"<typeOfContent>" + uploadThingData.typeOfContent + "</typeOfContent>\n" + 
						"<content>" + Base64.encode(uploadThingData.content.getBytes()) + "</content>\n" +
				"</m2m:contentInstance>";
		
		StringEntity entity = new StringEntity(
							new String(requestBody.getBytes()));
		
		URI uri = new URIBuilder()
				.setScheme("http")
				.setHost(INCSEAddress)
				.setPath("/Mobius/remoteCSE-" + CSEProfile.CSEID + "/AE-" + uploadThingData.appId + "/container-" + uploadThingData.containerName)
				.setParameter("ty", "contentInstance")
				.build();
		
		HttpPost post = new HttpPost(uri);
				post.setHeader("Content-Type", "application/onem2m-resource+xml");
				post.setHeader("Accept", "application/onem2m-resource+xml");
				post.setHeader("locale", "ko");
				post.setHeader("From", CSEProfile.from);
				post.setHeader("X-M2M-RI", Integer.toString(requestIndex));
				post.setHeader("dKey", CSEProfile.dKey);
				post.setEntity(entity);
				
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		HttpResponse response = httpClient.execute(post);
	
		HttpEntity responseEntity = response.getEntity();
			
		String responseString = EntityUtils.toString(responseEntity);
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		if (debugPrint) {
			System.out.println("HTTP Response Code : " + responseCode);
			System.out.println("HTTP Response String : " + responseString);
		}
		
		httpClient.close();
		
		return responseCode;
	}
}
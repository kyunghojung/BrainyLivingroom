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
import java.util.concurrent.BlockingQueue;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

/**
 * MQTT Broker와의 통신을 위한 MQTT client Class로서 Eclipse Paho library 활용
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class MqttClientKeti implements MqttCallback {
	private String mqttClientId = MqttClient.generateClientId();
	private String mqttServerUrl = "";
	private String mqttTopicName = "";
	private MqttClient mqc;
	
	private BlockingQueue<ArrayList<Object>> interactionManagerQueue;

	public MqttClientKeti(String serverUrl,
							BlockingQueue<ArrayList<Object>> interactionQueue) {
		
		this.mqttServerUrl = serverUrl;
		this.interactionManagerQueue = interactionQueue;
		
		System.out.println("[KETI MQTT Client] Client Initialize");
		
		try {
			mqc = new MqttClient(mqttServerUrl, mqttClientId);
			
			while(!mqc.isConnected()){
				mqc.connect();
				System.out.println("[KETI MQTT Client] Connection try");
			}
			
			System.out.println("[KETI MQTT Client] Connected to Server - " + mqttServerUrl);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * subscribe Method
	 * @param mqttTopic
	 * MQTT Broker로 subscription을 맺을 때 활용하는 Method
	 */
	public void subscribe(String mqttTopic) {
		try {
			this.mqttTopicName = mqttTopic;
			mqc.subscribe(this.mqttTopicName);
		} catch (MqttException e) {
			System.out.println("[KETI MQTT Client] Subscribe Failed - " + mqttTopic);
			e.printStackTrace();
		}
		mqc.setCallback(this);
	}
	
	/**
	 * publishKetiPayload Method
	 * @param topic
	 * @param mgmtObjName
	 * @param controlValue
	 * MQTT Broker로 publishing 시 활용하는 Method (KETI 활용 payload)
	 */
	public void publishKetiPayload(String topic, String mgmtObjName, String controlValue) {
		MqttMessage msg = new MqttMessage();
		String payload = mgmtObjName + "," + controlValue;
		msg.setPayload(payload.getBytes());
		try {
			mqc.publish(topic, msg);
			System.out.println("[KETI MQTT Client] MQTT Topic \"" + topic + "\" Publish Payload = " + payload);
		} catch (MqttPersistenceException e) {
			System.out.println("[KETI MQTT Client] Publish Failed - " + topic);
			e.printStackTrace();
		} catch (MqttException e) {
			System.out.println("[KETI MQTT Client] Publish Failed - " + topic);
			e.printStackTrace();
		}
	}
	
	/**
	 * publishFullPayload Method
	 * @param topic
	 * @param payload
	 * MQTT Broker로 publishing 시 활용하는 Method (공용 payload)
	 */
	public void publishFullPayload(String topic, String payload) {
		MqttMessage msg = new MqttMessage();
		msg.setPayload(payload.getBytes());
		try {
			mqc.publish(topic, msg);
			System.out.println("[KETI MQTT Client] MQTT Topic \"" + topic + "\" Publish Payload = " + payload);
		} catch (MqttPersistenceException e) {
			System.out.println("[KETI MQTT Client] Publish Failed - " + topic);
			e.printStackTrace();
		} catch (MqttException e) {
			System.out.println("[KETI MQTT Client] Publish Failed - " + topic);
			e.printStackTrace();
		}
	}
	
	/**
	 * connectionLost 시 자동으로 호출되어 reconnection을 수행하는 Method
	 */
	public void connectionLost(Throwable cause) {
		System.out.println("[KETI MQTT Client] Disconnected from MQTT Server");
		
		try {
			while(!mqc.isConnected()){
				mqc.connect();
				System.out.println("[KETI MQTT Client] Connection retry");
			}
			mqc.unsubscribe(this.mqttTopicName);
			mqc.subscribe(this.mqttTopicName);
		} catch (MqttSecurityException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		
		System.out.println("[KETI MQTT Client] Connected to Server - " + mqttServerUrl);
	}
	
	/**
	 * MQTT Broker를 통해 실제 메시지 도착 시 실행되는 Method
	 */
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("[KETI MQTT Client] MQTT Topic \"" + topic + "\" Subscription Payload = " + byteArrayToString(message.getPayload()));
		
		ArrayList<Object> interactionSendArrayList = new ArrayList<Object>();
		interactionSendArrayList.add("requestMgmtCmdControl");
		interactionSendArrayList.add(byteArrayToString(message.getPayload()));
		
		interactionManagerQueue.put(interactionSendArrayList);
	}
	
	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println("[KETI MQTT Client] Message delivered successfully");
	}
	
	// byte배열을 string으로 변환하기 위한 내부 메소드
	public String byteArrayToString(byte[] byteArray)
	{
	    String toString = "";

	    for(int i = 0; i < byteArray.length; i++)
	    {
	        toString += (char)byteArray[i];
	    }

	    return toString;    
	}
}
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

package kr.re.keti.ncube;

/**
 * (Next version �ݿ� ����) oneM2M ǥ�� �ݿ� LocationPolicy ������ ��� �����ϱ� ���� Data Class
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class LocationPolicy {
	
	public String resourceType = null;
	public String resourceID = null;
	public String parentID = null;
	public String creationTime = null;
	public String lastModifiedTime = null;
	public String locationSource = null;
	public String locationUpdatePeriod = null;
	public String locationTargetId = null;
	public String locationServer = null;
	public String locationContainerID = null;
	public String locationContainerName = null;
	public String locationStatus = null;
}
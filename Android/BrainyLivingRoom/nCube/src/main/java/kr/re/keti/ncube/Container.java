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

import java.util.ArrayList;

/**
 * oneM2M 표준 반영 Container Create 정보를 담고 관리하기 위한 Data Class
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class Container {

	public String parentName = null;
	public String resourceType = null;
	public String resourceID = null;
	public String parentID = null;
	public String creationTime = null;
	public String lastModifiedTime = null;
	public String labels = null;
	public String stateTag = null;
	public String maxNrOfInstances = null;
	public String maxByteSize = null;
	public String maxInstanceAge = null;
	public String currentNrOfInstances = null;
	public String currentByteSize = null;
	public String uploadCondition = null;
	public String uploadConditionValue = null;
	public String containerType = null;
	public String heartbeatPeriod = null;
	
	public ArrayList<Object> contentInstance = new ArrayList<Object>();
}
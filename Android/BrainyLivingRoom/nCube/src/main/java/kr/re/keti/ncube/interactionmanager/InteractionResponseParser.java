package kr.re.keti.ncube.interactionmanager;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import kr.re.keti.ncube.AE;
import kr.re.keti.ncube.CSEBase;
import kr.re.keti.ncube.Container;
import kr.re.keti.ncube.DeviceInfo;
import kr.re.keti.ncube.Firmware;
import kr.re.keti.ncube.MgmtCmd;
import kr.re.keti.ncube.Software;

/**
 * Mobius와 Interaction을 통해 수신한 메시지를 파싱하기 위한 Method를 모아놓은 Class로서 각 Method는 Dom Parser로 구현됨
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class InteractionResponseParser {
	
	/**
	 * CSERegistration Method
	 * @param updateProfile
	 * @param responseString
	 * @return CSERegistProfile
	 * @throws Exception
	 * CSE 등록에 대한 리턴메시지를 파싱하기 위한 Method
	 */
	public CSEBase CSERegistration(CSEBase updateProfile, String responseString) throws Exception {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(responseString));
		Document document = builder.parse(xmlSource);
		
		CSEBase CSERegistProfile = updateProfile;
		
		NodeList resourceTypeNodeList = document.getElementsByTagName("resourceType");
		Node resourceTypeNode = resourceTypeNodeList.item(0).getChildNodes().item(0);
		CSERegistProfile.resourceType = resourceTypeNode.getNodeValue();
		
		NodeList resourceIDNodeList = document.getElementsByTagName("resourceID");
		Node resourceIDNode = resourceIDNodeList.item(0).getChildNodes().item(0);
		CSERegistProfile.resourceID = resourceIDNode.getNodeValue();
		
		NodeList parentIDNodeList = document.getElementsByTagName("parentID");
		Node parentIDNode = parentIDNodeList.item(0).getChildNodes().item(0);
		CSERegistProfile.parentID = parentIDNode.getNodeValue();
		
		NodeList creationTimeNodeList = document.getElementsByTagName("creationTime");
		Node creationTimeNode = creationTimeNodeList.item(0).getChildNodes().item(0);
		CSERegistProfile.creationTime = creationTimeNode.getNodeValue();
		
		NodeList lastModifiedTimeNodeList = document.getElementsByTagName("lastModifiedTime");
		Node lastModifiedTimeNode = lastModifiedTimeNodeList.item(0).getChildNodes().item(0);
		CSERegistProfile.lastModifiedTime = lastModifiedTimeNode.getNodeValue();
		
		NodeList pointOfAccessNodeList = document.getElementsByTagName("pointOfAccess");
		Node pointOfAccessNode = pointOfAccessNodeList.item(0).getChildNodes().item(0);
		CSERegistProfile.pointOfAccess = pointOfAccessNode.getNodeValue();
		
		NodeList CSEIDNodeList = document.getElementsByTagName("CSE-ID");
		Node CSEIDNode = CSEIDNodeList.item(0).getChildNodes().item(0);
		CSERegistProfile.CSEID = CSEIDNode.getNodeValue();
		
		NodeList nodeLinkNodeList = document.getElementsByTagName("nodeLink");
		Node nodeLinkNode = nodeLinkNodeList.item(0).getChildNodes().item(0);
		CSERegistProfile.nodeLink = nodeLinkNode.getNodeValue();
		
		NodeList dKeyNodeList = document.getElementsByTagName("dKey");
		Node dKeyNode = dKeyNodeList.item(0).getChildNodes().item(0);
		CSERegistProfile.dKey = dKeyNode.getNodeValue();
		
		return CSERegistProfile;
	}
	
	/**
	 * firmwareCreateParse Method
	 * @param updateInfo
	 * @param responseString
	 * @return firmwareCreateInfo
	 * @throws Exception
	 * firmware create에 대한 리턴메시지를 파싱하기 위한 Method
	 */
	public Firmware firmwareCreateParse(Firmware updateInfo, String responseString) throws Exception {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(responseString));
		Document document = builder.parse(xmlSource);
		
		Firmware firmwareCreateInfo = updateInfo;
		
		NodeList resourceTypeNodeList = document.getElementsByTagName("resourceType");
		Node resourceTypeNode = resourceTypeNodeList.item(0).getChildNodes().item(0);
		firmwareCreateInfo.resourceType = resourceTypeNode.getNodeValue();
		
		NodeList resourceIDNodeList = document.getElementsByTagName("resourceID");
		if (resourceIDNodeList.getLength() > 0) {
			Node resourceIDNode = resourceIDNodeList.item(0).getChildNodes().item(0);
			firmwareCreateInfo.resourceID = resourceIDNode.getNodeValue();
		}
		
		NodeList parentIDNodeList = document.getElementsByTagName("parentID");
		Node parentIDNode = parentIDNodeList.item(0).getChildNodes().item(0);
		firmwareCreateInfo.parentID = parentIDNode.getNodeValue();
		
		NodeList creationTimeNodeList = document.getElementsByTagName("creationTime");
		Node creationTimeNode = creationTimeNodeList.item(0).getChildNodes().item(0);
		firmwareCreateInfo.creationTime = creationTimeNode.getNodeValue();
		
		NodeList lastModifiedTimeNodeList = document.getElementsByTagName("lastModifiedTime");
		Node lastModifiedTimeNode = lastModifiedTimeNodeList.item(0).getChildNodes().item(0);
		firmwareCreateInfo.lastModifiedTime = lastModifiedTimeNode.getNodeValue();
		
		NodeList labelsNodeList = document.getElementsByTagName("labels");
		Node labelsNode = labelsNodeList.item(0).getChildNodes().item(0);
		firmwareCreateInfo.labels = labelsNode.getNodeValue();
		
		NodeList versionNodeList = document.getElementsByTagName("version");
		Node versionNode = versionNodeList.item(0).getChildNodes().item(0);
		firmwareCreateInfo.version = versionNode.getNodeValue();
		
		NodeList nameNodeList = document.getElementsByTagName("name");
		Node nameNode = nameNodeList.item(0).getChildNodes().item(0);
		firmwareCreateInfo.name = nameNode.getNodeValue();
		
		NodeList URLNodeList = document.getElementsByTagName("URL");
		Node URLNode = URLNodeList.item(0).getChildNodes().item(0);
		firmwareCreateInfo.url = URLNode.getNodeValue();
		
		NodeList updateStatusNodeList = document.getElementsByTagName("updateStatus");
		if (updateStatusNodeList.getLength() > 0) {
			Node updateStatusNode = updateStatusNodeList.item(0).getChildNodes().item(0);
			firmwareCreateInfo.updateStatus = updateStatusNode.getNodeValue();
		}
		
		return firmwareCreateInfo;
	}
	
	/**
	 * deviceInfoCreateParse Method
	 * @param updateInfo
	 * @param responseString
	 * @return deviceInfoCreateInfo
	 * @throws Exception
	 * deviceInfo Create에 대한 리턴메시지를 파싱하기 위한 Method
	 */
	public DeviceInfo deviceInfoCreateParse(DeviceInfo updateInfo, String responseString) throws Exception {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(responseString));
		Document document = builder.parse(xmlSource);
		
		DeviceInfo deviceInfoCreateInfo = updateInfo;

		NodeList resourceTypeNodeList = document.getElementsByTagName("resourceType");
		Node resourceTypeNode = resourceTypeNodeList.item(0).getChildNodes().item(0);
		deviceInfoCreateInfo.resourceType = resourceTypeNode.getNodeValue();
		
		NodeList resourceIDNodeList = document.getElementsByTagName("resourceID");
		Node resourceIDNode = resourceIDNodeList.item(0).getChildNodes().item(0);
		deviceInfoCreateInfo.resourceID = resourceIDNode.getNodeValue();
		
		NodeList parentIDNodeList = document.getElementsByTagName("parentID");
		Node parentIDNode = parentIDNodeList.item(0).getChildNodes().item(0);
		deviceInfoCreateInfo.parentID = parentIDNode.getNodeValue();
		
		NodeList creationTimeNodeList = document.getElementsByTagName("creationTime");
		Node creationTimeNode = creationTimeNodeList.item(0).getChildNodes().item(0);
		deviceInfoCreateInfo.creationTime = creationTimeNode.getNodeValue();
		
		NodeList lastModifiedTimeNodeList = document.getElementsByTagName("lastModifiedTime");
		Node lastModifiedTimeNode = lastModifiedTimeNodeList.item(0).getChildNodes().item(0);
		deviceInfoCreateInfo.lastModifiedTime = lastModifiedTimeNode.getNodeValue();
		
		NodeList labelsNodeList = document.getElementsByTagName("labels");
		Node labelsNode = labelsNodeList.item(0).getChildNodes().item(0);
		deviceInfoCreateInfo.labels = labelsNode.getNodeValue();
		
		NodeList descriptionNodeList = document.getElementsByTagName("description");
		Node descriptionNode = descriptionNodeList.item(0).getChildNodes().item(0);
		deviceInfoCreateInfo.description = descriptionNode.getNodeValue();
		
		NodeList manufacturerNodeList = document.getElementsByTagName("manufacturer");
		Node manufacturerNode = manufacturerNodeList.item(0).getChildNodes().item(0);
		deviceInfoCreateInfo.manufacturer = manufacturerNode.getNodeValue();
		
		NodeList modelNodeList = document.getElementsByTagName("model");
		Node modelNode = modelNodeList.item(0).getChildNodes().item(0);
		deviceInfoCreateInfo.model = modelNode.getNodeValue();
		
		NodeList deviceTypeNodeList = document.getElementsByTagName("deviceType");
		Node deviceTypeNode = deviceTypeNodeList.item(0).getChildNodes().item(0);
		deviceInfoCreateInfo.deviceType = deviceTypeNode.getNodeValue();
		
		NodeList fwVersionNodeList = document.getElementsByTagName("fwVersion");
		Node fwVersionNode = fwVersionNodeList.item(0).getChildNodes().item(0);
		deviceInfoCreateInfo.fwVersion = fwVersionNode.getNodeValue();
		
		NodeList hwVersionNodeList = document.getElementsByTagName("hwVersion");
		Node hwVersionNode = hwVersionNodeList.item(0).getChildNodes().item(0);
		deviceInfoCreateInfo.hwVersion = hwVersionNode.getNodeValue();
		
		return deviceInfoCreateInfo;
	}
	
	/**
	 * containerCreateParse Method
	 * @param responseString
	 * @return containerRegistProfile
	 * @throws Exception
	 * Mobius Mashup에 Container 생성 후 받은 리턴메시지를 파싱하기 위한 Method 
	 */
	public Container containerCreateParse(String responseString) throws Exception {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(responseString));
		Document document = builder.parse(xmlSource);
		
		Container containerRegistProfile = new Container();
		
		NodeList resourceTypeNodeList = document.getElementsByTagName("resourceType");
		Node resourceTypeNode = resourceTypeNodeList.item(0).getChildNodes().item(0);
		containerRegistProfile.resourceType = resourceTypeNode.getNodeValue();
		
		NodeList resourceIDNodeList = document.getElementsByTagName("resourceID");
		Node resourceIDNode = resourceIDNodeList.item(0).getChildNodes().item(0);
		containerRegistProfile.resourceID = resourceIDNode.getNodeValue();
		
		NodeList parentIDNodeList = document.getElementsByTagName("parentID");
		Node parentIDNode = parentIDNodeList.item(0).getChildNodes().item(0);
		containerRegistProfile.parentID = parentIDNode.getNodeValue();
		
		NodeList creationTimeNodeList = document.getElementsByTagName("creationTime");
		Node creationTimeNode = creationTimeNodeList.item(0).getChildNodes().item(0);
		containerRegistProfile.creationTime = creationTimeNode.getNodeValue();
		
		NodeList lastModifiedTimeNodeList = document.getElementsByTagName("lastModifiedTime");
		Node lastModifiedTimeNode = lastModifiedTimeNodeList.item(0).getChildNodes().item(0);
		containerRegistProfile.lastModifiedTime = lastModifiedTimeNode.getNodeValue();
		
		NodeList labelsNodeList = document.getElementsByTagName("labels");
		Node labelsNode = labelsNodeList.item(0).getChildNodes().item(0);
		containerRegistProfile.labels = labelsNode.getNodeValue();
		
		NodeList stateTagNodeList = document.getElementsByTagName("stateTag");
		Node stateTagNode = stateTagNodeList.item(0).getChildNodes().item(0);
		containerRegistProfile.stateTag = stateTagNode.getNodeValue();
		
		NodeList maxNrOfInstancesNodeList = document.getElementsByTagName("maxNrOfInstances");
		Node maxNrOfInstancesNode = maxNrOfInstancesNodeList.item(0).getChildNodes().item(0);
		containerRegistProfile.maxNrOfInstances = maxNrOfInstancesNode.getNodeValue();
		
		NodeList maxByteSizeNodeList = document.getElementsByTagName("maxByteSize");
		Node maxByteSizeNode = maxByteSizeNodeList.item(0).getChildNodes().item(0);
		containerRegistProfile.maxByteSize = maxByteSizeNode.getNodeValue();
		
		NodeList maxInstanceAgeNodeList = document.getElementsByTagName("maxInstanceAge");
		Node maxInstanceAgeNode = maxInstanceAgeNodeList.item(0).getChildNodes().item(0);
		containerRegistProfile.maxInstanceAge = maxInstanceAgeNode.getNodeValue();
		
		NodeList currentNrOfInstancesNodeList = document.getElementsByTagName("currentNrOfInstances");
		Node currentNrOfInstancesNode = currentNrOfInstancesNodeList.item(0).getChildNodes().item(0);
		containerRegistProfile.currentNrOfInstances = currentNrOfInstancesNode.getNodeValue();
		
		NodeList currentByteSizeNodeList = document.getElementsByTagName("currentByteSize");
		Node currentByteSizeNode = currentByteSizeNodeList.item(0).getChildNodes().item(0);
		containerRegistProfile.currentByteSize = currentByteSizeNode.getNodeValue();
		
		NodeList uploadConditionNodeList = document.getElementsByTagName("uploadCondition");
		Node uploadConditionNode = uploadConditionNodeList.item(0).getChildNodes().item(0);
		containerRegistProfile.uploadCondition = uploadConditionNode.getNodeValue();
		
		NodeList uploadConditionValueNodeList = document.getElementsByTagName("uploadConditionValue");
		Node uploadConditionValueNode = uploadConditionValueNodeList.item(0).getChildNodes().item(0);
		containerRegistProfile.uploadConditionValue = uploadConditionValueNode.getNodeValue();
		
		NodeList containerTypeNodeList = document.getElementsByTagName("containerType");
		Node containerTypeNode = containerTypeNodeList.item(0).getChildNodes().item(0);
		containerRegistProfile.containerType = containerTypeNode.getNodeValue();

		return containerRegistProfile;
	}
	
	/**
	 * mgmtCmdCreateParse Method
	 * @param responseString
	 * @return mgmtCmdRegistProfile
	 * @throws Exception
	 * Mobius Mashup에 MgmtCmd 생성 후 받은 리턴메시지를 파싱하기 위한 Method 
	 */
	public MgmtCmd mgmtCmdCreateParse(String responseString) throws Exception {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(responseString));
		Document document = builder.parse(xmlSource);
		
		MgmtCmd mgmtCmdRegistProfile = new MgmtCmd();
		
		NodeList resourceTypeNodeList = document.getElementsByTagName("resourceType");
		Node resourceTypeNode = resourceTypeNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.resourceType = resourceTypeNode.getNodeValue();
		
		NodeList resourceIDNodeList = document.getElementsByTagName("resourceID");
		Node resourceIDNode = resourceIDNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.resourceID = resourceIDNode.getNodeValue();
		
		NodeList parentIDNodeList = document.getElementsByTagName("parentID");
		Node parentIDNode = parentIDNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.parentID = parentIDNode.getNodeValue();
		
		NodeList creationTimeNodeList = document.getElementsByTagName("creationTime");
		Node creationTimeNode = creationTimeNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.creationTime = creationTimeNode.getNodeValue();
		
		NodeList lastModifiedTimeNodeList = document.getElementsByTagName("lastModifiedTime");
		Node lastModifiedTimeNode = lastModifiedTimeNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.lastModifiedTime = lastModifiedTimeNode.getNodeValue();
		
		NodeList labelsNodeList = document.getElementsByTagName("labels");
		Node labelsNode = labelsNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.labels = labelsNode.getNodeValue();
		
		NodeList descriptionNodeList = document.getElementsByTagName("description");
		Node descriptionNode = descriptionNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.description = descriptionNode.getNodeValue();
		
		NodeList cmdTypeNodeList = document.getElementsByTagName("cmdType");
		Node cmdTypeNode = cmdTypeNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.cmdType = cmdTypeNode.getNodeValue();
		
		NodeList execTargetNodeList = document.getElementsByTagName("execTarget");
		Node execTargetNode = execTargetNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.execTarget = execTargetNode.getNodeValue();
		
		NodeList execModeNodeList = document.getElementsByTagName("execMode");
		Node execModeNode = execModeNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.execMode = execModeNode.getNodeValue();
		
		NodeList execFrequencyNodeList = document.getElementsByTagName("execFrequency");
		Node execFrequencyNode = execFrequencyNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.execFrequency = execFrequencyNode.getNodeValue();
		
		NodeList execDelayNodeList = document.getElementsByTagName("execDelay");
		Node execDelayNode = execDelayNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.execDelay = execDelayNode.getNodeValue();
		
		NodeList execNumberNodeList = document.getElementsByTagName("execNumber");
		Node execNumberNode = execNumberNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.execNumber = execNumberNode.getNodeValue();
		
		return mgmtCmdRegistProfile;
	}
	
	/**
	 * mgmtCmdRequestParse Method
	 * @param responseString
	 * @return mgmtCmdRegistProfile
	 * @throws Exception
	 * Mobius Mashup에서 MgmtCmd 제어 요청 수신메시지를 파싱하기 위한 Method 
	 */
	public MgmtCmd mgmtCmdRequestParse(String responseString) throws Exception {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(responseString));
		Document document = builder.parse(xmlSource);
		
		MgmtCmd mgmtCmdRegistProfile = new MgmtCmd();
		
		NodeList resourceIDNodeList = document.getElementsByTagName("resourceID");
		Node resourceIDNode = resourceIDNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.resourceID = resourceIDNode.getNodeValue();
		
		NodeList parentIDNodeList = document.getElementsByTagName("parentID");
		Node parentIDNode = parentIDNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.parentID = parentIDNode.getNodeValue();
		
		NodeList execTargetNodeList = document.getElementsByTagName("execTarget");
		Node execTargetNode = execTargetNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.execTarget = execTargetNode.getNodeValue();

		NodeList execReqArgsNodeList = document.getElementsByTagName("execReqArgs");
		Node execReqArgsNode = execReqArgsNodeList.item(0).getChildNodes().item(0);
		mgmtCmdRegistProfile.execReqArgs = execReqArgsNode.getNodeValue();
				
		return mgmtCmdRegistProfile;
	}
	
	
	/**
	 * aeCreateParse Method
	 * @param responseString
	 * @return responseString
	 * @throws Exception
	 * Mobius로 oneM2M AE resource Create 요청을 파싱하기 위한  Method
	 */
	public AE aeCreateParse(String responseString) throws Exception {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(responseString));
		Document document = builder.parse(xmlSource);
		
		AE aeRegistProfile = new AE();
		
		NodeList resourceTypeNodeList = document.getElementsByTagName("resourceType");
		Node resourceTypeNode = resourceTypeNodeList.item(0).getChildNodes().item(0);
		aeRegistProfile.resourceType = resourceTypeNode.getNodeValue();
		
		NodeList parentIDNodeList = document.getElementsByTagName("parentID");
		Node parentIDNode = parentIDNodeList.item(0).getChildNodes().item(0);
		aeRegistProfile.parentID = parentIDNode.getNodeValue();
		
		NodeList creationTimeNodeList = document.getElementsByTagName("creationTime");
		Node creationTimeNode = creationTimeNodeList.item(0).getChildNodes().item(0);
		aeRegistProfile.creationTime = creationTimeNode.getNodeValue();
		
		NodeList lastModifiedTimeNodeList = document.getElementsByTagName("lastModifiedTime");
		Node lastModifiedTimeNode = lastModifiedTimeNodeList.item(0).getChildNodes().item(0);
		aeRegistProfile.lastModifiedTime = lastModifiedTimeNode.getNodeValue();
		
		NodeList labelsNodeList = document.getElementsByTagName("labels");
		Node labelsNode = labelsNodeList.item(0).getChildNodes().item(0);
		aeRegistProfile.labels = labelsNode.getNodeValue();
		
		NodeList nameNodeList = document.getElementsByTagName("name");
		Node nameNode = nameNodeList.item(0).getChildNodes().item(0);
		aeRegistProfile.name = nameNode.getNodeValue();
		
		NodeList appIdNodeList = document.getElementsByTagName("App-ID");
		Node appIdNode = appIdNodeList.item(0).getChildNodes().item(0);
		aeRegistProfile.AppID = appIdNode.getNodeValue();

		NodeList aeIDNodeList = document.getElementsByTagName("AE-ID");
		Node aeIDNode = aeIDNodeList.item(0).getChildNodes().item(0);
		aeRegistProfile.AEID = aeIDNode.getNodeValue();
		
		NodeList pointOfAccessNodeList = document.getElementsByTagName("pointOfAccess");
		Node pointOfAccessNode = pointOfAccessNodeList.item(0).getChildNodes().item(0);
		aeRegistProfile.pointOfAccess = pointOfAccessNode.getNodeValue();
		
		return aeRegistProfile;
	}
	
	/**
	 * applicationDownloadParse Method
	 * @param responseString
	 * @return responseString
	 * @throws Exception
	 * Mobius로부터 수신한 Device Application (AE) Download 요청을 파싱하기 위한  Method
	 */
	public Software applicationDownloadParse(String responseString) throws Exception {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(responseString));
		Document document = builder.parse(xmlSource);
		
		Software softwareDownloadInfo = new Software();
		
		NodeList resourceTypeNodeList = document.getElementsByTagName("resourceType");
		Node resourceTypeNode = resourceTypeNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.resourceType = resourceTypeNode.getNodeValue();
		
		NodeList parentIDNodeList = document.getElementsByTagName("parentID");
		Node parentIDNode = parentIDNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.parentID = parentIDNode.getNodeValue();
		
		NodeList creationTimeNodeList = document.getElementsByTagName("creationTime");
		Node creationTimeNode = creationTimeNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.creationTime = creationTimeNode.getNodeValue();
		
		NodeList lastModifiedTimeNodeList = document.getElementsByTagName("lastModifiedTime");
		Node lastModifiedTimeNode = lastModifiedTimeNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.lastModifiedTime = lastModifiedTimeNode.getNodeValue();
		
		NodeList labelsNodeList = document.getElementsByTagName("labels");
		Node labelsNode = labelsNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.labels = labelsNode.getNodeValue();
		
		NodeList versionNodeList = document.getElementsByTagName("version");
		Node versionNode = versionNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.version = versionNode.getNodeValue();
		
		NodeList nameNodeList = document.getElementsByTagName("name");
		Node nameNode = nameNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.name = nameNode.getNodeValue();
		
		NodeList URLNodeList = document.getElementsByTagName("URL");
		Node URLNode = URLNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.url = URLNode.getNodeValue();
		
		NodeList installNodeList = document.getElementsByTagName("install");
		Node installNode = installNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.install = installNode.getNodeValue();
		
		NodeList uninstallNodeList = document.getElementsByTagName("uninstall");
		Node uninstallNode = uninstallNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.uninstall = uninstallNode.getNodeValue();
		
		return softwareDownloadInfo;
	}
	
	/**
	 * aeCreateParse Method
	 * @param responseString
	 * @return responseString
	 * @throws Exception
	 * Mobius로 oneM2M Software resource Create 요청에 대한 응답을 파싱하기 위한  Method
	 */
	public Software softwareCreateParse(String responseString) throws Exception {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(responseString));
		Document document = builder.parse(xmlSource);
		
		Software softwareDownloadInfo = new Software();
		
		NodeList resourceTypeNodeList = document.getElementsByTagName("resourceType");
		Node resourceTypeNode = resourceTypeNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.resourceType = resourceTypeNode.getNodeValue();
		
		NodeList resourceIdNodeList = document.getElementsByTagName("resourceID");
		Node resourceIdNode = resourceIdNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.resourceID = resourceIdNode.getNodeValue();
		
		NodeList parentIDNodeList = document.getElementsByTagName("parentID");
		Node parentIDNode = parentIDNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.parentID = parentIDNode.getNodeValue();
		
		NodeList creationTimeNodeList = document.getElementsByTagName("creationTime");
		Node creationTimeNode = creationTimeNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.creationTime = creationTimeNode.getNodeValue();
		
		NodeList lastModifiedTimeNodeList = document.getElementsByTagName("lastModifiedTime");
		Node lastModifiedTimeNode = lastModifiedTimeNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.lastModifiedTime = lastModifiedTimeNode.getNodeValue();
		
		NodeList labelsNodeList = document.getElementsByTagName("labels");
		Node labelsNode = labelsNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.labels = labelsNode.getNodeValue();
		
		NodeList versionNodeList = document.getElementsByTagName("version");
		Node versionNode = versionNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.version = versionNode.getNodeValue();
		
		NodeList nameNodeList = document.getElementsByTagName("name");
		Node nameNode = nameNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.name = nameNode.getNodeValue();
		
		NodeList installNodeList = document.getElementsByTagName("install");
		Node installNode = installNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.install = installNode.getNodeValue();
		
		NodeList uninstallNodeList = document.getElementsByTagName("uninstall");
		Node uninstallNode = uninstallNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.uninstall = uninstallNode.getNodeValue();
		
		NodeList installStatusNodeList = document.getElementsByTagName("installStatus");
		Node installStatusNode = installStatusNodeList.item(0).getChildNodes().item(0);
		softwareDownloadInfo.installStatus = installStatusNode.getNodeValue();
		
		return softwareDownloadInfo;
	}
}
package net.local.clustercontrol.core.logic.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.api.model.xml.WorkerResponse;
import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.logic.IWorkerHandler;
import net.local.clustercontrol.core.parsers.IStatusParser;

public abstract class AbstractWorkerHandler implements IWorkerHandler {

	private static final Logger logger = LoggerFactory.getLogger(AbstractWorkerHandler.class);

//	private static final String REPLACEMENT = "¤¤URL¤¤";
	
	String body;
	IHttpClient httpClient;
	String initUrl;
	String initHost;
	HashSet<String> urls = new HashSet<String>();
	Map<String, JkStatus> perHostStatuses = null;
	Map<String, String> statusMessages = new HashMap<String, String>();
	
	public AbstractWorkerHandler(IHttpClient httpClient, String body, String initUrl) {

		this.body = body;
		this.initUrl= initUrl;
		this.httpClient = httpClient;
		urls.add(initUrl);
		
		// setup for subsequent parsing
		IStatusParser parser = handleInit();
		if(parser == null || parser.getStatus() == null) {
			statusMessages.put("initStatus", "nok");
			statusMessages.put("initStatusMessage", "Failed to initialize using URL: "+initUrl);			
			return;
		}
		
		// Init the jkStatus container
		perHostStatuses = new HashMap<String, JkStatus>(1);
		
		this.initHost = getInitHost(initUrl);
		perHostStatuses.put(initUrl, parser.getStatus());
		getInitUrls();
		
		statusMessages.put("initStatus", "ok");
		statusMessages.put("initStatusMessage", "Initialize ["+perHostStatuses.size()+"] using URL: "+initUrl);
		
		// from now on its like polling the state
		handlePoll();
		
	}
	
	private String getInitHost(String initUrl) {
		int beginIndex = initUrl.indexOf("://");
		int endIndex = initUrl.indexOf("/", beginIndex+3);
		if(endIndex==-1) {
			return initUrl.substring(beginIndex+3);			
		} else {
			return initUrl.substring(beginIndex+3, endIndex);
		}
	}

	private void getInitUrls() {
		JkStatus jkStatus = perHostStatuses.get(initUrl);
		int memberCount = jkStatus.getBalancers().getBalancer().getMemberCount();
		String replace = jkStatus.getServer().getName(); 
		for (int i = 0; i < memberCount; i++) {
			JkMember jkMember = jkStatus.getBalancers().getBalancer().getMember().get(i);
			if(jkStatus.getServer().getName().equals(jkMember.getHost())) {
				// this host is already taken care of
				continue;
			}
			String url = initUrl.replaceAll(replace, jkMember.getHost());
			urls.add(url);
		}
	}

	Map<String, JkStatus> getPerHostStatuses() {
		return perHostStatuses;
	}
	
	protected abstract String getType();
	
	public void extractUrls()
	{
		if(perHostStatuses==null) {
			throw new RuntimeException("Not initialized");
		}
		//LinkedHashMap<String, HashMap<String, JkMember>> membersList = new LinkedHashMap<String, HashMap<String, JkMember>>();
		for (JkStatus jkStatus : perHostStatuses.values()) {
			String hostName = jkStatus.getServer().getName();
			Integer hostPort = jkStatus.getServer().getPort();
			List<JkMember> members = jkStatus.getBalancers().getBalancer().getMember();
			
			for (JkMember jkMember : members) {
				String workerName = jkMember.getName();
				logger.debug("Adding jkMember: "+hostName+": "+hostPort+": "+workerName+" "+jkMember.getActivation()+" "+jkMember.getType());
				
//				HashMap<String, JkMember> memberList = membersList.get(workerName);
//				if(memberList == null) {
//					// create new member list
//					memberList = new HashMap<String, JkMember>();
//					membersList.put(workerName, memberList);
//				}
//				memberList.put(hostName, jkMember);
			}
			String hostWithPort = hostName;
			if(hostPort != null && hostPort > 0 && hostPort != 80) {
				hostWithPort = hostWithPort + ":" + hostPort;
			}
//			if(false == _cluster.getHostNames().contains(hostWithPort)) {
//				_cluster.getHostNames().add(hostWithPort);
//			}
//			_cluster.setName(jkStatus.getBalancers().getBalancer().getName());
		}
	}

	protected void getBody(String url) {
		System.out.println("AbstractWorkerHandler");
		WorkerResponse wr = httpClient.getWorkerResponseForUrl(url);
		if(wr.getError() != null) {
			// throw error
			throw new RuntimeException("Failed to get response using URL: "+url);
		}
		body = wr.getBody();
	}
	@Override
	public Map<String, String> getStatus() {
		return statusMessages;
	}
}

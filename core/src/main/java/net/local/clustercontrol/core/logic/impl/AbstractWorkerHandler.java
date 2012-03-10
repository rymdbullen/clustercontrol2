package net.local.clustercontrol.core.logic.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.api.model.xml.WorkerResponse;
import net.local.clustercontrol.core.http.impl.HttpClient;
import net.local.clustercontrol.core.logic.IWorkerHandler;

public abstract class AbstractWorkerHandler implements IWorkerHandler {

	private static final Logger logger = LoggerFactory.getLogger(AbstractWorkerHandler.class);
	
	Map<String, JkStatus> perHostStatuses;
	String url;
	List<String> urls;
	String body;
	@Autowired HttpClient httpClient;
	
	public AbstractWorkerHandler(String url) {
		getBody(url);
		if(!urls.contains(url)) {
			urls.add(url);
		}
	}
	
	Map<String, JkStatus> getPerHostStatuses() {
		return perHostStatuses;
	}
	
	protected abstract String getType();
	
	protected void init(JkStatus jkStatus) {
		perHostStatuses = new HashMap<String, JkStatus>();
		perHostStatuses.put(url, jkStatus);
	}
	
	protected void extractUrls()
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
		WorkerResponse wr = httpClient.getWorkerResponseForUrl(url);
		if(wr.getError() == null) {
			// throw error
			throw new RuntimeException("Failed to get response using URL: "+url);
		}
		body = wr.getBody();
	}
	
	@Override
	public void handleInit() {
		
	}
	@Override
	public void handlePoll() {
		
	}
	@Override
	public void handleStart(String workerId) {
		
	}
	@Override
	public void handleStop(String workerId) {
		
	}
}

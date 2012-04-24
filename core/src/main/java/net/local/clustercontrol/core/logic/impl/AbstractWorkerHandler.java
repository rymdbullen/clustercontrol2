package net.local.clustercontrol.core.logic.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

	static final String REPLACEMENT = "¤¤URL¤¤";
	
	String body;
	IHttpClient httpClient;
	String initUrl;
	HashSet<String> urls = new HashSet<String>();
	ConcurrentHashMap<String, JkStatus> statusesPerHost = null;
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
		statusesPerHost = new ConcurrentHashMap<String, JkStatus>(1);
		
		statusesPerHost.put(initUrl, parser.getStatus());
		getInitUrls();
		
		// from now on its like polling the state
		handlePoll();
		
		statusMessages.put("initStatus", "ok");
		statusMessages.put("initStatusMessage", "Initialize ["+statusesPerHost.size()+"] hosts using URL: "+initUrl);
		
	}
	
	protected abstract String getType();
	
	private void getInitUrls() {
		JkStatus jkStatus = statusesPerHost.get(initUrl);
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

	Map<String, JkStatus> getStatusesPerHost() {
		return statusesPerHost;
	}
	
	protected void action(String workerId, String speed, String action) {
		for (JkStatus status : statusesPerHost.values()) {
			List<JkMember> members = status.getBalancers().getBalancer().getMember();
			for (JkMember member : members) {
				if(!member.getName().equals(workerId)) {
					continue;
				}
				// we want this one
				String contextUrl = createUrl(action, member, workerId);
				
				for (String url : urls)
				{
					if(!url.contains(status.getServer().getName())) {
						continue;
					}
					handleUrl(url, contextUrl);
				} 
			}
		}		
	}
	
	protected void getBody(String url) {
		if(logger.isTraceEnabled()) logger.trace("AbstractWorkerHandler");
		WorkerResponse wr = httpClient.getWorkerResponseForUrl(url);
		if(wr.getError() != null) {
			// throw error
			throw new RuntimeException("Failed to get response using URL: "+url);
		}
		body = wr.getBody();
	}
	@Override
	public Map<String, String> getStatusMessage() {
		return statusMessages;
	}
	@Override
	public Map<String, JkStatus> getStatuses() {
		return statusesPerHost;
	}
}

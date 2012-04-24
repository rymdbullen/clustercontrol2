package net.local.clustercontrol.core.logic.impl;

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
import net.local.clustercontrol.core.logic.ControlCommandException;
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
	
	public AbstractWorkerHandler() {}
	public AbstractWorkerHandler(IHttpClient httpClient, String initUrl) {

		this.initUrl= initUrl;
		this.httpClient = httpClient;

		// implicitly setting body 
		getBody(initUrl);
		urls.add(initUrl);
		
		// setup for subsequent parsing
		IStatusParser parser = handleInit();
		if(parser == null || parser.getStatus() == null) {
			return;
		}
		
		// Init the jkStatus container
		statusesPerHost = new ConcurrentHashMap<String, JkStatus>(1);
		
		statusesPerHost.put(initUrl, parser.getStatus());
		getInitUrls();
		
		// from now on its like polling the state
		handlePoll();
		
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

	protected Map<String, JkStatus> getStatusesPerHost() {
		return statusesPerHost;
	}
	
	protected void action(String workerId, String speed, String action) {
		for (JkStatus status : statusesPerHost.values()) {
			List<JkMember> members = status.getBalancers().getBalancer().getMember();
			for (JkMember member : members) {
				if(!member.getName().equals(workerId)) {
					continue;
				}
				// we want this member
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
	
	/**
	 * Delays the current thread
	 * @param i
	 * @param speed
	 */
	private void delay(int i, String speed) {
		if(speed==null) {
			return;
		}
		int millis = 3000;
    	try {
			long incrementalMillis = millis*i;
			logger.trace("Sleeping between requests: "+incrementalMillis+"ms"); // TODO add thread name
			Thread.sleep(incrementalMillis);
		} catch (InterruptedException e) {
			throw new ControlCommandException("Failed to sleep thread for: "+millis+" millis", e);
		}
	}
	
	protected void getBody(String url) {
		if(logger.isTraceEnabled()) logger.trace("getBody for url {}", url);
		WorkerResponse wr = httpClient.getWorkerResponseForUrl(url);
		if(wr.getError() != null) {
			// throw error
			throw new RuntimeException("Failed to get response using URL: "+url);
		}
		body = wr.getBody();
	}
	
	@Override
	public Map<String, JkStatus> getStatuses() {
		return statusesPerHost;
	}
}

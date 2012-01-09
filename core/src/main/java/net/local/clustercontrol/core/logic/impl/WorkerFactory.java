package net.local.clustercontrol.core.logic.impl;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.api.model.xml.WorkerResponse;
import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.http.impl.HttpClient;
import net.local.clustercontrol.core.logic.ControlCommandException;
import net.local.clustercontrol.core.logic.IWorkerFactory;
import net.local.clustercontrol.core.parsers.StatusParserHtml;
import net.local.clustercontrol.core.parsers.StatusParserXML;

/**
 * All four:
 * GET /balancer-manager?lf=1&ls=0&wr=s1&rr=&dw=Enable&w=ajp://localhost:8009&b=cluster&nonce=5210ef6b-5413-4340-b713-e60e31f5dff6 HTTP/1.1
 * GET /balancer-manager?lf=1&ls=0&wr=s1&rr=&dw=Disable&w=ajp://localhost:8009&b=cluster&nonce=5210ef6b-5413-4340-b713-e60e31f5dff6 HTTP/1.1
 * 
 * GET /balancer-manager?lf=2&ls=0&wr=s2&rr=&dw=Enable&w=ajp://localhost:8019&b=cluster&nonce=5210ef6b-5413-4340-b713-e60e31f5dff6 HTTP/1.1
 * GET /balancer-manager?lf=2&ls=0&wr=s2&rr=&dw=Disable&w=ajp://localhost:8019&b=cluster&nonce=5210ef6b-5413-4340-b713-e60e31f5dff6 HTTP/1.1
 */
@Component
public class WorkerFactory implements IWorkerFactory {

	private static final String REPLACEMENT = "@@replacement@@";
//	private static final String SPEED_MEDIUM = "medium";
//	private static final String SPEED_SLOW = "slow";
//	private static final String SPEED_FAST = "fast";

	private static final Logger logger = LoggerFactory.getLogger(WorkerFactory.class);
	
	private IHttpClient httpClient;
	private String url;
	private HashMap<String, JkStatus> statuses = new HashMap<String, JkStatus>();
	
	@Autowired
	public WorkerFactory(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	@Override
	public HashMap<String, JkStatus> getStatuses() {
		return statuses;
	}

	public void setStatuses(HashMap<String, JkStatus> statuses) {
		this.statuses = statuses;
	}
	@Override
	public boolean init(String url, String workerName, String action, String speed) {
		JkStatus jkStatus = getStatusForUrl(url);
		if(jkStatus == null) {
			return false;
		}
		statuses.put(jkStatus.getServer().getName(), jkStatus);
		this.url = url.replaceAll(jkStatus.getServer().getName(), REPLACEMENT);
		getAllStatuses(jkStatus, workerName, action, speed);
		return true;
	}
	@Override
	public boolean getAllStatuses(String workerName, String action, String speed) {
		if(statuses == null || statuses.size() == 0) {
			throw new IllegalArgumentException("Cluster statuses not initialized");
		}
		JkStatus jkStatus = statuses.get(statuses.keySet().iterator().next());
		getAllStatuses(jkStatus, workerName, action, speed);
		return true;
	}
	public boolean getAllStatuses(JkStatus jkStatus, String workerName, String action, String speed) {
		// create all urls
		String hostName = jkStatus.getServer().getName();
		Integer hostPort = jkStatus.getServer().getPort();
		int memberCount = jkStatus.getBalancers().getBalancer().getMemberCount();
		ArrayList<String> uniqueList = new ArrayList<String>();
		for (int i = 0; i < memberCount; i++) {
			JkMember jkMember = jkStatus.getBalancers().getBalancer().getMember().get(i);
			if(logger.isDebugEnabled()) { logger.debug(hostName+": "+hostPort+": "+jkMember.getName()+" "+jkMember.getActivation()+" "+jkMember.getType()); }
			
			if(false == "poll".equals(action) && false == jkMember.getName().equals(workerName)) {
				if(logger.isTraceEnabled()) { logger.trace("Dont touch this worker: "+jkMember.getName()+ " when we really want "+workerName); }
				continue;
			}
			if(uniqueList.contains(jkMember.getHost())) {
				continue;
			}
			uniqueList.add(jkMember.getHost());
			String newUrl = createContext(jkMember, url, workerName, action);
			
			delay(i, speed);
			
			JkStatus thisJkStatus = getStatusForUrl(newUrl);
			if(thisJkStatus == null) {
				continue;
			}
			statuses.put(jkStatus.getServer().getName(), thisJkStatus);
		}
		if(logger.isTraceEnabled() && statuses.size()==1) {
			logger.trace("No more hosts found except the initial: "+hostName);
		}
		return true;
	}
	
	String createContext(JkMember jkMember, String url, String workerName, String action) {
		String newUrl = url.replaceAll(REPLACEMENT, jkMember.getHost());
		if(action.equals("poll")) {
			return newUrl;
		}
		String lf = ""+jkMember.getLbfactor();
		String ls = ""+jkMember.getRead();  //member.setRead(Integer.valueOf(matcher.group(_5SET).trim()));
		String to = ""+jkMember.getBusy();  //member.setBusy(Integer.valueOf(matcher.group(_8TO).trim()));
		String wr = jkMember.getRoute();
		String actionContext = "&lf="+lf+"&ls="+ls+"&wr="+wr+"&rr=&dw="+action;
		if(logger.isTraceEnabled()) { logger.trace(""+lf+" : "+ls+" : "+to+" : "+jkMember.getType()); }
		if(logger.isDebugEnabled()) { logger.debug("Context: "+actionContext); }
logger.debug("Context: "+actionContext);
		
		String newContext = getContext(jkMember);
		
		String context = newContext.replaceAll(REPLACEMENT, workerName);
		newUrl = newUrl+context  + actionContext; 
		return newUrl;
	}

	private String getContext(JkMember jkMember) {
		String context = jkMember.getType().replaceAll(jkMember.getName(), REPLACEMENT);
		int beginIndex = context.indexOf("?");
		if(beginIndex>0) {
			return context.substring(beginIndex);
		}
		return null;
	}

	private JkStatus getStatusForUrl(String url) {
		//logger.info("Initializing with url: "+url);
		if(false==url.startsWith("http")) {
			url = "http://"+url;
		}
		
		WorkerResponse workerResponse = httpClient.getWorkerResponseForUrl(url);
		
		JkStatus jkStatus = parseWorkerStatus(workerResponse.getBody());
		if(jkStatus	== null) {
			return null;
		}
		return jkStatus;
	}
	/**
	 * 
	 * @param statusBody
	 * @return
	 */
	private JkStatus parseWorkerStatus(String statusBody) {
		
		// 1. Try with ajp host
		StatusParserXML statusParserXML = new StatusParserXML(statusBody); 
		JkStatus jkStatus = statusParserXML.getStatus();
		if(jkStatus != null) {
			return jkStatus;
		}
		
		// 2. Try with html host
		StatusParserHtml statusParserHtml = new StatusParserHtml(statusBody);
		jkStatus = statusParserHtml.getStatus();
		if(jkStatus != null) {
			return jkStatus;
		}
		
		return null;
	}
	/**
	 * 
	 * @param i
	 * @param speed
	 */
	private void delay(int i, String speed) {
		int millis = 3000;
    	try {
			long incrementalMillis = millis*i;
			logger.trace("Sleeping between requests: "+incrementalMillis+"ms"); // TODO add thread name
			Thread.sleep(incrementalMillis);
		} catch (InterruptedException e) {
			throw new ControlCommandException("Failed to sleep thread for: "+millis+" millis", e);
		}
	}
}

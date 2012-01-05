package net.local.clustercontrol.core.logic.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.local.clustercontrol.api.model.xml.JkBalancer;
import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.api.model.xml.WorkerResponse;
import net.local.clustercontrol.api.model.xml.WorkerResponses;
import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.http.impl.HttpClient;
import net.local.clustercontrol.core.logic.ControlCommandException;
import net.local.clustercontrol.core.logic.IWorkerFactory;
import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.core.model.dto.WorkerStatus;
import net.local.clustercontrol.core.model.dto.Workers;
import net.local.clustercontrol.core.parsers.StatusParserHtml;
import net.local.clustercontrol.core.parsers.StatusParserXML;
import net.local.clustercontrol.core.util.StringUtil;

@Component
public class WorkerFactory implements IWorkerFactory {

	private static final Logger logger = LoggerFactory.getLogger(WorkerFactory.class);
	
	private IHttpClient httpClient;
	
	@Autowired
	public WorkerFactory(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	@Override
	public Cluster initCluster(String url) {
		//logger.info("Initializing with url: "+url);
		if(false==url.startsWith("http")) {
			url = "http://"+url;
		}

		WorkerResponse workerResponse = httpClient.getWorkerResponseForUrl(url);
		
		Cluster cluster = new Cluster();
		cluster.setInitUrl(url);
		
		JkStatus jkStatus = parseWorkerStatus(cluster, workerResponse.getBody());
		if(jkStatus	== null) {
			cluster.setStatusMessage("Failed to initialize cluster");
			return null;
		}
		cluster.setAction("poll");
		setHostDetails(cluster);	
		updateCluster(cluster, jkStatus);
		performActionOnCluster(cluster, null, null);
		return cluster;
	}
	/**
	 * Sets the host details on the provided cluster
	 * @param cluster
	 */
	private void setHostDetails(Cluster cluster) {
		Pattern contextPattern = Pattern.compile("(http[s]{0,1}?)://(.*?)(:\\d{1,5})(/.*?)/{0,1}");
		Matcher contextMatcher = contextPattern.matcher(cluster.getInitUrl());
        if (contextMatcher.matches()) {
        	cluster.setContext(contextMatcher.group(4));
        	cluster.setProtocol(contextMatcher.group(1));
        	cluster.setPort(contextMatcher.group(3));
        }
	}

	@Override
	public boolean performActionOnCluster(Cluster cluster, String workerName, String speed) {

		WorkerResponses workerResponses = performAction(cluster, workerName, speed);
		
		if(workerResponses.getResponseList().size() == 0) {			
			// TODO what about the current worker statuses??
			logger.debug("No new statuses to update");
			return true;
		}
		
		List<WorkerResponse> list = workerResponses.getResponseList();
		ArrayList<JkStatus> jkStatuses = new ArrayList<JkStatus>(); 
		for (WorkerResponse workerResponse : list) {
			String body = workerResponse.getBody();
			if(body == null) {
				logger.debug("Error getting worker status: "+workerResponse.getError().getMessage());
				continue;
			}
			JkStatus jkStatus = parseWorkerStatus(cluster, body);
			
			if(workerResponse.getError().getMessage() != null &&
					workerResponse.getError().getMessage().trim().length() > 0) {
				cluster.setStatusMessage(workerResponse.getError().getMessage());
				return false;
			}
			
			// this host has this worker statuses, add to array
			jkStatuses.add(jkStatus);
		}
		cluster.getWorkers().clear();
		cluster.getWorkerNames().clear();
		cluster.getHostNames().clear();
		
		updateCluster(cluster, jkStatuses);
		return true;
	}
	/**
	 * 
	 * @param cluster
	 * @param workerName
	 * @param speed
	 * @return
	 */
	private WorkerResponses performAction(Cluster cluster, String workerName, String speed) {
		WorkerResponses workerResponses = new WorkerResponses();

		// FIXME Concurrency problem with cluster.getWorkers():
		
		ArrayList<String> uniqueList = new ArrayList<String>();
		ArrayList<Workers> allWorkers = cluster.getWorkers();
		for (Workers worker : allWorkers) {
			for (int hostIdx = 0; hostIdx < worker.getStatuses().size(); hostIdx++) {
				WorkerStatus workerStatus = worker.getStatuses().get(hostIdx);
				String hostname = workerStatus.getHostName();
				if(uniqueList.contains(hostname)) {
					continue;
				}
				uniqueList.add(hostname);
				
				if(workerName != null && false == workerName.equals(worker.getName())) {
					// perform action on this worker
					System.out.println("Perform action "+cluster.getAction()+" on workerName:"+workerName+" hostname:"+hostname);
				}
				// create url, we have all data in the worker status
				String context = createContext(workerStatus, cluster.getContext(), cluster.getAction(), cluster.getType(), workerName);
				String url = cluster.getProtocol()+"://"+hostname+cluster.getPort()+context;
				
				delay(hostIdx, speed);
				
				logger.debug(""+hostname+":"+workerStatus.getHostPort()+", "+workerStatus.getType()+ ": "+context);
				workerResponses.getResponseList().add(httpClient.getWorkerResponseForUrl(url));
			}
		}
		uniqueList = null;
		return workerResponses;
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

	/**
	 * 
	 * @param workerStatus
	 * @param context
	 * @param action
	 * @param type
	 * @param workerName
	 * @return
	 */
	private String createContext(WorkerStatus workerStatus, String context, String action, String type, String workerName) {
		if(action.equalsIgnoreCase("poll")) {
			return context;
		}
		String actionContext;
		if(type.equals("XML")) 
		{
			// TODO implement xml action context
			actionContext = "xml-context"+action;
			return StringUtil.removeTrailingSlash(actionContext);
		}
		else if(type.equals("HTML")) 
		{
			String lf = ""+workerStatus.getLoadFactor();
			String ls = ""+workerStatus.getSet();
			String to = ""+workerStatus.getTo();
			String name = workerStatus.getName();
			actionContext = workerStatus.getType() + "&lf="+lf+"&ls="+ls+"&wr="+name+"&rr=&dw="+action;
			logger.debug(""+lf+" : "+ls+" : "+to+" : "+type);
			logger.debug("Context: "+actionContext);
			
			return StringUtil.removeTrailingSlash(actionContext);
		}
		else
		{
			throw new ControlCommandException("Supplied type is not XML or HTML: "+type);
		}
	}
	/**
	 * 
	 * @param statusBody
	 * @return
	 */
	private JkStatus parseWorkerStatus(Cluster cluster, String statusBody) {
		
		// 1. Try with ajp host
		StatusParserXML statusParserXML = new StatusParserXML(statusBody); 
		JkStatus jkStatus = statusParserXML.getStatus();
		if(jkStatus != null) {
			cluster.setType("XML");
			return jkStatus;
		}
		
		// 2. Try with html host
		StatusParserHtml statusParserHtml = new StatusParserHtml(statusBody);
		jkStatus = statusParserHtml.getStatus();
		if(jkStatus != null) {
			cluster.setType("HTML");
			return jkStatus;
		}
		
		return null;
	}
	/**
	 * Converts the statuses from per-host to per worker. transforms the matrix.
	 * 
	 * @param cluster
	 * @param jkStatus
	 */
	private void updateCluster(Cluster cluster, JkStatus jkStatus) {
		ArrayList<JkStatus> firstStatus = new ArrayList<JkStatus>();
		firstStatus.add(jkStatus);
		updateCluster(cluster, firstStatus);
	}

	/**
	 * Converts the statuses from per-host to per worker. transforms the matrix.
	 * @param cluster
	 * @param jkStatuses
	 */
	void updateCluster(Cluster cluster, ArrayList<JkStatus> jkStatuses) {
		LinkedHashMap<String, HashMap<String, JkMember>> membersList = new LinkedHashMap<String, HashMap<String, JkMember>>();

		// convert statuses to cluster object
		for (JkStatus jkStatus : jkStatuses) {
			JkBalancer balancer = jkStatus.getBalancers().getBalancer();
			String hostName = jkStatus.getServer().getName();
			Integer hostPort = jkStatus.getServer().getPort();
			List<JkMember> members = balancer.getMember();
			
			for (JkMember jkMember : members) {
				logger.debug(hostName+": "+hostPort+": "+jkMember.getName()+" "+jkMember.getActivation()+" "+jkMember.getType());
				
				String workerName = jkMember.getName();
				HashMap<String, JkMember> memberList = membersList.get(workerName);
				if(memberList == null) {
					// create new member list
					memberList = new HashMap<String, JkMember>();
					membersList.put(workerName, memberList);
				}
				memberList.put(hostName, jkMember);
			}
			String hostWithPort = hostName;
			if(hostPort!=null && hostPort > 0) {
				hostWithPort = hostWithPort + ":" + hostPort;
			}
			if(false == cluster.getHostNames().contains(hostWithPort)) {
				cluster.getHostNames().add(hostWithPort);
			}
			cluster.setName(jkStatus.getBalancers().getBalancer().getName());
		}
		
		// convert to cluster
		
		Iterator<String> keysIter = membersList.keySet().iterator();
		while (keysIter.hasNext()) {
			Workers workers = new Workers();
			String workerName = keysIter.next();
			workers.setName(workerName);
			HashMap<String, JkMember> workerMemberList = membersList.get(workerName);
			Iterator<String> workerMemberListIter = workerMemberList.keySet().iterator();
			while (workerMemberListIter.hasNext()) {
				String hostName = workerMemberListIter.next();
				JkMember jkMember = workerMemberList.get(hostName);
				WorkerStatus workerStatus = new WorkerStatus();
				workerStatus.setHostName(hostName);
				workerStatus.setHostPort(""+jkMember.getPort());
				workerStatus.setStatus(jkMember.getActivation().trim());
				workerStatus.setType(jkMember.getType().trim());
				workerStatus.setTo(jkMember.getBusy());  // html to
				workerStatus.setSet(jkMember.getRead()); // html set
				workerStatus.setTransferred(jkMember.getTransferred());
				workerStatus.setLoadFactor(jkMember.getLbfactor());
				workerStatus.setName(jkMember.getName());
				workers.getStatuses().add(workerStatus);
			}
			cluster.getWorkers().add(workers);
		}
		
		cluster.getWorkerNames().addAll(membersList.keySet());
	}
}

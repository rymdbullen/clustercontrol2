package net.local.clustercontrol.core.logic.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import net.local.clustercontrol.api.model.xml.JkBalancer;
import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.core.logic.IWorkerFactory;
import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.core.model.dto.WorkerStatus;
import net.local.clustercontrol.core.model.dto.Workers;
import net.local.clustercontrol.core.parsers.StatusParserHtml;
import net.local.clustercontrol.core.parsers.StatusParserXML;

@Component
public class WorkerFactory implements IWorkerFactory {

	private static final Logger logger = LoggerFactory.getLogger(WorkerFactory.class);
	
	@Override
	public ArrayList<Workers> getWorkersForUrl(String url) {
		logger.info("Initializing with url: "+url);
		if(false==url.startsWith("http")) {
			url = "http://"+url;
		}
		
		// 1. Try with ajp host
		StatusParserXML statusParserXML = new StatusParserXML(url); 
		JkStatus jkStatus = statusParserXML.getStatus();
		if(jkStatus!=null) {
			return convert(jkStatus);
		}
		
		// 2. Try with html host
		StatusParserHtml statusParserHtml = new StatusParserHtml(url);
		jkStatus = statusParserHtml.getStatus();
		if(jkStatus!=null) {
			return convert(jkStatus);
		}
		
		// TODO Implement init 
		return null;
	}

	public ArrayList<Workers> convert(JkStatus status) {
		return null;
	}
	public ArrayList<Workers> convert(ArrayList<JkStatus> statuses, Cluster cluster) {
		LinkedHashMap<String, HashMap<String, String>> workerList = new LinkedHashMap<String, HashMap<String, String>>();
		// convert statuses to cluster object
		for (int jkStatusIdx = 0; jkStatusIdx < statuses.size(); jkStatusIdx++) {
			JkBalancer balancer = statuses.get(jkStatusIdx).getBalancers().getBalancer();
			String hostName = statuses.get(jkStatusIdx).getServer().getName();
			for (int memberIdx = 0; memberIdx < balancer.getMember().size(); memberIdx++) {
				JkMember workerStatus = balancer.getMember().get(memberIdx);
				logger.debug(hostName+": ["+jkStatusIdx+":"+memberIdx+"]: "+workerStatus.getName()+" "+workerStatus.getActivation());
				String workerName = workerStatus.getName();
				String status = workerStatus.getActivation();
				HashMap<String, String> hostList = workerList.get(workerName);
				if(hostList==null) {
					// create new list
					hostList = new HashMap<String, String>();
					workerList.put(workerName, hostList);
				}
				hostList.put(hostName, status);
			}
			cluster.getHostNames().add(hostName);
		}
		
		// convert to cluster
		
		Workers workers = new Workers();
		Iterator<String> keysIter = workerList.keySet().iterator();
		while (keysIter.hasNext()) {
			String key = (String) keysIter.next();
			HashMap<String, String> workerList1 = workerList.get(key);
			Iterator<String> workerKeysIter = workerList1.keySet().iterator();
			while (workerKeysIter.hasNext()) {
				String hostName = workerKeysIter.next();
				String status = workerList1.get(hostName);
				WorkerStatus thisWorkerStatus = new WorkerStatus();
				thisWorkerStatus.setHostName(hostName);
				thisWorkerStatus.setStatus(status);
				workers.getStatuses().add(thisWorkerStatus);
			}
		}
		
		cluster.getWorkerNames().addAll(workerList.keySet());
		cluster.getWorkers().add(workers);
		
		return null;
	}
	@Override
	public Cluster getAllStatuses(JkStatus jkStatus) {
		Cluster cluster = new Cluster();
		Integer numBalancers = jkStatus.getBalancers().getCount();
		if(numBalancers == null) {
			return null;
		}
		JkBalancer balancer = jkStatus.getBalancers().getBalancer();
		int numMembers = balancer.getMember().size();
		for (int memberIdx = 0; memberIdx < numMembers; memberIdx++) {
			
			// get one member at a time
			//JkMember member = balancer.getMember().get(memberIdx);
			
			// get all members at a time
			List<JkMember> members = getMembers(jkStatus);
			if(members==null) {
				continue;
			}
			HashSet<String> addressSet = getUniqueHosts(members);
			if (addressSet == null || addressSet.size() == 0) {
				return null;
			}
			Iterator<String> addressIterator = addressSet.iterator();
			while (addressIterator.hasNext()) {
				String ipaddress = (String) addressIterator.next();
				if(ipaddress.equals(jkStatus.getServer().getName())) {
					if(logger.isDebugEnabled()) { logger.debug("Skipped existing host: "+ipaddress); }
					continue;
				}
				//String newUrl = createUrlForIpaddress(ipaddress);
				//cluster. = getStatusForUrl(newUrl);
				//List<JkMember> newMembers = getMembers(newJkStatus);
				//if(newMembers==null) {
				//	continue;
				//}
				//addHost(createHost(newUrl));
				//statuses.add(newJkStatus);
			}
		}
		
		return cluster;
	}
	/**
	 * Returns the members for all balancers
	 * @param jkStatus the status to extract members from
	 * @return the members for all balancers
	 */
	private List<JkMember> getMembers(JkStatus jkStatus) {
		if(jkStatus.getBalancers() != null && 
				jkStatus.getBalancers().getBalancer() != null &&
				jkStatus.getBalancers().getBalancer().getMemberCount() > 0) {
			logger.debug("Getting "+jkStatus.getBalancers().getBalancer().getMember()+" balancers");
			return jkStatus.getBalancers().getBalancer().getMember();			
		}
		return null;
	}
	/**
	 * Returns a list of unique host addresses, or null if no more than one host is found
	 * @param members the members to get hosts from
	 * @return a list of unique host addresses, or null if no more than one host is found
	 */
	private HashSet<String> getUniqueHosts(List<JkMember> members) {
		HashSet<String> addressSet = new HashSet<String>(); 
		int memberCount = members.size();
		for (int memberIdx = 0; memberIdx < memberCount; memberIdx++) {
			JkMember member = members.get(memberIdx);
			String host = member.getHost();
			if(logger.isDebugEnabled()) {
				logger.debug(member.getName()+" using host: "+host);
			}
			addressSet.add(host);
		}
		if(addressSet.size()>1) {
			return addressSet;
		}
		if(logger.isDebugEnabled()) {
			logger.debug("found no more hosts");
		}
		return null;
	}
	/**
	 * 
	 * @param ipaddress
	 * @return
	 * @throws MalformedURLException
	 */
	private static String createUrlForIpaddress(String ipaddress) {
//		URL newUrl = new URL(_protocol, ipaddress, _port, _context);
		try {
			URL newUrl = new URL("_protocol", ipaddress, 8080, "_context");
			return newUrl.toExternalForm();
		} catch (MalformedURLException e) {
			logger.error("Failed to create url for: "+ipaddress);
			return null;
		}
	}
}

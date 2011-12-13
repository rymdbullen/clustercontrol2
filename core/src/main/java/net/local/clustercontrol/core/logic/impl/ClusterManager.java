package net.local.clustercontrol.core.logic.impl;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.local.clustercontrol.api.model.xml.Host;
import net.local.clustercontrol.api.model.xml.Hosts;
import net.local.clustercontrol.api.model.xml.JkBalancer;
import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.api.model.xml.WorkerResponse;
import net.local.clustercontrol.api.model.xml.WorkerResponses;
import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.http.impl.HttpClient;
import net.local.clustercontrol.core.logic.ControlCommandException;
import net.local.clustercontrol.core.logic.IWorkerFactory;
import net.local.clustercontrol.core.logic.IWorkerManager;
import net.local.clustercontrol.core.logic.WorkerNotFoundException;
import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.core.model.dto.WorkerStatus;
import net.local.clustercontrol.core.model.dto.Workers;
import net.local.clustercontrol.core.util.StringUtil;

@Service
public class ClusterManager implements IWorkerManager {

	private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);

	private IWorkerFactory workerFactory;
	private IHttpClient httpClient;

	private Cluster _cluster;

	@Autowired
	public ClusterManager(WorkerFactory workerFactory, HttpClient httpClient) {
		this.workerFactory = workerFactory;
		this.httpClient = httpClient;
	}
	
	/**
	 * Initializes the ClusterManager with a url.
	 * @param url the url to initialize with
	 * @throws MalformedURLException
	 * @throws WorkerNotFoundException
	 */
	@Override
	public boolean init(String url) throws MalformedURLException, WorkerNotFoundException {
		if(_cluster != null && _cluster.getWorkers() != null && _cluster.getWorkers().size() > 0) {
			logger.debug("Already initialized: "+ _cluster.getWorkerNames());
			_cluster.setStatusMessage("already intialized");
			
			// DEBUG set cluster
			_cluster = populate();

			return true;
		}
		_cluster = new Cluster();
		
		_cluster = populate();

		// DEBUG
//		ArrayList<Workers> workers = workerFactory.getWorkersForUrl(url);
//		if(workers == null) {
//			_cluster.setStatusMessage("nok");
//			return false;
//		}
//		_cluster.setWorkers(workers);
		_cluster.setStatusMessage("ok");
		return true;
	}

	@Override
	public Cluster enable(String workerName, String speed) {
		Hosts hosts = new Hosts();
		for (int i = 0; i < _cluster.getWorkers().size(); i++) {
			Workers worker = _cluster.getWorkers().get(i);
			delay(i, speed);
			String type = worker.getType();
			ArrayList<WorkerStatus> workerStatuses = worker.getStatuses();
			for (WorkerStatus workerStatus : workerStatuses) {
				String hostName = workerStatus.getHostName();
				String hostPort = workerStatus.getHostPort();
				logger.info("Activating ["+type+"] worker: " + worker + " on host: "+ hostName);
				Host host = createHost(hostName, hostPort, worker, type);
				hosts.getHostList().add(host);
			}
		}
		updateCluster(httpClient.performActionOnHosts(hosts));
		return null;
	}
	/**
	 * 
	 * @param workerResponses
	 */
	private void updateCluster(WorkerResponses workerResponses) {
		List<WorkerResponse> list = workerResponses.getResponseList();
		for (WorkerResponse workerResponse : list) {
			workerResponse.getBody();
			workerResponse.getHost();
			workerResponse.getError().getMessage();
		}
	}
	/**
	 * 
	 * @param hostname
	 * @param port
	 * @param worker
	 * @param type
	 * @return
	 */
	private Host createHost(String hostname, String port, Workers worker, String type) {
		String action = getActionForType(type);
		Host host = new Host();
		host.setIpAddress(hostname);
		if(port!=null) {
			host.setPort(Integer.parseInt(port));
		}
		host.setContext(StringUtil.checkPath(action));
		return host;
	}
	/**
	 * 
	 * @param type
	 * @return
	 */
	private String getActionForType(String type) {
		if(type.equals("XML")) {
			return "xml-context";
		} else if(type.equals("XML")) {
			return "html-context";
		} else {
			throw new ControlCommandException("Supplied type is not XML or HTML: "+type);
		}
	}

	@Override
	public Cluster disable(String worker, String speed) {
		throw new IllegalArgumentException("Not implemented yet");
	}

	@Override
	public Cluster stop(String worker) {
		throw new IllegalArgumentException("Not implemented yet");
	}

	@Override
	public Cluster getCluster() {
		return _cluster;
	}
	private void delay(int i, String speed) {
		int millis = 3000;
    	try {
			long incrementalMillis = millis*i;
			Thread.sleep(incrementalMillis);
		} catch (InterruptedException e) {
			throw new ControlCommandException("Failed to sleep thread for: "+millis+" millis", e);
		}
	}
	
	@Override
	public Cluster poll() {
		// DEBUG set cluster
		_cluster = populate();
		return _cluster;
	}

	// ================================================================================
	// ================================================================================
	// ================================================================================
	
	public void convert(ArrayList<JkStatus> statuses, Cluster cluster) {
		LinkedHashMap<String, HashMap<String, String>> list = new LinkedHashMap<String, HashMap<String, String>>();
		// convert statuses to cluster object
		for (int jkStatusIdx = 0; jkStatusIdx < statuses.size(); jkStatusIdx++) {
			JkBalancer balancer = statuses.get(jkStatusIdx).getBalancers().getBalancer();
			String hostName = statuses.get(jkStatusIdx).getServer().getName();
			for (int memberIdx = 0; memberIdx < balancer.getMember().size(); memberIdx++) {
				JkMember workerStatus = balancer.getMember().get(memberIdx);
				logger.debug(hostName+": ["+jkStatusIdx+":"+memberIdx+"]: "+workerStatus.getName()+" "+workerStatus.getActivation());
				String workerName = workerStatus.getName();
				String status = workerStatus.getActivation();
				HashMap<String, String> workerList = list.get(workerName);
				if(workerList==null) {
					// create new list
					workerList = new HashMap<String, String>();
					list.put(workerName, workerList);
				}
				workerList.put(hostName, status);
			}
			cluster.getHostNames().add(hostName);
		}
		
		// convert to cluster
		
		Workers workers = new Workers();
		Iterator<String> keysIter = list.keySet().iterator();
		while (keysIter.hasNext()) {
			String key = (String) keysIter.next();
			HashMap<String, String> workerList = list.get(key);
			Iterator<String> workerKeysIter = workerList.keySet().iterator();
			while (workerKeysIter.hasNext()) {
				String hostName = workerKeysIter.next();
				String status = workerList.get(hostName);
				WorkerStatus thisWorkerStatus = new WorkerStatus();
				thisWorkerStatus.setHostName(hostName);
				thisWorkerStatus.setStatus(status);
				workers.getStatuses().add(thisWorkerStatus);
			}
		}
		
		cluster.getWorkerNames().addAll(list.keySet());
		cluster.getWorkers().add(workers);
	}

	private Cluster populate() {
		Workers workers1 = new Workers();
		workers1.setName("workerName1");
		ArrayList<WorkerStatus> workerStatusList1 = new ArrayList<WorkerStatus>();
		WorkerStatus workerStatus11 = setWorkerStatus("wsHost1");
		WorkerStatus workerStatus12 = setWorkerStatus("wsHost2");
		workerStatusList1.add(workerStatus11);
		workerStatusList1.add(workerStatus12);
		workers1.setStatuses(workerStatusList1);
		
		ArrayList<WorkerStatus> workerStatusList2 = new ArrayList<WorkerStatus>();
		WorkerStatus workerStatus21 = setWorkerStatus("wsHost1");
		WorkerStatus workerStatus22 = setWorkerStatus("wsHost2");
		workerStatusList2.add(workerStatus21);
		workerStatusList2.add(workerStatus22);
		
		Workers workers2 = new Workers();
		workers2.setName("workerName2");
		workers2.setStatuses(workerStatusList2);
		
		ArrayList<Workers> workersList = new ArrayList<Workers>();
		workersList.add(workers1);
		workersList.add(workers2);
		
		Cluster cluster = new Cluster();
		cluster.setWorkers(workersList);
		//cluster.setWorkerHosts(list);
		ArrayList<String> names = new ArrayList<String>();
		names.add(workerStatus11.getHostName());
		names.add(workerStatus21.getHostName());
		cluster.setHostNames(names);
		
		return cluster;
	}

	private WorkerStatus setWorkerStatus(String hostName) {
		WorkerStatus status = new WorkerStatus();
		status.setHostName(hostName);
		if(Math.random() < 0.5d) {
			status.setLastStatus("nok");
			status.setStatus("ok");
		} else {
			status.setLastStatus("ok");
			status.setStatus("nok");			
		}
		return status;
	}	
}

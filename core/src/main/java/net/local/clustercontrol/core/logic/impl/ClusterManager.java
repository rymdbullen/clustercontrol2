package net.local.clustercontrol.core.logic.impl;

import java.net.MalformedURLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import net.local.clustercontrol.core.logic.IWorkerFactory;
import net.local.clustercontrol.core.logic.IWorkerManager;
import net.local.clustercontrol.core.logic.WorkerNotFoundException;
import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.core.model.dto.WorkerStatus;
import net.local.clustercontrol.core.model.dto.Workers;

@Service
public class ClusterManager implements IWorkerManager {

	private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);

	private IWorkerFactory workerFactory;

	private Cluster _cluster;

	@Autowired
	public ClusterManager(WorkerFactory workerFactory) {
		this.workerFactory = workerFactory;
	}
	
	/**
	 * Initializes the ClusterManager with a url.
	 * @param url the url to initialize with
	 * @throws MalformedURLException
	 * @throws WorkerNotFoundException
	 */
	@Override
	public boolean init(String url) {
		if(_cluster != null && _cluster.getWorkers() != null) {
			logger.debug("Already initialized: "+ _cluster.getWorkerNames());
			_cluster.setStatusMessage("Already intialized");
			
			return true;
		}
		// initialize new cluster
		_cluster = new Cluster();

		Cluster cluster = workerFactory.initCluster(url);
		if(cluster == null) {
			_cluster.setStatusMessage("nok");
			return false;
		}
		// initialize new cluster
		_cluster.setStatusMessage("ok");
		_cluster = cluster;
	
		return true;
	}

	@Override
	@Async
	public Cluster enable(String workerName, String speed) {
		if(logger.isTraceEnabled()) {
			String threadName = Thread.currentThread().getName();
			logger.trace("Process thread " + threadName + " using Async method");
		}
		_cluster.setAction("Enable");
		boolean isSuccess = workerFactory.performActionOnCluster(_cluster, workerName, speed);
		if(isSuccess) {
			_cluster.setStatusMessage("Updated cluster status");
			return _cluster;			
		}
		_cluster.setStatusMessage("Failed to update cluster status");
		return _cluster;
	}
	@Override
	@Async
	public Cluster disable(String workerName, String speed) {
		if(logger.isTraceEnabled()) {
			String threadName = Thread.currentThread().getName();
			logger.trace("Process thread '" + threadName + "' using Async method");
		}
		_cluster.setAction("Disable");
		boolean isSuccess = workerFactory.performActionOnCluster(_cluster, workerName, speed);
		if(isSuccess) {
			_cluster.setStatusMessage("Updated cluster status");
			return _cluster;			
		}
		_cluster.setStatusMessage("Failed to update cluster status");
		return _cluster;
	}

	@Override
	@Async
	public Cluster stop(String workerName) {
		if(logger.isTraceEnabled()) {
			String threadName = Thread.currentThread().getName();
			logger.trace("Process thread " + threadName + " using Async method");
		}
		_cluster.setAction("Disable");
		boolean isSuccess = workerFactory.performActionOnCluster(_cluster, workerName, null);
		if(isSuccess) {
			_cluster.setStatusMessage("Updated cluster status");
			return _cluster;			
		}
		_cluster.setStatusMessage("Failed to update cluster status");
		return _cluster;
	}
	
	@Override
	public Cluster getCluster() {
		return _cluster;
	}
	
	@Override
	public Cluster poll() {
		if(_cluster==null) {
			return null;
		}
		_cluster.setAction("poll");
		boolean isSuccess = workerFactory.performActionOnCluster(_cluster, null, null);
		if(isSuccess) {
			_cluster.setStatusMessage("Updated cluster status");
			return _cluster;			
		}
		_cluster.setStatusMessage("Failed to update cluster status");
		return _cluster;
	}

	// ================================================================================
	// ================================================================================
	// ================================================================================
	
	@SuppressWarnings("unused")
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

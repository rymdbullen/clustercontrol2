package net.local.clustercontrol.core.logic.impl;

import java.net.MalformedURLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.local.clustercontrol.api.model.JkStatus;
import net.local.clustercontrol.core.logic.IWorkerFactory;
import net.local.clustercontrol.core.logic.IWorkerManager;
import net.local.clustercontrol.core.logic.WorkerNotFoundException;

public class ClusterManager implements IWorkerManager {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);

//	@Autowired
	private IWorkerFactory workerFactory;

	private ArrayList<JkStatus> statuses;
	
	/**
	 * Initializes the WorkerManager with a url. 
	 * @param url the url to initialize with
	 * @throws MalformedURLException 
	 * @throws WorkerNotFoundException 
	 */
	public ArrayList<JkStatus> init(String url) throws MalformedURLException, WorkerNotFoundException {
		
		JkStatus primaryJkStatus = workerFactory.getStatusForUrl(url);
		
		ArrayList<JkStatus> jkStatuses = workerFactory.getMultipleStatusForStatus(primaryJkStatus);
		this.statuses = jkStatuses;
		return getStatuses();
	}

	public ArrayList<JkStatus> getStatuses() {
		return statuses;
	}
}

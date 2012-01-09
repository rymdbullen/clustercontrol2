package net.local.clustercontrol.core.logic;

import java.util.HashMap;

import net.local.clustercontrol.api.model.xml.JkStatus;

public interface IWorkerFactory {
	/**
	 * Returns 
	 * @param url the url to get status for
	 * @param workerName
	 * @param action 
	 * @param speed 
	 * @return 
	 */
	boolean init(String url, String workerName, String action, String speed);
	
	HashMap<String, JkStatus> getStatuses();
	
	boolean getAllStatuses(String workerName, String action, String speed);
}

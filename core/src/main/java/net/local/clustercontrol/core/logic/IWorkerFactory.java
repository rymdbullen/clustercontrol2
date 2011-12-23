package net.local.clustercontrol.core.logic;

import net.local.clustercontrol.core.model.dto.Cluster;

public interface IWorkerFactory {
	/**
	 * Returns 
	 * @param cluster
	 * @param workerName 
	 * @param speed
	 * @return
	 */
	boolean performActionOnCluster(Cluster cluster, String workerName, String speed);
	/**
	 * Returns 
	 * @param url the url to get status for
	 * @return 
	 */
	Cluster initCluster(String url);
}

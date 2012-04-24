package net.local.clustercontrol.core.logic;

import java.util.Map;

import net.local.clustercontrol.core.model.dto.Cluster;

/**
 * @author jstenvall
 *
 */
public interface IClusterManager {
	/**
	 * Returns the status message for an initialization of the ClusterManager with the provided url
	 * @param url the url to initialize with
	 */
	Map<String, String> init(String url);
	/**
	 * Enables the worker and returns a boolean status of the action
	 * @param worker the worker to enable
	 * @param speed the speed to enable the worker
	 * @return list of statusesPerHost
	 */
	void enable(String worker, String speed);
	/**
	 * Disables the worker and returns a boolean status of the action
	 * @param worker the worker to disable
	 * @param speed the speed to disable the worker
	 * @return list of statusesPerHost
	 */
	void disable(String worker, String speed);
	/**
	 * Stops the worker and returns a boolean status of the action
	 * @param worker the worker to disable
	 * @param speed the speed to stop the worker
	 * @return list of statusesPerHost
	 */
	void stop(String worker);
	/**
	 * Returns the cluster instance
	 * @return the cluster instance
	 */
	Cluster getCluster();
	/**
	 * Polls status from workers
	 * @return
	 */
	boolean poll();
}

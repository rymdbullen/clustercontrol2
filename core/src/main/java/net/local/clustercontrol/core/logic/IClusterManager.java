package net.local.clustercontrol.core.logic;

import java.io.Serializable;
import java.util.Map;

import net.local.clustercontrol.core.model.dto.Cluster;

/**
 * @author jstenvall
 *
 */
public interface IClusterManager extends Serializable {
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
	 * Handles the action on the worker with the provided speed
	 * @param workerId
	 * @param speed
	 * @param action 
	 */
	void handle(String workerId, String speed, String action);
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
	void poll();
}

package net.local.clustercontrol.core.logic;

import java.net.MalformedURLException;
import java.util.Map;

import net.local.clustercontrol.core.model.dto.Cluster;

/**
 * @author jstenvall
 *
 */
public interface IClusterManager {
	/**
	 * Initializes the WorkerManager with a url a boolean status of the action
	 * @param url the url to initialize with
	 * @throws MalformedURLException 
	 * @throws WorkerNotFoundException 
	 */
	Map<String, String> init(String url);
	/**
	 * Enables the worker for the supplied loadbalancer and returns a boolean status of the action
	 * @param worker the worker to enable
	 * @param speed the speed to enable the worker
	 * @return list of statuses
	 */
	boolean enable(String worker, String speed);
	/**
	 * Disables the worker for the supplied loadbalancer and returns a boolean status of the action
	 * @param worker the worker to disable
	 * @param speed the speed to disable the worker
	 * @return list of statuses
	 */
	boolean disable(String worker, String speed);
	/**
	 * Stops the worker for the supplied loadbalancer and returns a boolean status of the action
	 * @param worker the worker to disable
	 * @return list of statuses
	 */
	boolean stop(String worker);
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

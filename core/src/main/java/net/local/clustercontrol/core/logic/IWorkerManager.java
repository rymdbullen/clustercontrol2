package net.local.clustercontrol.core.logic;

import java.net.MalformedURLException;

import net.local.clustercontrol.core.model.dto.Cluster;

/**
 * @author jstenvall
 *
 */
public interface IWorkerManager {
	/**
	 * Initializes the WorkerManager with a url. 
	 * @param url the url to initialize with
	 * @throws MalformedURLException 
	 * @throws WorkerNotFoundException 
	 */
	boolean init(String url) throws MalformedURLException, WorkerNotFoundException;
	/**
	 * Enables the worker for the supplied loadbalancer and returns the list of balancers
	 * @param worker the worker to enable
	 * @param speed the speed to enable the worker
	 * @return list of statuses
	 */
	Cluster enable(String worker, String speed);
	/**
	 * Disables the worker for the supplied loadbalancer and returns the list of balancers
	 * @param worker the worker to disable
	 * @param speed the speed to disable the worker
	 * @return list of statuses
	 */
	Cluster disable(String worker, String speed);
	/**
	 * Stops the worker for the supplied loadbalancer and returns the list of balancers
	 * @param worker the worker to disable
	 * @return list of statuses
	 */
	Cluster stop(String worker);
	/**
	 * Returns the cluster instance
	 * @return the cluster instance
	 */
	Cluster getCluster();
	/**
	 * Polls status from workers
	 * @return
	 */
	Cluster poll();
}

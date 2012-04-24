package net.local.clustercontrol.core.logic;

import java.util.Map;

import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.core.parsers.IStatusParser;

public interface IWorkerHandler {
	
	/**
	 * This method will setup the handler to handle subsequent calls
	 * @return 
	 */
	IStatusParser handleInit();

	/**
	 * Handles the poll of all statues on all servers in cluster
	 */
	void handlePoll();
	
	/**
	 * Handles the start of a worker on all servers in cluster
	 * @param workerId
	 * @param speed
	 */
	void handleStart(String workerId, String speed);
	
	/**
	 * Handles the stop of a worker on all servers in cluster
	 * @param workerId
	 * @param speed
	 */
	void handleStop(String workerId, String speed);

	/**
	 * Creates the action url for each server in cluster
	 * @param action the action to perform
	 * @param jkMember the jkMember to get data from to construct the url
	 * @param workerId the worker id to handle
	 * @return the action url for each server in cluster
	 */
	String createUrl(String action, JkMember jkMember, String workerId);

	/**
	 * Returns all the statuses for all servers in cluster 
	 * @return all the statuses for all servers in cluster
	 */
	Map<String, JkStatus> getStatuses();

	/**
	 * Handles the url, i.e. gets the html/xml body and parses it 
	 * @param url the url to get body for
	 * @param context the context of the url
	 */
	void handleUrl(String url, String context);
	
}

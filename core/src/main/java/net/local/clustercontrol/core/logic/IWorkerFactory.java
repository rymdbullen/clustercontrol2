package net.local.clustercontrol.core.logic;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashMap;

import net.local.clustercontrol.api.model.xml.JkStatus;

public interface IWorkerFactory {
	/**
	 * Returns 
	 * @param body the body to get status for
	 * @param workerName
	 * @param action 
	 * @param speed 
	 * @return 
	 * @throws UnknownHostException 
	 * @throws MalformedURLException 
	 */
	boolean init(String url, String workerName, String action, String speed) throws MalformedURLException, UnknownHostException;
	boolean init(String url) throws MalformedURLException, UnknownHostException;
	
	HashMap<String, JkStatus> getStatuses();
	
	boolean performAction(String workerName, String action, String speed) throws MalformedURLException, UnknownHostException;

}

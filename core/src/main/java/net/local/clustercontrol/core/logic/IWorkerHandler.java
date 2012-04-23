package net.local.clustercontrol.core.logic;

import java.util.Map;

import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.core.parsers.IStatusParser;

public interface IWorkerHandler {
	
	/**
	 * This method will setup the handler to handle subsequent calls
	 * @return 
	 */
	IStatusParser handleInit();

	void handlePoll();
	
	void handleStart(String workerId);
	
	void handleStop(String workerId);

	String createUrl(String url, String action, JkMember jkMember, String workerId);

	Map<String, String> getStatus();
	
}

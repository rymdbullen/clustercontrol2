package net.local.clustercontrol.core.logic;

public interface IWorkerHandler {
	
	void handleInit();

	void handlePoll();
	
	void handleStart(String workerId);
	
	void handleStop(String workerId);
	
}

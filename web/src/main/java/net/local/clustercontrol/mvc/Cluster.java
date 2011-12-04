package net.local.clustercontrol.mvc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Cluster of tomcats contains arrays of workers. Each worker contains one status for each host.
 * <pre>
 * Cluster
 *        -> Workers
 *                  -> WorkerStatuses
 * </pre>
 * @author jstenvall
 *
 */
public class Cluster {
	private String name;
	private String lastPoll;
	
	private ArrayList<Workers> workers = new ArrayList<Workers>(0);
	private ArrayList<String> hostNames = new ArrayList<String>(0);
	private ArrayList<String> workerNames = new ArrayList<String>(0);
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<String> getHostNames() {
		return hostNames;
	}
	public void setHostNames(ArrayList<String> hostNames) {
		this.hostNames = hostNames;
	}
	public ArrayList<Workers> getWorkers() {
		return workers;
	}
	public void setWorkers(ArrayList<Workers> workers) {
		this.workers = workers;
	}
	public String getLastPoll() {
		lastPoll = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date());
		return lastPoll;
	}
	public void setLastPoll(String lastPoll) {
		this.lastPoll = lastPoll;
	}
	public ArrayList<String> getWorkerNames() {
		return workerNames;
	}
	public void setWorkerNames(ArrayList<String> workerNames) {
		this.workerNames = workerNames;
	}
	@Override
	public String toString() {
		StringBuilder sbWorkers = new StringBuilder();
		for (int i = 0; i < workers.size(); i++) {
			Workers status = workers.get(i);
			sbWorkers.append(status.toString()+"\n");
		}
		StringBuilder sbHostNames = new StringBuilder();
		for (int i = 0; i < hostNames.size(); i++) {
			sbHostNames.append(hostNames.get(i)+",");
		}
		StringBuilder sbWorkerNames = new StringBuilder();
		for (int i = 0; i < workerNames.size(); i++) {
			sbWorkerNames.append(workerNames.get(i)+",");
		}

		return "Cluster [name=" + name + ", workers={" + sbWorkers.toString() +"}"
				+ ", workerNames={" + sbWorkerNames.toString().substring(0, sbWorkerNames.toString().length()-1) + "}"
				+ ", hostNames={" + sbHostNames.toString().substring(0, sbHostNames.toString().length()-1) + "}]";
	}
}

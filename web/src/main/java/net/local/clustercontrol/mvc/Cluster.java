package net.local.clustercontrol.mvc;

import java.util.ArrayList;

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
	
	private ArrayList<Workers> workers = new ArrayList<Workers>(0);
	private ArrayList<String> hostNames = new ArrayList<String>(0);
	
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
	@Override
	public String toString() {
		StringBuilder sbWorkerHosts = new StringBuilder();
//		for (int i = 0; i < workerHosts.size(); i++) {
//			WorkerHost status = workerHosts.get(i);
//			sbWorkerHosts.append(status.toString()+"\n");
//		}
		StringBuilder sbWorkers = new StringBuilder();
		for (int i = 0; i < workers.size(); i++) {
			Workers status = workers.get(i);
			sbWorkers.append(status.toString()+"\n");
		}
		StringBuilder sbWorkerNames = new StringBuilder();
		for (int i = 0; i < hostNames.size(); i++) {
			String name = hostNames.get(i);
			sbWorkerNames.append(name.toString()+"\n");
		}

		return "Cluster [name=" + name + ", workers={" + sbWorkers.toString() +"}" + ", workerHosts={" + sbWorkerHosts.toString() +"}"
				+ ", workerNames={" + sbWorkerNames.toString() + "}]";
	}
}

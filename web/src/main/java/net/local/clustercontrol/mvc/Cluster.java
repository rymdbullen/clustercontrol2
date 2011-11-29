package net.local.clustercontrol.mvc;

import java.util.ArrayList;

public class Cluster {
	private String name;
	private ArrayList<WorkerHost> workerHosts = new ArrayList<WorkerHost>(0);
	private ArrayList<String> workerNames = new ArrayList<String>(0);
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<WorkerHost> getWorkerHosts() {
		return workerHosts;
	}
	public void setWorkerHosts(ArrayList<WorkerHost> workerHosts) {
		this.workerHosts = workerHosts;
	}
	public ArrayList<String> getWorkerNames() {
		return workerNames;
	}
	public void setWorkerNames(ArrayList<String> workerNames) {
		this.workerNames = workerNames;
	}
	@Override
	public String toString() {
		StringBuilder sbWorkerHosts = new StringBuilder();
		for (int i = 0; i < workerHosts.size(); i++) {
			WorkerHost status = workerHosts.get(i);
			sbWorkerHosts.append(status.toString()+"\n");
		}
		StringBuilder sbWorkerNames = new StringBuilder();
		for (int i = 0; i < workerNames.size(); i++) {
			String name = workerNames.get(i);
			sbWorkerNames.append(name.toString()+"\n");
		}

		return "Cluster [name=" + name + ", workerHosts=" + sbWorkerHosts.toString()
				+ ", workerNames=" + sbWorkerNames.toString() + "]";
	}
}

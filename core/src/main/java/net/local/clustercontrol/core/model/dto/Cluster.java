package net.local.clustercontrol.core.model.dto;

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
	private String type;
	private String context;
	private String protocol;
	private String port;
	private String statusMessage;
	private String lastPoll;
	private String action;
	private String initUrl;
	
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
	public String getStatusMessage() {
		return statusMessage;
	}
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public String getInitUrl() {
		return initUrl;
	}
	public void setInitUrl(String initUrl) {
		this.initUrl = initUrl;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
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
		return "Cluster [name=" + name + ", type=" + type + ", context="
				+ context + ", protocol=" + protocol + ", port=" + port + ", statusMessage="
				+ statusMessage + ", lastPoll=" + lastPoll + ", action="
				+ action + ", initUrl=" + initUrl + ", workers={" + sbWorkers.toString() +"}"
						+ ", workerNames={" + sbWorkerNames.toString().substring(0, sbWorkerNames.toString().length()-1) + "}"
						+ ", hostNames={" + sbHostNames.toString().substring(0, sbHostNames.toString().length()-1) + "}]";
	}
}

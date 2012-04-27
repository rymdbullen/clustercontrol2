package net.local.clustercontrol.core.model.dto;

import java.util.ArrayList;

/**
 * Cluster of tomcats contains arrays of workers. Each worker contains one status for each host.
 * <pre>
 * Cluster
 *        -> Worker
 *                  -> WorkerStatuses
 * </pre>
 * @author jstenvall
 *
 */
public class Worker {
	public String TYPE_XML  = "XML";
	public String TYPE_HTML = "HTML";
	private String status;
	private String name;
	private String host;
	private String type;
	private String id;
	
	/** all workers per host */
	private ArrayList<WorkerStatus> statusesPerHost = new ArrayList<WorkerStatus>();

	public ArrayList<WorkerStatus> getStatusesPerHost() {
		return statusesPerHost;
	}
	public void setStatusesPerHost(ArrayList<WorkerStatus> statusesPerHost) {
		this.statusesPerHost = statusesPerHost;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		String lastStatus = null;
		boolean sameAsLast = false;
		
 		for (WorkerStatus workerStatus : statusesPerHost) {
			String thisStatus = workerStatus.getStatus();
			if(lastStatus!=null && lastStatus.equalsIgnoreCase(thisStatus)) {
				sameAsLast = true;
			} else {
				sameAsLast = false;
			}
			lastStatus = thisStatus;
		}
		if(statusesPerHost.size()>1 && !sameAsLast) {
			return "unknown";
		}
		/// TODO The parser must set a generic status... otherwise this will break
		/// TODO The parser must set a generic status... otherwise this will break
		/// TODO The parser must set a generic status... otherwise this will break
		if(lastStatus.equalsIgnoreCase("dis")) {
			return "allDisabled";
		} if(lastStatus.equalsIgnoreCase("ok")) {
			return "allEnabled";
		}
		return "unknown";
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHost() {
		return host;
	}
	public void setHostname(String host) {
		this.host = host;
	}
	@Override
	public String toString() {
		StringBuilder sbWorkers = new StringBuilder();
		for (int i = 0; i < statusesPerHost.size(); i++) {
			WorkerStatus status = statusesPerHost.get(i);
			sbWorkers.append(status.toString()+"\n");
		}
		return "Worker [name=" + name + ", id=" + id  + ", host=" + host + ", status=" + status + ", statusesPerHost={" + statusesPerHost + "}]";
	}
}

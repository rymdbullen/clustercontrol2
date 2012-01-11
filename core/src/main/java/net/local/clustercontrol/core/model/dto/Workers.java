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
public class Workers {
	public String TYPE_XML  = "XML";
	public String TYPE_HTML = "HTML";
	private String id;
	@SuppressWarnings("unused")
	private String status;
	private String name;
	private String type;
	
	/** all workers with same name, ie not per host */
	private ArrayList<WorkerStatus> statuses = new ArrayList<WorkerStatus>();

	public ArrayList<WorkerStatus> getStatuses() {
		return statuses;
	}
	public void setStatuses(ArrayList<WorkerStatus> statuses) {
		this.statuses = statuses;
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
		
 		for (WorkerStatus workerStatus : statuses) {
			String thisStatus = workerStatus.getStatus();
			if(lastStatus!=null && lastStatus.equalsIgnoreCase(thisStatus)) {
				sameAsLast = true;
			} else {
				sameAsLast = false;
			}
			lastStatus = thisStatus;
		}
		if(statuses.size()>1 && !sameAsLast) {
			return "unknown";
		}
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
	@Override
	public String toString() {
		StringBuilder sbWorkers = new StringBuilder();
		for (int i = 0; i < statuses.size(); i++) {
			WorkerStatus status = statuses.get(i);
			sbWorkers.append(status.toString()+"\n");
		}
		return "Workers [name=" + name + ", statuses={" + statuses + "}]";
	}
}

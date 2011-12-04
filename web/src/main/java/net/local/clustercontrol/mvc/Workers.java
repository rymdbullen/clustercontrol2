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
public class Workers {
//	private ArrayList<String> names = new ArrayList<String>();
	private String name = new String();
	@SuppressWarnings("unused")
	private String status;
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
		if(!sameAsLast) {
			return "unknown";
		}
		if(lastStatus.equalsIgnoreCase("nok")) {
			return "allDisabled";
		} if(lastStatus.equalsIgnoreCase("ok")) {
			return "allEnabled";
		}
		return "unknown";
	}
	public void setStatus(String status) {
		this.status = status;
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

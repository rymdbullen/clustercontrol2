package net.local.clustercontrol.mvc;

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
public class WorkerStatus {
	private String status;
	private String lastStatus;
	private String hostName;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getLastStatus() {
		return lastStatus;
	}
	public void setLastStatus(String lastStatus) {
		this.lastStatus = lastStatus;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	@Override
	public String toString() {
		return "Status [hostName=" + hostName + ", status=" + status + ", lastStatus=" + lastStatus + "]";
	}
}

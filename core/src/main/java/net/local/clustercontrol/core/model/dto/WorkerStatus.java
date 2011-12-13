package net.local.clustercontrol.core.model.dto;

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
	private String hostPort;
	
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
	public String getHostPort() {
		return hostPort;
	}
	public void setHostPort(String hostPort) {
		this.hostPort = hostPort;
	}
	@Override
	public String toString() {
		return "Status [hostName=" + hostName + ", hostPort=" + hostPort + ", status=" + status + ", lastStatus=" + lastStatus + "]";
	}
}

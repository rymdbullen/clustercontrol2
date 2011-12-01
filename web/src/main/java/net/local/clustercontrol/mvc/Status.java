package net.local.clustercontrol.mvc;

public class Status {
	private String status;
	private String lastStatus;
	private String workerName;
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
	public String getWorkerName() {
		return workerName;
	}
	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	@Override
	public String toString() {
		return "Status [workerName=" + workerName + ", hostName=" + hostName + ", status=" + status
				+ ", lastStatus=" + lastStatus + "]";
	}
}

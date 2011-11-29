package net.local.clustercontrol.mvc;

public class Status {
	private String workerName;
	private String status;
	private String lastStatus;
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
	@Override
	public String toString() {
		return "Status [workerName=" + workerName + ", status=" + status
				+ ", lastStatus=" + lastStatus + "]";
	}
}

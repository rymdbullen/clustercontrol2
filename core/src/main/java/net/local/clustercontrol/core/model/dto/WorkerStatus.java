package net.local.clustercontrol.core.model.dto;

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
public class WorkerStatus {
	private String name;
	private String status;
	private String lastStatus;
	private String id;
	private String hostName;
	private String hostPort;
	private String type;
	private String route;
	private Integer to;
	private Integer set;
	private Integer transferred;
	private Integer loadFactor;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
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
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getTo() {
		return to;
	}
	public void setTo(Integer to) {
		this.to = to;
	}
	public Integer getSet() {
		return set;
	}
	public void setSet(Integer set) {
		this.set = set;
	}
	public Integer getTransferred() {
		return transferred;
	}
	public void setTransferred(Integer transferred) {
		this.transferred = transferred;
	}
	public Integer getLoadFactor() {
		return loadFactor;
	}
	public void setLoadFactor(Integer loadFactor) {
		this.loadFactor = loadFactor;
	}
	public String getRoute() {
		return route;
	}
	public void setRoute(String route) {
		this.route = route;
	}
	@Override
	public String toString() {
		return "WorkerStatus [id="+id+", name=" + name + ", status=" + status
				+ ", lastStatus=" + lastStatus + ", hostName=" + hostName
				+ ", hostPort=" + hostPort + ", type=" + type + ", to=" + to
				+ ", set=" + set + ", transferred=" + transferred
				+ ", loadFactor=" + loadFactor + ", route"+ route + "]";
	}
	
}

package net.local.clustercontrol.mvc;

import java.util.ArrayList;

/**
 * 
 * <pre>
 * {
 *         "host1": {
 *             "worker1": "ok",
 *             "worker2": "ok"
 *             },
 *         "host2": {
 *             "worker1": "ok",
 *             "worker2": "ok"
 *             }
 * }
 */
public class WorkerHost {

	private String url;
	private String hostName;
	
	private ArrayList<Status> workers = new ArrayList<Status>(0);
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ArrayList<Status> getWorkers() {
		return workers;
	}

	public void setWorkers(ArrayList<Status> workers) {
		this.workers = workers;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	public String getStatus() {
		String thisStatus = null;
//		HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
//		
//		for (int j = 0; j < workerHosts.size(); j++) {
//			WorkerHost status = workerHosts.get(j);
//			ArrayList<Status> list = status.getWorkers();
//			for (Status thiStatus : list) {
//				if(thiStatus.getWorkerName().equals(name)) {
//					
//				}
//			}
//		}
		return thisStatus;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < workers.size(); i++) {
			Status status = workers.get(i);
			sb.append(status.toString()+"\n");
		}
		return "WorkerHost [url=" + url + ", hostName=" + hostName
				+ ", workers=" + sb.toString() + "]";
	}
}

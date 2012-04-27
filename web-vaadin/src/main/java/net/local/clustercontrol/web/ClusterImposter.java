package net.local.clustercontrol.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.core.logic.IClusterManager;
import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.core.model.dto.WorkerStatus;
import net.local.clustercontrol.core.model.dto.Worker;
import net.local.clustercontrol.core.parsers.StatusParserHtml;

public class ClusterImposter {

	private static Cluster _cluster;
	
	private static String body1 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">"
			+ "<html><head><title>Balancer Manager</title></head>"
			+ "<body><h1>Load Balancer Manager for 172.18.151.174</h1>"
			+ "<dl><dt>Server Version: Apache/2.2.20 (Ubuntu)</dt>"
			+ "<dt>Server Built: Nov  7 2011 22:45:49</dt></dl>"
			+ "<hr />"
			+ "<h3>LoadBalancer Status for balancer://cluster</h3>"
			+ "<table border=\"0\" style=\"text-align: left;\"><tr><th>StickySession</th><th>Timeout</th><th>FailoverAttempts</th><th>Method</th></tr>"
			+ "<tr><td> - </td><td>0</td><td>5</td>"
			+ "<td>bytraffic</td>"
			+ "</table>"
			+ "<br />"
			+ "<table border=\"0\" style=\"text-align: left;\"><tr><th>Worker URL</th><th>Route</th><th>RouteRedir</th><th>Factor</th><th>Set</th><th>Status</th><th>Elected</th><th>To</th><th>From</th></tr>"
			+ "<tr>"
			+ "<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.172:8009&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.172:8009</a></td><td>s1</td><td></td><td>1</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"
			+ "<tr>"
			+ "<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.172:8019&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.172:8019</a></td><td>s2</td><td></td><td>2</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"
			+ "<tr>"
			+ "<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.172:8029&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.172:8029</a></td><td>s3</td><td></td><td>2</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"
			+ "<tr>"
			+ "<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.174:8009&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.174:8009</a></td><td>s1</td><td></td><td>1</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"
			+ "<tr>"
			+ "<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.174:8019&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.174:8019</a></td><td>s2</td><td></td><td>2</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"
			+ "<tr>"
			+ "<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.174:8029&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.174:8029</a></td><td>s3</td><td></td><td>2</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"
			+ "</table>"
			+ "<hr />"
			+ "<address>Apache/2.2.20 (Ubuntu) Server at 172.18.151.174 Port 80</address>"
			+ "</body></html>";

	private static String body2 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">"
			+ "<html><head><title>Balancer Manager</title></head>"
			+ "<body><h1>Load Balancer Manager for 172.18.151.172</h1>"
			+ "<dl><dt>Server Version: Apache/2.2.20 (Ubuntu)</dt>"
			+ "<dt>Server Built: Nov  7 2011 22:45:49</dt></dl>"
			+ "<hr />"
			+ "<h3>LoadBalancer Status for balancer://cluster</h3>"
			+ "<table border=\"0\" style=\"text-align: left;\"><tr><th>StickySession</th><th>Timeout</th><th>FailoverAttempts</th><th>Method</th></tr>"
			+ "<tr><td> - </td><td>0</td><td>5</td>"
			+ "<td>bytraffic</td>"
			+ "</table>"
			+ "<br />"
			+ "<table border=\"0\" style=\"text-align: left;\"><tr><th>Worker URL</th><th>Route</th><th>RouteRedir</th><th>Factor</th><th>Set</th><th>Status</th><th>Elected</th><th>To</th><th>From</th></tr>"
			+ "<tr>"
			+ "<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.172:8009&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.172:8009</a></td><td>s1</td><td></td><td>1</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"
			+ "<tr>"
			+ "<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.172:8019&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.172:8019</a></td><td>s2</td><td></td><td>2</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"
			+ "<tr>"
			+ "<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.172:8029&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.172:8029</a></td><td>s3</td><td></td><td>2</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"
			+ "<tr>"
			+ "<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.174:8009&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.174:8009</a></td><td>s1</td><td></td><td>1</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"
			+ "<tr>"
			+ "<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.174:8019&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.174:8019</a></td><td>s2</td><td></td><td>2</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"
			+ "<tr>"
			+ "<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.174:8029&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.174:8029</a></td><td>s3</td><td></td><td>2</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"
			+ "</table>"
			+ "<hr />"
			+ "<address>Apache/2.2.20 (Ubuntu) Server at 172.18.151.172 Port 80</address>"
			+ "</body></html>";

	public static Cluster generateCluster(IClusterManager clusterManager) {
		StatusParserHtml parser1 = new StatusParserHtml(body1, "http://172.18.151.174/balancerManager");
		StatusParserHtml parser2 = new StatusParserHtml(body2, "http://172.18.151.172/balancerManager");
		
		JkStatus status1 = parser1.getStatus();
		JkStatus status2 = parser2.getStatus();
		
		Collection<JkStatus> jkStatuses = new HashSet<JkStatus>();
		jkStatuses.add(status1);
		jkStatuses.add(status2);
		LinkedHashMap<String, HashMap<String, JkMember>> membersList = statusListPerWorker(jkStatuses);
		updateCluster(membersList);
		return _cluster;
	}
	
	/**
	 * Converts the statusesPerHost from per-host to per-worker. transforms the matrix.
	 */
	private static LinkedHashMap<String, HashMap<String, JkMember>> statusListPerWorker(Collection<JkStatus> jkStatuses) {
		_cluster = new Cluster();
		
		LinkedHashMap<String, HashMap<String, JkMember>> membersList = new LinkedHashMap<String, HashMap<String, JkMember>>();
		for (JkStatus jkStatus : jkStatuses) 
		{
			String hostName = jkStatus.getServer().getName();
			Integer hostPort = jkStatus.getServer().getPort();
			List<JkMember> members = jkStatus.getBalancers().getBalancer().getMember();
			
			for (JkMember jkMember : members) 
			{
				String workerName = jkMember.getName();
				HashMap<String, JkMember> memberList = membersList.get(workerName);
				if(memberList == null) {
					// create new member list
					memberList = new HashMap<String, JkMember>();
					membersList.put(workerName, memberList);
				}
				memberList.put(hostName, jkMember);
			}
			String hostWithPort = hostName;
			if(hostPort != null && hostPort > 0 && hostPort != 80) {
				hostWithPort = hostWithPort + ":" + hostPort;
			}
			if(!_cluster.getHostNames().contains(hostWithPort)) {
				_cluster.getHostNames().add(hostWithPort);
			}
			_cluster.setName(jkStatus.getBalancers().getBalancer().getName());
		}
		return membersList;
	}
	/**
	 * Converts the statusesPerHost from per-host to per worker. transforms the matrix.
	 * 
	 * @param statusesPerHost
	 */
	private static void updateCluster(LinkedHashMap<String, HashMap<String, JkMember>> membersList) {
		// initialize new cluster
		_cluster = new Cluster();
		// convert to cluster
		Iterator<String> keysIter = membersList.keySet().iterator();
		while (keysIter.hasNext()) {
			Worker worker = new Worker();
			String workerName = keysIter.next();
			worker.setName(workerName);
			worker.setId(cssValidName(workerName, true));
			
			HashMap<String, JkMember> jkMembersList = membersList.get(workerName);
			Iterator<String> workerMemberListIter = jkMembersList.keySet().iterator();
			while (workerMemberListIter.hasNext()) {
				String hostName = workerMemberListIter.next();
				JkMember jkMember = jkMembersList.get(hostName);
				WorkerStatus workerStatus = new WorkerStatus();
				workerStatus.setHostName(hostName);
				workerStatus.setHostPort(""+jkMember.getPort());
				workerStatus.setStatus(jkMember.getActivation());
				workerStatus.setType(jkMember.getType());
				workerStatus.setTo(jkMember.getBusy());  // html to
				workerStatus.setSet(jkMember.getRead()); // html set
				workerStatus.setRoute(jkMember.getRoute()); // html set
				workerStatus.setTransferred(jkMember.getTransferred());
				workerStatus.setLoadFactor(jkMember.getLbfactor());
				workerStatus.setName(jkMember.getName());
//				workerStatus.setId(cssValidName(hostName, true)+"-"+cssValidName(workerName, true));
				workerStatus.setId(cssValidName(workerName, true));
				worker.getStatusesPerHost().add(workerStatus);
				worker.setHostname(hostName);
			}
			_cluster.getWorkers().add(worker);
		}
		
		_cluster.getWorkerNames().addAll(membersList.keySet());
	}
	private static String cssValidName(String name, boolean encode) {
		String cssValidName = name;
		String[] replace0 = new String[] {"://", "_-_"};
		String[] replace1 = new String[] {"\\.", "_"};
		String[] replace2 = new String[] {":", "-"};
		String[] replace3 = new String[] {"/", "_"};
		ArrayList<String[]> list = new ArrayList<String[]>();
		list.add(replace0);
		list.add(replace1);
		list.add(replace2);
		list.add(replace3);
		for (String[] replacement : list) 
		{
			if(encode) 
			{
				cssValidName = cssValidName.replaceAll(replacement[0], replacement[1]); 
			} 
			else
			{
				cssValidName = cssValidName.replaceAll(replacement[1], replacement[0]);
			}
		}
		return cssValidName;
	}
}

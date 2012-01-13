package net.local.clustercontrol.core.logic.impl;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.core.logic.IWorkerFactory;
import net.local.clustercontrol.core.logic.IWorkerManager;
import net.local.clustercontrol.core.logic.WorkerNotFoundException;
import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.core.model.dto.WorkerStatus;
import net.local.clustercontrol.core.model.dto.Workers;

@Service
public class ClusterManager implements IWorkerManager {

	private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);

	private IWorkerFactory workerFactory;

	private Cluster _cluster;

	@Autowired
	public ClusterManager(WorkerFactory workerFactory) {
		this.workerFactory = workerFactory;
	}
	
	/**
	 * Initializes the ClusterManager with a url.
	 * @param url the url to initialize with
	 * @throws MalformedURLException
	 * @throws WorkerNotFoundException
	 */
	@Override
	public boolean init(String url) {
		if(_cluster != null && _cluster.getWorkers() != null) {
			logger.debug("Already initialized: "+ _cluster.getWorkerNames());
			_cluster.setStatusMessage("Already intialized");
			return true;
		}

		boolean initOk = workerFactory.init(url, null, "poll", null);
		if(initOk) {
			if(workerFactory.getStatuses() != null) {
				// initialize new cluster
				_cluster = new Cluster();
				updateCluster();
				_cluster.setStatusMessage("ok");
			
				return true;
			}
		}
		_cluster = null;
		return false;
	}

	@Override
	@Async
	public boolean enable(String workerId, String speed) {
		if(logger.isTraceEnabled()) {
			String threadName = Thread.currentThread().getName();
			logger.trace("Process thread " + threadName + " using Async method");
		}
		String action = "Enable";
		String workerName = cssValidName(workerId, false);
		boolean isSuccess = workerFactory.getAllStatuses(workerName, action, speed);
		if(isSuccess) {
			updateCluster();
			_cluster.setStatusMessage("ok");
			return true;		
		}
		_cluster.setStatusMessage("Failed to update cluster status");
		return false;
	}
	
	@Override
	@Async
	public boolean disable(String workerId, String speed) {
		if(logger.isTraceEnabled()) {
			String threadName = Thread.currentThread().getName();
			logger.trace("Process thread '" + threadName + "' using Async method");
		}
		String action = "Disable";
		String workerName = cssValidName(workerId, false);
		boolean isSuccess = workerFactory.getAllStatuses(workerName , action , speed);
		if(isSuccess) {
			updateCluster();
			_cluster.setStatusMessage("ok");
			return true;			
		}
		_cluster.setStatusMessage("Failed to update cluster status");
		return false;
	}

	@Override
	@Async
	public boolean stop(String workerId) {
		if(logger.isTraceEnabled()) {
			String threadName = Thread.currentThread().getName();
			logger.trace("Process thread " + threadName + " using Async method");
		}
		String action = "Disable";
		String workerName = cssValidName(workerId, false);
		boolean isSuccess = workerFactory.getAllStatuses(workerName, action, "fast");
		if(isSuccess) {
			updateCluster();
			_cluster.setStatusMessage("ok");
			return true;			
		}
		_cluster.setStatusMessage("Failed to update cluster status");
		return false;
	}
	
	@Override
	public Cluster getCluster() {
		return _cluster;
	}
	
	@Override
	public boolean poll() {
 		if(_cluster==null) {
			return false;
		}
		boolean isSuccess = workerFactory.getAllStatuses(null, "poll", null);
		if(isSuccess) {
			updateCluster();
			_cluster.setStatusMessage("ok");
			return true;			
		}
		_cluster.setStatusMessage("Failed to update cluster status");
		return false;
	}

	/**
	 * Converts the statuses from per-host to per worker. transforms the matrix.
	 * 
	 * @param statuses
	 */
	private void updateCluster() {
		HashMap<String, JkStatus> statuses = workerFactory.getStatuses(); 
		_cluster.getHostNames().clear();
		_cluster.getWorkerNames().clear();
		_cluster.getWorkers().clear();
		Collection<JkStatus> jkStatuses = statuses.values();
		LinkedHashMap<String, HashMap<String, JkMember>> membersList = new LinkedHashMap<String, HashMap<String, JkMember>>();

		// convert statuses to cluster object
		for (JkStatus jkStatus : jkStatuses) {
			String hostName = jkStatus.getServer().getName();
			Integer hostPort = jkStatus.getServer().getPort();
			List<JkMember> members = jkStatus.getBalancers().getBalancer().getMember();
			
			for (JkMember jkMember : members) {
				logger.debug("Adding jkMember: "+hostName+": "+hostPort+": "+jkMember.getName()+" "+jkMember.getActivation()+" "+jkMember.getType());
				
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
			if(hostPort!=null && hostPort > 0 && hostPort != 80) {
				hostWithPort = hostWithPort + ":" + hostPort;
			}
			if(false == _cluster.getHostNames().contains(hostWithPort)) {
				_cluster.getHostNames().add(hostWithPort);
			}
			_cluster.setName(jkStatus.getBalancers().getBalancer().getName());
		}
		
		// convert to cluster
		Iterator<String> keysIter = membersList.keySet().iterator();
		while (keysIter.hasNext()) {
			Workers workers = new Workers();
			String workerName = keysIter.next();
			workers.setName(workerName);
			workers.setId(cssValidName(workerName, true));
			
			HashMap<String, JkMember> jkMembersList = membersList.get(workerName);
			Iterator<String> workerMemberListIter = jkMembersList.keySet().iterator();
			while (workerMemberListIter.hasNext()) {
				String hostName = workerMemberListIter.next();
				JkMember jkMember = jkMembersList.get(hostName);
				WorkerStatus workerStatus = new WorkerStatus();
				workerStatus.setHostName(hostName);
				workerStatus.setHostPort(""+jkMember.getPort());
				workerStatus.setStatus(jkMember.getActivation().trim());
				workerStatus.setType(jkMember.getType().trim());
				workerStatus.setTo(jkMember.getBusy());  // html to
				workerStatus.setSet(jkMember.getRead()); // html set
				workerStatus.setRoute(jkMember.getRoute()); // html set
				workerStatus.setTransferred(jkMember.getTransferred());
				workerStatus.setLoadFactor(jkMember.getLbfactor());
				workerStatus.setName(jkMember.getName());
				workerStatus.setId(cssValidName(hostName, true)+"-"+cssValidName(workerName, true));
				workers.getStatuses().add(workerStatus);
			}
			_cluster.getWorkers().add(workers);
		}
		
		_cluster.getWorkerNames().addAll(membersList.keySet());
	}
	private String cssValidName(String name, boolean encode) {
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

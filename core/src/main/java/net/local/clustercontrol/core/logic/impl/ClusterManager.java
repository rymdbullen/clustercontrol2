package net.local.clustercontrol.core.logic.impl;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.core.logic.IWorkerFactory;
import net.local.clustercontrol.core.logic.IClusterManager;
import net.local.clustercontrol.core.logic.IWorkerHandler;
import net.local.clustercontrol.core.logic.IWorkerHandlerFactory;
import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.core.model.dto.WorkerStatus;
import net.local.clustercontrol.core.model.dto.Workers;

@Service
public class ClusterManager implements IClusterManager {

	private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);

	private IWorkerFactory workerFactory;
	private IWorkerHandlerFactory workerHandlerFactory;

	private Cluster _cluster;
	private IWorkerHandler handler;

	@Autowired
	public ClusterManager(IWorkerFactory workerFactory, IWorkerHandlerFactory workerHandlerFactory) {
		this.workerFactory = workerFactory;
		this.workerHandlerFactory = workerHandlerFactory;
	}

	@Override
	public Map<String, String> init(String url) {
		// get handler only if none exists previously
		if(handler == null) {
			handler = workerHandlerFactory.getHandler(url);
		}
		return handler.getStatusMessage();
	}

	@Override
	@Async
	public void enable(String workerId, String speed) {
		handler.handleStart(workerId, speed);
	}
	
	@Override
	@Async
	public void disable(String workerId, String speed) {
		handler.handleStop(workerId, speed);
	}

	@Override
	@Async
	public void stop(String workerId) {
		handler.handleStop(workerId, null);
	}
	
	/**
	 * 
	 * @param action
	 * @param workerId
	 * @param speed
	 * @return
	 */
	boolean action(String action, String workerId, String speed) {
		if(logger.isTraceEnabled()) {
			String threadName = Thread.currentThread().getName();
			logger.trace("Process thread " + threadName + " using Async method");
		}
		String workerName = cssValidName(workerId, false);
		try {
			workerFactory.performAction(workerName, action, speed);
			updateCluster(statusListPerWorker(workerFactory.getStatuses()));
			_cluster.setStatusMessage("ok");
			return true;
		} catch (MalformedURLException e) {
			_cluster.setStatusMessage("Failed to perform action="+action+", workerId="+workerId+", speed="+speed+", update cluster status");
		} catch (UnknownHostException e) {
			_cluster.setStatusMessage("Failed to perform action="+action+", workerId="+workerId+", speed="+speed+", update cluster status");
		}
		return false;
	}
	
	@Override
	public Cluster getCluster() {
		LinkedHashMap<String, HashMap<String, JkMember>> statusListPerWorker = statusListPerWorker(handler.getStatuses());
		updateCluster(statusListPerWorker);
		return _cluster;
	}
	
	@Override
	public boolean poll() {
		handler.handlePoll();
		
		return action("poll", null, null);
// 		if(_cluster==null) {
//			return false;
//		}
//		boolean isSuccess = workerFactory.performAction(null, "poll", null);
//		if(isSuccess) {
//			updateCluster(statusListPerWorker());
//			_cluster.setStatusMessage("ok");
//			return true;
//		}
//		_cluster.setStatusMessage("Failed to update cluster status");
//		return false;
	}

	/**
	 * Converts the statusesPerHost from per-host to per-worker. transforms the matrix.
	 */
	private LinkedHashMap<String, HashMap<String, JkMember>> statusListPerWorker(Map<String, JkStatus> map) {
		return statusListPerWorker(map.values());
	}
	/**
	 * Converts the statusesPerHost from per-host to per-worker. transforms the matrix.
	 */
	private LinkedHashMap<String, HashMap<String, JkMember>> statusListPerWorker(Collection<JkStatus> jkStatuses) {
		_cluster = new Cluster();
		
		LinkedHashMap<String, HashMap<String, JkMember>> membersList = new LinkedHashMap<String, HashMap<String, JkMember>>();
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
		return membersList;
	}
	/**
	 * Converts the statusesPerHost from per-host to per worker. transforms the matrix.
	 * 
	 * @param statusesPerHost
	 */
	private void updateCluster(LinkedHashMap<String, HashMap<String, JkMember>> membersList) {
		// initialize new cluster
		_cluster = new Cluster();
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
				workerStatus.setStatus(jkMember.getActivation());
				workerStatus.setType(jkMember.getType());
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

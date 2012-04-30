package net.local.clustercontrol.core.logic.impl;

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
import net.local.clustercontrol.core.configuration.Constants;
import net.local.clustercontrol.core.configuration.EnvironmentAwareProperties;
import net.local.clustercontrol.core.logic.IClusterManager;
import net.local.clustercontrol.core.logic.IWorkerHandler;
import net.local.clustercontrol.core.logic.IWorkerHandlerFactory;
import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.core.model.dto.WorkerStatus;
import net.local.clustercontrol.core.model.dto.Worker;

@Service
public class ClusterManager implements IClusterManager {

	private static final long serialVersionUID = -400828556917282230L;

	private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);

	@Autowired 
	private IWorkerHandlerFactory workerHandlerFactory;

	private Map<String, String> statusMessages = new HashMap<String, String>();
	
	private Cluster _cluster;
	private IWorkerHandler handler;

	@Override
	public Map<String, String> init(String initUrl) {
		// get handler only if none exists previously
		if(handler == null) {
			handler = workerHandlerFactory.getHandler(initUrl);
		}
		if(handler.getStatuses()==null) {
			statusMessages.put("initStatus", "nok");
			statusMessages.put("initStatusMessage", "Failed to initialize using URL: "+initUrl);
		}
		LinkedHashMap<String, HashMap<String, JkMember>> statusListPerWorker = statusListPerWorker(handler.getStatuses());
		updateCluster(statusListPerWorker);
		statusMessages.put("initStatus", "ok");
		statusMessages.put("initStatusMessage", "Initialize ["+_cluster.getWorkers().size()+"] workers on ["+_cluster.getHostNames().size()+"] hosts using URL: "+initUrl);
		
		return statusMessages;
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
	
	@Override
	public Cluster getCluster() {
		if(EnvironmentAwareProperties.getInstance().getProperty("dev_mode") != null && Constants.IS_DEVMODE) {
			return ClusterImposter.generateCluster(this);
		}
		if(_cluster == null) {
			return null;
		}
		LinkedHashMap<String, HashMap<String, JkMember>> statusListPerWorker = statusListPerWorker(handler.getStatuses());
		updateCluster(statusListPerWorker);
		return _cluster;
	}
	
	@Override
	public void poll() {
		handler.handlePoll();
	}

	@Override
	public void handle(String workerId, String speed, String action) {
		if(action.equals("Enable")) {
			this.enable(workerId, speed);
		} else if(action.equals("Disable")) {
			this.disable(workerId, speed);			
		} else if(action.equals("Stop")) {
			this.stop(workerId);						
		} else {
			throw new IllegalArgumentException("Failed to map action: " + action);
		}
			
	}
	
	// ----------------------------------------------------------------------- private
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
		for (JkStatus jkStatus : jkStatuses) 
		{
			String hostName = jkStatus.getServer().getName();
			Integer hostPort = jkStatus.getServer().getPort();
			List<JkMember> members = jkStatus.getBalancers().getBalancer().getMember();
			
			for (JkMember jkMember : members) 
			{
				if(logger.isTraceEnabled()) logger.trace("Adding jkMember: "+hostName+": "+hostPort+": "+jkMember.getName()+" "+jkMember.getActivation()+" "+jkMember.getType());
				
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
	private void updateCluster(LinkedHashMap<String, HashMap<String, JkMember>> membersList) {
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
				workerStatus.setTransferred(Integer.parseInt(jkMember.getTransferred()));
				workerStatus.setLoadFactor(jkMember.getLbfactor());
				workerStatus.setName(jkMember.getName());
				workerStatus.setId(cssValidName(hostName, true)+"-"+cssValidName(workerName, true));
				worker.getStatusesPerHost().add(workerStatus);
				worker.setHostname(hostName);
			}
			_cluster.getWorkers().add(worker);
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

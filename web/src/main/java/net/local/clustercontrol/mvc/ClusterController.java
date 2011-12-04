package net.local.clustercontrol.mvc;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.local.clustercontrol.api.model.JkBalancer;
import net.local.clustercontrol.api.model.JkMember;
import net.local.clustercontrol.api.model.JkStatus;
import net.local.clustercontrol.core.logic.WorkerNotFoundException;
import net.local.clustercontrol.core.logic.impl.WorkerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value="/cluster")
public class ClusterController {
	
	private static final Logger logger = LoggerFactory.getLogger(WorkerManager.class);
	
	private Cluster _cluster = null;
	
	private Validator validator;
	
	@Autowired
	public ClusterController(Validator validator) {
		this.validator = validator;
	}
	
	@RequestMapping(method=RequestMethod.GET)
	public String getCreateForm(Model model) {
		if(_cluster==null) {
			model.addAttribute(new SetupHost());
			return "cluster/setupForm";
		} else {
			model.addAttribute("cluster", _cluster);
			return "cluster/workersForm";
		}
	}

	/**
	 *          worker 1, worker 2
	 * host 1:    ok    ,   ok
	 * host 2:    ok    ,   nok
	 * 
	 * @param url
	 * @return
	 */
	@RequestMapping(value="/poll", method=RequestMethod.GET)
	public @ResponseBody Cluster getAvailability() {
		String url = "";
		return populate(url);
	}
	
	@RequestMapping(value="/enable/{id}/{speed}", method=RequestMethod.GET)
	public @ResponseBody Cluster getEnable(@PathVariable String id, @PathVariable String speed) {
		String url = "enable";
		return populate(url);
	}
	
	@RequestMapping(value="/disable/{id}/{speed}", method=RequestMethod.GET)
	public @ResponseBody Cluster getDisable(@PathVariable String id, @PathVariable String speed) {
		String url = "disable";
		return populate(url);
	}

	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody Map<String, ? extends Object> create(@RequestBody SetupHost setupHost, HttpServletResponse response, Model model) {
		Set<ConstraintViolation<SetupHost>> failures = validator.validate(setupHost);
		model.addAttribute(setupHost);
		if (!failures.isEmpty())
		{
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return validationMessages(failures);
		}
		else
		{
			init(setupHost.getUrl());
			return Collections.singletonMap("initStatus", "ok");
		}
	}
	
	private Cluster init(String url) {
		Cluster cluster = null;
		try {
			ArrayList<JkStatus> statuses = WorkerManager.init(url);
			convert(statuses, cluster);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WorkerNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cluster = populate(url);
		return cluster;
	}
	public void convert(ArrayList<JkStatus> statuses, Cluster cluster) {
		LinkedHashMap<String, HashMap<String, String>> list = new LinkedHashMap<String, HashMap<String, String>>();
		// convert statuses to cluster object
		for (int jkStatusIdx = 0; jkStatusIdx < statuses.size(); jkStatusIdx++) {
			JkBalancer balancer = statuses.get(jkStatusIdx).getBalancers().getBalancer();
			String hostName = statuses.get(jkStatusIdx).getServer().getName();
			for (int memberIdx = 0; memberIdx < balancer.getMember().size(); memberIdx++) {
				JkMember workerStatus = balancer.getMember().get(memberIdx);
				logger.debug(hostName+": ["+jkStatusIdx+":"+memberIdx+"]: "+workerStatus.getName()+" "+workerStatus.getActivation());
				String workerName = workerStatus.getName();
				String status = workerStatus.getActivation();
				HashMap<String, String> workerList = list.get(workerName);
				if(workerList==null) {
					// create new list
					workerList = new HashMap<String, String>();
					list.put(workerName, workerList);
				}
				workerList.put(hostName, status);
			}
			cluster.getHostNames().add(hostName);
		}
		
		// convert to cluster
		
		Workers workers = new Workers();
		Iterator<String> keysIter = list.keySet().iterator();
		while (keysIter.hasNext()) {
			String key = (String) keysIter.next();
			HashMap<String, String> workerList = list.get(key);
			Iterator<String> workerKeysIter = workerList.keySet().iterator();
			while (workerKeysIter.hasNext()) {
				String hostName = workerKeysIter.next();
				String status = workerList.get(hostName);
				WorkerStatus thisWorkerStatus = new WorkerStatus();
				thisWorkerStatus.setHostName(hostName);
				thisWorkerStatus.setStatus(status);
				workers.getStatuses().add(thisWorkerStatus);
			}
		}
		
		cluster.getWorkerNames().addAll(list.keySet());
		cluster.getWorkers().add(workers);
	}

	private Cluster populate(String url) {		
		Workers workers1 = new Workers();
		workers1.setName("workerName1");
		ArrayList<WorkerStatus> workerStatusList1 = new ArrayList<WorkerStatus>();
		WorkerStatus workerStatus11 = setWorkerStatus("wsHost1");
		WorkerStatus workerStatus12 = setWorkerStatus("wsHost2");
		workerStatusList1.add(workerStatus11);
		workerStatusList1.add(workerStatus12);
		workers1.setStatuses(workerStatusList1);
		
		ArrayList<WorkerStatus> workerStatusList2 = new ArrayList<WorkerStatus>();
		WorkerStatus workerStatus21 = setWorkerStatus("wsHost1");
		WorkerStatus workerStatus22 = setWorkerStatus("wsHost2");
		workerStatusList2.add(workerStatus21);
		workerStatusList2.add(workerStatus22);
		
		Workers workers2 = new Workers();
		workers2.setName("workerName2");
		workers2.setStatuses(workerStatusList2);
		
		ArrayList<Workers> workersList = new ArrayList<Workers>();
		workersList.add(workers1);
		workersList.add(workers2);
		
		Cluster cluster = new Cluster();
		cluster.setWorkers(workersList);
		//cluster.setWorkerHosts(list);
		ArrayList<String> names = new ArrayList<String>();
		names.add(workerStatus11.getHostName());
		names.add(workerStatus21.getHostName());
		cluster.setHostNames(names);
		
		// set cluster
		_cluster = cluster;
		return cluster;
	}

	private WorkerStatus setWorkerStatus(String hostName) {
		WorkerStatus status = new WorkerStatus();
		status.setHostName(hostName);
		if(Math.random() < 0.5d) {
			status.setLastStatus("nok");
			status.setStatus("ok");
		} else {
			status.setLastStatus("ok");
			status.setStatus("nok");			
		}
		return status;
	}

	@RequestMapping(value="{id}", method=RequestMethod.GET)
	public @ResponseBody Cluster get(@PathVariable Long id) {
		WorkerHost workerHost = new WorkerHost();
		workerHost.setUrl("url111");
		return populate(workerHost.getUrl());
	}
	
	// internal helpers
	
	private Map<String, String> validationMessages(Set<ConstraintViolation<SetupHost>> failures) {
		Map<String, String> failureMessages = new HashMap<String, String>();
		for (ConstraintViolation<SetupHost> failure : failures) {
			failureMessages.put(failure.getPropertyPath().toString(), failure.getMessage());
		}
		return failureMessages;
	}
}

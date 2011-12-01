package net.local.clustercontrol.mvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

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
	
	@RequestMapping(value="/enable/{id}", method=RequestMethod.GET)
	public @ResponseBody Cluster getEnable(@PathVariable String id) {
		String url = "enable";
		return populate(url);
	}
	
	@RequestMapping(value="/disable/{id}", method=RequestMethod.GET)
	public @ResponseBody Cluster getDisable(@PathVariable String id) {
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
			populate(setupHost.getUrl());
			return Collections.singletonMap("initStatus", "ok");
		}
	}
	
	private Cluster populate(String url) {
		Status status1 = setStatus("worker1");
		Status status2 = setStatus("worker2");
		
		WorkerHost workerHost = new WorkerHost();
		ArrayList<Status> aworkers = workerHost.getWorkers();
		workerHost.setHostName("host1");
		aworkers.add(status1);
		aworkers.add(status2);
		
		workerHost.setWorkers(aworkers);
		workerHost.setUrl(workerHost.getUrl());
		
		WorkerHost workerHost2 = new WorkerHost();
		workerHost2.setHostName("host2");
		workerHost2.setUrl("url2");
		ArrayList<Status> aworkers2 = workerHost2.getWorkers(); 

		Status status3 = setStatus("worker1");
		Status status4 = setStatus("worker2");

		aworkers2.add(status3);
		aworkers2.add(status4);
		
		ArrayList<WorkerHost> list = new ArrayList<WorkerHost>();
		list.add(workerHost);
		list.add(workerHost2);
		
		Workers workers1 = new Workers();
		workers1.setName("workerName1");
		ArrayList<WorkerStatus> workerStatusList1 = new ArrayList<WorkerStatus>();
		WorkerStatus workerStatus11 = setWorkerStatus("wsHost1");
		WorkerStatus workerStatus12 = setWorkerStatus("wsHost2");
		workerStatusList1.add(workerStatus11);
		workerStatusList1.add(workerStatus12);
		workers1.setName("workerName1");
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
	private Status setStatus(String hostName) {
		Status status = new Status();
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
//		WorkerHost account = workerHosts.get(id);
//		if (account == null) {
//			throw new ResourceNotFoundException(id);
//		}
//		return account;
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

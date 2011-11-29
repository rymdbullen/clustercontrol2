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
		Status status1 = new Status();
		if(Math.random() < 0.5d) {
			status1.setLastStatus("nok");
			status1.setStatus("ok");
		} else {
			status1.setLastStatus("ok");
			status1.setStatus("nok");			
		}
		status1.setWorkerName("worker1");
		
		Status status2 = new Status();
		if(Math.random() < 0.5d) {
			status2.setLastStatus("nok");
			status2.setStatus("ok");
		} else {
			status2.setLastStatus("ok");
			status2.setStatus("nok");			
		}
		status2.setWorkerName("worker2");
		
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

		Status status3 = new Status();
		if(Math.random() < 0.5d) {
			status3.setLastStatus("nok");
			status3.setStatus("ok");
		} else {
			status3.setLastStatus("ok");
			status3.setStatus("nok");			
		}
		status3.setWorkerName("worker1");
		
		Status status4 = new Status();
		if(Math.random() < 0.5d) {
			status4.setLastStatus("nok");
			status4.setStatus("ok");
		} else {
			status4.setLastStatus("ok");
			status4.setStatus("nok");			
		}
		status4.setWorkerName("worker2");

		aworkers2.add(status3);
		aworkers2.add(status4);
		
		ArrayList<WorkerHost> list = new ArrayList<WorkerHost>();
		list.add(workerHost);
		list.add(workerHost2);
		
		Cluster cluster = new Cluster();
		cluster.setWorkerHosts(list);
		ArrayList<String> names = new ArrayList<String>();
		names.add("worker1");
		names.add("worker2");
		cluster.setWorkerNames(names);
		// set cluster
		_cluster = cluster;
		return cluster;
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

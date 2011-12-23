package net.local.clustercontrol.mvc;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.mvc.SetupHost;
import net.local.clustercontrol.core.logic.IWorkerManager;
import net.local.clustercontrol.core.logic.WorkerNotFoundException;
import net.local.clustercontrol.core.logic.impl.ClusterManager;

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
	
	private static final Logger logger = LoggerFactory.getLogger(ClusterController.class);
	
	//private Cluster _cluster = null;
	
	private Validator validator;
	private IWorkerManager clusterManager;
	
	@Autowired
	public ClusterController(Validator validator, ClusterManager clusterManager) {
		this.validator = validator;
		this.clusterManager = clusterManager;
	}
	
	@RequestMapping(method=RequestMethod.GET)
	public String getCreateForm(Model model) {
		if(clusterManager.getCluster() == null) {
			model.addAttribute(new SetupHost());
			return "cluster/setupForm";
		} else {
			model.addAttribute("cluster", clusterManager.getCluster());
			return "cluster/workersForm";
		}
	}

	@RequestMapping(value="/poll", method=RequestMethod.GET)
	public @ResponseBody Cluster getPoll() {
		return clusterManager.poll();
	}
	
	@RequestMapping(value="/enable/{id}/{speed}", method=RequestMethod.GET)
	public @ResponseBody Cluster setEnable(@PathVariable String id, @PathVariable String speed) {
		return clusterManager.enable(id, speed);
	}
	
	@RequestMapping(value="/disable/{id}/{speed}", method=RequestMethod.GET)
	public @ResponseBody Cluster setDisable(@PathVariable String id, @PathVariable String speed) {
		if(speed.equals("disable")) {
			return clusterManager.stop(id);
		}
		return clusterManager.disable(id, speed);
	}

	@RequestMapping(value="/stop/{id}", method=RequestMethod.GET)
	public @ResponseBody Cluster setStop(@PathVariable String id) {
		return clusterManager.stop(id);
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
			// TODO implement error handling 
			if(false == init(setupHost.getUrl())) {
				return errorMessages(failures);
			}
			return Collections.singletonMap("initStatus", "ok");
		}
	}
	/**
	 * 
	 * @param url
	 * @return
	 */
	private boolean init(String url) {
		try {
			return clusterManager.init(url);
		} catch (MalformedURLException e) {
			logger.error("Failed to create url from supplied string: "+url, e);
			return false;
		} catch (WorkerNotFoundException e) {
			logger.error("Failed to find worker for supplied url: "+url, e);
			return false;
		}
	}

	// internal helpers
	
	private Map<String, String> validationMessages(Set<ConstraintViolation<SetupHost>> failures) {
		Map<String, String> failureMessages = new HashMap<String, String>();
		for (ConstraintViolation<SetupHost> failure : failures) {
			failureMessages.put(failure.getPropertyPath().toString(), failure.getMessage());
		}
		return failureMessages;
	}
	private Map<String, String> errorMessages(Set<ConstraintViolation<SetupHost>> failures) {
		Map<String, String> errorMessages = new HashMap<String, String>();
		for (ConstraintViolation<SetupHost> failure : failures) {
			errorMessages.put(failure.getPropertyPath().toString(), failure.getMessage());
		}
		return errorMessages;
	}
}

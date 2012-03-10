package net.local.clustercontrol.mvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.mvc.SetupHost;
import net.local.clustercontrol.core.logic.IClusterManager;
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
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ClusterController.class);
	
	private Validator validator;
	private IClusterManager clusterManager;
	
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
	public @ResponseBody Cluster handlePoll() {
		clusterManager.poll();
		Cluster cluster = clusterManager.getCluster();
		return cluster;
	}
	
	@RequestMapping(value="/enable/{id}/{speed}", method=RequestMethod.GET)
	public @ResponseBody Cluster handleEnable(@PathVariable String id, @PathVariable String speed) {
		clusterManager.enable(id, speed);
		return clusterManager.getCluster();
	}
	
	@RequestMapping(value="/disable/{id}/{speed}", method=RequestMethod.GET)
	public @ResponseBody Cluster handleDisable(@PathVariable String id, @PathVariable String speed) {
		if(speed.equals("disable")) {
			clusterManager.stop(id);
		}
		clusterManager.disable(id, speed);
		return clusterManager.getCluster();
	}

	@RequestMapping(value="/stop/{id}", method=RequestMethod.GET)
	public @ResponseBody Cluster handleStop(@PathVariable String id) {
		clusterManager.stop(id);
		return clusterManager.getCluster();
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody Map<String, ? extends Object> handleSetup(@RequestBody SetupHost setupHost, HttpServletResponse response, Model model) {
		Set<ConstraintViolation<SetupHost>> failures = validator.validate(setupHost);
		model.addAttribute(setupHost);
		if (!failures.isEmpty())
		{
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return validationMessages(failures);
		}
		else
		{
			return clusterManager.init(setupHost.getUrl());
			// TODO implement error handling 
//			try {
//					return Collections.singletonMap("initStatus", "ok");
//				} else {
//					return Collections.singletonMap("initStatus", "nok");
//				}
//			} catch (MalformedURLException e) {
//				logger.error("Failed to create url from supplied string: "+setupHost.getUrl(), e);
//				errorMessages(failures);
//				return Collections.singletonMap("initStatus", "nok");
//			} catch (WorkerNotFoundException e) {
//				logger.error("Failed to find worker for supplied url: "+setupHost.getUrl(), e);
//				//return errorMessages(failures);
//				return Collections.singletonMap("initStatus", "nok");
//			}
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
//	private Map<String, String> errorMessages(Set<ConstraintViolation<SetupHost>> failures) {
//		Map<String, String> errorMessages = new HashMap<String, String>();
//		for (ConstraintViolation<SetupHost> failure : failures) {
//			errorMessages.put(failure.getPropertyPath().toString(), failure.getMessage());
//		}
//		return errorMessages;
//	}
}

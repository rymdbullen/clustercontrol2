package net.local.clustercontrol.web.controller.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;

import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.web.SetupHost;
import net.local.clustercontrol.web.controller.IClusterController;
import net.local.clustercontrol.core.logic.IClusterManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value="/cluster")
public class ClusterController implements IClusterController {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ClusterController.class);
	
	@Autowired 
	private Validator validator;
	@Autowired 
	private IClusterManager clusterManager;

	@InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(validator);
    }
	
	@Override
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

	@Override
	@RequestMapping(value="/poll", method=RequestMethod.GET)
	public @ResponseBody Cluster handlePoll() {
		clusterManager.poll();
		Cluster cluster = clusterManager.getCluster();
		return cluster;
	}
	
	@Override
	@RequestMapping(value="/enable/{id}/{speed}", method=RequestMethod.GET)
	public @ResponseBody Cluster handleEnable(@PathVariable String id, @PathVariable String speed) {
		clusterManager.enable(id, speed);
		return clusterManager.getCluster();
	}
	
	@Override
	@RequestMapping(value="/disable/{id}/{speed}", method=RequestMethod.GET)
	public @ResponseBody Cluster handleDisable(@PathVariable String id, @PathVariable String speed) {
		if(speed.equals("disable")) {
			clusterManager.stop(id);
		}
		clusterManager.disable(id, speed);
		return clusterManager.getCluster();
	}

	@Override
	@RequestMapping(value="/stop/{id}", method=RequestMethod.GET)
	public @ResponseBody Cluster handleStop(@PathVariable String id) {
		clusterManager.stop(id);
		return clusterManager.getCluster();
	}
	
	@Override
	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody Map<String, ? extends Object> handleSetup(@Valid @RequestBody SetupHost setupHost, HttpServletResponse response, Model model) {
		BindingResult result = null;
		model.addAttribute(setupHost);
		if (false && !result.hasErrors())
		{
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return resultMessages(result.getAllErrors());
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

	@SuppressWarnings("unused")
	private Map<String, String> validationMessages(Set<ConstraintViolation<SetupHost>> failures) {
		Map<String, String> failureMessages = new HashMap<String, String>();
		for (ConstraintViolation<SetupHost> failure : failures) {
			failureMessages.put(failure.getPropertyPath().toString(), failure.getMessage());
		}
		return failureMessages;
	}
	private Map<String, ? extends Object> resultMessages(List<ObjectError> allErrors) {
		Map<String, String> failureMessages = new HashMap<String, String>();
		for (ObjectError failure : allErrors) {
			failureMessages.put(failure.getObjectName(), failure.getDefaultMessage());
		}
		return failureMessages;
	}
}

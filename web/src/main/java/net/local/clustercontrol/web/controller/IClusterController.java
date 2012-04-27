package net.local.clustercontrol.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.web.SetupHost;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

public interface IClusterController {

	String getCreateForm(Model model);

	Cluster handlePoll();

	Cluster handleEnable(String id, String speed);

	Cluster handleDisable(String id, String speed);

	Cluster handleStop(String id);

	Map<String, ? extends Object> handleSetup(SetupHost setupHost,
			/*BindingResult result,*/ HttpServletResponse response, Model model);

}

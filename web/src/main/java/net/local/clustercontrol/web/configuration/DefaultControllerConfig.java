package net.local.clustercontrol.web.configuration;

import net.local.clustercontrol.web.controller.IClusterController;
import net.local.clustercontrol.web.controller.impl.ClusterController;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultControllerConfig implements IControllerConfig {

	@Bean
	@Override
	public IClusterController clusterController() {
		return new ClusterController();
	}
	
}

package net.local.clustercontrol.core.configuration;

import net.local.clustercontrol.core.http.impl.HttpClient;
import net.local.clustercontrol.core.logic.IClusterManager;
import net.local.clustercontrol.core.logic.IWorkerHandlerFactory;
import net.local.clustercontrol.core.logic.impl.ClusterManager;
import net.local.clustercontrol.core.logic.impl.WorkerHandlerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultServiceConfig implements ServiceConfig {

	@Override
	@Bean 
	public IClusterManager clusterManager() {
		return new ClusterManager();
	}

	@Override
	@Bean
	public IWorkerHandlerFactory workerHandlerFactory() {
		return new WorkerHandlerFactory();
	}
	
	@Bean
	public HttpClient httpClient() 
	{
		return new HttpClient();
	}

}

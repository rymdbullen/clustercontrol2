package net.local.clustercontrol;

import net.local.clustercontrol.core.configuration.EnvironmentAwarePropertyConfigurer;
import net.local.clustercontrol.core.http.impl.HttpClient;
import net.local.clustercontrol.core.logic.impl.ClusterManager;
import net.local.clustercontrol.core.logic.impl.WorkerFactory;
import net.local.clustercontrol.web.LoginView;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan( basePackages = "net.local.clustercontrol.core" )
public class AppConfig {
	
	@Bean
	public static PropertyPlaceholderConfigurer properties() 
	{
		PropertyPlaceholderConfigurer ppc = new EnvironmentAwarePropertyConfigurer();
		ppc.setIgnoreUnresolvablePlaceholders(true);
		return ppc;
	}

	@Bean
	public LoginView loginView() 
	{
		return new LoginView(); 
	}
	
	@Bean
	public ClusterManager clusterManager() 
	{
		return new ClusterManager(this.workerFactory()); 
	}
	
	@Bean
	public WorkerFactory workerFactory() 
	{
		return new WorkerFactory(this.httpClient());
	}
	
	@Bean
	public HttpClient httpClient() 
	{
		return new HttpClient();
	}

}

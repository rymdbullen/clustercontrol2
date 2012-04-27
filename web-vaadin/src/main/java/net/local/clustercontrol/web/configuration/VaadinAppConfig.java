package net.local.clustercontrol.web.configuration;

import net.local.clustercontrol.core.configuration.AppConfig;
import net.local.clustercontrol.core.logic.IClusterManager;
import net.local.clustercontrol.web.views.LoginView;
import net.local.clustercontrol.web.views.MainView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ AppConfig.class, VaadinComponentScanConfig.class })
public class VaadinAppConfig {
	
	@Autowired IClusterManager clusterManager;
	
	@Bean
	public LoginView loginView() 
	{
		return new LoginView(); 
	}
	
	@Bean
	public MainView mainView() 
	{
		return new MainView(clusterManager); 
	}
	
}

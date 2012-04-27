package net.local.clustercontrol.core.configuration;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ DefaultServiceConfig.class, CoreComponentScanConfig.class })
public class AppConfig {
	
	@Bean
	public static PropertyPlaceholderConfigurer properties() 
	{
		PropertyPlaceholderConfigurer ppc = new EnvironmentAwarePropertyConfigurer();
		ppc.setIgnoreUnresolvablePlaceholders(true);
		return ppc;
	}
	
}


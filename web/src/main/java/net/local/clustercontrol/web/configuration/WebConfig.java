package net.local.clustercontrol.web.configuration;


import net.local.clustercontrol.core.configuration.AppConfig;
import net.local.clustercontrol.core.configuration.DefaultServiceConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

/**
 * This is the final config that defines Web Application configuration and
 * imports Service, Dao and Controller Configuration Rather than doing an
 * import, one can choose to do classpath scanning if thats what is desired via:
 * 
 * <pre>
 *   @ComponentScan(basePackages = "com.company")
 * </pre>
 * 
 * The @EnableWebMvc is a hook-in into the Spring Servlet
 */
@Configuration
@EnableWebMvc
@Import({ AppConfig.class, DefaultControllerConfig.class, DefaultServiceConfig.class, ComponentScanConfig.class })
public class WebConfig extends WebMvcConfigurerAdapter {
	
	/*
	 STILL  MISSING:
	  
	<!-- Forwards requests to the "/" resource to the "welcome" view -->
	<mvc:view-controller path="/" view-name="welcome"/>

 	<!-- Handles HTTP GET requests for /resources/** by efficiently serving up static resources in the ${webappRoot}/resources/ directory -->
	<mvc:resources mapping="/resources/**" location="/resources/" />
	 */
	
	@Override
	@Bean(name="validator")
	public Validator getValidator() {
		return new LocalValidatorFactoryBean();
	}
	
	@Bean 
	public ViewResolver getViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setViewClass(JstlView.class);
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".jsp");

		return resolver;
	}
	
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		return new LocaleChangeInterceptor();
	}
	
	@Bean 
	public CookieLocaleResolver localeResolver() {
		return new CookieLocaleResolver();
	}
	
	@Bean 
	public ReloadableResourceBundleMessageSource messageSource() {
		ReloadableResourceBundleMessageSource rrbms = new ReloadableResourceBundleMessageSource();
		rrbms.setBasename("/WEB-INF/messages/messages");
		rrbms.setCacheSeconds(0);
		return rrbms;
	}
	
	@Bean
	public DefaultAnnotationHandlerMapping defaultAnnotationHandlerMapping() {
	    return new DefaultAnnotationHandlerMapping();
	}
	
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}
}

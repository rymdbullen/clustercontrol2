/**
 * Copyright 2011 Blue-Infinity
 * All Rights Reserved.
 * 
 * NOTICE: Blue-Infinity permits you to use, modify, and distribute this file
 * in accordance with the terms of the license agreement accompanying it.
 */
package net.local.clustercontrol.web.logging;

import java.util.logging.Handler;
import java.util.logging.LogManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.PropertyConfigurator;

import net.local.clustercontrol.core.configuration.EnvironmentAwareProperties;

/**
 * Logging Bootstrap listener initializes a bridge for JUL to log4j by 
 * removing all java.util.logging handlers for root logger and replacing it with {@link JuliToLog4jHandler}s. 
 *
 * <p>This listener should be registered as first listener as it initializes log4j</p>
 *
 * @author Jan Stenvall - initial version
 * 
 */
public class LoggingBootstrapListener implements ServletContextListener {
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		
		// sets the log path, ie catalina home is root directory
		//
		setupProperties();
		
		PropertyConfigurator.configure(EnvironmentAwareProperties.getInstance());
		
		java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
		Handler[] handlers = rootLogger.getHandlers();
		for (int i = 0; i < handlers.length; i++) {
			System.out.println("removed rootlogger handler: "+handlers[i]);
			rootLogger.removeHandler(handlers[i]);
		}
		rootLogger.addHandler(new JuliToLog4jHandler());
		
		EnvironmentAwareProperties.getInstance().logConfig();
	}
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
	/**
	 * Setup of properties needed for log file directories
	 */
	private void setupProperties() {
		String appserverHome = System.getProperty("catalina.home");
        if(appserverHome != null) {
        	System.setProperty("appserver.home.dir", appserverHome.replace('\\', '/'));
        	System.setProperty("appserver.temp.dir", System.getProperty("java.io.tmpdir").replace('\\', '/'));
        	return;
        }
        
        // check for weblogic properties
        //
        appserverHome = System.getProperty("user.dir");
        String wlsHome = System.getProperty("wls.home");
       	if(appserverHome != null && wlsHome != null && !wlsHome.equals("")) {
        	System.setProperty("appserver.home.dir", appserverHome);
        	System.setProperty("appserver.temp.dir", System.getProperty("java.io.tmpdir").replace('\\', '/'));
        }
	}
}

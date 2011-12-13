/**
 * Copyright 2011 Blue-Infinity
 * All Rights Reserved.
 * 
 * NOTICE: Blue-Infinity permits you to use, modify, and distribute this file
 * in accordance with the terms of the license agreement accompanying it.
 */
package net.local.clustercontrol.core.configuration;

import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class EnvironmentAwarePropertyConfigurer extends PropertyPlaceholderConfigurer {
	
	private Properties props = EnvironmentAwareProperties.getInstance();

	protected Properties mergeProperties() {
		return props;
	}
}

/**
 * Copyright 2011 Blue-Infinity
 * All Rights Reserved.
 * 
 * NOTICE: Blue-Infinity permits you to use, modify, and distribute this file
 * in accordance with the terms of the license agreement accompanying it.
 */
package net.local.clustercontrol.core.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EnvironmentAwareProperties creates an hierarchy of inherited properties. 
 * It provides the means to have one configuration file for several environments.
 * <p>
 * To enable a profile, the classpath of the application server must contain
 * a file named <code>allconfig-currentenv.properties</code>.
 * The file must contain the following line <code>environment=&lt;env-name&gt;</code>.
 * <p>
 * Maven tests are preferably run with the environment as <code>environment=maven-test</code>.
 * 
 * @author jstenvall
 * @since 1.0
 */
public class EnvironmentAwareProperties extends Properties {
	
	/**
	 * Generated Serial Version UID
	 */
	private static final long serialVersionUID = -5563564153639974606L;

	private static final Logger logger = LoggerFactory.getLogger(EnvironmentAwareProperties.class);
	
	/** the static singleton instance */
	private static EnvironmentAwareProperties environmentAwareProperties = null;
	
	private static final String CURRENTENV_PROPERTIES_FILE_NAME = "/allconfig-currentenv.properties";	

	private static final String ALLCONFIG_PROPERTIES_FILE_NAME = "/allconfig.properties";

	private static final String ALLCONFIG_LOCAL_PROPERTIES_FILE_NAME = "/allconfig-local.properties";

	private static final String ENVIRONMENT_PROPERTY_PREFIX = "environment";

	private static final String DEFAULT_ENVIRONMENT = "default";

	private static String currentEnvironmentName;

	private Map<String, EnvironmentConfig> environmentByName = new HashMap<String, EnvironmentConfig>();
	
	/**
	 * Default constructor
	 */
	public EnvironmentAwareProperties() {
		// Exists only to defeat instantiation.
	}
	/**
	 * Returns the singleton property manager.
	 * @return the singleton property manager.
	 */
	public static synchronized EnvironmentAwareProperties getInstance() {
		if(environmentAwareProperties == null) {
			environmentAwareProperties = new EnvironmentAwareProperties();
			environmentAwareProperties.init();
		}
		return environmentAwareProperties;
	}
	
	protected void init() {
		
		// create a new instance
		Properties standardProps = getStandardProperties();

		// get current environment
		currentEnvironmentName = getCurrentEnvironmentName();
		
		setup(standardProps);
		
		if(null == getEnvironmentConfig(currentEnvironmentName, false)) {
			throw new IllegalArgumentException("Non Existing environment '"+currentEnvironmentName+"' specified in file: "+CURRENTENV_PROPERTIES_FILE_NAME);
		}
		if ("true".equals(getProperty("allow-local-override"))) {
			Properties localProps = getPropertiesFromClassPath(ALLCONFIG_LOCAL_PROPERTIES_FILE_NAME);
			setup(localProps);	
		}
	}
	private void setup(Properties standardProps) {
		for (Iterator<Map.Entry<Object,Object>> iter = standardProps.entrySet().iterator(); iter.hasNext();) {
			Map.Entry<Object,Object> entry = (Map.Entry<Object,Object>) iter.next();
			if (((String) entry.getKey()).startsWith(ENVIRONMENT_PROPERTY_PREFIX)) {
				//if(logger.isTraceEnabled()) { logger.trace("Parse Environment "+(String) entry.getValue()); };
				parseEnvironmentDefinition((String) entry.getValue());
			}
		}

		for (Iterator<Map.Entry<Object,Object>> iter = standardProps.entrySet().iterator(); iter.hasNext();) {
			Map.Entry<Object,Object> entry = (Map.Entry<Object,Object>) iter.next();
			if (!((String) entry.getKey()).startsWith(ENVIRONMENT_PROPERTY_PREFIX)) {
				parsePropertyDefinition((String) entry.getKey(), (String) entry.getValue());
			}
		}
	}
	
	public void logConfig() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n**********************************************************************************\n");
		sb.append(addAsciiDecoration(""));
		sb.append(addAsciiDecoration("       "+Constants.APPLICATION_NAME_DISPLAY+" INITIALIZING - '"+currentEnvironmentName+"'"));
		sb.append(addAsciiDecoration(""));
		sb.append(dumpCurrentConfig(true));
		sb.append(addAsciiDecoration(""));
		sb.append(addAsciiDecoration("       "+Constants.APPLICATION_NAME_DISPLAY+" INITIALIZED  - '"+currentEnvironmentName+"'"));
		sb.append(addAsciiDecoration(""));
		sb.append("**********************************************************************************\n");
		logger.info(sb.toString());
	}
	
	public String getCurrentEnvironmentName() {
		if (currentEnvironmentName == null) {
			Properties currentEnvProps = getPropertiesFromClassPath(CURRENTENV_PROPERTIES_FILE_NAME);
			currentEnvironmentName = currentEnvProps.getProperty("environment");
			if(currentEnvironmentName==null) {
				currentEnvironmentName = DEFAULT_ENVIRONMENT;
			}
			if (System.getProperty("allconfig-currentenv-override") != null) {
				currentEnvironmentName = System.getProperty("allconfig-currentenv-override");
			}
		}
		
		return currentEnvironmentName;
	}

	private String addAsciiDecoration(String message) {
		String debugString = "* " + message; 
		int length = debugString.length();
		int targetLength = 80;
		if(length>targetLength) {
			return debugString+"\r\n";
		}
		int diff = targetLength-length;
		int index = 0;
		StringBuilder sb = new StringBuilder(debugString);
		while (index<=diff) {
			index++;
			sb.append(" ");
		}
		sb.append("*");
		return sb.toString()+"\r\n";
	}
	private static Properties getPropertiesFromClassPath(String path) {
		Properties props = new Properties();
		InputStream inputStream = null;
		try {
			try {
				inputStream = EnvironmentAwareProperties.class.getResourceAsStream(path);
				if (inputStream != null) {
					props.load(inputStream);
				} else {
					logger.info("Failed to find properties: "+path);
				}

			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}
		} catch (IOException e) {
			System.err.println("Could not load '" + path + "' from classpath");
		}
		return props;
	}

	protected Properties getStandardProperties() {
		Properties propertiesFromClassPath = getPropertiesFromClassPath(ALLCONFIG_PROPERTIES_FILE_NAME);
		Properties overridePropertiesFromClassPath = getPropertiesFromClassPath(ALLCONFIG_LOCAL_PROPERTIES_FILE_NAME);
		propertiesFromClassPath.putAll(overridePropertiesFromClassPath);
		
		return propertiesFromClassPath;
	}

	private class EnvironmentConfig {
		private String name;

		private List<EnvironmentConfig> superConfigs = new ArrayList<EnvironmentConfig>();

		private Map<String, String> propertyValues = new HashMap<String, String>();

		public void addSuperConfig(EnvironmentConfig envConfig) {
			superConfigs.add(envConfig);
		}

		public void addProperty(String name, String value) {
			propertyValues.put(name, value);
		}

		/**
		 * Fetch a property using the current <em>environment</em> chain.
		 * 
		 * @param name
		 * @return The value of the property, trimmed, or <code>null</code> if
		 *         the property does not exist.
		 */
		public String getProperty(String name) {
			String value = (String) propertyValues.get(name);
			if (value != null) {
				return value.trim();
			}

			for (Iterator<EnvironmentConfig> iter = superConfigs.iterator(); iter.hasNext();) {
				EnvironmentConfig envConfig = (EnvironmentConfig) iter.next();
				value = envConfig.getProperty(name);
				if (value != null) {
					return value.trim();
				}
			}

			return null;
		}

		@SuppressWarnings("unused")
		public String getName() {
			return name;
		}

		public EnvironmentConfig(String name) {
			this.name = name;
		}

		private Set<String> _propNames = null;

		public Set<String> getAllPropertyNames() {			
			if (_propNames == null) {
				Set<String> temp = new HashSet<String>();
				
				temp.addAll(propertyValues.keySet());
				
				for (EnvironmentConfig superConfig : superConfigs) {				
					temp.addAll(superConfig.getAllPropertyNames());
				}
				
				_propNames = temp;
			}
			return _propNames;
		}
	}

	private boolean hasEnvironmentConfig(String name) {
		return environmentByName.containsKey(name);
	}

	private EnvironmentConfig getEnvironmentConfig(String name, boolean create) {
		EnvironmentConfig envConfig = (EnvironmentConfig) environmentByName.get(name);
		if (envConfig == null && create) {
			envConfig = new EnvironmentConfig(name);
			environmentByName.put(name, envConfig);
		}

		return envConfig;
	}

	private EnvironmentConfig getEnvironmentConfig(String name) {
		return getEnvironmentConfig(name, true);
	}

	private String cleanPropertyName(String propertyName) {
		StringTokenizer tokenizer = new StringTokenizer(propertyName, ". \t", true);
		StringBuilder cleanPropertyName = new StringBuilder();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			cleanPropertyName.append(token.trim());
		}

		return cleanPropertyName.toString();
	}

	private void parseEnvironmentDefinition(String value) {
		StringTokenizer tokenizer = new StringTokenizer(value, ":, \t");
		String environmentName = tokenizer.nextToken();

		EnvironmentConfig envConfig = getEnvironmentConfig(environmentName);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if(logger.isTraceEnabled()) { logger.trace("Environment: ["+envConfig.name+"] inherits ["+token+"]"); }
			envConfig.addSuperConfig(getEnvironmentConfig(token));
		}
	}

	private void parsePropertyDefinition(String name, String value) {
		String cleanName = cleanPropertyName(name);
		int firstDotIndex = cleanName.indexOf('.');

		String environmentName = null;
		if (firstDotIndex != -1) {
			environmentName = cleanName.substring(0, firstDotIndex);
		}
		if (environmentName != null && hasEnvironmentConfig(environmentName)) {
			String propertyName = cleanName.substring(firstDotIndex + 1);
			String sysValue = System.getProperty(propertyName);
			String thisValue = null;
			if(sysValue != null) {
				thisValue = filterValue(sysValue);
				logger.info("Property '" + cleanName + "' allconfig value ["+value+"] overridden System Property value: ["+sysValue+"]");
			} else {
				thisValue = filterValue(value);
			}
			getEnvironmentConfig(environmentName).addProperty(propertyName, thisValue);
		} else {
			logger.error("Property '" + cleanName + "' lacks environment prefix, dropped");
			System.err.println("Property '" + cleanName + "' lacks environment prefix, dropped");
		}
	}
	/**
	 * Filters placeholders with System properties.
	 * @param value the value to filter
	 * @return a filtered value 
	 */
	String filterValue(String value) {
		Pattern placeholderPattern = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher placeholderMatcher = placeholderPattern.matcher(value);
		
		while (placeholderMatcher.find()) 
		{
			String key = placeholderMatcher.group(1);
			String replaceValue = getProperty(key);
			if(replaceValue==null) {				
				replaceValue = System.getProperty(key);
			}
			if(replaceValue!=null) {
				String filteredValue = value.replaceAll("\\$\\{"+key+"\\}", replaceValue);
				System.out.println("EnvironmentAwareProperties: Converted ["+value+"] to ["+filteredValue+"]");
				logger.info("Converted ["+value+"] to ["+filteredValue+"]");
				return filteredValue;
			}
		}
		return value;
	}
	/**
	 * Returns the value for the supplied property name
	 * @param propertyName the property to value for
	 * @return the value for the supplied property name
	 */
        @Override
	public String getProperty(String propertyName) {
		String value = null;
		EnvironmentConfig config = getEnvironmentConfig(currentEnvironmentName, false);
		if (config == null) {
			throw new IllegalArgumentException("Non Existing environment '"+currentEnvironmentName+"' specified in file: "+CURRENTENV_PROPERTIES_FILE_NAME);
		} 
		value = config.getProperty(propertyName);
		return value;
	}
	/**
	 * Returns a string with all propertys and correlating values 
	 * @param useAsciiArt adds some fancy ascii art decorations
	 * @return a string with all propertys and correlating values
	 */
	public StringBuilder dumpCurrentConfig(boolean useAsciiArt) {
		StringBuilder sb = new StringBuilder();
		List<String> propertyNameList = getCurrentPropertyNamesSorted();
		for (int nameIdx = 0; nameIdx < propertyNameList.size(); nameIdx++) {
			String name = propertyNameList.get(nameIdx);
			String value = this.getProperty(name);
			if(name.contains("password")) {
				// scramble password
				value = "**********";
			}
			String message = name+"="+value;
			if(useAsciiArt) {
				message = addAsciiDecoration(message);
			}
			sb.append(message);
		}
		return sb;
	}
	/**
	 * Returns a sorted list of property names
	 * @return a sorted list of property names
	 */
	public List<String> getCurrentPropertyNamesSorted() {
		EnvironmentConfig config = getEnvironmentConfig(currentEnvironmentName, false);
		if(config==null) {
			throw new IllegalArgumentException("Failed to initiate '"+currentEnvironmentName+"'");
		}
		List<String> propertyNameList = new ArrayList<String>(config.getAllPropertyNames());
		Collections.sort(propertyNameList);
		return propertyNameList;
	}
	/**
	 * 
	 * @param writer
	 */
	public void dumpAllConfig(PrintWriter writer) {
		Set<String> allEnvNames = new HashSet<String>(environmentByName.keySet());
		Set<String> printedProperties = new HashSet<String>();
		try {
			InputStream inputStream = null;
			try {
				inputStream = EnvironmentAwareProperties.class.getResourceAsStream(ALLCONFIG_PROPERTIES_FILE_NAME);

				if (inputStream == null) {
					writer.write("Could not load '"
							+ ALLCONFIG_PROPERTIES_FILE_NAME
							+ "' from classpath. Missing?");
				} else {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(inputStream));

					String configLine = reader.readLine();
					while (configLine != null) {
						configLine = configLine.trim();

						if (configLine.startsWith("#")
								|| configLine.length() == 0
								|| configLine.startsWith(ENVIRONMENT_PROPERTY_PREFIX)) {
							writer.write(configLine);
						} else {
							int firstDotIndex = configLine.indexOf('.');
							if (firstDotIndex == -1) {
								writer.write("### IGNORED (missing environment prefix): " + configLine);
							} else {
								String envName = configLine.substring(0,
										firstDotIndex);
								if (allEnvNames.contains(envName)) {
									StringTokenizer tokenizer = new StringTokenizer(
											configLine, "=");
									String propertyName = tokenizer.nextToken()
											.substring(firstDotIndex + 1)
											.trim();
									if (!printedProperties
											.contains(propertyName)) {
										writer.write(propertyName);
										writer.write("=");
										if (propertyName.toLowerCase().indexOf(
												"password") == -1) {
											writer.write(getProperty(propertyName));
										} else {
											writer.write("****");
										}
										printedProperties.add(propertyName);
									}
								} else {
									writer
											.write("### IGNORED (undeclared environment prefix): "
													+ configLine);
								}
							}
						}

						configLine = reader.readLine();
					}
				}

			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}
		} catch (IOException e) {
			writer.write("Could not load '" + ALLCONFIG_PROPERTIES_FILE_NAME
					+ "' from classpath: " + e.getClass().getName() + ": "
					+ e.getMessage());
		}
	}
	/**
	 * Fetches all property names for the current environment, recursively, from
	 * the environment itself, and all super configurations.
	 */
        @Override
	public Enumeration<String> propertyNames() {
		EnvironmentConfig config = getEnvironmentConfig(currentEnvironmentName, false);
		if (config == null) {
			logger.warn("No properties for the current environment, " + currentEnvironmentName);
			return Collections.enumeration(Collections.<String>emptySet());
		}
		return Collections.enumeration(config.getAllPropertyNames());
	}

        @Override
	public boolean containsKey(Object propertyName) {
		EnvironmentConfig config = getEnvironmentConfig(currentEnvironmentName, false);
		return config.getAllPropertyNames().contains(propertyName);
	}
}

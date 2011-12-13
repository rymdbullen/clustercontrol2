package net.local.clustercontrol.core.logic;

import net.local.clustercontrol.api.model.xml.Hosts;

public class ConfigManager {

	private static Hosts _hosts = null;
	private static String _context = "jkmanager";

	public static Hosts getdHosts() {
		return _hosts;
	}
	/**
	 * 
	 * @return
	 */
	public static String getJdkContext() {
		return _context;
	}

}

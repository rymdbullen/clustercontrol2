package net.local.clustercontrol.core.util;

public class StringUtil {

	private static final String WORKER_STOP = "2";
	private static final String WORKER_DISABLE = "1";
	private static final String WORKER_ACTIVATE = "0";

	public static String getDisableParameters(String loadBalancer, String worker) {
		String parameters = addUpdateParameters(loadBalancer, worker, WORKER_DISABLE);
		String xmlMimeParameters = StringUtil.getMimeXmlParameters();
		return parameters + "&" + xmlMimeParameters;
	}
	public static String getStopParameters(String loadBalancer, String worker) {
		String parameters = addUpdateParameters(loadBalancer, worker, WORKER_STOP);
		String xmlMimeParameters = StringUtil.getMimeXmlParameters();
		return parameters + "&" + xmlMimeParameters;
	}
	public static String getActivateParameters(String loadBalancer, String worker) {
		String parameters = addUpdateParameters(loadBalancer, worker, WORKER_ACTIVATE);
		String xmlMimeParameters = StringUtil.getMimeXmlParameters();
		return parameters + "&" + xmlMimeParameters;
	}
	/**
	 * Adds update parameters to 
	 * @param loadBalancer
	 * @param worker
	 * @param command
	 * @return
	 */
	private static String addUpdateParameters(String loadBalancer, String worker, String command) {
		if(loadBalancer==null||worker==null) {
			throw new IllegalArgumentException("loadBalancer name must be specified");
		}
		String parameters = "cmd=update&from=list&w="+ loadBalancer +"&sw="+ worker +"&wa="+ command +"&wf=1&wn="+ worker +"&wr=&wc=&wd=0";
		return (parameters);
	}
	/**
	 * 
	 * @return
	 */
	public static String getMimePropertiesParameters() {
		return "mime=prop&opt=4";
	}
	/**
	 * 
	 * @return
	 */
	public static String getMimeXmlParameters() {
		return "mime=xml&opt=4";
	}
	/**
	 * 
	 * @return
	 */
	public static String getMimeTextParameters() {
		return "mime=txt&opt=4";
	}
	/**
	 * Checks and removes trailing slash character 
	 * @param path the path to check
	 * @return a non slash trailing context path
	 */
	public static String removeTrailingSlash(String path) {
		if(path.endsWith("/")) {
			return path.substring(0, path.length()-1);
		}
		return path;
	}
}

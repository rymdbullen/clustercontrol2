package net.local.clustercontrol.core.logic.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.local.clustercontrol.api.model.Hosts;
import net.local.clustercontrol.api.model.Host;
import net.local.clustercontrol.api.model.JkStatus;
import net.local.clustercontrol.api.model.JkMember;
import net.local.clustercontrol.api.model.JkResult;
import net.local.clustercontrol.api.model.WorkerResponse;
import net.local.clustercontrol.api.model.WorkerResponses;
import net.local.clustercontrol.core.http.HttpClient;
import net.local.clustercontrol.core.logic.ControlCommandException;
import net.local.clustercontrol.core.logic.IWorkerManager;
import net.local.clustercontrol.core.logic.WorkerNotFoundException;
import net.local.clustercontrol.core.util.StringUtil;

public class WorkerManager implements IWorkerManager {

	private static final Logger logger = LoggerFactory.getLogger(WorkerManager.class);

	private static final String RESPONSE_FORMAT_XML = "xml";
	private static final String RESPONSE_FORMAT_PROPERTIES = "prop";
	private static final String RESPONSE_FORMAT_TEXT = "txt";
	/** the hosts container */
	private static Hosts _hosts = new Hosts();
	private static String _loadBalancer;
	private static String _context;
	private static String _protocol;
	private static int _port = -1;
	private static String _initUrl;
	private static String[] _workerNames;
	private static Calendar _lastUpdated;
	private static ArrayList<JkStatus> _currentStatus = new ArrayList<JkStatus>();
	
	/**
	 * Initializes the WorkerManager with a url. 
	 * @param url the url to initialize with
	 * @throws MalformedURLException 
	 * @throws WorkerNotFoundException 
	 */
	public static ArrayList<JkStatus> init(String url) throws MalformedURLException, WorkerNotFoundException {		
		logger.info("Initializing with url: "+url);
		if(false==url.startsWith("http")) {
			url = "http://"+url;
		}
		//
		// if no members return null
		HashSet<String> addressSet = null;
		JkStatus jkStatus = unmarshalResponse(getWorkerResponse(url));
		if(jkStatus==null) {
			return null;
		}
		List<JkMember> members = WorkerManager.getMembers(jkStatus);
		if(members==null) {
			return null;
		}
		addressSet = WorkerManager.getUniqueHosts(members);
		if (addressSet==null || addressSet.size()==0) {
			return null;
		}
		ArrayList<JkStatus> statuses = new ArrayList<JkStatus>();
		statuses.add(jkStatus);
		addHost(createHost(url));
		
		if(_loadBalancer==null) {
			_loadBalancer = jkStatus.getBalancers().getBalancer().getName();
		}
		
		if(logger.isDebugEnabled()) { logger.debug("More than one unique host found, initializes {} hosts", addressSet.size()); }
		
		Iterator<String> addressIterator = addressSet.iterator();
		while (addressIterator.hasNext()) {
			String ipaddress = (String) addressIterator.next();
			if(ipaddress.equals(jkStatus.getServer().getName())) {
				if(logger.isDebugEnabled()) { logger.debug("Skipped existing host: "+ipaddress); }
				continue;
			}
			String newUrl = createUrlForIpaddress(ipaddress);
			JkStatus newJkStatus = unmarshalResponse(getWorkerResponse(newUrl));
			if(newJkStatus==null) {
				continue;
			}
			List<JkMember> newMembers = getMembers(newJkStatus);
			if(newMembers==null) {
				continue;
			}
			addHost(createHost(newUrl));
			statuses.add(newJkStatus);
		}
		_initUrl = url;
		_workerNames = getWorkerNames(jkStatus);
		_lastUpdated = Calendar.getInstance();
		return statuses;
	}
	/**
	 * Returns the worker names for the given status
	 * @param jkStatus the status to get worker names for
	 * @return the worker names for the given status
	 */
	private static String[] getWorkerNames(JkStatus jkStatus) {
		int count =jkStatus.getBalancers().getBalancer().getMemberCount();
		String[] retval = new String[count];
		for (int index = 0; index < count; index++) {
			JkMember member = jkStatus.getBalancers().getBalancer().getMember().get(index);
			retval[index] = member.getName();
		}
		return retval;
	}
	/**
	 * 
	 * @param ipaddress
	 * @return
	 * @throws MalformedURLException
	 */
	private static String createUrlForIpaddress(String ipaddress) throws MalformedURLException {
		URL newUrl = new URL(_protocol, ipaddress, _port, _context);
		return newUrl.toExternalForm();
	}
	/**
	 * Initializes the WorkerManager from supplied url. Sets up a hosts container and returns a list of balancers.
	 * @param url the url to initialize with
	 * @throws MalformedURLException
	 * @throws WorkerNotFoundException
	 */
	public static WorkerResponse getWorkerResponse(String url) throws MalformedURLException, WorkerNotFoundException {
		if(logger.isDebugEnabled()) { logger.debug("Get response for url: "+url); }
		//
		// try to get workers for this url
		String parameters = StringUtil.getMimeXmlParameters();
		WorkerResponse workerResponse = HttpClient.executeUrl(url, parameters);
		if(workerResponse.getBody()==null) {
			throw new WorkerNotFoundException();
		}
		//
		// this may not work
		JkStatus balancer = statusComplex(workerResponse);
		if(balancer==null || balancer.getBalancers().getBalancer().getMemberCount() == 0) {			
			return null;
		}
		return workerResponse;
	}
	/**
	 * 
	 * @param jkStatus
	 * @return
	 */
	private static List<JkMember> getMembers(JkStatus jkStatus) {
		if(jkStatus.getBalancers() != null && 
				jkStatus.getBalancers().getBalancer() != null ) {
			if(jkStatus.getBalancers().getBalancer().getMemberCount()>0) {
				return jkStatus.getBalancers().getBalancer().getMember(); 
			}
		}
		return null;
	}
	/**
	 * Initializes the WorkerManager with a hosts container. 
	 * @param hosts the hosts container
	 */
	public static void init(Hosts hosts) {
		_hosts = hosts;
	}
	/**
	 * Resets the WorkerManager hosts container. 
	 */
	public static void reset() {
		_hosts = new Hosts();
	}
	/**
	 * Initializes the WorkerManager from supplied url. Sets up a hosts container and returns a list of balancers.
	 * @param url the url to initialize with
	 * @throws MalformedURLException
	 * @throws WorkerNotFoundException
	 */
	public static WorkerResponse getBalancers(String url) throws MalformedURLException, WorkerNotFoundException {
		
		// this is a getStatus(url);
		
		String parameters = StringUtil.getMimeXmlParameters();
		//
		// try to get workers for this url
		WorkerResponse workerResponse = HttpClient.executeUrl(url, parameters);
		if(workerResponse.getBody()==null) {
			throw new WorkerNotFoundException();
		}
		//
		// this may not work
		JkStatus balancer = statusComplex(workerResponse);
		if(balancer==null || balancer.getBalancers().getBalancer().getMemberCount() == 0) {			
			return null;
		}
		return workerResponse;
	}
	/**
	 * Checks if the provided host is exists, if not it adds it to the hosts container
	 * @param newHost the host to add
	 * @return true if host added, false if not
	 */
	private static boolean addHost(Host newHost) {
		if(_hosts==null) {
			if(logger.isDebugEnabled()) {
				logger.debug("_hosts==null: resetting _hosts and continues");
			}
			reset();
		}
		//
		// previous hosts found, check if already setup
		int hostsCount = _hosts.getHostList().size();
		for (int i = 0; i < hostsCount; i++) {
			Host host = _hosts.getHostList().get(i);
			if(host.getIpAddress().equals(newHost.getIpAddress())) {
				if(host.getPort().equals(newHost.getPort())) {
					if(host.getContext().equals(newHost.getContext())) {
						logger.info("Already exist; trying to add host, "+host.getIpAddress()+", "+host.getPort()+", "+host.getContext());
						return false;
					}
				}
			}
		}
		logger.info("Added host, "+newHost.getIpAddress()+", "+newHost.getPort()+", "+newHost.getContext());
		_hosts.getHostList().add(newHost);
		return true;
	}
	/**
	 * Activates the worker for the supplied loadbalancer and returns the list of balancers
	 * @param loadBalancer the loadbalancer
	 * @param worker the worker to activate
	 * @return list of balancers
	 * @throws ControlCommandException 
	 */
	public static ArrayList<JkStatus> activate(String loadBalancer, String worker) {
		//
		// Perform the activate action
		logger.info("Activating worker: "+worker);
		String activateParameters = StringUtil.getActivateParameters(loadBalancer, worker);
		WorkerResponses workerResponses = HttpClient.executeUrls(_hosts, activateParameters);

		try {
			parseStatusAndPause(workerResponses, worker);
		} catch (ControlCommandException e) {
			// set latest status message
			
		}

		return statusComplex();
	}

	/**
	 * Activates the worker for the initialized loadbalancer and returns the list of balancers
	 * @param worker the worker to activate
	 * @return list of balancers
	 * @throws ControlCommandException 
	 */
	public static ArrayList<JkStatus> activate(String worker) {
		return activate(_loadBalancer, worker);
	}
	/**
	 * Activates all the workers for the initialized loadbalancer and returns the list of balancers
	 * @param rate the rate to activate workers
	 * @return list of statuses
	 * @throws ControlCommandException 
	 */
	public static ArrayList<JkStatus> activateAll(String rate) {		
		//
		// logic to handle activation
		for (int workersIdx = 0; workersIdx < _workerNames.length; workersIdx++) {
			String worker = _workerNames[workersIdx];
			//
			// Perform the activate action
			logger.info("Activating worker: "+worker);
			String activateParameters = StringUtil.getActivateParameters(_loadBalancer, worker);
			String xmlMimeParameters = StringUtil.getMimeXmlParameters();
			WorkerResponses workerResponses = HttpClient.executeUrls(_hosts, activateParameters + "&" + xmlMimeParameters);

			try {
				parseStatusAndPause(workerResponses, worker);
			} catch (ControlCommandException e) {
				// set latest status message
			}

			waitLogic(workersIdx, _workerNames.length, rate);
		}
		return statusComplex();
	}	
	/**
	 * Logic for getting an interval
	 * @param workersIdx
	 * @param length
	 * @param rate
	 */
	private static void waitLogic(int workersIdx, int length, String rate) {
		// weight
		// idx * 
		double defaultTime = 90.0d;
		int[] degrees;
		int[] degreesSlow = new int[] {90, 155};
		int[] degreesMedium = new int[] {125, 145};
		int[] degreesAggressive = new int[] {155, 165};
		
		if(rate.equals("slow")) {
			degrees = degreesSlow;
		} else if(rate.equals("medium")) {
			degrees = degreesMedium;
		} else if(rate.equals("aggressive")) {
			degrees = degreesAggressive;
		} else {
			degrees = degreesSlow;
		}
		long delay;
		double angle;
		if(workersIdx==0) {
			int weight = degrees[0];
			angle = weight * 2.0 * Math.PI/360.0;
			System.out.println("["+workersIdx+"] "+weight+": "+ Math.round(Math.sin(angle)*defaultTime));
		} else if(workersIdx==1) {
			int weight = degrees[1];
			angle = weight * 2.0 * Math.PI/360.0;
			System.out.println("["+workersIdx+"] "+weight+": "+Math.round(Math.sin(angle)*defaultTime));
		} else {
			int weight = degrees[2];
			angle = weight * 2.0 * Math.PI/360.0;
			System.out.println("["+workersIdx+"] "+weight+": "+Math.round(Math.sin(angle)*defaultTime));
		}
		delay = Math.round(Math.sin(angle)*defaultTime);
		//
		// wait for x seconds
		try {
			long millis = delay*1000;
			logger.info("Sleeping "+millis+"ms");
			// Sleep for 3 seconds
			Thread.sleep(millis);
		} catch (Exception e) {
			if(logger.isDebugEnabled()) {
				logger.debug("Error when suspending thread: "+e.getMessage());
			}
		}		
		
	}
	/**
	 * Disables the worker for the initialized loadbalancer and returns the list of balancers
	 * @param loadBalancer the loadbalancer
	 * @param worker the worker to disable
	 * @return list of balancers
	 * @throws ControlCommandException 
	 */
	public static ArrayList<JkStatus> disable(String loadBalancer, String worker) {
		//
		// Perform the disable action
		logger.info("Disabling worker: "+worker);
		String disableParameters = StringUtil.getDisableParameters(loadBalancer, worker);
		String xmlMimeParameters = StringUtil.getMimeXmlParameters();
		WorkerResponses workerResponses = HttpClient.executeUrls(_hosts, disableParameters + "&" + xmlMimeParameters);

		try {
			parseStatusAndPause(workerResponses, worker);
		} catch (ControlCommandException e) {
			// set latest status message
		}

		return statusComplex();
	}
	/**
	 * Disables the worker for the initialized loadbalancer and returns the list of balancers
	 * @param worker the worker to disable
	 * @return list of balancers
	 * @throws ControlCommandException 
	 */
	public static ArrayList<JkStatus> disable(String worker) {
		return disable(_loadBalancer, worker);
	}
	/**
	 * Disables all workers for the initialized loadbalancer and returns the list of balancers
	 * @param rate the rate to disable workers
	 * @return list of statuses
	 * @throws ControlCommandException 
	 */
	public static ArrayList<JkStatus> disableAll(String rate) {
		//
		// logic to handle disabling
		for (int workersIdx = 0; workersIdx < _workerNames.length; workersIdx++) {
			String worker = _workerNames[workersIdx];
			//
			// Perform the disable action
			logger.info("Disabling worker: "+worker);
			String disableParameters = StringUtil.getDisableParameters(_loadBalancer, worker);
			String xmlMimeParameters = StringUtil.getMimeXmlParameters();
			WorkerResponses workerResponses = HttpClient.executeUrls(_hosts, disableParameters + "&" + xmlMimeParameters);

			try {
				parseStatusAndPause(workerResponses, worker);
			} catch (ControlCommandException e) {
				// set latest status message
			}
		}
		return statusComplex();
	}
	/**
	 * Parses the supplied workerResponses and pause for a period
	 * @param workerResponses the workerResponse, ie html bodys
	 * @param worker the worker to get status for
	 * @throws ControlCommandException 
	 */
	private static void parseStatusAndPause(WorkerResponses workerResponses, String worker) throws ControlCommandException {
		//
		// process the action responses
		int hostsCount = workerResponses.getResponseList().size();
		for (int hostIdx = 0; hostIdx < hostsCount; hostIdx++) {
			WorkerResponse workerResponse = workerResponses.getResponseList().get(hostIdx);
			JkStatus status = unmarshalResponse(workerResponse); 
			if (status.getResult().getType().equals("OK")) {
				if(logger.isDebugEnabled()) {
					logger.debug("Worker: '" + worker + "' action OK!");
				}
			} else {
				logger.error("Worker: '" + worker + "' action NOK!");
				throw new ControlCommandException("Worker: '" + worker + "' action failed");
			}
		}

		//
		// wait for x seconds
		try {
			int millis = 3000;
			logger.info("Post Action: Sleeping "+millis+"ms");
			//
			// Sleep for 3 seconds
			Thread.sleep(millis);
		} catch (Exception e) {
			if(logger.isDebugEnabled()) {
				logger.debug("Error when suspending thread: "+e.getMessage());
			}
		}
	}
	/**
	 * Returns the status from supplied response object
	 * @param workerResponse the response object to get status from
	 * @return the status from supplied response object
	 */
	private static JkStatus unmarshalResponse(WorkerResponse workerResponse) {
		return getWorkerStatus(workerResponse.getBody());
		//IWorkerStatus workerStatus = new IWorkerStatus(workerResponse.getBody());
		//return workerStatus.unmarshal(workerResponse.getBody());
	}
	private static JkStatus getWorkerStatus(String body) {
		
		return null;
	}
	/**
	 * Stops the worker for the supplied loadbalancer and returns the list of balancers
	 * @param loadBalancer the loadbalancer
	 * @param worker the worker to stop
	 * @return list of balancers
	 * @throws ControlCommandException 
	 */
	public static ArrayList<JkStatus> stop(String loadBalancer, String worker) {
		String stopParameters = StringUtil.getStopParameters(loadBalancer, worker);
		WorkerResponses workerResponses = HttpClient.executeUrls(_hosts, stopParameters);

		try {
			parseStatusAndPause(workerResponses, worker);
		} catch (ControlCommandException e) {
			//set latest status message
		}

		return statusComplex();
	}

	/**
	 * Stops the worker for the initialized loadbalancer and returns the list of balancers
	 * @param worker the worker to stop
	 * @return list of balancers
	 * @throws ControlCommandException 
	 */
	public static List<JkStatus> stop(String worker) {
		return stop(_loadBalancer, worker);
	}
	/**
	 * Returns the balancers
	 * @return the balancers
	 */
	public static ArrayList<JkStatus> statusComplex() {
		//
		// retrieve the statuses
		WorkerResponses workerResponses = HttpClient.executeUrls(_hosts, StringUtil.getMimeXmlParameters());
		_lastUpdated = Calendar.getInstance();
		return statusComplex(workerResponses);
	}
	/**
	 * Returns a list of balancer for supplied worker responses
	 * @param workerResponse the responses to convert to balancers
	 * @return a list of balancers
	 */
	public static ArrayList<JkStatus> statusComplex(WorkerResponses workerResponses) {
		int workerStatusCount = workerResponses.getResponseList().size();
		ArrayList<JkStatus> resultList = new ArrayList<JkStatus>();
		for (int workerStatusIdx = 0; workerStatusIdx < workerStatusCount; workerStatusIdx++) {
			WorkerResponse workerResponse = workerResponses.getResponseList().get(workerStatusIdx);
			try {
				//
				// get the status
				JkStatus balancer = statusComplex(workerResponse);
				resultList.add(balancer);
			} catch (WorkerNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("Could not get status for ");
			}
		}
		return resultList;
	}
	/**
	 * Returns a balancer for supplied worker response
	 * @param workerResponse the response to convert to a balancer
	 * @return a balancer
	 * @throws WorkerNotFoundException 
	 */
	public static JkStatus statusComplex(WorkerResponse workerResponse) throws WorkerNotFoundException {
		JkStatus jkStatus = unmarshalResponse(workerResponse);
		JkResult result = jkStatus.getResult();
		if(result.getType().equals("NOK")) {
			// TODO: implement StatusNOKException?
			throw new WorkerNotFoundException();
		}
		return jkStatus;
	}
	/**
	 * Returns the status in the supplied format
	 * @param format the format to get status for
	 * @return the status in the supplied format
	 */
	public static WorkerResponses getStatus(String format) {
		String parameters;
		if (format.equals(RESPONSE_FORMAT_PROPERTIES)) {
			parameters = StringUtil.getMimePropertiesParameters();
		} else if (format.equals(RESPONSE_FORMAT_XML)) {
			parameters = StringUtil.getMimeXmlParameters();
		} else if (format.equals(RESPONSE_FORMAT_TEXT)) {
			parameters = StringUtil.getMimeTextParameters();
		} else {
			throw new IllegalArgumentException("Format is not valid: "+format);
		}
		_lastUpdated = Calendar.getInstance();
		return HttpClient.executeUrls(_hosts, parameters);
	}
	/**
	 * Returns the status per host, in the properties format
	 * @return the status per host, in the properties format
	 */
	public WorkerResponses getStatusAsProperties() {
		return getStatus(StringUtil.getMimePropertiesParameters());
	}
	/**
	 * Returns the status per host, in the text format
	 * @return the status per host, in the text format
	 */
	public WorkerResponses getStatusAsText() {
		return getStatus(StringUtil.getMimeTextParameters());
	}

	/**
	 * Returns the status per host, in the xml format
	 * @return the status per host, in the xml format
	 */
	public static WorkerResponses getStatusAsXml() {
		return getStatus(StringUtil.getMimeXmlParameters());
	}
	/**
	 * Returns the hosts container
	 * @return the hosts container
	 */
	public static Hosts getHostsContainer() {
		return _hosts;
	}
	/**
	 * Returns true if one or more hosts added to hosts container, false if not
	 * @return true if one or more hosts added to hosts container, false if not
	 */
	public static Boolean isInitialized() {
		if(_hosts!=null && _hosts.getHostList().size()>0){
			logger.info("initialized: "+_hosts.getHostList().size()+" hosts");
			return true;
		}
		logger.info("WorkerManager not initialized");
		return false;
	}
	/**
	 * Returns all hosts in hosts container
	 * @return all hosts in hosts container
	 */
	public static List<Host> getHosts() {
		if(_hosts!=null && _hosts.getHostList().size()>0){
			logger.info("returning "+_hosts.getHostList().size()+" hosts");
			return _hosts.getHostList();
		} 
		return new ArrayList<Host>(0);
	}
	/**
	 * Returns a list of unique host addresses, or null if no more than one host is found
	 * @param members the members to get hosts from
	 * @return a list of unique host addresses, or null if no more than one host is found
	 */
	private static HashSet<String> getUniqueHosts(List<JkMember> members) {
		HashSet<String> addressSet = new HashSet<String>(); 
		int memberCount = members.size();
		for (int memberIdx = 0; memberIdx < memberCount; memberIdx++) {
			JkMember member = members.get(memberIdx);
			String host = member.getHost();
			if(logger.isDebugEnabled()) {
				logger.debug(member.getName()+" using host: "+host);
			}
			addressSet.add(host);
		}
		if(addressSet.size()>1) {
			return addressSet;
		}
		if(logger.isDebugEnabled()) {
			logger.debug("found no more hosts");
		}
		return null;
	}
	/**
	 * 
	 * @param url
	 * @return
	 * @throws MalformedURLException 
	 */
	private static Host createHost(String url) throws MalformedURLException {
		URL newUrl = new URL(url);
		if(_context==null) {
			_context = newUrl.getPath();
		}
		if(_port==-1 && newUrl.getPort()!=-1) {
			_port = newUrl.getPort();
		}
		if(_protocol==null) {
			_protocol = newUrl.getProtocol();
		}
		return createHost(newUrl);
	}
	/**
	 * 
	 * @param url
	 * @return
	 */
	private static Host createHost(URL url) {
		Host host = new Host();
		host.setIpAddress(url.getHost());
		host.setPort(url.getPort());
		String jkContext = StringUtil.checkPath(url.getPath());
		host.setContext(jkContext);
		return host;
	}
	/**
	 * Returns the current status of the workers.
	 * @return the current status of the workers.
	 */
	public static ArrayList<JkStatus> getStatus() {
		return _currentStatus;
	}
	/**
	 * Returns the current initialized url.
	 * @return the current initialized url.
	 */
	public static String getInitUrl() {
		return _initUrl;
	}	
	/**
	 * Returns the last update timestamp.
	 * @return the last update timestamp.
	 */
	public static Calendar getLastUpdated() {
		return _lastUpdated;
	}	
}

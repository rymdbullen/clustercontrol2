package net.local.clustercontrol.core.logic.impl;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.api.model.xml.WorkerResponse;
import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.http.impl.HttpClient;
import net.local.clustercontrol.core.logic.ControlCommandException;
import net.local.clustercontrol.core.logic.IWorkerFactory;
import net.local.clustercontrol.core.parsers.StatusParserHtml;
import net.local.clustercontrol.core.parsers.StatusParserXml;

/**
 * All four:
 * GET /balancer-manager?lf=1&ls=0&wr=s1&rr=&dw=Enable&w=ajp://localhost:8009&b=cluster&nonce=5210ef6b-5413-4340-b713-e60e31f5dff6 HTTP/1.1
 * GET /balancer-manager?lf=1&ls=0&wr=s1&rr=&dw=Disable&w=ajp://localhost:8009&b=cluster&nonce=5210ef6b-5413-4340-b713-e60e31f5dff6 HTTP/1.1
 * 
 * GET /balancer-manager?lf=2&ls=0&wr=s2&rr=&dw=Enable&w=ajp://localhost:8019&b=cluster&nonce=5210ef6b-5413-4340-b713-e60e31f5dff6 HTTP/1.1
 * GET /balancer-manager?lf=2&ls=0&wr=s2&rr=&dw=Disable&w=ajp://localhost:8019&b=cluster&nonce=5210ef6b-5413-4340-b713-e60e31f5dff6 HTTP/1.1
 * 
 */
@Component
public class WorkerFactory implements IWorkerFactory {

	private static final Logger logger = LoggerFactory.getLogger(WorkerFactory.class);

	private static final String REPLACEMENT = "@@replacement@@";
//	private static final String SPEED_MEDIUM = "medium";
//	private static final String SPEED_SLOW = "slow";
//	private static final String SPEED_FAST = "fast";

	
	/** the http client that performs the http get */
	private IHttpClient httpClient;
	/** the initial body, hostname replaced with placeholder */
	private String _url;
	/** Contains the current view of all hosts statusesPerHost, one status per host */ 
	private HashMap<String, JkStatus> _statuses;

	/**
	 * Constructor with autowired http client
	 * @param httpClient
	 */
	@Autowired
	public WorkerFactory(HttpClient httpClient) {
		this.httpClient = httpClient;
	}
	/**
	 * Constructor
	 * @param httpClient
	 * @param body
	 */
	public WorkerFactory(HttpClient httpClient, String url) {
		this._url = url;
		_statuses = new HashMap<String, JkStatus>(); 
	}

	@Override
	public HashMap<String, JkStatus> getStatuses() {
		return _statuses;
	}
	
	@Override
	public boolean init(String url) throws MalformedURLException, UnknownHostException {
		return init(url, null, "poll", null);
	}
	
	@Override
	public boolean init(String url, String workerName, String action, String speed) throws MalformedURLException, UnknownHostException {
		logger.info("Initializing with body: "+url);
		
		WorkerResponse response = getStatusForUrl(url);
		if(response.getError()!=null) {
			logger.error(response.getError().getMessage());
			
		}
		JkStatus jkStatus = getJkStatus(response);
		
		if(jkStatus==null) {
			return false;
		}
		this._url = url.replaceAll(jkStatus.getServer().getName(), REPLACEMENT);
		
		ArrayList<String> urls = initUrlsFromJkStatus(jkStatus);
		
		return getAllStatuses(urls, null);
	}
	@Override
	public boolean performAction(String workerName, String action, String speed) throws MalformedURLException, UnknownHostException {
		ArrayList<String> urls = createUrls(workerName, action);
		return getAllStatuses(urls, speed);
	}
	/**
	 * 
	 * @param jkStatus
	 * @return
	 */
	ArrayList<String> initUrlsFromJkStatus(JkStatus jkStatus) {
		int memberCount = jkStatus.getBalancers().getBalancer().getMemberCount();
		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < memberCount; i++) {
			JkMember jkMember = jkStatus.getBalancers().getBalancer().getMember().get(i);
			if(jkStatus.getServer().getName().equals(jkMember.getHost())) {
				// this host is already taken care of
				continue;
			}

			String url = _url.replaceAll(REPLACEMENT, jkMember.getHost());
			
			set.add(url);
		}
		return new ArrayList<String>(set);
	}
	/**
	 * 
	 * @param urls
	 * @param speed
	 * @return
	 * @throws UnknownHostException 
	 * @throws MalformedURLException 
	 */
	private boolean getAllStatuses(ArrayList<String> urls, String speed) throws MalformedURLException, UnknownHostException {
		for (int i = 0; i < urls.size(); i++) {
			String url = urls.get(i);
			delay(i, speed);
			WorkerResponse response = getStatusForUrl(url);
			if(response.getError()!=null) {
				logger.error(response.getError().getMessage());
			}
			getJkStatus(response);
		}
		return true;
	}
	/**
	 * 
	 * @param jkStatus
	 * @param workerName
	 * @param action
	 * @return
	 */
	ArrayList<String> createUrls(String workerName, String action) {
		if(_statuses == null || _statuses.size() == 0) {
			throw new IllegalArgumentException("Cluster statusesPerHost not initialized");
		}
		
		ArrayList<String> urls = new ArrayList<String>();
		for (String host : _statuses.keySet()) {
			JkStatus jkStatus = _statuses.get(host);
			if(action.equals("poll")) {
				return initUrlsFromJkStatus(jkStatus);
			}
			urls.add(createUrl(jkStatus, workerName, action));
		}
		
		return urls;
	}
	/**
	 * 
	 * @param jkStatus
	 * @param workerName
	 * @param action
	 * @return
	 */
	String createUrl(JkStatus jkStatus, String workerName, String action) {
		
		String initialHostName = jkStatus.getServer().getName();
		Integer initialHostPort = jkStatus.getServer().getPort();
		int memberCount = jkStatus.getBalancers().getBalancer().getMemberCount();
		for (int i = 0; i < memberCount; i++) {
			JkMember jkMember = jkStatus.getBalancers().getBalancer().getMember().get(i);
			if(logger.isTraceEnabled()) { logger.trace(""+action+": "+initialHostName+": "+initialHostPort+": "+jkMember.getName()+" "+jkMember.getActivation()+" "+jkMember.getType()); }
			
			if(false == jkMember.getName().equals(workerName)) {
				continue;
			}
			
			String newUrl = createUrl(jkMember, initialHostName, workerName, action);
			if(logger.isDebugEnabled()) { logger.debug(newUrl); }
			return newUrl;
		}
		if(workerName !=null) {
			throw new IllegalArgumentException("Failed to find body for worker: "+workerName);
		}
		return null;
	}
	/**
	 * Creates a complete body for the wanted action
	 * @param jkMember
	 * @param workerName
	 * @param action
	 * @return
	 */
	String createUrl(JkMember jkMember, String host, String workerName, String action) {
		String newUrl = _url.replaceAll(REPLACEMENT, host);
		if(action.equals("poll")) {
			return newUrl;
		}
		
		// FIXME this is only for proxy_balancer_ajp
		String lf = ""+jkMember.getLbfactor();
		String ls = ""+jkMember.getRead();  //member.setRead(Integer.valueOf(matcher.group(_5SET).trim()));
		String to = ""+jkMember.getBusy();  //member.setBusy(Integer.valueOf(matcher.group(_8TO).trim()));
		String wr = jkMember.getRoute();
		String actionContext = "&lf="+lf+"&ls="+ls+"&wr="+wr+"&rr=&dw="+action;
		if(logger.isTraceEnabled()) { logger.trace(""+lf+" : "+ls+" : "+to+" : "+jkMember.getType()); }
		if(logger.isDebugEnabled()) { logger.debug("Context: "+actionContext); }
		
		String newContext = getContext(jkMember);
		
		if(workerName.indexOf('/') > 0 || 
				workerName.indexOf(':') > 0) {
			try {
				workerName = URLEncoder.encode(workerName,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error(workerName, e);
			}
		}
		
		String context = newContext.replaceAll(REPLACEMENT, workerName);
		newUrl = newUrl + "?" + actionContext + "&" + context; 
		return newUrl;
	}
	
	private String getInitHost(String initUrl) {
		int beginIndex = initUrl.indexOf("://");
		int endIndex = initUrl.indexOf("/", beginIndex+3);
		if(endIndex==-1) {
			return initUrl.substring(beginIndex+3);			
		} else {
			return initUrl.substring(beginIndex+3, endIndex);
		}
	}

	private String getContext(JkMember jkMember) {
		String context = jkMember.getType().replaceAll(jkMember.getName(), REPLACEMENT);
		int beginIndex = context.indexOf("?");
		if(beginIndex > 0) {
			return context.substring(beginIndex+1);
		}
		return null;
	}

	private WorkerResponse getStatusForUrl(String url) throws MalformedURLException, UnknownHostException {
		if(false==url.startsWith("http")) {
			url = "http://"+url;
		}
		
		URL properUrl = new URL(url);
		String workerAddress = InetAddress.getByName(properUrl.getHost()).getHostAddress();
		if(logger.isDebugEnabled()) { logger.debug("properUrl.getHost(): " + properUrl.getHost() + " ip: " +workerAddress); }

		return httpClient.getWorkerResponseForUrl(url);
	}
	private JkStatus getJkStatus(WorkerResponse workerResponse) {
		JkStatus jkStatus = parseWorkerStatus(workerResponse.getBody());
		if(jkStatus	== null) {
			return null;
		}
		if(_statuses == null) {
			_statuses = new HashMap<String, JkStatus>();
		} else {
			if(_statuses.containsKey(jkStatus.getServer().getName())) {
				_statuses.remove(jkStatus.getServer().getName());
			}
		}
		_statuses.put(jkStatus.getServer().getName(), jkStatus);

		return jkStatus;
	}

	/**
	 * 
	 * @param statusBody
	 * @return
	 */
	private JkStatus parseWorkerStatus(String statusBody) {
		
		// 1. Try with ajp host
		StatusParserXml statusParserXml = new StatusParserXml(statusBody); 
		JkStatus jkStatus = statusParserXml.getStatus();
		if(jkStatus != null) {
			return jkStatus;
		}
		
		// 2. Try with html host
		StatusParserHtml statusParserHtml = new StatusParserHtml(statusBody);
		jkStatus = statusParserHtml.getStatus();
		if(jkStatus != null) {
			return jkStatus;
		}
		
		return null;
	}
	/**
	 * Delays the current thread
	 * @param i
	 * @param speed
	 */
	private void delay(int i, String speed) {
		if(speed==null) {
			return;
		}
		int millis = 3000;
    	try {
			long incrementalMillis = millis*i;
			logger.trace("Sleeping between requests: "+incrementalMillis+"ms"); // TODO add thread name
			Thread.sleep(incrementalMillis);
		} catch (InterruptedException e) {
			throw new ControlCommandException("Failed to sleep thread for: "+millis+" millis", e);
		}
	}
}

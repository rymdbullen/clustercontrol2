package net.local.clustercontrol.core.logic.impl;

import java.util.Iterator;
import java.util.List;

import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.logic.IWorkerHandler;
import net.local.clustercontrol.core.parsers.IStatusParser;
import net.local.clustercontrol.core.parsers.StatusParserHtml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class parsing the following html status <td><a href=
 * "/balancer-manager?b=cluster&w=ajp://localhost:8009&nonce=3af62151-30da-4ea5-85fc-eb3c7c37d564"
 * >ajp://localhost:8009</a></td><td>s1</td><td></td><td>1</td><td>0</td><td>Ok</td>
 * <td>0</td><td>0</td><td>0</td>
 * 
 * @author jstenvall
 */
public class WorkerHandlerHtml extends AbstractWorkerHandler implements IWorkerHandler {

	private static final Logger logger = LoggerFactory.getLogger(WorkerHandlerHtml.class);
	private static final String type = "html";

	public WorkerHandlerHtml(IHttpClient httpClient, String body, String initUrl) {
		super(httpClient, body, initUrl);
	}

	@Override
	protected String getType() {
		return type;
	}

	@Override
	public IStatusParser handleInit() {
		// parse body
		return new StatusParserHtml(this.body);
	}

	@Override
	public void handlePoll() {
		for (String url : urls) {
			getBody(url);
			StatusParserHtml status = new StatusParserHtml(body);
			boolean notInit = false;
			if(notInit && statusesPerHost.get(url) != null) {
				if(logger.isTraceEnabled()) logger.trace("url already added: "+url);
				continue;
			}
			statusesPerHost.put(url, status.getStatus());
		}
	}

	@Override
	public void handleStart(String workerId, String speed) {
		String action = "start";
		Iterator<String> urls = statusesPerHost.keySet().iterator();
		while (urls.hasNext()) {
			String url = (String) urls.next();
			String host = "";
			// create url for this workerId
			JkMember jkMember = getMember(statusesPerHost.get(url), workerId, host);
			url = createUrl(url, action, jkMember, workerId);
			getBody(url);
			if(body != null && body.trim().length() > 0) {
				if(logger.isTraceEnabled()) logger.trace("url already added: "+url);				
			}
		}
		handlePoll();
	}
	
	@Override
	public void handleStop(String workerId, String speed) {
		if(speed==null) {
			
		}
		for (JkStatus status : statusesPerHost.values()) {
			System.out.println(status.getBalancers().getBalancer().getMemberCount());
			List<JkMember> members = status.getBalancers().getBalancer().getMember();
			for (JkMember member : members) {
				System.out.println(member.getName());
				if(member.getName().equals(workerId)) {
					// we want this one
					logger.debug(workerId+" "+speed);
					logger.debug(createUrl(speed, "stop", member, workerId));
				}
			}
		}
	}
	
	@Override
	public String createUrl(String url, String action, JkMember jkMember, String workerId) {
		String newUrl;// = url.replaceAll(REPLACEMENT, host);
		
		String lf = ""+jkMember.getLbfactor();
		String ls = ""+jkMember.getRead();  //member.setRead(Integer.valueOf(matcher.group(_5SET).trim()));
		String to = ""+jkMember.getBusy();  //member.setBusy(Integer.valueOf(matcher.group(_8TO).trim()));
		String wr = jkMember.getRoute();
		String actionContext = "&lf="+lf+"&ls="+ls+"&wr="+wr+"&rr=&dw="+action;
		if(logger.isTraceEnabled()) { logger.trace(""+lf+" : "+ls+" : "+to+" : "+jkMember.getType()); }
		if(logger.isDebugEnabled()) { logger.debug("Context: "+actionContext); }
		
		String newContext = getContext(jkMember);
		
//		if(workerId.indexOf('/') > 0 || 
//				workerId.indexOf(':') > 0) {
//			try {
//				workerId = URLEncoder.encode(workerId,"UTF-8");
//			} catch (UnsupportedEncodingException e) {
//				logger.error(workerId, e);
//			}
//		}
		
		String context = newContext.replaceAll(REPLACEMENT, workerId);
		newUrl = "?" + actionContext + "&" + context; 
		return newUrl;
	}
	
	private JkMember getMember(JkStatus jkStatus, String workerId, String host) {
		int memberCount = jkStatus.getBalancers().getBalancer().getMemberCount();
		for (int i = 0; i < memberCount; i++) {
			JkMember jkMember = jkStatus.getBalancers().getBalancer().getMember().get(i);
			if(jkMember.getAddress().equals(host) &&
					jkMember.getName().equals(workerId)) {
				return jkMember;
			}
		}
		return null;
	}

	private String getContext(JkMember jkMember) {
		String context = jkMember.getType().replaceAll(jkMember.getName(), REPLACEMENT);
		int beginIndex = context.indexOf("?");
		if(beginIndex > 0) {
			return context.substring(beginIndex+1);
		}
		return null;
	}
}
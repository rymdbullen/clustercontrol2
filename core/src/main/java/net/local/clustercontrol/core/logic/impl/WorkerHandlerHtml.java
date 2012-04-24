package net.local.clustercontrol.core.logic.impl;

import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.logic.IWorkerHandler;
import net.local.clustercontrol.core.parsers.IStatusParser;
import net.local.clustercontrol.core.parsers.StatusParserHtml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class parsing the following html status 
 * <td><a href="/balancer-manager?b=cluster&w=ajp://localhost:8009&nonce=3af62151-30da-4ea5-85fc-eb3c7c37d564"
 * >ajp://localhost:8009</a></td><td>s1</td><td></td><td>1</td><td>0</td><td>Ok</td> * <td>0</td><td>0</td><td>0</td>
 * 
 * All four:
 * GET /balancer-manager?lf=1&ls=0&wr=s1&rr=&dw=Enable&w=ajp://localhost:8009&b=cluster&nonce=5210ef6b-5413-4340-b713-e60e31f5dff6 HTTP/1.1
 * GET /balancer-manager?lf=1&ls=0&wr=s1&rr=&dw=Disable&w=ajp://localhost:8009&b=cluster&nonce=5210ef6b-5413-4340-b713-e60e31f5dff6 HTTP/1.1
 * 
 * GET /balancer-manager?lf=2&ls=0&wr=s2&rr=&dw=Enable&w=ajp://localhost:8019&b=cluster&nonce=5210ef6b-5413-4340-b713-e60e31f5dff6 HTTP/1.1
 * GET /balancer-manager?lf=2&ls=0&wr=s2&rr=&dw=Disable&w=ajp://localhost:8019&b=cluster&nonce=5210ef6b-5413-4340-b713-e60e31f5dff6 HTTP/1.1
 * 
 * @author jstenvall
 */
public class WorkerHandlerHtml extends AbstractWorkerHandler implements IWorkerHandler {

	private static final Logger logger = LoggerFactory.getLogger(WorkerHandlerHtml.class);
	private static final String type = "html";

	public WorkerHandlerHtml() {}
	public WorkerHandlerHtml(IHttpClient httpClient, String url) {
		super(httpClient, url);
	}

	@Override
	protected String getType() {
		return type;
	}

	@Override
	public IStatusParser handleInit() {
		// parse body
		return new StatusParserHtml(this.body, this.initUrl);
	}

	@Override
	public void handlePoll() {
		for (String url : this.urls) {
			handleUrl(url, "");
		}
	}

	@Override
	public void handleUrl(String url, String context) {
		getBody(url+context);
		StatusParserHtml status = new StatusParserHtml(this.body, url);
		statusesPerHost.put(url, status.getStatus());
	}

	@Override
	public void handleStart(String workerId, String speed) {
		String action = "Enable";
		action(workerId, speed, action);
	}
	
	@Override
	public void handleStop(String workerId, String speed) {
		if(speed==null) {
			// stop-fastest-possible case
			
		}
		String action = "Disable";
		action(workerId, speed, action);
	}
	
	@Override
	public String createUrl(String action, JkMember jkMember, String workerId) {
		String newUrl;// = url.replaceAll(REPLACEMENT, host);
		
		String lf = ""+jkMember.getLbfactor();
		String ls = ""+jkMember.getRead();  //member.setRead(Integer.valueOf(matcher.group(_5SET).trim()));
		String wr = jkMember.getRoute();
		String actionContext = "lf="+lf+"&ls="+ls+"&wr="+wr+"&rr=&dw="+action;
		
		String basicContext = getContext(jkMember);
		if(logger.isTraceEnabled()) { logger.trace("Context: [{}] basicContext: [{}]", actionContext, basicContext); }
		
		String context = basicContext.replaceAll(REPLACEMENT, workerId);
		newUrl = "?" + actionContext + "&" + context;
		
		if(logger.isTraceEnabled()) { logger.trace("Created new action URL: {}", newUrl); }
		return newUrl;
	}
	
	private String getContext(JkMember jkMember) {
		String context = jkMember.getType().replaceAll(jkMember.getName(), REPLACEMENT);
		int beginIndex = context.indexOf("?");
		if(beginIndex > 0) {
			return context.substring(beginIndex + 1);
		}
		return null;
	}
}
package net.local.clustercontrol.core.logic.impl;

import java.util.Iterator;

import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.http.impl.HttpClient;
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

	@SuppressWarnings("unused")
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
			if(notInit && perHostStatuses.get(url) != null) {
				System.out.println("url already added: "+url);
				continue;
			}
			perHostStatuses.put(url, status.getStatus());
		}
	}

	@Override
	public void handleStart(String workerId) {
		String action = "start";
		Iterator<String> urls = perHostStatuses.keySet().iterator();
		while (urls.hasNext()) {
			String url = (String) urls.next();
			String host = "";
			// create url for this workerId
			JkMember jkMember = getMember(perHostStatuses.get(url), workerId, host);
			url = createUrl(url, action, jkMember, workerId);
			getBody(url);
			if(body != null && body.trim().length() > 0) {
				System.out.println("url already added: "+url);				
			}
		}
		handlePoll();
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

	@Override
	public String createUrl(String url, String action, JkMember jkMember, String workerId) {
//		String newUrl = url.replaceAll(REPLACEMENT, host);
//		
//		jkStatus.getBalancers().getBalancer().getMemberCount();
//		
//		String lf = ""+jkMember.getLbfactor();
//		String ls = ""+jkMember.getRead();  //member.setRead(Integer.valueOf(matcher.group(_5SET).trim()));
//		String to = ""+jkMember.getBusy();  //member.setBusy(Integer.valueOf(matcher.group(_8TO).trim()));
//		String wr = jkMember.getRoute();
//		String actionContext = "&lf="+lf+"&ls="+ls+"&wr="+wr+"&rr=&dw="+action;
//		if(logger.isTraceEnabled()) { logger.trace(""+lf+" : "+ls+" : "+to+" : "+jkMember.getType()); }
//		if(logger.isDebugEnabled()) { logger.debug("Context: "+actionContext); }
//		
//		String newContext = getContext(jkMember);
//		
//		if(workerName.indexOf('/') > 0 || 
//				workerName.indexOf(':') > 0) {
//			try {
//				workerName = URLEncoder.encode(workerName,"UTF-8");
//			} catch (UnsupportedEncodingException e) {
//				logger.error(workerName, e);
//			}
//		}
//		
//		String context = newContext.replaceAll(REPLACEMENT, workerId);
//		newUrl = newUrl + "?" + actionContext + "&" + context; 
//		return newUrl;
		return null;
	}

	@Override
	public void handleStop(String workerId) {

	}

}
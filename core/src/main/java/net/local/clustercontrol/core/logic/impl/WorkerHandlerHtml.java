package net.local.clustercontrol.core.logic.impl;

import net.local.clustercontrol.core.logic.IWorkerHandler;
import net.local.clustercontrol.core.parsers.StatusParserHtml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class parsing the following html status 
 * <td><a href="/balancer-manager?b=cluster&w=ajp://localhost:8009&nonce=3af62151-30da-4ea5-85fc-eb3c7c37d564">ajp://localhost:8009</a></td><td>s1</td><td></td><td>1</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td>
 * 
 * @author jstenvall
 */
public class WorkerHandlerHtml extends AbstractWorkerHandler implements IWorkerHandler {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(WorkerHandlerHtml.class);
	private static final String type = "html";

	public WorkerHandlerHtml(String url) {
		super(url);
		StatusParserHtml status = new StatusParserHtml(body);
		if(status == null || status.getStatus() == null) {
			return;
		}
		init(status.getStatus());
		extractUrls();
	}

	@Override
	protected String getType() {
		return type;
	}
	
	@Override
	public void handleInit() {
		// parse url
//		String body = getBody(url);
//		StatusParserHtml status = new StatusParserHtml(body);
	}

	@Override
	protected void extractUrls() {
		
	}

}
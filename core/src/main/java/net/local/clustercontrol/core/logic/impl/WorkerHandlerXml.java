package net.local.clustercontrol.core.logic.impl;

import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.logic.IWorkerHandler;
import net.local.clustercontrol.core.parsers.IStatusParser;
import net.local.clustercontrol.core.parsers.StatusParserXml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WorkerHandlerXml extends AbstractWorkerHandler implements IWorkerHandler {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(WorkerHandlerXml.class);
	private static final String type = "xml";

	public WorkerHandlerXml(IHttpClient httpClient, String body, String initUrl) {
		super(httpClient, body, initUrl);
	}
	
	@Override
	protected String getType() {
		return type;
	}

	@Override
	public IStatusParser handleInit() {
		// parse body
		return new StatusParserXml(this.body);
	}
	
	@Override
	public void handlePoll() {
		
	}
	
	@Override
	public void handleStart(String workerId, String speed) {
		
	}
	
	@Override
	public void handleStop(String workerId, String speed) {
		
	}

	@Override
	public String createUrl(String url, String action, JkMember jkMember, String workerId) {
		return null;
	}
}
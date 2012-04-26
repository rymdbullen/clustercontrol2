package net.local.clustercontrol.core.logic.impl;

import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.logic.IWorkerHandler;
import net.local.clustercontrol.core.parsers.IStatusParser;
import net.local.clustercontrol.core.parsers.StatusParserXml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerHandlerXml extends AbstractWorkerHandler implements IWorkerHandler {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(WorkerHandlerXml.class);
	private static final String type = "xml";

	public WorkerHandlerXml() {}
	public WorkerHandlerXml(IHttpClient httpClient, String url) {
		super(httpClient, url);
	}
	
	@Override
	protected String getType() {
		return type;
	}

	@Override
	public IStatusParser handleInit() {
		// parse body
		return new StatusParserXml(this.body, initUrl);
	}
	
	@Override
	public void handlePoll() {
		throw new RuntimeException("Not implemented");
	}
	
	@Override
	public void handleUrl(String url, String context) {
		throw new RuntimeException("Not implemented");
	}
	
	@Override
	public void handleStart(String workerId, String speed) {
		throw new RuntimeException("Not implemented");
	}
	
	@Override
	public void handleStop(String workerId, String speed) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String createUrl(String action, JkMember jkMember, String workerId) {
		throw new RuntimeException("Not implemented");
	}
}
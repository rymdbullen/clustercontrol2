package net.local.clustercontrol.core.logic.impl;

import net.local.clustercontrol.core.logic.IWorkerHandler;
import net.local.clustercontrol.core.parsers.StatusParserXml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerHandlerXml extends AbstractWorkerHandler implements IWorkerHandler {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(WorkerHandlerXml.class);
	private static final String type = "xml";

	public WorkerHandlerXml(String url) {
		super(url);
		localInit();
	}
	
	void localInit() {
		StatusParserXml status = new StatusParserXml(body);
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
		
	}

	@Override
	protected void extractUrls() {
		
	}
}
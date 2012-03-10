package net.local.clustercontrol.core.logic.impl;

import org.springframework.stereotype.Component;

import net.local.clustercontrol.core.logic.IWorkerHandler;
import net.local.clustercontrol.core.logic.IWorkerHandlerFactory;

@Component
public class WorkerHandlerFactory implements IWorkerHandlerFactory {
	
	@Override
	public IWorkerHandler getHandler(String initUrl) {
		// 1. Try with ajp host
		WorkerHandlerXml workerHandlerXml = new WorkerHandlerXml(initUrl);
		if(workerHandlerXml.getPerHostStatuses() != null) {
			return workerHandlerXml;
		}
		
		// 2. Try with html host
		WorkerHandlerHtml workerHandlerHtml = new WorkerHandlerHtml(initUrl);
		if(workerHandlerHtml.getPerHostStatuses() != null) {
			return workerHandlerHtml;
		}
		
		throw new UnsupportedOperationException("This type of request not yet handle");

	}
}

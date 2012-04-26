package net.local.clustercontrol.core.logic.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.logic.IWorkerHandler;
import net.local.clustercontrol.core.logic.IWorkerHandlerFactory;

@Service
public class WorkerHandlerFactory implements IWorkerHandlerFactory {

	
	// TODO Remove httpClient from WorkerHandlerFactory 
	// TODO Remove httpClient from WorkerHandlerFactory 
	// TODO Remove httpClient from WorkerHandlerFactory 
	@Autowired private IHttpClient httpClient;

	@Override
	public IWorkerHandler getHandler(String initUrl) {
		
		// 1. Try with ajp host
		WorkerHandlerXml workerHandlerXml = new WorkerHandlerXml(httpClient, initUrl);
		if(workerHandlerXml.getStatusesPerHost() != null) {
			return workerHandlerXml;
		}
		
		// 2. Try with html host
		WorkerHandlerHtml workerHandlerHtml = new WorkerHandlerHtml(httpClient, initUrl);
		if(workerHandlerHtml.getStatusesPerHost() != null) {
			return workerHandlerHtml;
		}
		
		throw new UnsupportedOperationException("Failed to handle this type of request for url: " + initUrl);

	}
}

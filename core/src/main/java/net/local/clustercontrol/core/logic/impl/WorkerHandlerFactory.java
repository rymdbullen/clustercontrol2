package net.local.clustercontrol.core.logic.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.local.clustercontrol.api.model.xml.WorkerResponse;
import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.logic.IWorkerHandler;
import net.local.clustercontrol.core.logic.IWorkerHandlerFactory;

@Service
public class WorkerHandlerFactory implements IWorkerHandlerFactory {
	
	@Autowired private IHttpClient httpClient;

	@Override
	public IWorkerHandler getHandler(String initUrl) {
		
		// get the body for further processing
		String body = getBody(initUrl);
		
		// 1. Try with ajp host
		WorkerHandlerXml workerHandlerXml = new WorkerHandlerXml(httpClient, body, initUrl);
		if(workerHandlerXml.getPerHostStatuses() != null) {
			return workerHandlerXml;
		}
		
		// 2. Try with html host
		WorkerHandlerHtml workerHandlerHtml = new WorkerHandlerHtml(httpClient, body, initUrl);
		if(workerHandlerHtml.getPerHostStatuses() != null) {
			return workerHandlerHtml;
		}
		
		throw new UnsupportedOperationException("Failed to handle this type of request for url: " + initUrl);

	}
	private String getBody(String initUrl) {
		WorkerResponse wr = httpClient.getWorkerResponseForUrl(initUrl);
		if(wr.getError() != null) {
			// throw error
			throw new RuntimeException("Failed to get response using URL: "+initUrl);
		}
		return wr.getBody();
	}
}

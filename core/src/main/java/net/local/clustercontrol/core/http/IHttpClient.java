package net.local.clustercontrol.core.http;

import net.local.clustercontrol.api.model.xml.WorkerResponse;

public interface IHttpClient {
	/**
	 * Returns the worker response for provided url
	 * @param url
	 * @return  the worker response for provided url
	 */
	WorkerResponse getWorkerResponseForUrl(String url);
}

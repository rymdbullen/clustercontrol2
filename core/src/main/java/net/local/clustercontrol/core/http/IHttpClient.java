package net.local.clustercontrol.core.http;

import net.local.clustercontrol.api.model.xml.WorkerResponse;

public interface IHttpClient {
	/**
	 * Returns the worker response for provided body
	 * @param body
	 * @return  the worker response for provided body
	 */
	WorkerResponse getWorkerResponseForUrl(String url);
}

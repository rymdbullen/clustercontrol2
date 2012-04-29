package net.local.clustercontrol.core.http;

import java.io.Serializable;

import net.local.clustercontrol.api.model.xml.WorkerResponse;

public interface IHttpClient extends Serializable {
	/**
	 * Returns the worker response for provided body
	 * @param body
	 * @return  the worker response for provided body
	 */
	WorkerResponse getWorkerResponseForUrl(String url);
}

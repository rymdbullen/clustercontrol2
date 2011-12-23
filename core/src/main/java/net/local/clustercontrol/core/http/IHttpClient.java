package net.local.clustercontrol.core.http;

import net.local.clustercontrol.api.model.xml.WorkerResponse;
import net.local.clustercontrol.api.model.xml.WorkerResponses;
import net.local.clustercontrol.core.model.dto.Cluster;

public interface IHttpClient {
	/**
	 * Returns all hosts workers responses for provided url, ie html bodys and eventual errors
	 * @param cluster the cluster to get hosts from and to update
	 * @return all hosts workers responses for provided url
	 */
	WorkerResponses getWorkerResponseForAction(Cluster cluster);
	/**
	 * Returns the worker response for provided url
	 * @param url
	 * @return  the worker response for provided url
	 */
	WorkerResponse getWorkerResponseForUrl(String url);
}

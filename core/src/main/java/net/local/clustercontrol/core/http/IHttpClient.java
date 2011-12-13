package net.local.clustercontrol.core.http;

import net.local.clustercontrol.api.model.xml.Hosts;
import net.local.clustercontrol.api.model.xml.WorkerResponses;

public interface IHttpClient {
	public WorkerResponses performActionOnHosts(Hosts hosts);
}

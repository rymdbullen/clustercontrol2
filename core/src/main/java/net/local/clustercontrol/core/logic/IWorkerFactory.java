package net.local.clustercontrol.core.logic;

import java.util.ArrayList;

import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.core.model.dto.Workers;

public interface IWorkerFactory {
	/**
	 * Returns the status for one host specified by the provided url
	 * @param url the url to get status for
	 * @return one host status
	 */
	ArrayList<Workers> getWorkersForUrl(String url);
	/**
	 * Returns a list of all statuses for all hosts specified in the provided status
	 * @param jkStatus the status to get all hosts statuses for
	 * @return a list of all statuses for all hosts
	 */
	Cluster getAllStatuses(JkStatus jkStatus);
}

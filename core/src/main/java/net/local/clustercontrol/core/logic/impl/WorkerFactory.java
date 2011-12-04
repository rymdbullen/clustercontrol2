package net.local.clustercontrol.core.logic.impl;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.local.clustercontrol.api.model.JkStatus;
import net.local.clustercontrol.core.logic.IWorkerFactory;

public class WorkerFactory implements IWorkerFactory {

	private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);
	
	@Override
	public JkStatus getStatusForUrl(String url) {
		logger.info("Initializing with url: "+url);
		if(false==url.startsWith("http")) {
			url = "http://"+url;
		}

		return null;
	}

	@Override
	public ArrayList<JkStatus> getMultipleStatusForStatus(JkStatus jkStatus) {
		// TODO Auto-generated method stub
		return null;
	}

}

package net.local.clustercontrol.core.configuration;

import net.local.clustercontrol.core.logic.IClusterManager;
import net.local.clustercontrol.core.logic.IWorkerHandlerFactory;

public interface ServiceConfig {

	IClusterManager clusterManager();

	IWorkerHandlerFactory workerHandlerFactory();

}

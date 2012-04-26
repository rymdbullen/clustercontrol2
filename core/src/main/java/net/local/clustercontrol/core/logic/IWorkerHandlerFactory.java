package net.local.clustercontrol.core.logic;



public interface IWorkerHandlerFactory {
	IWorkerHandler getHandler(String url);

}

package net.local.clustercontrol.core.logic;

import java.io.Serializable;



public interface IWorkerHandlerFactory extends Serializable {
	IWorkerHandler getHandler(String url);

}

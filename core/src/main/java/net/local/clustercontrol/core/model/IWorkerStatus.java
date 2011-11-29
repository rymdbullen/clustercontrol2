package net.local.clustercontrol.core.model;

import net.local.clustercontrol.api.model.JkStatus;

public abstract class IWorkerStatus {
	public JkStatus jkStatus = null;
	
	public JkStatus getStatus() {
		return jkStatus;
	}
}

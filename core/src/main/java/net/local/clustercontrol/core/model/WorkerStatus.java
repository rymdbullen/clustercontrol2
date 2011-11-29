package net.local.clustercontrol.core.model;

import net.local.clustercontrol.api.model.JkStatus;

public class WorkerStatus {
	private JkStatus jkStatus;

	public JkStatus getJkStatus() {
		return jkStatus;
	}

	public void setJkStatus(JkStatus jkStatus) {
		this.jkStatus = jkStatus;
	}
	
}

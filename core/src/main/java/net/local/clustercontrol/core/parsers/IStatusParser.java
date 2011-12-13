package net.local.clustercontrol.core.parsers;

import net.local.clustercontrol.api.model.xml.JkStatus;

public abstract class IStatusParser implements IWorker {
	public String hostName;
	public JkStatus jkStatus = null;
	
	public JkStatus getStatus() {
		return jkStatus;
	}	
}

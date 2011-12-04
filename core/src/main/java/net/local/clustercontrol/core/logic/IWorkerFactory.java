package net.local.clustercontrol.core.logic;

import java.util.ArrayList;

import net.local.clustercontrol.api.model.JkStatus;

public interface IWorkerFactory {
	JkStatus getStatusForUrl(String url);
	ArrayList<JkStatus> getMultipleStatusForStatus(JkStatus jkStatus);
}

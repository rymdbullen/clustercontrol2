package net.local.clustercontrol.core.parsers;

public interface IWorker {
	String getEnableUrl(String workerName);
	String getDisableUrl(String workerName);
//	JkStatus stop(String workername);
//	JkStatus enable(String workername);
//	JkStatus disable(String workername);
}

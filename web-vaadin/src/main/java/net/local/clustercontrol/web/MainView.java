package net.local.clustercontrol.web;

import java.util.ArrayList;

import net.local.clustercontrol.core.logic.IClusterManager;
import net.local.clustercontrol.core.model.dto.WorkerStatus;
import net.local.clustercontrol.core.model.dto.Workers;


import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;

public class MainView extends CustomComponent {

	private static final long serialVersionUID = 3140593056123673046L;
	
	// ui variables
	private TextField speed = new TextField("Speed");
	private CheckBox chbAutoRefresh = new CheckBox("Update");        // Toggle Auto Refresh
	private Button button = new Button("Update");                    // Manual Refresh
	private TextField fieldLastAction = new TextField("LastAction"); // text describing the last action 
	private TextField fieldLastUpdate = new TextField("LastUpdate"); // timestamp for last update
	private IClusterManager clusterManager;
	
	public MainView(IClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}
	public void init() {
		GridLayout layout = new GridLayout();
		setCompositionRoot(layout);
		layout.addComponent(fieldLastAction);
		layout.addComponent(fieldLastUpdate);
		
		if(clusterManager.getCluster()==null) {
			return;
		}
		clusterManager.getCluster().getHostNames();
		clusterManager.getCluster().getWorkerNames();
		clusterManager.getCluster().getLastPoll();

/*
		<c:forEach items="${cluster.workers}" var="worker">
		<div id="col1" class="hostName"><c:out value="${worker.name}"/></div><c:forEach items="${worker.statuses}" var="status">
		<div class="status status-${status.id} status-${status.status}" id="stat" title="${status.hostName}, ${status.route}, ${status.loadFactor}, ${status.transferred}"><c:out value="${status.status}"/></div></c:forEach>
		<div class="status btn-${worker.id}" id="stat-btn">
			<input id="${worker.id}-disable" class="${worker.id}-disable" type="button" value="Disable" onclick="confirmAction('disable','${worker.id}','${worker.name}')" disabled />
		</div>
		<div class="status btn-${worker.id}" id="stat-btn">
			<input id="${worker.id}-enable" class="${worker.id}-enable" type="button" value="Enable" onclick="confirmAction('enable','${worker.id}','${worker.name}')" disabled />
		</div>
		<div style="clear: both;"></div></c:forEach>
*/
		
		
		// hoover value: ${status.hostName}, ${status.route}, ${status.loadFactor}, ${status.transferred}
		// id of each div status-${status.id} status-${status.status}

		// ITERATION 1
		//      col1         col2          col3         ...  colX           Disable    Enable
		// row1 worker.name  row2 status1  row2 status1 ...  row2 statusX   btnDisable btnEnable
		// 

		// ITERATION 2
		//      col1         col2          col3         ...  colX           Disable    Enable
		// row2 worker.name  row2 status1  row2 status1 ...  row2 statusX   btnDisable btnEnable
		
		GridLayout statusesLayout = new GridLayout();
		
		ArrayList<Workers> workers = clusterManager.getCluster().getWorkers();
		for (int i=0; i < workers.size(); i++)
		{
			Workers thisWorker = workers.get(i);
			for (WorkerStatus status : thisWorker.getStatuses()) {
				
				String s = status.getStatus();
				TextField statusField = new TextField(s);
				if(s.equals("OK")) {
					statusField.setStyleName("status status-"+status.getId()+" status-${status.status}");
				} else if(s.equals("Nok")) {
					statusField.setStyleName("status status-"+status.getId()+" status-${status.status}");					
				}
				
				statusesLayout.addComponent(statusField);
			}
			// add disable button
			Button btnDisable = new Button("Disable"); // worker.id;
			btnDisable.setData(thisWorker.getId());
			btnDisable.setEnabled(thisWorker.getStatus().equals("allDisabled"));
			statusesLayout.addComponent(btnDisable);
			// add enable button
			Button btnEnable = new Button("Enable"); // worker.id; 
			btnDisable.setEnabled(thisWorker.getStatus().equals("allEnabled"));
			btnEnable.setData(thisWorker.getId());
			statusesLayout.addComponent(btnEnable);
			
		}
		layout.addComponent(statusesLayout);
	}
}

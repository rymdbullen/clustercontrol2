package net.local.clustercontrol.web.views;

import java.util.ArrayList;

import net.local.clustercontrol.core.logic.IClusterManager;
import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.core.model.dto.WorkerStatus;
import net.local.clustercontrol.core.model.dto.Worker;
import net.local.clustercontrol.web.ClusterImposter;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

public class MainView extends CustomComponent {

	private static final long serialVersionUID = 3140593056123673046L;
	
	// ui variables
	private Label speed = new Label("Speed");
	private CheckBox chkAutoRefresh = new CheckBox("Update");        // Toggle Auto Refresh
	private Button button = new Button("Update");                    // Manual Refresh
	private TextField fieldLastAction = new TextField("LastAction"); // text describing the last action 
	private TextField fieldLastPoll = new TextField("LastUpdate"); // timestamp for last update
	private IClusterManager clusterManager;

	private boolean TEST = true;
	
	public MainView(IClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}
	
	public void init() {
		
		final HandleWorkerPopup subwindow = new HandleWorkerPopup();
		
		Cluster cluster; 
		if(!TEST) {
			cluster = clusterManager.getCluster();
		} else {
			cluster = ClusterImposter.generateCluster(clusterManager);
		}
		if(cluster==null) {
			throw new IllegalArgumentException("Failed to retreive Cluster");
		}
		
		GridLayout layout = new GridLayout(3,3);

		Panel panel = new Panel();
		panel.setWidth("600px"); // Defined width.
		panel.addComponent(layout);
				

		setCompositionRoot(panel);
		
		fieldLastPoll.setValue(cluster.getLastPoll());
		fieldLastAction.setValue("");
		
		layout.addComponent(fieldLastAction, 1, 1);
		layout.addComponent(fieldLastPoll, 1, 2);

//		ClassResource resource = new ClassResource(
//	            "/com/vaadin/book/examples/application/images/image.png",
//	            getApplication());
		
//		cluster.getHostNames();
//		cluster.getWorkerNames();
//		cluster.getLastPoll();

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

		// HEADER
		
		
		// ITERATION 1
		//      col1         col2          col3         ...  colX           Disable    Enable
		// row1 worker.name  row2 status1  row2 status1 ...  row2 statusX   btnDisable btnEnable
		// 

		// ITERATION 2
		//      col1         col2          col3         ...  colX           Disable    Enable
		// row2 worker.name  row2 status1  row2 status1 ...  row2 statusX   btnDisable btnEnable

		// ITERATION Y
		//      col1         col2          col3         ...  colX           Disable    Enable
		// rowY worker.name  row2 status1  row2 status1 ...  row2 statusX   btnDisable btnEnable

		ArrayList<Worker> worker = cluster.getWorkers();
		
		int columnSize = worker.get(0).getStatusesPerHost().size()+3;  // col1=name, ..., colX-1=disable, colX=enable  
		int rowSize = worker.size()+1;                                 // +1 for column header
		
		
		int colIdx = 0;
		int rowIdx = 0;
		
		
		GridLayout statusesLayout = new GridLayout(columnSize, rowSize);
		
		// header ROW 0 - COL 0-X
		Label workerNameLabel = new Label("Worker Name");
		statusesLayout.addComponent(workerNameLabel, colIdx, rowIdx);     System.out.println("header workerNameLabel["+workerNameLabel.getValue()+"]: 0:0");
		Label disableLabel = new Label("Disable");
		statusesLayout.addComponent(disableLabel, columnSize-2, rowIdx);  System.out.println("header disableLabel   ["+disableLabel.getValue()+"]: 0:"+(columnSize-2));
		Label enableLabel = new Label("Enable");
		statusesLayout.addComponent(enableLabel, columnSize-1, rowIdx);   System.out.println("header enableLabel    ["+enableLabel.getValue()+"]: 0:"+(columnSize-1));

		for (int workerIdx = 0; workerIdx < worker.size(); workerIdx++)
		{
			rowIdx = workerIdx+1;
			colIdx = 1;				// offset for header
			
			Worker thisWorker = worker.get(workerIdx);
			
//			Label hostNameLabel = new Label(thisWorker.getHost());
//			statusesLayout.addComponent(hostNameLabel, colIdx, rowIdx); System.out.println("header hostNameLabel["+hostNameLabel.getValue()+"]:"+rowIdx+":"+colIdx);

			for (int statusIdx=0; statusIdx < thisWorker.getStatusesPerHost().size(); statusIdx++)
			{
				WorkerStatus workerStatus = thisWorker.getStatusesPerHost().get(statusIdx); 
				String statusText = workerStatus.getStatus();
				Label statusLabel = new Label(statusText);
				statusLabel.setStyleName("status"+statusText);
				colIdx += statusIdx;
				statusesLayout.addComponent(statusLabel, colIdx, rowIdx);    System.out.println("statusLabel["+statusLabel.getValue()+"]:"+rowIdx+":"+colIdx);
				if(statusIdx==0) {
					Label workerName = new Label(workerStatus.getId());
					statusesLayout.addComponent(workerName, 0, rowIdx);      System.out.println("workerName["+workerName.getValue()+"]:"+rowIdx+":0");
				}
			}
			colIdx++;
			// add disable button
			Button btnDisable = new Button("Disable"); // worker.id;
			btnDisable.setData(thisWorker.getId());
			btnDisable.setEnabled(thisWorker.getStatus().equals("allDisabled"));
			statusesLayout.addComponent(btnDisable, colIdx, rowIdx);
			System.out.println("btnDisable:"+rowIdx+":"+colIdx);
			
			colIdx++;
			// add enable button
			Button btnEnable = new Button("Enable", new Button.ClickListener() {
	            // inline click-listener
	            public void buttonClick(ClickEvent event) {
	                if (subwindow.getParent() != null) {
	                    // window is already showing
	                    getWindow().showNotification("Window is already open");
	                } else {
	                    // Open the subwindow by adding it to the parent window
	                    getWindow().addWindow(subwindow);
	                }
	            }
	        });
			
			btnDisable.setEnabled(thisWorker.getStatus().equals("allEnabled"));
			btnEnable.setData(thisWorker.getId());
			statusesLayout.addComponent(btnEnable, colIdx, rowIdx);
			System.out.println("btnEnable:"+rowIdx+":"+colIdx);
		}
		layout.addComponent(statusesLayout, 0, 0);
		Label labelAlternatives = new Label("Alternatives");
		layout.addComponent(labelAlternatives , 1, 0);
	}
}

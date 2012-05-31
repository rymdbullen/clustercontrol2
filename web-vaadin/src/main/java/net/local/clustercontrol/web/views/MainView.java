package net.local.clustercontrol.web.views;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.local.clustercontrol.core.logic.IClusterManager;
import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.core.model.dto.WorkerStatus;
import net.local.clustercontrol.core.model.dto.Worker;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * hoover value: ${status.hostName}, ${status.route}, ${status.loadFactor}, ${status.transferred}
 * id of each div status-${status.id} status-${status.status}
 *
 *<pre>
 * HEADER
 * 
 * ITERATION 1
 *      col1         col2          col3         ...  colX           Disable    Enable
 * row1 worker.name  row2 status1  row2 status1 ...  row2 statusX   btnDisable btnEnable
 * 
 * ITERATION 2
 *      col1         col2          col3         ...  colX           Disable    Enable
 * row2 worker.name  row2 status1  row2 status1 ...  row2 statusX   btnDisable btnEnable
 * 
 * ITERATION Y
 *      col1         col2          col3         ...  colX           Disable    Enable
 * rowY worker.name  row2 status1  row2 status1 ...  row2 statusX   btnDisable btnEnable
 *</pre>
 *
 * @author jstenvall
 *
 */
public class MainView extends VerticalLayout {
	
	private static final Logger logger = LoggerFactory.getLogger(MainView.class); 

	private static final long serialVersionUID = 3140593056123673046L;
	
	// ui variables
	private CheckBox chkAutoRefresh = new CheckBox("AutoRefresh");        // Toggle Auto Refresh
	private Button button = new Button("Update");                    // Manual Refresh
	private TextField fieldLastAction = new TextField("LastAction"); // text describing the last action 
	private TextField fieldLastPoll = new TextField("LastUpdate"); // timestamp for last update
	private IClusterManager clusterManager;
	final HandleWorkerPopup subwindow = new HandleWorkerPopup();

	/**
	 * Constructor 	
	 * @param clusterManager
	 */
	public MainView(IClusterManager clusterManager) {
		this.clusterManager = clusterManager;
		
		Property lastPollObject = new ObjectProperty<String>(clusterManager.getCluster().getLastPoll());
		fieldLastPoll.setPropertyDataSource(lastPollObject);
		
		
    	// Configure the windows layout; by default a VerticalLayout
        VerticalLayout layout = (VerticalLayout) subwindow.getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
		
        // Add some content; a label and a close-button
        Label message = new Label("This is a subwindow");
        subwindow.addComponent(message);
        
        Button cancel = new Button("Cancel", new Button.ClickListener() {
			private static final long serialVersionUID = -6777718430390788755L;

			// inline click-listener
			@Override
            public void buttonClick(ClickEvent event) {
                // close the window by removing it from the parent window
				(subwindow.getParent()).removeWindow(subwindow);
            }
        });
		
        // The components added to the window are actually added to the window's
        // layout; you can use either. Alignments are set using the layout
        layout.addComponent(cancel);
        layout.setComponentAlignment(cancel, Alignment.BOTTOM_RIGHT);
	}
	
	public void init() {
		
		Cluster cluster = clusterManager.getCluster();
		if(cluster==null) {
			throw new IllegalArgumentException("Failed to retrieve Cluster");
		}
		
//		ClassResource resource = new ClassResource(
//	            "/com/vaadin/book/examples/application/images/image.png",
//	            getApplication());
		
		ArrayList<Worker> worker = cluster.getWorkers();
		
		// +3 for col0=workerName, ..., colX-1=btnDisable, colX=btnEnable
		int columnSize = worker.get(0).getStatusesPerHost().size()+3;    
		// +1 for column header
		int rowSize = worker.size()+1;
		
		int colIdx = 0;
		int rowIdx = 0;
		
		GridLayout statusesLayout = new GridLayout(columnSize, rowSize);
		
		for (int hostIdx = 0; hostIdx < cluster.getHostNames().size(); hostIdx++)
		{
			String hostname = cluster.getHostNames().get(hostIdx);
			Label hostNameLabel = new Label(hostname);
			logger.debug("{}:{} header hostNameLabel[{}]:", new Object[] {0, hostIdx+1, hostNameLabel.getValue()});
			statusesLayout.addComponent(hostNameLabel, hostIdx+1, 0);
		}
		
		// header ROW 0 - COL 0-X
		Label workerNameLabel = new Label("Worker Name");
		logger.debug("{}:{} header workerNameLabel[{}]", new Object[] {0, 0, workerNameLabel.getValue()});
		statusesLayout.addComponent(workerNameLabel, colIdx, rowIdx);     
		Label disableLabel = new Label("Disable");
		logger.debug("{}:{} header disableLabel   [{}]", new Object[] {0, columnSize-2, disableLabel.getValue()});
		statusesLayout.addComponent(disableLabel, columnSize-2, rowIdx);  
		Label enableLabel = new Label("Enable");
		logger.debug("{}:{} header enableLabel    [{}]", new Object[] {0, columnSize-1, enableLabel.getValue()});
		statusesLayout.addComponent(enableLabel, columnSize-1, rowIdx);   

		for (int workerIdx = 0; workerIdx < worker.size(); workerIdx++)
		{
			rowIdx = workerIdx+1;	// offset for worker name in col0
			colIdx = 1;
			
			final Worker thisWorker = worker.get(workerIdx);
			
			for (int statusIdx=0; statusIdx < thisWorker.getStatusesPerHost().size(); statusIdx++)
			{
				WorkerStatus workerStatus = thisWorker.getStatusesPerHost().get(statusIdx);
				
				Label statusLabel = new Label(workerStatus.getStatus());
				statusLabel.setStyleName("status"+workerStatus.getStatus());
				colIdx = statusIdx+1; // offset for header         row0
				logger.debug("{}:{} statusLabel[{}]:", new Object[] {rowIdx, colIdx, statusLabel.getValue()});
				statusesLayout.addComponent(statusLabel, colIdx, rowIdx);    
				
				if(statusIdx==0) {
					Label workerName = new Label(workerStatus.getName());
					logger.debug("{}:{} header workerName[{}]:", new Object[] {rowIdx, 0, workerName.getValue()});
					statusesLayout.addComponent(workerName, 0, rowIdx);      
				}
			}
			colIdx++;
			// add disable button
			logger.debug("{}:{} header btnDisable[]:", new Object[] {rowIdx, colIdx});
			statusesLayout.addComponent(createActionButton("Disable", thisWorker), colIdx, rowIdx);			
			
			colIdx++;
			// add enable button
			logger.debug("{}:{} header btnEnable[]:", new Object[] {rowIdx, colIdx});
			statusesLayout.addComponent(createActionButton("Enable", thisWorker), colIdx, rowIdx);			
		}
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		
		Panel panel = new Panel();
		panel.setWidth("600px"); // Defined width.
		panel.addComponent(layout);
		
		addComponent(panel);
		setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
		
		fieldLastPoll.setValue(cluster.getLastPoll());
		fieldLastAction.setValue("");
		
		layout.addComponent(statusesLayout);
		Label labelAlternatives = new Label("Alternatives");
		layout.addComponent(labelAlternatives);
		layout.addComponent(fieldLastAction);
		layout.addComponent(fieldLastPoll);
		layout.addComponent(chkAutoRefresh);
		layout.addComponent(button);

	}

	private Button createActionButton(final String action, final Worker thisWorker) {
		Button button = new Button(action, new Button.ClickListener() {
			private static final long serialVersionUID = 4310917167531139947L;

			// inline click-listener
            public void buttonClick(ClickEvent event) {
                if (subwindow.getParent() != null) {
                    // window is already showing
                    getWindow().showNotification("Window is already open");
                } else {
                	subwindow.activate(thisWorker.getId(), action);
                	subwindow.setModal(true);
                    // Open the subwindow by adding it to the parent window
                    getWindow().addWindow(subwindow);
                }
            }
        });
		button.setData(thisWorker.getId());
		button.setEnabled(!thisWorker.getStatus().equals("all"+action+"d"));
		return button;
	}
}

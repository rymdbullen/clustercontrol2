package net.local.clustercontrol.web.views;

import net.local.clustercontrol.core.configuration.Constants;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Window;

public class HandleWorkerPopup extends Window implements
	Property.ValueChangeListener {
	
	private static final long serialVersionUID = -3527449000925672817L;
	
	private Label workerName = new Label(); 
	private OptionGroup speedOptions; 
	private Label action = new Label();
	private Button apply;

    public HandleWorkerPopup() {
    	
        speedOptions = new OptionGroup("Select speed: ", Constants.SPEEDS);
        speedOptions.setNullSelectionAllowed(false); // user can not 'unselect'
        speedOptions.setImmediate(true); 			  // send the change to the server at once
        speedOptions.addListener(this);              // react when the user selects something
        
        // Add some content; a label and a close-button
        addComponent(workerName);
        addComponent(speedOptions);
        addComponent(action);
        this.setWidth("200px");

        apply = new Button("Apply", new Button.ClickListener() {
			private static final long serialVersionUID = -6777718430390788756L;

			// inline click-listener
			@Override
            public void buttonClick(ClickEvent event) {
                // close the window by removing it from the parent window
                
                // what to do here?
                System.out.println(event.getButton().getValue());
                
                //clusterManager.handle(workerName.getCaption(), speedOptions.getValue(), action.getCaption());
                
            }
        });
        // The components added to the window are actually added to the window's
        // layout; you can use either. Alignments are set using the layout
        addComponent(apply);
    }
    /**
     * 
     * @param workerName
     * @param action
     */
    public void activate(String workerName, String action) {
    	
    	this.workerName.setCaption(workerName);
    	this.action.setCaption(action);
    	if(action.equals("Enable")) {
    		speedOptions.setItemEnabled("Halt", false);
    	} else {
    		speedOptions.setItemEnabled("Halt", true);
    	}
        this.apply.setEnabled(false);

    	this.setCaption("Action: "+action);

    }
	@Override
	public void valueChange(ValueChangeEvent event) {
		apply.setEnabled(event.getProperty()!=null);
	}
}

package net.local.clustercontrol.web.views;

import java.util.Arrays;
import java.util.List;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class HandleWorkerPopup extends Window implements
	Property.ValueChangeListener {
	
	private static final long serialVersionUID = -3527449000925672817L;
	
	private Window subwindow = new Window("A subwindow");;
	
	private Label workerName = new Label(); 
	private OptionGroup speed; 
	private Label action = new Label(); 
	
	private static final List<String> speeds = Arrays.asList(new String[] {
            "Fast", "Medium", "Slow" });
    public HandleWorkerPopup() {
    	
        // Configure the windows layout; by default a VerticalLayout
        VerticalLayout layout = (VerticalLayout) subwindow.getContent();
        layout.setMargin(true);
        layout.setSpacing(true);

        speed = new OptionGroup("Select speed: ", speeds);
        speed.setNullSelectionAllowed(false); // user can not 'unselect'
        speed.select("Berlin"); // select this by default
        speed.setImmediate(true); // send the change to the server at once
        speed.addListener(this); // react when the user selects something
        
        // Add some content; a label and a close-button
        Label message = new Label("This is a subwindow: "+ workerName + " "+ speed + " "+ action);
        addComponent(message);
        addComponent(workerName);
        addComponent(speed);
        addComponent(action);

        Button close = new Button("Close", new Button.ClickListener() {
			private static final long serialVersionUID = -6777718430390788755L;

			// inline click-listener
            public void buttonClick(ClickEvent event) {
                // close the window by removing it from the parent window
                (subwindow.getParent()).removeWindow(subwindow);
            }
        });
        // The components added to the window are actually added to the window's
        // layout; you can use either. Alignments are set using the layout
        layout.addComponent(close);
        layout.setComponentAlignment(close, Alignment.BOTTOM_RIGHT);
    }
    public void activate(String workerName, String action) {
    	
    	this.workerName.setCaption(workerName);
    	this.speed.setCaption("What speed");
    	this.action.setCaption(action);

    	
    	
    	
    	
    	this.setCaption("Action: "+action);

    }
	@Override
	public void valueChange(ValueChangeEvent event) {
		getWindow().showNotification("Selected speed: " + event.getProperty());
	}
}

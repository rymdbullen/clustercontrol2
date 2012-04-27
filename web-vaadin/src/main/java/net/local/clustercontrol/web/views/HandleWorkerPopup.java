package net.local.clustercontrol.web.views;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class HandleWorkerPopup extends Window {
	private static final long serialVersionUID = -3527449000925672817L;
	
	Window subwindow;

    public HandleWorkerPopup() {

        // Create the window
        subwindow = new Window("A subwindow");

        // Configure the windws layout; by default a VerticalLayout
        VerticalLayout layout = (VerticalLayout) subwindow.getContent();
        layout.setMargin(true);
        layout.setSpacing(true);

        // Add some content; a label and a close-button
        Label message = new Label("This is a subwindow");
        subwindow.addComponent(message);

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
        layout.setComponentAlignment(close, Alignment.TOP_RIGHT);
    }
}

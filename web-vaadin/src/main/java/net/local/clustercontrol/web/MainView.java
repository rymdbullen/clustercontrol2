package net.local.clustercontrol.web;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;

public class MainView extends CustomComponent {

	/**
	 * generated serial version uid
	 */
	private static final long serialVersionUID = 3140593056123673046L;
	private TextField login = new TextField("Login");
	
	public MainView() {
		GridLayout layout = new GridLayout();
		setCompositionRoot(layout);
		layout.addComponent(login);
	}
}

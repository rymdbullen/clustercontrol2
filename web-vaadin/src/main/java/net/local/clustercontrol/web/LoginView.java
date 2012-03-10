package net.local.clustercontrol.web;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import net.local.clustercontrol.core.logic.IClusterManager;
import net.local.clustercontrol.core.logic.impl.WorkerHandlerFactory;

import com.vaadin.terminal.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

public class LoginView extends CustomComponent {

    Logger logger = LoggerFactory.getLogger(LoginView.class);

	@Autowired private IClusterManager clusterManager;
    @Autowired private WorkerHandlerFactory factory;

	/**
	 * generated serial version uid
	 */
	private static final long serialVersionUID = -7238406652914240712L;

	private TextField textFieldUrl = new TextField("Url");

	public LoginView() {

		FormLayout layout = new FormLayout();

		setCompositionRoot(layout);

		layout.addComponent(textFieldUrl);

		Button button = new Button("Login");

		layout.addComponent(button);

		button.addListener(new ClickListener() {

			/**
			 * generated serial version uid
			 */
			private static final long serialVersionUID = -7238406652914248812L;
			
			@Override
			public void buttonClick(ClickEvent event) {
				String url = ""+textFieldUrl.getValue();
				Map<String, String> response = clusterManager.init(url);
				if(response.get("initStatus").equals("nok")) {
					textFieldUrl.setComponentError(
							new UserError(response.get("initStatusMessage")));
				} else {
					System.out.println(response);
					getApplication().getMainWindow().setContent(new MainView());
				}
			}
		});
	}
}

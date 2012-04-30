package net.local.clustercontrol.web.views;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import net.local.clustercontrol.core.configuration.Constants;
import net.local.clustercontrol.core.logic.IClusterManager;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.RegexpValidator;
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
	@Autowired private MainView mainView;

	/**
	 * generated serial version uid
	 */
	private static final long serialVersionUID = -7238406652914240712L;

	private TextField textFieldUrl = new TextField("Url", "http://host1/balancer-manager");

	public LoginView() {

		FormLayout layout = new FormLayout();

		setCompositionRoot(layout);

		String regExp="^(?:https|http)://[a-z0-9-]+(?:.[a-z0-9-]+)+";
		RegexpValidator validator = new RegexpValidator(regExp, "{0} is not a valid URL") ;
		textFieldUrl.setRequired(true);
		textFieldUrl.setRequiredError("URL is required");
		textFieldUrl.addValidator(validator);
		layout.addComponent(textFieldUrl);

		final Button button = new Button("Login");
		button.setEnabled(textFieldUrl.isValid());
		ValueChangeListener valueChangeListener = new ValueChangeListener() {
			
			private static final long serialVersionUID = -258297701571239546L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				button.setEnabled(textFieldUrl.isValid());
			}
		};
		textFieldUrl.setImmediate(true);
		textFieldUrl.addListener(valueChangeListener);
		
		layout.addComponent(button);

		button.addListener(new ClickListener() 
		{
			private static final long serialVersionUID = -7238406652914248812L;
			
			@Override
			public void buttonClick(ClickEvent event) {
				
				if(Constants.IS_DEVMODE) {
					mainView.init();
					getApplication().getMainWindow().setContent(mainView);
					return;
				}
				
				@SuppressWarnings("unused")
				String url = ""+textFieldUrl.getValue();
				Map<String, String> response = clusterManager.init(url);
				if(response.get("initStatus").equals("nok")) {
					textFieldUrl.setComponentError(new UserError(response.get("initStatusMessage")));
				} else {
					mainView.init();
					getApplication().getMainWindow().setContent(mainView);
				}
			}
		});
	}
}

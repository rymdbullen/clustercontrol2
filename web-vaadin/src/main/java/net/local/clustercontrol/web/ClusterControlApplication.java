package net.local.clustercontrol.web;


import net.local.clustercontrol.web.configuration.VaadinAppConfig;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import com.vaadin.Application;
import com.vaadin.ui.Window;

/**
 * The Application's "main" class
 */
@Component
public class ClusterControlApplication extends Application {
	private static final long serialVersionUID = 7230334633423374190L;
	private Window window;

	@Override
	public void init() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(VaadinAppConfig.class);
        LoginView loginView = context.getBean(LoginView.class);

		window = new Window("Cluster Control Application");
		setMainWindow(window);
		window.addComponent(loginView);

	}

}

/*
 * Copyright 2009 IT Mill Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.local.clustercontrol.web;


import net.local.clustercontrol.AppConfig;
import net.local.clustercontrol.core.configuration.EnvironmentAwareProperties;
import net.local.clustercontrol.core.logic.IClusterManager;
import net.local.clustercontrol.core.logic.impl.ClusterManager;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.vaadin.Application;
import com.vaadin.ui.Window;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class MyVaadinApplication extends Application {
	private Window window;

	@Override
	public void init() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        LoginView loginView = context.getBean(LoginView.class);
//        IClusterManager clusterManager = context.getBean(ClusterManager.class);

//        EnvironmentAwareProperties eap = EnvironmentAwareProperties.getInstance();
//        System.out.println( eap.getCurrentEnvironmentName() );
        
		window = new Window("My Vaadin Application");
		setMainWindow(window);
		window.addComponent(loginView);

	}

}

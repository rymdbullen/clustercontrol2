/**
 * Copyright 2011 Blue-Infinity
 * All Rights Reserved.
 * 
 * NOTICE: Blue-Infinity permits you to use, modify, and distribute this file
 * in accordance with the terms of the license agreement accompanying it.
 */
package net.local.clustercontrol.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import net.local.clustercontrol.core.configuration.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet should be loaded at startup to initialize the
 * application.
 * <p>
 * Note that we can't call Logger.getLogger() until StartupServletinit(ServletContext) is executed,
 * ie ApplicationRepositorySelector.init() is called.  For all other classes in the
 * webapp, you can call Logger.getLogger() at any time.
 * </p>
 */
public class StartupServlet extends GenericServlet {
    private static final String SERVLET_CONTEXT_ATTR_STARTUP_SERVLET = "STARTUP_SERVLET";

	/**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -7309138022832693654L;
   
    /** The logger instance */
	private static final Logger logger = LoggerFactory.getLogger(StartupServlet.class);
    private static StartupServlet _startupServlet;
    private boolean _initializationOk = false;
    private String VERSION = "trunk";
    private static String BUILD_NUMBER = "trunk";
    
    public static String getBuildNumber() {
    	return BUILD_NUMBER;
    }

    /**
     * @see javax.servlet.GenericServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        throw new ServletException("This servlet should not be invoked!");
    }
    /**
     * Returns the singleton instance.
     * @return the singleton instance.
     * @depricated
     */
    public static StartupServlet getStartupServlet() {
        return _startupServlet;
    }
	/**
     * Adds functionality in the initialization stage. Initializes the log4j
     * repository for this application.
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        // grab the name and version of the application server
//        if(System.getProperties().containsKey("catalina.base")) {
//            initTomcat(config);
//        } else if(config.getClass().getName().startsWith("org.mortbay.jetty")) {
//            initJetty(config);
//        } else {
//            System.out.println("**** Servlet Container ****"+config.getClass().getName());
//            System.out.println("**** Servlet Container ****"+config.getClass().getName());
//            System.out.println("**** Servlet Container ****"+config.getClass().getName());
//            //throw new Exception("This application can only run on a Tomcat or Jetty instance.");
//        }
        
        super.init(config);
    }
    /**
     * Adds functionality in the initialization stage.
     *
     * @throws ServletException if an exception occurs
     */
    @Override
    public void init() throws ServletException {
    	
    	//
    	// setup the log file directory
//    	String serverInfo = getServletContext().getServerInfo();
//    	if(serverInfo.toLowerCase().contains("tomcat")) {
//			initTomcat(null);
//        } else if(serverInfo.toLowerCase().contains("jetty")) {
//            initJetty(null);
//        } else if(serverInfo.toLowerCase().contains("weblogic")) {
//            initWeblogic(null);
//        } else {
//            System.out.println("**** Servlet Container ****: "+serverInfo);
//            System.out.println("**** Servlet Container ****: "+serverInfo);
//            System.out.println("**** Servlet Container ****: "+serverInfo);
//            //throw new Exception("This application can only run on a Tomcat or Jetty instance.");
//        }
    	
        //
        // setup the startup servlet
        getServletContext().setAttribute(SERVLET_CONTEXT_ATTR_STARTUP_SERVLET, this);
        _startupServlet = this;
        //
        // the status of the initialization
        Throwable error = null;
        try {
            super.init();
            
            //
            // retrieve build version from war manifest file
            getVersionFromManifest();

            //
            // dump JNDI context
            //
            InitialContext initCtx = new InitialContext();
            logger.info("vvvvvv JNDI vvvvvv");
            Name name = initCtx.getNameParser("").parse("java:");
            Context javaCtx = (Context) initCtx.lookup(name);
            dumpContext(0,name,javaCtx);
            logger.info("^^^^^^ JNDI ^^^^^^");
           
            //
            // application is starting up...
            //
            logger.info("Initializing "+Constants.APPLICATION_NAME+" Application version " + VERSION + ", r"+BUILD_NUMBER);
            
            //
            // do common init first
            //
            doCommonInit();
           
            _initializationOk = true;
        } catch(Throwable e) {
            error = e;
            throw new ServletException("StartupServlet.init:", e);
        } finally {
            String message = "Initialization "+Constants.APPLICATION_NAME+" Application version " + VERSION + ", r"+BUILD_NUMBER+" done " + (error == null ? "ok" : "with error");
            if(error == null) {
                logger.info(message);
            } else {
				logger.error(message, error);
            }
        }
    }
	/**
     * Gets the version and subversion build number from manifest file
     */
    private void getVersionFromManifest() {
    	String appServerHome = getServletContext().getRealPath("/");
    	File manifestFile = new File(appServerHome, "META-INF/MANIFEST.MF");
    	Manifest mf = new Manifest();
    	
    	InputStream stream = null; 
    	
    	try {
	    	try {
	    		stream = new FileInputStream(manifestFile);
				mf.read(stream);		
			} finally {
				if (stream != null) {
					stream.close();
				}
			}
    	} catch (FileNotFoundException e) {
			logger.warn("FileNotFoundException META-INF/MANIFEST.MF");
			return;
		} catch (IOException e) {
			logger.warn("IOException META-INF/MANIFEST.MF");
			return;
		}

    	Attributes attributes = mf.getMainAttributes();
    	VERSION = attributes.getValue("Implementation-Version");
    	BUILD_NUMBER = attributes.getValue("Implementation-Build");
	}
    /**
     * Performs initialization for a weblogic instance.
     * @param config the configuration
     * @throws ServletException
     */
    private void initWeblogic(Object object) {
    	logger.info("Setting up application for Weblogic Server");
        Properties props = new Properties();
        props.putAll(System.getProperties());
        String version = VERSION;
        String buildNumber = BUILD_NUMBER;
        props.setProperty("name", Constants.APPLICATION_NAME);
        props.setProperty(Constants.APPLICATION_NAME+".version", version);
		props.setProperty(Constants.APPLICATION_NAME+".build.number", buildNumber);
        props.setProperty("appserver.home.dir", "./servers/AdminServer/");
        props.setProperty("appserver.temp.dir", props.getProperty("java.io.tmpdir").replace('\\', '/'));
        props.setProperty("build-date", "unknown");

        System.getProperties().setProperty("name", Constants.APPLICATION_NAME);
        System.getProperties().setProperty(Constants.APPLICATION_NAME+".version", version);
        System.getProperties().setProperty(Constants.APPLICATION_NAME+".build.number", buildNumber);
        
        dumpAppServerProps(props);
	}
    /**
     * Performs initialization for a tomcat instance.
     * @param config the configuration
     * @throws ServletException
     */
    private void initTomcat(ServletConfig config) throws ServletException {
    	logger.info("Setting up application for Tomcat Server");
        //
        // create a new environment for the properties
        //
        Properties props = new Properties();
        props.putAll(System.getProperties());
//        String catalinaBase = props.getProperty("catalina.base").replace('\\', '/');
        String version = VERSION;
        String buildNumber = BUILD_NUMBER;
        props.setProperty("name", Constants.APPLICATION_NAME);
        props.setProperty(Constants.APPLICATION_NAME+".version", version);
		props.setProperty(Constants.APPLICATION_NAME+".build.number", buildNumber);
//        props.setProperty("appserver.home.dir", catalinaBase);
        props.setProperty("appserver.temp.dir", props.getProperty("java.io.tmpdir").replace('\\', '/'));
//        props.setProperty("appserver.deploy.dir", catalinaBase + "/webapps");
        props.setProperty("build-date", "unknown");
       
        dumpAppServerProps(props);
       
        // override settings from the initial context
        // defined by tomcat config
        String ctx = "java:comp/env/constants";
        try {
            InitialContext ic = new InitialContext();
            Enumeration<Binding> bindingEnum = ic.listBindings(ctx);
            while(bindingEnum.hasMoreElements()) {
                Binding binding = (Binding) bindingEnum.nextElement();
                String name = binding.getName();
                String prefix = ctx+"/";
                if(name.startsWith(prefix)) {
                    name = name.substring(prefix.length());
                }
               
                Object value = binding.getObject();
                props.put(name, value);
                logger.info("Overriding config parameter:" + name + ":" + value);
                System.out.println("Overriding config parameter:" + name + ":" + value);
            }
        } catch(NameNotFoundException e) {
        	logger.info("No context binding for ctx: "+ctx);
        } catch (NamingException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
    /**
     * Performs initialization for a jetty instance.
     * @param config the configuration
     * @throws MalformedURLException
     * @throws NamingException
     */
    private void initJetty(ServletConfig config) {
    	logger.info("Setting up application for Jetty Server");
        Properties props = new Properties();
        props.putAll(System.getProperties());
        String version = VERSION;
        String buildNumber = BUILD_NUMBER;
        props.setProperty("name", Constants.APPLICATION_NAME);
        props.setProperty(Constants.APPLICATION_NAME+".version", version);
		props.setProperty(Constants.APPLICATION_NAME+".build.number", buildNumber);
        props.setProperty("appserver.home.dir", "./target");
        props.setProperty("appserver.temp.dir", props.getProperty("java.io.tmpdir").replace('\\', '/'));
        props.setProperty("appserver.deploy.dir", ".");
        props.setProperty("build-date", "unknown");
        props.setProperty("webapp.dir", "./src/main/webapp");

        System.getProperties().setProperty("name", Constants.APPLICATION_NAME);
        System.getProperties().setProperty(Constants.APPLICATION_NAME+".version", version);
        System.getProperties().setProperty(Constants.APPLICATION_NAME+".build.number", buildNumber);
        
        dumpAppServerProps(props);
    }
    /**
     * Dumps the Application Server properties
     * @param props the Application Server properties to dump
     */
    private void dumpAppServerProps(Properties props) {
        Enumeration<?> keys = props.propertyNames();
        logger.info("vvvvvv Application Server Props vvvvvv");
        ArrayList<String> nameValueList = new ArrayList<String>();
        while (keys.hasMoreElements()) {
            String property = (String) keys.nextElement();
            String value = props.getProperty(property);
            nameValueList.add(property + ":" + value);
        }
		Collections.sort(nameValueList);
		for ( int i = 0 ; i < nameValueList.size() ; i++ ) {
		    String message = nameValueList.get(i);
            logger.info(message);
	    }
        logger.info("^^^^^^ Application Server Props ^^^^^^");
    }
    /**
     * Dumps the current context to system out.
     * @param indent the indentation level
     * @param context the context to dump
     */
    private void dumpContext(int indent, Name ctxName, Context ctx) {
        try {
            logger.info(indent(indent)+"-::"+ctxName);
            Enumeration<NameClassPair> names = ctx.list(ctx.composeName("", ""));
            while(names.hasMoreElements()) {
                Object name = names.nextElement();
                if(name instanceof NameClassPair) {
                    NameClassPair ncp = (NameClassPair) name;
                    dumpName(indent+1,ctx.getNameParser(ctxName).parse(ncp.getName()), ctx);
                } else {
                    logger.info("Unknown name type:"+name.getClass().getName());
                }
            }
            logger.info(indent(indent)+"- - - -");
        } catch (OperationNotSupportedException e) {
            logger.info(indent(indent+1)+"-: <unknown content>");
        } catch (ClassCastException e) {
            logger.info(indent(indent+1)+"-: <unknown content>");
        } catch (NamingException e) {
            logger.info("throw new InternalException(\"StartupServlet.dumpContext:\", e)");
        }
    }
    /**
     * Dumps the name in the supplied context.
     * @param indent the intentation to use
     * @param name the name of the object to lookup
     * @param ctx the context that the name belongs to.
     */
    private void dumpName(int indent, Name name, Context ctx) {
        Object o = null;
        try {
            o = ctx.lookup(name);
        } catch (NamingException e) {
        }
        if(o instanceof Context) {
            Context subCtx = (Context) o;
            dumpContext(indent,name,subCtx);
        } else {
            logger.info(indent(indent)+"-"+name);
        }
    }

    /**
     * Creates an intentation.
     * @param indent the indentation level
     * @return the indentation string.
     */
    private String indent(int indent) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < indent; i++) {
            sb.append(" |");
        }
        return sb.toString();
    }
    /**
     * @see javax.servlet.Servlet#destroy()
     */
    public void destroy() {
        super.destroy();
       
        logger.info("Exiting "+Constants.APPLICATION_NAME+" Application version " + VERSION + ", r"+BUILD_NUMBER);
       
        doCommonDestroy();

        logger.info("Exit "+Constants.APPLICATION_NAME+" Application version " + VERSION + ", r"+BUILD_NUMBER + " done ok");
    }
   
    /**
     * Ensures that the initialiation is performed ok.
     * @throws ServletException if the initialization fails
     */
    public void assertInitializationOk() throws ServletException {
        if(_initializationOk) {
            return;
        }
        throw new ServletException("Startup servlet initialization failed.");
    }
   
    /**
     * Returns the startup servlet in the supplied context.
     * @param servletContext the servlet context to get the startup servlet from.
     * @return the startup servlet in the supplied context.
     * @throws ServletException
     */
    public static StartupServlet getInstance(ServletContext servletContext) throws ServletException {
        StartupServlet startupServlet = (StartupServlet) servletContext.getAttribute(SERVLET_CONTEXT_ATTR_STARTUP_SERVLET);
        if(startupServlet == null) {
            throw new ServletException("No startup servlet registered in context.");
        }
        return startupServlet;
    }
   
    /**
     * This method is invoked from both the <code>StartupServlet</code> and the <code>JUnit</code> tests.
     */
    public static void doCommonInit() throws Exception {

    }
   
    /**
     * This method is invoked from both the <code>StartupServlet</code> and the <code>JUnit</code> tests.
     */
    public static void doCommonDestroy() {
		// This manually deregisters JDBC driver, which prevents Tomcat 7 from complaining about 
		// memory leaks wrto this class
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                logger.info(String.format("Deregistering jdbc driver: %s", driver));
            } catch (SQLException e) {
                logger.error(String.format("Error deregistering driver %s", driver), e);
            }
        }
    }
}

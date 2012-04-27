package net.local.clustercontrol.web;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

public abstract class AbstractWebTestCase {

	private final static Logger logger = Logger.getLogger(AbstractWebTestCase.class);

	/*
	 * Maven must be run with these arguments:
	 * mvn clean install -Dcargo.port=9090 -DargLine="-Dcargo.port=9090"
	 */
	public static String defaultPort = "9090";
	public static String targetUrl = "http://localhost:"; 
	public static String baseUrl = null;
	
	protected static WebClient webClient;
	
	protected final static String DOTCOM = "http://www.ericsson.com"; 
	
	@BeforeClass
	public static void setUp() {
		if(baseUrl == null) {
			String cargoPort = System.getProperty("cargo.port");
			if (StringUtils.isBlank(cargoPort)) {
				cargoPort = defaultPort;
			}
			baseUrl = targetUrl+cargoPort;
			logger.trace("Setting up web client: "+baseUrl);
			System.out.println("Setting up web client: "+baseUrl);
		}
		
		webClient = new WebClient();
		logger.trace("-> setting JavascriptEnabled = false");
		webClient.setJavaScriptEnabled(false);
		webClient.setPrintContentOnFailingStatusCode(false);
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		webClient.closeAllWindows();
	}

	protected Page getPage(String relativeUrl) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		logger.info("Loading " + baseUrl + relativeUrl);
		Page page = webClient.getPage(baseUrl + relativeUrl);
		assertNotNull("Page must not be null", page);
		return page;
	}
}

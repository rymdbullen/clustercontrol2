package net.local.clustercontrol.web.selenium;

import java.lang.reflect.Method;
import java.util.Properties;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations = { "classpath:applicationContextTest.xml" })
public class SeleniumFlexTestCase extends AbstractJUnit4SpringContextTests {

	private static final Logger logger = LoggerFactory.getLogger(AbstractJUnit4SpringContextTests.class);

	@Autowired
	@Qualifier("testProperties")
	public Properties testProperties;

	public String serverHost;
	public int serverPort;
	public String browserStartCommand;
	public String host;
	public String context;
	public String browserURL;

	@Before
	public void setUpTest() {
		serverHost = testProperties.getProperty("seleniumServer.serverHost");
		serverPort = Integer.parseInt(testProperties.getProperty("seleniumServer.serverPort"));
		browserStartCommand = "*" + testProperties.getProperty("seleniumServer.browserStartCommand");
		host = testProperties.getProperty("environment.webapp.host");
		context = testProperties.getProperty("environment.webapp.context");
		browserURL = host + context;
		
		Properties props = new Properties();
		props.put("log4j.appender.unit-test", "org.apache.log4j.ConsoleAppender");
		props.put("log4j.appender.unit-test.layout", "org.apache.log4j.PatternLayout");
		props.put("log4j.appender.unit-test.layout.ConversionPattern", "%-5p %c{2} - %m%n");
		props.put("log4j.rootLogger", "DEBUG, unit-test");

		PropertyConfigurator.configure(props);
	}
	@Test
	public void Dummy() {
		assertTrue(true);
	}

	protected String getData(String property) {
		
		String value = testProperties.getProperty(property);
		if(value==null) {
			logger.warn("Failed to find value for property: "+property);
			return "";
		}
		return value;
	}

	protected void setDataSource(Properties testProperties) {
		testProperties = this.testProperties;
	}

	protected DefaultSeleniumFlex getDefaultSeleniumFlex() {
		logger.debug("Setting up DefaultSelenium \n * Key : Value\n * serverHost :"+serverHost+"\n * serverPort :"+serverPort+"\n * browserStartCommand :"+browserStartCommand+"\n * browserURL :"+browserURL);
		DefaultSeleniumFlex selenium = new DefaultSeleniumFlex(serverHost, serverPort, browserStartCommand, browserURL);

//		WebDriver driver = new FirefoxDriver();
//		Selenium selenium = new WebDriverBackedSelenium(driver, "http://www.yoursite.com");
		
		return selenium;
	}

	protected void waitForFlexExists(String objectID, int timeout,
			DefaultSeleniumFlex selenium) throws Exception {
		while (timeout > 0 && !selenium.getFlexExists(objectID).equals("true")) {
			Thread.sleep(1000);
			timeout--;
		}
		if (timeout == 0) {
			throw new Exception("waitForFlexExists flex object:" + objectID
					+ " Timed Out");
		}
	}

	protected void waitForFlexVisible(String objectID, int timeout,
			DefaultSeleniumFlex selenium) throws Exception {
		while (timeout > 0 &&  !selenium.getFlexVisible(objectID).equals("true")) {
			Thread.sleep(1000);
			timeout--;
		}
		if (timeout == 0) {
			throw new Exception("waitForFlexVisible flex object:" + objectID + " Timed Out");
		}
	}

	protected void openModule(String module, DefaultSeleniumFlex selenium)
			throws Exception {
		// open a module and wait for it to complete loading
		String coreBlank = host + "selenium-server/core/Blank.html";
		if (selenium.getLocation().equals(coreBlank)) {
			selenium.open(host);
			// just buffer with a little time to stop the test from stepping on
			// its own feet
			Thread.sleep(3000);
			waitForFlexExists("loadModCombo", 20, selenium);
			selenium.flexSelectIndex("loadModCombo", module);
		} else {
			selenium.refresh();
			Thread.sleep(3000);
			waitForFlexExists("loadModCombo", 20, selenium);
			selenium.flexSelectIndex("loadModCombo", module);
		}
	}

	protected void completeCreditCard(DefaultSeleniumFlex selenium,
			String ancestor) throws Exception {
		// just fills out a credit card component
		selenium.flexSelect(ancestor + "creditCardTypeCombo", "Visa");
		selenium.flexType(ancestor + "creditCardNameInput", "Fergal Test");
		selenium.flexType(ancestor + "creditCardNumberInput",
				"4111 1111 1111 1111");
		selenium.flexSelect(ancestor + "creditCardExpiryMonthCombo", "02");
		selenium.flexSelect(ancestor + "creditCardExpiryYearCombo", "2012");
		selenium.flexType(ancestor + "creditCardSecurityNumberInput", "123");
	}

	protected void doLogin(DefaultSeleniumFlex selenium, String loginId,
			String pin) throws Exception {
		// do a login on the authenticated module
		openModule(getData("module.login"), selenium);
		waitForFlexExists("loginIDInput", 20, selenium);
		selenium.flexType("loginIDInput", loginId);
		selenium.flexType("pinInput", pin);
		assertTrue(selenium.getFlexEnabled("loginBtn") == "true");
		selenium.flexClick("loginBtn");
		waitForFlexVisible("_AuthenticatedModule_Button3", 20, selenium);
	}

	protected void waitForOneFailForTheOther(DefaultSeleniumFlex selenium,
			String methodWait, String targetWait, String valueWait,
			String methodFail, String targetFail, String valueFail, int timeOut)
			throws Exception {
		Method methWait = selenium.getClass().getMethod(methodWait);
		Method methFail = selenium.getClass().getMethod(methodFail);

		while (methWait.invoke(selenium, targetWait).equals(valueWait)
				&& methFail.invoke(selenium, targetFail).equals(valueFail)
				&& timeOut > 0) {
			Thread.sleep(1000);
			timeOut--;
		}

		if (methFail.invoke(selenium, targetFail).equals(valueFail)) {
			throw new Exception("waitForOneFailForTheOther: " + methodWait
					+ " target: " + targetFail + " - value: " + valueFail);
		}

		if (timeOut == 0) {
			throw new Exception("waitForOneFailForTheOther: Timed Out");
		}
	}

	protected void continueFailExists(DefaultSeleniumFlex selenium,
			String continueIf, String failIf) throws Exception {
		// pass and continue if one element exists, fail and stop if another
		// elements exists
		int timeOut = 60;

		while (selenium.getFlexExists(continueIf).equals("false")
				&& selenium.getFlexExists(failIf).equals("false")
				&& timeOut > 0) {
			Thread.sleep(1000);
			timeOut--;
		}
		assertTrue(selenium.getFlexExists(continueIf) == "true");
		assertTrue(selenium.getFlexExists(failIf) == "false");
	}
}

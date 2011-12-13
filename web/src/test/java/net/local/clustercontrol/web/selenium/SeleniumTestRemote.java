package net.local.clustercontrol.web.selenium;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SeleniumTestRemote {

	@Test
	public void testMyRolex() throws MalformedURLException {
		
		// Create a new instance of the Firefox driver
		// Notice that the remainder of the code relies on the interface,
		// not the implementation.
		//WebDriver driver = new InternetExplorerDriver();
		//WebDriver driver = new OperaDriver();
		WebDriver driver = new RemoteWebDriver(new URL("http://172.18.151.172:4444/wd/hub"), DesiredCapabilities.firefox());
		
		assertNotNull(driver);
		
		// http://sunc01017.gva.pictet.com:13350/workspace/
			 
		// tu peux utiliser ce user : sptest1/password
			
		// And now use this to visit Google
//driver.get("http://sunc01017.gva.pictet.com:13350/workspace/");
		driver.get("http://172.18.151.172:8080/MyRolexWorldService/static/login.jsp");

		// Find the login text input element by its name
		driver.findElement(By.name("j_username")).clear();
		driver.findElement(By.name("j_username")).sendKeys("fred");
		driver.findElement(By.name("j_password")).clear();
		driver.findElement(By.name("j_password")).sendKeys("a");
		driver.findElement(By.cssSelector("input.submitBtn")).click();

		
		driver.findElement(By.name("searchButton")).click();

		driver.findElement(By.name("contentBox")).sendKeys("a");

		// Check the title of the page
		System.out.println("Page title is: " + driver.getTitle());

		// Google's search is rendered dynamically with JavaScript.
		// Wait for the page to load, timeout after 10 seconds
		(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return d.getTitle().toLowerCase().startsWith("cheese!");
			}
		});

		// Should see: "cheese! - Google Search"
		System.out.println("Page title is: " + driver.getTitle());

		// Close the browser
		driver.quit();
	}
}

package net.local.clustercontrol.web.integration;

import java.io.IOException;
import java.net.MalformedURLException;

import net.local.clustercontrol.web.AbstractWebTestCase;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import static org.junit.Assert.assertEquals;

public class BootstrapTest extends AbstractWebTestCase {

    @Test
	public void testThatSiteIsReachable() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		assertEquals("Ericsson WIRE Surveillance page", 
				((HtmlPage)getPage("/surveillance.html")).getTitleText());
	}

	protected void verifyLeftNavigation(HtmlPage page) {
		// No left navigation on the surveillance page
	}
}


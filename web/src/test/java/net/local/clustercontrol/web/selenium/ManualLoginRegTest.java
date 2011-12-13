package net.local.clustercontrol.web.selenium;

import static org.junit.Assert.assertTrue;
import junit.framework.AssertionFailedError;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ManualLoginRegTest extends SeleniumFlexTestCase {
	private DefaultSeleniumFlex selenium;

	private String flashId = "MyRolexWorldService";
	
	@Before
	public void setUp() throws Exception {
		selenium = getDefaultSeleniumFlex();
		selenium.start();
	}

	@After
	public void tearDown() throws Exception {
		selenium.stop();
	}

	@Test
	public void testLogin() {
		try {

            selenium.open(context);
            selenium.windowFocus();
            selenium.setSpeed("1000");

			selenium.type("j_username", "fred");
            selenium.type("j_password", "a");
			selenium.submit("f");
			selenium.flexWaitForElement(flashId);
			waitForFlexVisible(flashId, 20, selenium);

			// check the labels on the landing page
			assertTrue(selenium.getFlexText("_AccountDetailsView_Label2") == getData("ManualLoginRegTest.accountNumber"));
			assertTrue(selenium.getFlexText("_AccountDetailsView_Text2") == getData("ManualLoginRegTest.customer"));
			assertTrue(selenium.getFlexText("_AccountDetailsView_Text4") == getData("ManualLoginRegTest.accountOpeningDate"));
			assertTrue(selenium.getFlexText("_AccountDetailsView_Text5") == getData("ManualLoginRegTest.ongoingPaymentMethod"));
			assertTrue(selenium
					.getFlexText("_AccountDetailsView_deliveryMethod") == getData("ManualLoginRegTest.statementDeliveryMethod"));
			assertTrue(selenium.getFlexText("_AccountDetailsView_Text9") == getData("ManualLoginRegTest.statementType"));
			assertTrue(selenium
					.getFlexText("_AccountDetailsView_deliveryCycleId") == getData("ManualLoginRegTest.billingFrequency"));
			assertTrue(selenium.getFlexText("_AccountDetailsView_Text19") == getData("ManualLoginRegTest.product"));
			assertTrue(selenium.getFlexText("_AccountDetailsView_Text21") == getData("ManualLoginRegTest.paymentMethod"));

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("\nTest BuyPassTest FAILED (" + e.getMessage() + ")");
			throw new AssertionFailedError();
		}
	}
}

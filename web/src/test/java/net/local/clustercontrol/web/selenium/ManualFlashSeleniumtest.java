package net.local.clustercontrol.web.selenium;

import static org.junit.Assert.*;

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.junit.*;

import com.thoughtworks.selenium.FlashSelenium;
import com.thoughtworks.selenium.FlexUISelenium;
import com.thoughtworks.selenium.Selenium;

public class ManualFlashSeleniumtest extends SeleniumFlexTestCase {

	private DefaultSeleniumFlex selenium;
	private FlashSelenium flashApp;
	private FlexUISelenium flexUITester;
	private int i = 0;

	@Before
	public void setUp() throws Exception {
		selenium = getDefaultSeleniumFlex();
		selenium.start();

		String flashId = "MyRolexWorldService";
		flexUITester = new FlexUISelenium(selenium, flashId);
		flashApp = new FlashSelenium(selenium, flashId);

		Properties props = new Properties();
		props.put("log4j.appender.unit-test", "org.apache.log4j.ConsoleAppender");
		props.put("log4j.appender.unit-test.layout", "org.apache.log4j.PatternLayout");
		props.put("log4j.appender.unit-test.layout.ConversionPattern", "%-5p %c{2} - %m%n");
		props.put("log4j.rootLogger", "DEBUG, unit-test");
		
		PropertyConfigurator.configure(props);
	}

	@After
	public void tearDown() throws Exception {
		selenium.stop();
	}

	@Test
	public void verifyRolexMenuMovements() {

		selenium.open(context);
		selenium.windowFocus();
		selenium.setSpeed("1000");

		selenium.type("j_username", "fred");
		selenium.type("j_password", "a");
		selenium.submit("f");

		// http://stackoverflow.com/questions/6216712/selenium-java-waitforcondition

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		flexUITester.click("moveButton");
		//flexUITester.type("10").at("txtNumber");

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		show("_FinderFolderRenderer_Label1");
		show("panelList.dataProvider");
		show("panelList.dataProvider.names");
		show("panelList.dataProvider.names.value");
		show("panelList.dataProvider.names._items");
		show("panelList.dataProvider.names.items");
		show("panelList.dataProvider.numElements");
		show("panelList.dataProvider.id");
		
		assertEquals("", "");
	}
	
	private String show(String property) {
		i++;
		System.out.println(i+" "+property+"\t:" + flexUITester.readFrom(property));
		return flexUITester.readFrom(property);
	}

	@Test
	public void verifyRolexMenuSearch() throws Exception {

		selenium.open(context);
		selenium.windowFocus();
		selenium.setSpeed("1000");

		selenium.type("j_username", "fred");
		selenium.type("j_password", "a");
		selenium.submit("f");

		// http://stackoverflow.com/questions/6216712/selenium-java-waitforcondition

		while (flashApp.PercentLoaded() != 100) {
			System.out.println("hejsan"+(i++));
            Thread.sleep(1000);
		}

		
		Thread.sleep(10000);

		
		flexUITester.click("searchButton");
		flexUITester.type("10").at("txtNumber");

		
		Thread.sleep(6000);

		
		flexUITester.click("btnCallService");
		try {
			Thread.sleep(7000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		System.out.println(selenium.getFlexDataGridRowCount("researchResultList.typicalItem.names"));
		
		String listData = flashApp.call("getCustomLayoutListData", "#");
        String[] items = listData.split("#");
        Assert.assertEquals(items.length, 2);
        Assert.assertTrue(items[0].toLowerCase().contains("br"));
        Assert.assertTrue(items[1].toLowerCase().contains("br"));

		i=0;
		show("researchResultList.typicalItem");
		show("researchResultList.typicalItem.id");
		show("researchResultList.typicalItem.names");
		show("researchResultList.typicalItem.names.list");
		show("researchResultList.typicalItem.names.length");
		show("researchResultList.DataGroup");
		// show("researchResultList.dataGroup");
		// show("researchResultList.DataGroup.numElements"));
		// show("researchResultList.dataGroup.numElements"));
		// System.out.println("");
		// show("researchResultList.DataGroup.baselinePosition"));
		// show("researchResultList.DataGroup.typicalItem.names"));
//		System.out.println("");
//		show("researchResultList.dropEnabled"));
//		show("researchResultList.dropIndicator"));
//		show("researchResultList.selectedItems"));
//		show("ResearchResultList.dataProvider"));
		show("researchResultList.dataProvider");
		// show("researchResultList.dataGroup.baselinePosition"));
		// show("researchResultList.dataGroup.typicalItem.names"));
		// System.out.println("");
		// System.out.println("");
		// System.out.println("");
		// show("researchResultList.typicalItem.names.item(0)"));
		// show("researchResultList.typicalItem.names.itemAt(0)"));
		// show("researchResultList.typicalItem.names.getItemAt(0)"));
		// show("researchResultList.typicalItem.type"));
		// show("researchResultList.typicalItem.code"));
		// show("researchResultList.typicalItem.names.id"));
		// show("researchResultList.typicalItem.names.source"));
		// show("researchResultList.typicalItem.names[0]"));
		// show("researchResultList.typicalItem.names[0].value"));
		// show("researchResultList.typicalItem.names(0)"));
		// show("researchResultList.typicalItem.names(0).value"));
		System.out.println("");
		// show("researchResultList.[0].txtItem"));
		// show("researchResultList[0].txtItem"));
		// show("researchResultList.dataGroup"));
		// show("researchResultList.dataGroup.list"));
		// show("researchResultList.dataGroup.length"));
		// show("researchResultList.dataGroup[0].txtItem"));
		// show("researchResultList.dataGroup.list[0].txtItem"));
		// show("researchResultList.dataGroup.list.length"));

		// show("researchResultList[0]"));
		// show("researchResultList.length"));
		// show("researchResultList.list[0].txtItem"));
		// show("researchResultList.names[0]"));
		// System.out.println("");
		// show("researchResultList.dataGroup.dataProvider.list"));
		// show("researchResultList.dataGroup.dataProvider.list.length"));
		// show("researchResultList.dataGroup.dataProvider.list.size"));
		// show("researchResultList.dataGroup.dataProvider"));
		// show("researchResultList.dataGroup.dataProvider.length"));

		// show("dataGroup.typicalItem.names[0]"));
		// show("dataGroup.typicalItem.type"));
		// //assertEquals("Failed to get dataGroup List value", "5",
		// flexUITester.readFrom("researchResultList"));
		//
		// show("researchResultList.names"));
		// show("researchResultList.names[0]"));
		// show("researchResultList.list"));
		// show("researchResultList.list[0]"));
		// show("dataGroup"));
		// show("researchResultList"));
		// flashApp.call("doFlexClick", "searchButton", "");
		// flashApp.call("doFlexType", "", "from selenium flex");
		//
		// assertEquals(100, flashApp.PercentLoaded());
	}
}
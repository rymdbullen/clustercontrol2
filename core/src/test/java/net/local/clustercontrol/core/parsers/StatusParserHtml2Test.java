package net.local.clustercontrol.core.parsers;

import static org.junit.Assert.*;

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.local.clustercontrol.core.parsers.StatusParserHtml;
import net.local.clustercontrol.api.model.xml.JkStatus;

public class StatusParserHtml2Test {
	
	private String body = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\"> <html><head><title>Balancer Manager</title></head>" +
	                      "<body><h1>Load Balancer Manager for 192.168.10.116</h1>  <dl><dt>Server Version: Apache/2.2.11 (Ubuntu) PHP/5.2.6-3ubuntu4.6 with Suhosin-Patch</dt>" +
	                      "<dt>Server Built: Aug 16 2010 17:45:31</dt></dl> <hr /> <h3>LoadBalancer Status for balancer://mycluster</h3>  "+
	                      "  <table border=\"0\" style=\"text-align: left;\"><tr><th>StickySession</th><th>Timeout</th><th>FailoverAttempts</th><th>Method</th></tr>"+
	                      "<tr><td> - </td><td>0</td><td>1</td> <td>byrequests</td> </table> <br />  <table border=\"0\" style=\"text-align: left;\"><tr><th>Worker URL</th>"+
	                      "<th>Route</th><th>RouteRedir</th><th>Factor</th><th>Set</th><th>Status</th><th>Elected</th><th>To</th><th>From</th></tr>"+
	                      "<tr> <td><a href=\"/balancer-manager?b=mycluster&w=ajp://127.0.0.1:8109&nonce=51b485f1-ab7c-42ae-81c3-ee9cc9610b7c\">ajp://127.0.0.1:8109</a></td>"+
	                      "<td>t1</td><td></td><td>1</td><td>0</td><td>Ok</td><td>2</td><td>  0 </td><td>2.0K</td></tr>"+
	                      "<tr> <td><a href=\"/balancer-manager?b=mycluster&w=ajp://127.0.0.1:8209&nonce=51b485f1-ab7c-42ae-81c3-ee9cc9610b7c\">ajp://127.0.0.1:8209</a></td>"+
	                      "<td>t2</td><td></td><td>1</td><td>0</td><td>Ok</td><td>1</td><td>  0 </td><td>1.0K</td></tr> </table> <hr /> "+
	                      "<address>Apache/2.2.11 (Ubuntu) PHP/5.2.6-3ubuntu4.6 with Suhosin-Patch Server at 192.168.10.116 Port 80</address> </body></html> ";

	private String url = "http://192.168.10.116/balancer-manager";

	private String body2 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">"+
			"<html><head><title>Balancer Manager</title></head>"+
			"<body><h1>Load Balancer Manager for 172.18.151.172</h1>"+
			"<dl><dt>Server Version: Apache/2.2.20 (Ubuntu)</dt>"+
			"<dt>Server Built: Nov  7 2011 22:45:49</dt></dl>"+
			"<hr />"+
			"<h3>LoadBalancer Status for balancer://cluster</h3>"+
			"<table border=\"0\" style=\"text-align: left;\"><tr><th>StickySession</th><th>Timeout</th><th>FailoverAttempts</th><th>Method</th></tr>"+
			"<tr><td> - </td><td>0</td><td>5</td>"+
			"<td>bytraffic</td>"+
			"</table>"+
			"<br />"+
			"<table border=\"0\" style=\"text-align: left;\"><tr><th>Worker URL</th><th>Route</th><th>RouteRedir</th><th>Factor</th><th>Set</th><th>Status</th><th>Elected</th><th>To</th><th>From</th></tr>"+
			"<tr>"+
			"<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.172:8009&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.172:8009</a></td><td>s1</td><td></td><td>1</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"+
			"<tr>"+
			"<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.172:8019&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.172:8019</a></td><td>s2</td><td></td><td>2</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"+
			"<tr>"+
			"<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.172:8029&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.172:8029</a></td><td>s3</td><td></td><td>2</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"+
			"<tr>"+
			"<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.174:8009&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.174:8009</a></td><td>s1</td><td></td><td>1</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"+
			"<tr>"+
			"<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.174:8019&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.174:8019</a></td><td>s2</td><td></td><td>2</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"+
			"<tr>"+
			"<td><a href=\"/balancer-manager?b=cluster&w=ajp://172.18.151.174:8029&nonce=57badbdb-e0bf-4cf2-8ad3-375a2ebeaca9\">ajp://172.18.151.174:8029</a></td><td>s3</td><td></td><td>2</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td></tr>"+
			"</table>"+
			"<hr />"+
			"<address>Apache/2.2.20 (Ubuntu) Server at 172.18.151.172 Port 80</address>"+
			"</body></html>";

	private String url2 = "http://172.18.151.172/balancer-manager";

	@BeforeClass
	public static void setUp() throws Exception {
        System.setProperty("catalina.home", "./target");
        System.setProperty("appserver.home.dir", "./target");

        Properties props = new Properties();
        props.put("log4j.appender.unit-test", "org.apache.log4j.ConsoleAppender");
        props.put("log4j.appender.unit-test.layout", "org.apache.log4j.PatternLayout");
        props.put("log4j.appender.unit-test.layout.ConversionPattern", "%-5p %c{2} - %m%n");
        props.put("log4j.rootLogger", "DEBUG, unit-test");
        
        PropertyConfigurator.configure(props);
	}
	
	@Before
	public void before() {
	}	
	
	@Test
	public void getEntityTest() {
		StatusParserHtml ws = new StatusParserHtml(body, url);
		JkStatus jkStatus = ws.getStatus();
		
		assertEquals(new Integer(80), jkStatus.getServer().getPort());
	}
	@Test
	public void getEntity2Test() {
		StatusParserHtml ws = new StatusParserHtml(body2, url2);
		JkStatus jkStatus = ws.getStatus();
		assertEquals(new Integer(6), jkStatus.getBalancers().getBalancer().getMemberCount());
		assertEquals(new Integer(80), jkStatus.getServer().getPort());
	}
	@Test
	public void getHostTest() {
		StatusParserHtml ws = new StatusParserHtml(body, url);
		String value = ws.getStatus().getServer().getName();
		assertEquals("the host was not retrieved correct", "192.168.10.116", value);
	}
	@Test
	public void getHost2Test() {
		StatusParserHtml ws = new StatusParserHtml(body2, url2);
		String value = ws.getStatus().getServer().getName();
		assertEquals("the host was not retrieved correct", "172.18.151.172", value);
	}
}

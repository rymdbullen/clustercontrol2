package net.local.clustercontrol.web.mvc;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Properties;

import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.core.logic.impl.ClusterManager;
import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.core.model.dto.Workers;
import net.local.clustercontrol.core.parsers.StatusParserHtml;

import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClusterControllerTest {

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
            "<address>Apache/2.2.11 (Ubuntu) PHP/5.2.6-3ubuntu4.6 with Suhosin-Patch Server at 192.168.10.116 Port 80</address> </body></html>";

	@BeforeClass
	public static void setUp() throws Exception {
		Properties props = new Properties();
		props.put("log4j.appender.unit-test", "org.apache.log4j.ConsoleAppender");
		props.put("log4j.appender.unit-test.layout", "org.apache.log4j.PatternLayout");
		props.put("log4j.appender.unit-test.layout.ConversionPattern", "%-5p %c{2} - %m%n");
		props.put("log4j.rootLogger", "DEBUG, unit-test");

		PropertyConfigurator.configure(props);
	}
	@Test
	public void convertTest() {
		JkStatus jkStatus = (new StatusParserHtml(body)).getStatus();
		JkStatus jkStatus2 = (new StatusParserHtml(body)).getStatus();
		String server = jkStatus2.getServer().getName();
		server = server.substring(0, server.length()-1) + "7";
		jkStatus2.getServer().setName(server);
		
		ClusterManager cm = new ClusterManager(null, null);
		Cluster cluster = new Cluster();
		ArrayList<JkStatus> statuses = new ArrayList<JkStatus>();
		statuses.add(jkStatus);
		statuses.add(jkStatus2);
		cm.convert(statuses, cluster);
		
		ArrayList<Workers> workers = cluster.getWorkers();
		for (Workers worker : workers) {
			System.out.println(worker.getStatus());
		}
		assertEquals(new Integer(80), jkStatus.getServer().getPort());
	}

	@Test
	public void testConvert() {
		System.out.println("Not implemented");
	}
}

package net.local.clustercontrol.core.model;

import static org.junit.Assert.*;

import org.junit.Test;

import net.local.clustercontrol.core.model.WorkerStatusHtml;
import net.local.clustercontrol.api.model.JkStatus;

public class WorkerStatusHtmlTest {
	
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
	
	@Test
	public void getEntityTest() {
		WorkerStatusHtml ws = new WorkerStatusHtml(body);
		JkStatus jkStatus = ws.getStatus();
		
		assertEquals(new Integer(80), jkStatus.getServer().getPort());
	}
	@Test
	public void getHostTest() {
		WorkerStatusHtml ws = new WorkerStatusHtml(body);
		String value = ws.getStatus().getServer().getName();
		assertEquals("the host was not retrieved correct", "192.168.10.116", value);
	}
}

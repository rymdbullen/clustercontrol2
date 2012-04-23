package net.local.clustercontrol.core.logic.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import net.local.clustercontrol.api.model.xml.JkBalancer;
import net.local.clustercontrol.api.model.xml.JkBalancers;
import net.local.clustercontrol.api.model.xml.JkMap;
import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkResult;
import net.local.clustercontrol.api.model.xml.JkServer;
import net.local.clustercontrol.api.model.xml.JkSoftware;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.api.model.xml.ObjectFactory;
import net.local.clustercontrol.core.http.impl.HttpClient;
import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.core.parsers.StatusParserHtml;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class ManualWorkerFactoryTest {

	@SuppressWarnings("unused")
	private static final String AJP_127_0_0_1_8109 = "ajp://127.0.0.1:8109";
	private static final String AJP_127_0_0_1_8209 = "ajp://127.0.0.1:8209";

	private static final String JAXB_DOMAIN_NAMESPACE = "net.local.clustercontrol.api.model.xml";

	private JkStatus jkStatus = null;
	private HttpClient httpClient;
	
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

	@SuppressWarnings("unused")
	private static final String testUrl = "http://192.168.10.116:8080/balancer-manager";
	private static final String localhostUrl = "http://localhost:8080/balancer-manager";

	/*
	 * 
	 		Worker URL, Route, RouteRedir, Factor, Set, Status, Elected, To, From
            "<tr> <td><a href=\"/balancer-manager?b=mycluster&w=ajp://127.0.0.1:8109&nonce=51b485f1-ab7c-42ae-81c3-ee9cc9610b7c\">ajp://127.0.0.1:8109</a></td>"+
                        t1     -           1       0    Ok      2        0   2.0K
            "<tr> <td><a href=\"/balancer-manager?b=mycluster&w=ajp://127.0.0.1:8209&nonce=51b485f1-ab7c-42ae-81c3-ee9cc9610b7c\">ajp://127.0.0.1:8209</a></td>"+
                        t2     -           1       0    Ok      1        0   1.0K
                        
	 */
	
	
	
	
//	Request header
//	GET /balancer-manager?lf=2&ls=0&wr=&rr=&dw=Disable&w=ajp%3A%2F%2Flocalhost%3A8019&b=cluster&nonce=aaf4843d-0f1c-4542-a8d3-9571d5819a09 HTTP/1.1
//			Host: localhost:8080
//			Connection: keep-alive
//			User-Agent: Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.13 (KHTML, like Gecko) Chrome/18.0.970.0 Safari/535.13
//			Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//			Referer: http://localhost:8080/balancer-manager?lf=2&ls=0&wr=&rr=&dw=Enable&w=ajp%3A%2F%2Flocalhost%3A8019&b=cluster&nonce=aaf4843d-0f1c-4542-a8d3-9571d5819a09
//			Accept-Encoding: gzip,deflate,sdch
//			Accept-Language: en-US,en;q=0.8,sv;q=0.6,fr-CH;q=0.4,fr;q=0.2
//			Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3

	@BeforeClass
	public static void setUp() throws Exception {
        System.setProperty("catalina.home", "./target");
        System.setProperty("appserver.home.dir", "./target");

        Properties props = new Properties();
        props.put("log4j.appender.unit-test", "org.apache.log4j.ConsoleAppender");
        props.put("log4j.appender.unit-test.layout", "org.apache.log4j.PatternLayout");
        props.put("log4j.appender.unit-test.layout.ConversionPattern", "%-5p %c{2} - %m%n");
        props.put("log4j.rootLogger", "TRACE, unit-test");
        
        PropertyConfigurator.configure(props);
	}
	@Before
	public void before() {
		jkStatus = (new StatusParserHtml(body)).getStatus();
		httpClient = new HttpClient();
	}

//	@Test
//	public void testGetAllStatusesPoll() {
//		WorkerFactory workerFactory = new WorkerFactory(httpClient);
//		Cluster cluster = new Cluster();
//		boolean initOk = workerFactory.getAllStatuses(jkStatus, null, "poll", null);
//		assertEquals("This must work, init must be true", true, initOk);
//		System.out.println(cluster.getStatusMessage());
//	}
//	
//	@Test
//	public void testGetAllStatusesDisable() {
//		WorkerFactory workerFactory = new WorkerFactory(httpClient);
//		Cluster cluster = new Cluster();
//		boolean initOk = workerFactory.getAllStatuses(jkStatus, AJP_127_0_0_1_8209, "Disable", "medium");
//		assertEquals("This must work, init must be true", true, initOk);
//		System.out.println(cluster.getStatusMessage());
//	}
	
	@Test
	public void testCreateContextPoll() {
		WorkerFactory workerFactory = new WorkerFactory(httpClient);
		Cluster cluster = new Cluster();
		String initOk = workerFactory.createUrl(jkStatus.getBalancers().getBalancer().getMember().get(0), null, null, "poll");
		assertEquals("This must work, init must be true", "http://192.168.10.116:8080/balancer-manager", initOk);
		System.out.println(cluster.getStatusMessage());
	}
	
	@Test
	public void testCreateContextDisable() {
		WorkerFactory workerFactory = new WorkerFactory(httpClient);
		Cluster cluster = new Cluster();
		String createdContext = workerFactory.createUrl(jkStatus.getBalancers().getBalancer().getMember().get(1), "host", AJP_127_0_0_1_8209, "Disable");
		String expected = "/balancer-manager?b=mycluster&w=ajp://127.0.0.1:8209&nonce=51b485f1-ab7c-42ae-81c3-ee9cc9610b7c&lf=1&ls=0&wr=t2&rr=&dw=Disable";
		assertEquals("This must work, init must be true", expected, createdContext);
		System.out.println(cluster.getStatusMessage());
	}
	
	@Test
	public void testNewInitWorkerFactory() throws MalformedURLException, UnknownHostException {
		WorkerFactory wf = new WorkerFactory(httpClient);
		boolean initOk = wf.init(localhostUrl , null, "poll", null);
		assertEquals("This must work, init must be true", true, initOk);
		assertEquals("There must be one status", 1, wf.getStatuses().size());
	}
	@Test
	public void testNewInitClusterManager() {
		WorkerFactory workerFactory = new WorkerFactory(httpClient);
		ClusterManager clusterManager = new ClusterManager(workerFactory, null);
		Map<String, String> response = clusterManager.init(localhostUrl);
		assertEquals("This must work, init must be 'ok'", "ok", response.get("initStatus"));
		
		assertEquals("There must be one status", 1, workerFactory.getStatuses().size());
		assertEquals("The init must be ok", "ok", clusterManager.getCluster().getStatusMessage());
	}
	@Test
	public void testNewDisableClusterManager() {
		WorkerFactory workerFactory = new WorkerFactory(httpClient);
		ClusterManager clusterManager = new ClusterManager(workerFactory, null);
		Map<String, String> response = clusterManager.init(localhostUrl);
		assertEquals("This must work, init must be 'ok'", "ok", response.get("initStatus"));
		
		boolean initOk = clusterManager.disable("ajp://localhost:8019", "medium");
		assertEquals("This must work, init must be true", true, initOk);
		assertEquals("This must work, init must be true", "ok", clusterManager.getCluster().getStatusMessage());
		
		assertEquals("There must be one status", 1, workerFactory.getStatuses().size());
		assertEquals("The init must be ok", "ok", clusterManager.getCluster().getStatusMessage());
	}
	@Test
	public void testNewEnableClusterManager() {
		WorkerFactory workerFactory = new WorkerFactory(httpClient);
		ClusterManager clusterManager = new ClusterManager(workerFactory, null);
		Map<String, String> response = clusterManager.init(localhostUrl);
		assertEquals("This must work, init must be 'ok'", "ok", response.get("initStatus"));
		
		boolean initOk = clusterManager.enable("ajp://localhost:8019", "medium");
		assertEquals("This must work, init must be true", true, initOk);
		assertEquals("This must work, init must be true", "ok", clusterManager.getCluster().getStatusMessage());
		
		assertEquals("There must be one status", 1, workerFactory.getStatuses().size());
		assertEquals("The init must be ok", "ok", clusterManager.getCluster().getStatusMessage());
	}
	@Test
	public void testNewPollClusterManager() {
		WorkerFactory workerFactory = new WorkerFactory(httpClient);
		ClusterManager clusterManager = new ClusterManager(workerFactory, null);
		Map<String, String> response = clusterManager.init(localhostUrl);
		assertEquals("This must work, init must be 'ok'", "ok", response);
		
		boolean initOk = clusterManager.poll();
		assertEquals("This must work, init must be true", true, initOk);
		assertEquals("This must work, init must be true", "ok", clusterManager.getCluster().getStatusMessage());
		
		assertEquals("There must be one status", 1, workerFactory.getStatuses().size());
		assertEquals("The init must be ok", "ok", clusterManager.getCluster().getStatusMessage());
	}
	
	/**
	 * 
	 * @throws JAXBException
	 * @throws FileNotFoundException 
	 * @throws URISyntaxException 
	 */
	@SuppressWarnings("unchecked")
	//@Before
	public void testUnmarshallStatus() throws JAXBException, FileNotFoundException, URISyntaxException {
		JAXBContext jc = JAXBContext.newInstance(JAXB_DOMAIN_NAMESPACE);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		
		Schema mySchema;
		SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
		try {
			URL url = getClass().getResource("/xsd/jkStatus.xsd");
		    File schemaFile = new File(url.toURI());
			mySchema = sf.newSchema( schemaFile );
		} catch( SAXException saxe ){
		    // ...(error handling)
		    mySchema = null;
		}
		
		unmarshaller.setSchema(mySchema);
		unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
		//
		// Open file
		final FileInputStream fis = new FileInputStream("src/test/resources/status.xml");

		JAXBElement<JkStatus> jkActionStatus = (JAXBElement<JkStatus>) unmarshaller.unmarshal(fis);
		jkStatus = jkActionStatus.getValue();
		assertEquals("Balancers.getCount()", new Integer(1), jkStatus.getBalancers().getCount());
		
		//logger.debug("Balancers.getCount()="+result.getBalancers().getCount());
		System.out.println("Balancers.getCount()="+jkStatus.getBalancers().getCount());
	}
	/**
	 * 
	 * @throws JAXBException
	 * @throws FileNotFoundException 
	 * @throws URISyntaxException 
	 */
	@SuppressWarnings("unchecked")
	public void testUnmarshallActionStatus() throws JAXBException, FileNotFoundException, URISyntaxException {
		JAXBContext jc = JAXBContext.newInstance(JAXB_DOMAIN_NAMESPACE);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		
		Schema mySchema;
		SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
		try {
			URL url = getClass().getResource("/xsd/jkStatus.xsd");
		    File schemaFile = new File(url.toURI());
			mySchema = sf.newSchema( schemaFile );
		} catch( SAXException saxe ){
		    // ...(error handling)
		    mySchema = null;
		}
		
		unmarshaller.setSchema(mySchema);
		unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
		//
		// Open file
		final FileInputStream fis = new FileInputStream("src/test/resources/actionStatus.xml");

		JAXBElement<JkStatus> jkActionStatus = (JAXBElement<JkStatus>) unmarshaller.unmarshal(fis);
		JkStatus result = jkActionStatus.getValue();
		//logger.debug("Result: "+result.getResult().getType()+" "+result.getResult().getMessage());
		System.out.println("Result: "+result.getResult().getType()+" "+result.getResult().getMessage());
	}
	/**
	 * 
	 * @throws JAXBException
	 */
	public void testMarshall() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(JAXB_DOMAIN_NAMESPACE);
		ObjectFactory factory = new ObjectFactory();
		JkStatus status = factory.createJkStatus();
		JkServer server = factory.createJkServer();
		server.setName("localhost");
		status.setServer(server);
		JkResult result = new JkResult();
		result.setMessage("message");
		result.setType("type");
		status.setResult(result);
		JkBalancers balancers = factory.createJkBalancers();
		JkBalancer balancer = factory.createJkBalancer();
		JkMember jkMember = new JkMember(); 
		balancer.getMember().add(jkMember);
		JkMap map = new JkMap();
		balancer.getMap().add(map);
		JkSoftware software = factory.createJkSoftware();
		balancers.setBalancer(balancer);
		
		status.setBalancers(balancers);
		status.setSoftware(software);
		
		Schema mySchema;
		SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
		try {
			URL url = getClass().getResource("/xsd/jkStatus.xsd");
		    File schemaFile = new File(url.toURI());
			mySchema = sf.newSchema( schemaFile );
		} catch( SAXException saxe ){
		    // could not read xsd, set null schema
		    mySchema = null;
		} catch (URISyntaxException e) {
			// could not find xsd, set null schema
			mySchema = null;
		}
		JAXBElement<JkStatus> element = factory.createStatus(status);
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.setSchema(mySchema);
		m.marshal(element, System.out);
	}
}

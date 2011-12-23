package net.local.clustercontrol.core.http.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import net.local.clustercontrol.api.model.xml.Host;
import net.local.clustercontrol.api.model.xml.WorkerResponse;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ManualHttpClientTest {
	private HttpClient httpClient;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
        System.setProperty("catalina.home", "./target");
        System.setProperty("appserver.home.dir", "./target");

        Properties props = new Properties();
        props.put("log4j.appender.unit-test", "org.apache.log4j.ConsoleAppender");
        props.put("log4j.appender.unit-test.layout", "org.apache.log4j.PatternLayout");
        props.put("log4j.appender.unit-test.layout.ConversionPattern", "%-5p %c{2} - %m%n");
        props.put("log4j.rootLogger", "DEBUG, unit-test");
        
        props.put("log4j.category.ch.romandeenergie.gv", "TRACE");

        PropertyConfigurator.configure(props);
	}

	@Before
	public void setUp() {
		//httpClient = mock(HttpClient.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPerformActionOnHost() throws ClientProtocolException, IOException {
//		Host host = new Host();
//		host.setContext("/balancer-manager");
//		host.setIpAddress("localhost");
//		host.setPort(8080);
		httpClient = new HttpClient(); 
		URL url = new URL("http://localhost:8080/balancer-manager");
		WorkerResponse response = httpClient.performActionOnHost(url );
		DefaultHttpClient httpclient = mock(DefaultHttpClient.class);
		when(httpclient.execute(isA(HttpGet.class), isA(ResponseHandler.class))).thenReturn("body");
		
		assertNotNull(response.getBody());
	}

}

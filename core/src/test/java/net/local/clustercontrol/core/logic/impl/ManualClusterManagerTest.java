/**
 * 
 */
package net.local.clustercontrol.core.logic.impl;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.http.client.HttpResponseException;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.local.clustercontrol.core.configuration.Constants;
import net.local.clustercontrol.core.http.impl.HttpClient;
import net.local.clustercontrol.core.logic.WorkerNotFoundException;
import net.local.clustercontrol.core.logic.impl.ClusterManager;
import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.core.model.dto.WorkerStatus;
import net.local.clustercontrol.core.model.dto.Workers;

/**
 * @author admin
 */
public class ManualClusterManagerTest {

	private static final Logger logger = LoggerFactory.getLogger(ManualClusterManagerTest.class);

	private ClusterManager clusterManager;

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
		HttpClient httpClient = new HttpClient();
		WorkerFactory workerFactory = new WorkerFactory(httpClient);
		clusterManager = new ClusterManager(workerFactory);
	}	
	
	/**
	 * Test method for {@link net.local.clustercontrol.logic.ClusterManager#init(String)}.
	 * @throws MalformedURLException 
	 * @throws WorkerNotFoundException 
	 */
	@Test
	public void testCycle() throws MalformedURLException, WorkerNotFoundException {
		logger.debug("Running testInit");
		String url = Constants.TEST_URL;
		
		boolean response = clusterManager.init(url);
		assertEquals(true, response);
		
		Cluster cluster = clusterManager.getCluster();
		assertEquals(true, cluster.getStatusMessage() == null);
		assertEquals(2,cluster.getWorkers().size());
		assertEquals(2,cluster.getWorkerNames().size());
		assertEquals(1,cluster.getHostNames().size());
		
		ArrayList<Workers> workers = cluster.getWorkers();
		for (int index = 0; index < workers.size(); index++) {
			Workers worker = workers.get(index);
			ArrayList<WorkerStatus> statuses = worker.getStatuses();
			for (WorkerStatus workerStatus : statuses) {
				logger.debug("Found: worker: "+worker.getName()+" on "+workerStatus.getHostName()+": "+workerStatus.getStatus());
			}
			assertTrue("Number of statuses for one host must be 1", statuses.size()==1);
		}
		
		// poll
		clusterManager.poll();
		cluster = clusterManager.getCluster();
		assertEquals("ok", cluster.getStatusMessage());
		assertEquals(2,cluster.getWorkers().size());
		assertEquals(2,cluster.getWorkerNames().size());
		assertEquals(1,cluster.getHostNames().size());
		
		// disable
		clusterManager.enable("s1", "medium");
		cluster = clusterManager.getCluster();
		assertEquals("ok", cluster.getStatusMessage());
		assertEquals(2,cluster.getWorkers().size());
		assertEquals(2,cluster.getWorkerNames().size());
		assertEquals(1,cluster.getHostNames().size());
	}
	/**
	 * Test method for {@link net.local.clustercontrol.logic.ClusterManager#initCluster(java.net.URL)}.
	 * @throws MalformedURLException 
	 */
	@Test
	public void testBadInit() throws MalformedURLException {
		logger.debug("Running testInit");
		boolean init = clusterManager.init("http://192.168.10.115/jkger");
		assertFalse("Init must not succeed", init);
		String message = clusterManager.getCluster().getStatusMessage();
		assertNotNull(message);
		assertEquals("nok", message);
	}
	/**
	 * Test method for {@link net.local.clustercontrol.logic.ClusterManager#activate(String)}.
	 */
	@Test
	public void testEnable() throws HttpResponseException {
		logger.debug("Running testEnable");
		String worker = "s1";
		String speed = "medium";
		boolean initOk = clusterManager.enable(worker, speed);
		assertEquals(true, initOk);
		ArrayList<Workers> workerLists = clusterManager.getCluster().getWorkers();
		for (int i = 0; i < workerLists.size(); i++) {
			Workers workerList = workerLists.get(i);
			ArrayList<WorkerStatus> statuses = workerList.getStatuses();
			for (int index = 0; index < statuses.size(); index++) {
				WorkerStatus workerStatus = statuses.get(index);
				logger.debug("["+i+":"+index+"]: "+workerStatus.getHostName()+" "+workerStatus.getStatus());
				if(worker.equals(workerStatus.getHostName())) {
					if(workerStatus.getStatus().equalsIgnoreCase("Ok")) {						
						assertEquals("Ok", workerStatus.getStatus());
					} else if(workerStatus.getStatus().equalsIgnoreCase("act")) {						
						assertEquals("ACT", workerStatus.getStatus());
					}
				}
			}
		}
	}
	/**
	 * Test method for {@link net.local.clustercontrol.logic.ClusterManager#disable(String)}.
	 */
	@Test
	public void testDisableUnmarshall() throws HttpResponseException {
		logger.debug("Running testDisable");
		String worker = "s1";
		String speed = "medium";
		boolean initOk = clusterManager.disable(worker, speed);
		assertEquals("This must work, init must be true", true, initOk);
		assertEquals("This must work, init must be true", "ok", clusterManager.getCluster().getStatusMessage());

		ArrayList<Workers> workerLists = clusterManager.getCluster().getWorkers();
		for (int i = 0; i < workerLists.size(); i++) {
			Workers workerList = workerLists.get(i);
			ArrayList<WorkerStatus> statuses = workerList.getStatuses();
			for (int index = 0; index < statuses.size(); index++) {
				WorkerStatus workerStatus = statuses.get(index);
				logger.debug("["+i+":"+index+"]: "+workerStatus.getHostName()+" "+workerStatus.getStatus());
				if(worker.equals(workerStatus.getHostName())) {
					if(workerStatus.getStatus().equalsIgnoreCase("Dis")) {						
						assertEquals("Dis", workerStatus.getStatus());
					} else if(workerStatus.getStatus().equalsIgnoreCase("Dis")) {						
						assertEquals("DIS", workerStatus.getStatus());
					}
				}
			}
		}
	}
}
/**
 * 
 */
package net.local.clustercontrol.core.integration;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import net.local.clustercontrol.core.configuration.Constants;
import net.local.clustercontrol.api.model.xml.JkBalancer;
import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.core.logic.IClusterManager;
import net.local.clustercontrol.core.parsers.StatusParserXml;
import junit.framework.TestCase;

/**
 * @author admin
 *
 */
public class WorkerStatusTest {

	private static final Logger logger = LoggerFactory.getLogger(WorkerStatusTest.class);

	@Autowired
	private IClusterManager clusterManager;
//	@Autowired
//	private IWorkerFactory workerFactory;
//	@Autowired
//	private IHttpClient httpClient;
	
	@Before
	protected void setUp() throws Exception {
		logger.debug("==================================================");
		logger.debug("Running tests against: "+Constants.TEST_URL);
		clusterManager.init(Constants.TEST_URL);
	}

	/**
	 * Test method for {@link StatusParserXml.avegagroup.clustercontrol.util.WorkerStatusXML#unmarshal(java.lang.String)}.
	 */
	public void testActivateUnmarshall() {
		logger.debug("Running testActivateUnmarshall");
		String workerName = "footprint1";
		String speed = "medium";
		boolean initOk = clusterManager.enable(workerName, speed);
		
		ArrayList<JkStatus> workerLists = null;
		for (int i = 0; i < workerLists.size(); i++) {
			JkStatus workerList = workerLists.get(i);
			JkBalancer balancer = workerList.getBalancers().getBalancer();
			for (int index = 0; index < balancer.getMemberCount(); index++) {
				JkMember workerStatus = balancer.getMember().get(index);
				logger.debug("["+i+":"+index+"]: "+workerStatus.getName()+" "+workerStatus.getActivation());
				if(workerName.equals(workerStatus.getName())) {
					assertEquals("ACT", workerStatus.getActivation());
				}
			}
		}
	}
	/**
	 * Test method for {@link StatusParserXml.avegagroup.clustercontrol.util.WorkerStatusXML#unmarshal(java.lang.String)}.
	 */
	public void testDisableUnmarshall() {
		logger.debug("Running testDisableUnmarshall");
		String worker = "footprint1";
		String speed = "medium";
		clusterManager.disable(worker, speed);
		ArrayList<JkStatus> workerLists = null;
		for (int i = 0; i < workerLists.size(); i++) {
			JkStatus workerList = workerLists.get(i);
			JkBalancer balancer = workerList.getBalancers().getBalancer();
			for (int index = 0; index < balancer.getMemberCount(); index++) {
				JkMember workerStatus = balancer.getMember().get(index);
				logger.debug("["+i+":"+index+"]: "+workerStatus.getName()+" "+workerStatus.getActivation());
				if(worker.equals(workerStatus.getName())) {
					assertEquals("DIS", workerStatus.getActivation());
				}
			}
		}
	}
}

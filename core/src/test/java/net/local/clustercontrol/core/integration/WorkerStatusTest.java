/**
 * 
 */
package net.local.clustercontrol.core.integration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import net.local.clustercontrol.core.configuration.Constants;
import net.local.clustercontrol.api.model.xml.Hosts;
import net.local.clustercontrol.api.model.xml.JkBalancer;
import net.local.clustercontrol.api.model.xml.JkBalancers;
import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.api.model.xml.WorkerResponse;
import net.local.clustercontrol.api.model.xml.WorkerResponses;
import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.logic.IWorkerFactory;
import net.local.clustercontrol.core.logic.IWorkerManager;
import net.local.clustercontrol.core.logic.impl.ClusterManager;
import net.local.clustercontrol.core.model.dto.Cluster;
import net.local.clustercontrol.core.parsers.StatusParserXML;
import junit.framework.TestCase;

/**
 * @author admin
 *
 */
public class WorkerStatusTest extends TestCase {

	private static final Logger logger = LoggerFactory.getLogger(WorkerStatusTest.class);

	@Autowired
	private IWorkerManager clusterManager;
	@Autowired
	private IWorkerFactory workerFactory;
	@Autowired
	private IHttpClient httpClient;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logger.debug("==================================================");
		logger.debug("Running tests against: "+Constants.TEST_URL);
		clusterManager.init(Constants.TEST_URL);
	}
	/**
	 * Test method for {@link StatusParserXML.avegagroup.clustercontrol.util.WorkerStatusXML#unmarshal(java.lang.String)}.
	 */
	public void testGetStatusUnmarshall() {
		
		logger.debug("Running testGetStatusUnmarshall");
		
		WorkerResponses workerResponses = httpClient.getWorkerResponseForAction(null);
		
		int hostsCount = workerResponses.getResponseList().size();
		for (int hostIdx = 0; hostIdx < hostsCount; hostIdx++) {
			WorkerResponse workerResponse = workerResponses.getResponseList().get(hostIdx);
			StatusParserXML workerStatus = new StatusParserXML(workerResponse.getBody());
			JkStatus jkStatus = workerStatus.getStatus();
			assertNotNull(jkStatus);
			assertEquals(new Integer(1), jkStatus.getBalancers().getCount());
			JkBalancers balancers =  jkStatus.getBalancers();
			assertEquals(new Integer(4), balancers.getBalancer().getMemberCount());
			List<JkMember> members = balancers.getBalancer().getMember();
			Iterator<JkMember> membersIter = members.iterator();
			while (membersIter.hasNext()) {
				JkMember jkMember = (JkMember) membersIter.next();
				logger.debug("["+hostIdx+"]: "+jkMember.getName()+" "+jkMember.getActivation()+" "+jkMember.getState());
			}
		}
	}
	/**
	 * Test method for {@link StatusParserXML.avegagroup.clustercontrol.util.WorkerStatusXML#unmarshal(java.lang.String)}.
	 */
	public void testActivateUnmarshall() {
		logger.debug("Running testActivateUnmarshall");
		String workerName = "footprint1";
		String speed = "medium";
		Cluster cluster = clusterManager.enable(workerName, speed);
		cluster.getWorkers();
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
	 * Test method for {@link StatusParserXML.avegagroup.clustercontrol.util.WorkerStatusXML#unmarshal(java.lang.String)}.
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

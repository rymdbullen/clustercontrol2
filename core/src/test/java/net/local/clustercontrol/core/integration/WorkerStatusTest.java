/**
 * 
 */
package net.local.clustercontrol.core.integration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.local.clustercontrol.core.configuration.Constants;
import net.local.clustercontrol.model.JkBalancer;
import net.local.clustercontrol.model.JkBalancers;
import net.local.clustercontrol.model.JkMember;
import net.local.clustercontrol.model.JkStatus;
import net.local.clustercontrol.model.WorkerResponse;
import net.local.clustercontrol.model.WorkerResponses;
import net.local.clustercontrol.core.logic.impl.WorkerManager;
import net.local.clustercontrol.core.model.WorkerStatusXML;
import junit.framework.TestCase;

/**
 * @author admin
 *
 */
public class WorkerStatusTest extends TestCase {

	private static final Logger logger = LoggerFactory.getLogger(WorkerStatusTest.class);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logger.debug("==================================================");
		logger.debug("Running tests against: "+Constants.TEST_URL);
		WorkerManager.init(Constants.TEST_URL);
	}
	/**
	 * Test method for {@link se.avegagroup.clustercontrol.util.WorkerStatusXML#unmarshal(java.lang.String)}.
	 */
	public void testGetStatusUnmarshall() {
		
		logger.debug("Running testGetStatusUnmarshall");
		
		WorkerResponses workerResponses = WorkerManager.getStatus("xml");
		
		int hostsCount = workerResponses.getResponseList().size();
		for (int hostIdx = 0; hostIdx < hostsCount; hostIdx++) {
			WorkerResponse workerResponse = workerResponses.getResponseList().get(hostIdx);
			WorkerStatusXML workerStatus = new WorkerStatusXML(workerResponse.getBody());
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
	 * Test method for {@link se.avegagroup.clustercontrol.util.WorkerStatusXML#unmarshal(java.lang.String)}.
	 */
	public void testActivateUnmarshall() {
		logger.debug("Running testActivateUnmarshall");
		String worker = "footprint1";
		ArrayList<JkStatus> workerLists = WorkerManager.activate(worker);
		for (int i = 0; i < workerLists.size(); i++) {
			JkStatus workerList = workerLists.get(i);
			JkBalancer balancer = workerList.getBalancers().getBalancer();
			for (int index = 0; index < balancer.getMemberCount(); index++) {
				JkMember workerStatus = balancer.getMember().get(index);
				logger.debug("["+i+":"+index+"]: "+workerStatus.getName()+" "+workerStatus.getActivation());
				if(worker.equals(workerStatus.getName())) {
					assertEquals("ACT", workerStatus.getActivation());
				}
			}
		}
	}
	/**
	 * Test method for {@link se.avegagroup.clustercontrol.util.WorkerStatusXML#unmarshal(java.lang.String)}.
	 */
	public void testDisableUnmarshall() {
		logger.debug("Running testDisableUnmarshall");
		String worker = "footprint1";
		ArrayList<JkStatus> workerLists = WorkerManager.disable(worker);
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

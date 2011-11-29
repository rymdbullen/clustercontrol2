/**
 * 
 */
package net.local.clustercontrol.core.integration;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.local.clustercontrol.core.configuration.Constants;
import net.local.clustercontrol.core.logic.WorkerNotFoundException;
import net.local.clustercontrol.core.logic.impl.WorkerManager;
import net.local.clustercontrol.api.model.JkBalancer;
import net.local.clustercontrol.api.model.JkMember;
import net.local.clustercontrol.api.model.JkStatus;
import net.local.clustercontrol.api.model.WorkerResponse;
import junit.framework.TestCase;

/**
 * @author admin
 */
public class WorkerManagerTest extends TestCase {

	private static final Logger logger = LoggerFactory.getLogger(WorkerManagerTest.class);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logger.debug("==================================================");

	}
	/**
	 * Test method for {@link se.avegagroup.clustercontrol.logic.WorkerManager#getBalancers(java.net.URL)}.
	 * @throws MalformedURLException 
	 * @throws WorkerNotFoundException 
	 */
	public void testInitDouble() throws MalformedURLException, WorkerNotFoundException {
		WorkerManager.reset();
		logger.debug("Running testInit");
		String urll = Constants.TEST_URL;
		
		ArrayList<JkStatus> response = WorkerManager.init(urll);		
//		if(response.getError()!=null) {
//			assertEquals(true, response.getBody()==null);
//			assertEquals(true, response.getError().getMessageKey()!=null);
//			assertEquals(true, response.getError().getMessageKey().equals(""));			
//		} else {			
//			assertEquals(true, response.getBody()!=null);
			assertEquals(2,response.size());
			assertEquals(2,WorkerManager.getHosts().size());
//		}
		
		response = WorkerManager.init(urll);
//		if(response.getError()!=null) {
//			assertEquals(true, response.getBody()==null);
//			assertEquals(true, response.getError().getMessageKey()!=null);
//			assertEquals(true, response.getError().getMessageKey().equals(""));			
//		} else {			
//			assertEquals(true, response.getBody()!=null);
			assertEquals(2,response.size());
			assertEquals(2,WorkerManager.getHosts().size());
//		}
	}		
	/**
	 * Test method for {@link se.avegagroup.clustercontrol.logic.WorkerManager#init(java.net.URL)}.
	 * @throws MalformedURLException 
	 */
	public void testBadInit() throws MalformedURLException {
		WorkerManager.reset();
		logger.debug("Running testInit");
		try {
			ArrayList<JkStatus> responses = WorkerManager.init("http://192.168.10.115/jkger");
			for (int index = 0; index < responses.size(); index++) {
				JkStatus response = responses.get(index);
				assertNotNull(response.getBalancers().getBalancer().getMember());
			}
		} catch (WorkerNotFoundException e) {
			return;
		}
		fail("Expected WorkerNotFoundException");
	}
	/**
	 * Test method for {@link se.avegagroup.clustercontrol.logic.WorkerManager#getBalancers(java.net.URL)}.
	 * @throws MalformedURLException 
	 * @throws WorkerNotFoundException 
	 */
	public void testInit() throws MalformedURLException, WorkerNotFoundException{
		WorkerManager.reset();
		logger.debug("Running testInit");
		String urll = Constants.TEST_URL;
		
		WorkerResponse response = WorkerManager.getBalancers(urll);
		JkStatus balancer = WorkerManager.statusComplex(response);
	
		assertNotNull(balancer);
		List<JkMember> members = balancer.getBalancers().getBalancer().getMember();
		Iterator<JkMember> membersIter = members.iterator();
		while (membersIter.hasNext()) {
			JkMember jkMember = (JkMember) membersIter.next();
			logger.debug(jkMember.getName()+" "+jkMember.getActivation()+" "+jkMember.getState());
		}
	}
	/**
	 * Test method for {@link se.avegagroup.clustercontrol.logic.WorkerManager#activate(String)}.
	 */
	public void testActivateUnmarshall() throws HttpResponseException {
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
	 * Test method for {@link se.avegagroup.clustercontrol.logic.WorkerManager#disable(String)}.
	 */
	public void testDisableUnmarshall() throws HttpResponseException {
		logger.debug("Running testDisableUnmarshall");
		String worker = "footprint1";
		ArrayList<JkStatus> workerLists = WorkerManager.disable(worker);
		for (int i = 0; i < workerLists.size(); i++) {
			JkStatus workerList = workerLists.get(i);
			JkBalancer balancer = workerList.getBalancers().getBalancer();
			for (int index = 0; index < balancer.getMemberCount(); index++) {
				JkMember workerStatus = balancer.getMember().get(index);
				logger.debug("["+index+"]: "+workerStatus.getName()+" "+workerStatus.getActivation());
				if(worker.equals(workerStatus.getName())) {
					assertEquals("DIS", workerStatus.getActivation());
				}
			}
		}
	}
}

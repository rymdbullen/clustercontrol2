package net.local.clustercontrol.core.integration;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.local.clustercontrol.core.configuration.Constants;
import net.local.clustercontrol.api.model.JkBalancer;
import net.local.clustercontrol.api.model.JkMember;
import net.local.clustercontrol.api.model.JkStatus;
import net.local.clustercontrol.core.logic.impl.WorkerManager;
import junit.framework.TestCase;

public class ControllerActionBeanTest extends TestCase {
	private static final Logger logger = LoggerFactory.getLogger(ControllerActionBeanTest.class);
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logger.debug("==================================================");

	}
//	public void testMath() {
//		
//		int[] inte = new int[] {0, 1, 2, 3, 5, 6};
//		int[] degreesSlow = new int[] {0, 90, 155};
//		int[] degreesMedium = new int[] {0, 135, 155};
//		int[] degreesAggressive = new int[] {0, 155, 165};
//		double defaultTime = 90;
//		
//		System.out.println("");
//		System.out.println("");
//		for (int workersIdx = 0; workersIdx < inte.length; workersIdx++) {
//			if(workersIdx==0) {
//				int weight = degreesSlow[0];
//				double angle = weight * 2.0 * Math.PI/360.0;
//				System.out.println("["+workersIdx+"] "+weight+": "+ Math.round(Math.sin(angle)*defaultTime));
//			} else if(workersIdx==1) {
//				int weight = degreesSlow[1];
//				double angle = weight * 2.0 * Math.PI/360.0;
//				System.out.println("["+workersIdx+"] "+weight+": "+Math.round(Math.sin(angle)*defaultTime));
//			} else {
//				int weight = degreesSlow[2];
//				double angle = weight * 2.0 * Math.PI/360.0;
//				System.out.println("["+workersIdx+"] "+weight+": "+Math.round(Math.sin(angle)*defaultTime));
//			}
//		}
//		System.out.println("");
//		System.out.println("");
//		
//		for (int workersIdx = 0; workersIdx < inte.length; workersIdx++) {
//			if(workersIdx==0) {
//				int weight = degreesMedium[0];
//				double angle = weight * 2.0 * Math.PI/360.0;
//				System.out.println("["+workersIdx+"] "+weight+": "+ Math.round(Math.sin(angle)*defaultTime));
//			} else if(workersIdx==1) {
//				int weight = degreesMedium[1];
//				double angle = weight * 2.0 * Math.PI/360.0;
//				System.out.println("["+workersIdx+"] "+weight+": "+ Math.round(Math.sin(angle)*defaultTime));
//			} else {
//				int weight = degreesMedium[2];
//				double angle = weight * 2.0 * Math.PI/360.0;
//				System.out.println("["+workersIdx+"] "+weight+": "+ Math.round(Math.sin(angle)*defaultTime));
//			}
//		}
//		System.out.println("");
//		System.out.println("");
//		
//		for (int workersIdx = 0; workersIdx < inte.length; workersIdx++) {
//			if(workersIdx==0) {
//				int weight = degreesAggressive[0];
//				double angle = weight * 2.0 * Math.PI/360.0;
//				System.out.println("["+workersIdx+"] "+weight+": "+ Math.round(Math.sin(angle)*defaultTime));
//			} else if(workersIdx==1) {
//				int weight = degreesAggressive[1];
//				double angle = weight * 2.0 * Math.PI/360.0;
//				System.out.println("["+workersIdx+"] "+weight+": "+ Math.round(Math.sin(angle)*defaultTime));
//			} else {
//				int weight = degreesAggressive[2];
//				double angle = weight * 2.0 * Math.PI/360.0;
//				System.out.println("["+workersIdx+"] "+weight+": "+ Math.round(Math.sin(angle)*defaultTime));
//			}
//		}		
//	}

	// Consider using rmock or mockit
	
	public void testInitWithUrl() throws MalformedURLException {
		WorkerManager.reset();
		logger.debug("Running tests against: "+Constants.TEST_URL);
		ArrayList<JkStatus> balancerList = null;//ControllerAction.initWithUrl(Constants.TEST_URL);
		Iterator<JkStatus> listIter = balancerList.iterator();
		System.out.println("Found these hosts:");
		while (listIter.hasNext()) {
			JkStatus jkStatus = (JkStatus) listIter.next();
			System.out.println(jkStatus.getServer().getName());
		}
		assertEquals(2, balancerList.size());
		assertEquals(2, WorkerManager.getHosts().size());
	}
	public void testDisable() {
		logger.debug("Running testDisable");
		String worker = "footprint1";
		ArrayList<JkStatus> workerLists = null;//ControllerAction.disable(worker);
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
	public void testActivate() {
		logger.debug("Running testActivate");
		String worker = "footprint1";
		ArrayList<JkStatus> workerLists = null;//ControllerAction.activate(worker);
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
}

package net.local.clustercontrol.core.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.local.clustercontrol.model.JkBalancer;
import net.local.clustercontrol.model.JkBalancers;
import net.local.clustercontrol.model.JkMember;
import net.local.clustercontrol.model.JkServer;
import net.local.clustercontrol.model.JkStatus;

public class WorkerStatusHtml extends IWorkerStatus {
	
	private static final Logger logger = LoggerFactory.getLogger(WorkerStatusHtml.class);
	public static final int _1WORKER_URL = 1;
	public static final int _2ROUTE = 2;
	public static final int _3ROUTE_REDIR = 3;
	public static final int _4FACTOR = 4;
	public static final int _5SET = 5;
	public static final int _6STATUS = 6;
	public static final int _7ELECTED = 7;
	public static final int _8TO = 8;
	public static final int _9FROM = 9;
	
	public WorkerStatusHtml(String body) {
		getJkStatus(body);
	}
	
	/**
	 * Creates and returns the {@link JkStatus} from the balancer-manager HTML page
	 * @param body
	 * @return the {@link JkStatus} from the balancer-manager HTML page
	 */
	private void getJkStatus(String body) {
		JkStatus jkStatus = new JkStatus();
		Pattern pattern = Pattern.compile("<td>(.*?)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td>");

		Pattern patternRow = Pattern.compile("<tr>\\s{0,2}(<td><a href.*?)</tr>");
		Matcher matcherRow = patternRow.matcher(body);
		while(matcherRow.find()) {
            String row = matcherRow.group(1);
    		if(logger.isTraceEnabled()) {
    			logger.trace("Pattern: ["+patternRow.pattern()+"]: "+row);
    		}
            Matcher matcher = pattern.matcher(row);
            while (matcher.find()) {
            	JkBalancers jkBalancers = new JkBalancers();
            	JkBalancer jkBalancer = new JkBalancer();
            	JkMember member = new JkMember();
            	member.setLbfactor(Integer.valueOf(matcher.group(_4FACTOR)));
            	member.setAddress(matcher.group(_1WORKER_URL));
            	member.setActivation(matcher.group(_6STATUS));
            	member.setName(matcher.group(_2ROUTE));
            	member.setRoute(matcher.group(_2ROUTE));
            	member.setElected(Integer.valueOf(matcher.group(_7ELECTED)));
            	member.setState(matcher.group(_6STATUS));
            	
				jkBalancer.getMember().add(member);
				jkBalancers.setBalancer(jkBalancer);
				jkStatus.setBalancers(jkBalancers);
            	
            }     
            jkStatus.setServer(getJkServer(body));
        }
		
		this.jkStatus = jkStatus;
	}
	/**
	 * 
	 * @param body
	 * @return
	 */
	JkServer getJkServer(String body) {
		Pattern pattern = Pattern.compile(".*<address>.*erver at (.*) Port (.*)</address>.*");
		Matcher matcher = pattern.matcher(body);
		if (matcher.matches()) {
        	JkServer jkServer = new JkServer();
        	jkServer.setName(matcher.group(1));
        	jkServer.setPort(Integer.parseInt(matcher.group(2)));
        	return jkServer;
		}
		if(logger.isTraceEnabled()) {
			logger.trace("Failed to match with pattern: "+pattern.pattern());
		}
		return null;
	}
	
	/**
	 * 
	 * @param body
	 * @return
	 */
	String getHost(String body) {
		String text = "Load Balancer Manager for ";
		Pattern pattern = Pattern.compile(".*<h1>"+text+"(.*)</h1>.*");
		Matcher matcher = pattern.matcher(body);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		if(logger.isTraceEnabled()) {
			logger.trace("Failed to match with pattern: "+pattern.pattern());
		}
		int beginIndex = body.indexOf("<body><h1>");
		if(beginIndex<0) {
			return null;
		}
		String tag = "<body><h1>"+text;
		int endIndex = body.indexOf("</h1>", beginIndex);
		if(endIndex<0) {
			return null;
		}
		
		return body.substring(beginIndex+tag.length(), endIndex);
	}
}
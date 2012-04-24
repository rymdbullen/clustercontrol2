package net.local.clustercontrol.core.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.local.clustercontrol.api.model.xml.JkBalancer;
import net.local.clustercontrol.api.model.xml.JkBalancers;
import net.local.clustercontrol.api.model.xml.JkMember;
import net.local.clustercontrol.api.model.xml.JkServer;
import net.local.clustercontrol.api.model.xml.JkStatus;
import net.local.clustercontrol.core.logic.ControlCommandException;

/**
 * Class parsing the following html status 
 * <td><a href="/balancer-manager?b=cluster&w=ajp://localhost:8009&nonce=3af62151-30da-4ea5-85fc-eb3c7c37d564">ajp://localhost:8009</a></td><td>s1</td><td></td><td>1</td><td>0</td><td>Ok</td><td>0</td><td>  0 </td><td>  0 </td>
 * 
 * @author jstenvall
 */
public class StatusParserHtml extends IStatusParser {
	
	private static final Logger logger = LoggerFactory.getLogger(StatusParserHtml.class);
	private static final Pattern workerPattern = Pattern.compile("<td>(.*?)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td>");
	private static final Pattern workerAddressPattern = Pattern.compile("<a href=\"(.*b=(.*)&(?:w=(.*))&.*)\">(.*)</a>");

	public static final int _1WORKER_URL = 1;
	public static final int _2ROUTE = 2;
	public static final int _3ROUTE_REDIR = 3;
	public static final int _4FACTOR = 4;
	public static final int _5SET = 5;
	public static final int _6STATUS = 6;
	public static final int _7ELECTED = 7;
	public static final int _8TO = 8;
	public static final int _9FROM = 9;
	
	public StatusParserHtml(String body) {
		if(body==null) {
			return;
		}
		parseJkStatus(body);
	}
	
	/**
	 * Creates and populates the {@link JkStatus} from the balancer-manager HTML page
	 * <td><a href="/balancer-manager?b=mycluster&w=ajp://127.0.0.1:8109&nonce=51b485f1-ab7c-42ae-81c3-ee9cc9610b7c">ajp://127.0.0.1:8109</a></td><td>t1</td><td></td><td>1</td><td>0</td><td>Ok</td><td>2</td><td>  0 </td><td>2.0K</td>
	 * @param body
	 * @return the {@link JkStatus} from the balancer-manager HTML page
	 */
	private void parseJkStatus(String body) {
		
		// host 
		JkStatus jkStatus = new JkStatus();
		jkStatus.setServer(getJkServer(body));
		
		JkBalancer jkBalancer = new JkBalancer();

		Pattern patternRow = Pattern.compile("<tr>\\s{0,2}(<td><a href.*?)</tr>");
		Matcher matcherRow = patternRow.matcher(body);
		while(matcherRow.find()) {
            String row = matcherRow.group(1);
//if(logger.isTraceEnabled()) { logger.trace("Pattern: \n["+patternRow.pattern()+"]: \n"+row); }
            Matcher matcher = workerPattern.matcher(row);
            while (matcher.find()) {            	
//if(logger.isTraceEnabled()) { logger.trace("Pattern: \n["+workerPattern.pattern()+"]: \n"+row); }
            	parseJkStatus(jkBalancer, matcher);
            }
        }
		
		JkBalancers jkBalancers = new JkBalancers();
		jkBalancers.setCount(1);
		jkBalancer.setMemberCount(jkBalancer.getMember().size());
		jkBalancers.setBalancer(jkBalancer);
		jkStatus.setBalancers(jkBalancers);
		
		this.jkStatus = jkStatus;
	}
	/**
	 * Creates and populates the {@link JkStatus} from the balancer-manager HTML page
	 * @param jkBalancer
	 * @param matcher
	 */
	private void parseJkStatus(JkBalancer jkBalancer, Matcher matcher) {
		String balancerName;
		JkMember member = new JkMember();
		
		String address = null;
		String workerPort = null;
		String context = null;
		
		String workerAddressText = matcher.group(_1WORKER_URL);
		String workerName = null;
		Matcher workerAddressMatcher = workerAddressPattern.matcher(workerAddressText);
		if(workerAddressMatcher.matches()) {
//if(logger.isTraceEnabled()) { logger.trace("Pattern: \n["+workerAddressPattern.pattern()+"]: \n"+workerAddressText); }
			
			balancerName = workerAddressMatcher.group(2);
			workerName = workerAddressMatcher.group(3);
			String workerAddress = workerAddressMatcher.group(4);
			int endIndex = workerAddress.lastIndexOf(':');
			if(endIndex<0) {
				throw new ControlCommandException("Failed to find adress");
			}
			workerPort = workerAddress.substring(endIndex+1);
			int beginIndex = workerAddress.indexOf("://");
			if(beginIndex>0) {
				address = workerAddress.substring(beginIndex+3, endIndex);
			}
			context = workerAddressMatcher.group(1);
			if(logger.isTraceEnabled()) { logger.trace("Parsed: balancerName: "+balancerName+", address: "+address+", workerName: "+workerName+", workerPort:"+workerPort + ", context: "+context); }            		
		} else {
			throw new IllegalArgumentException("Failed to find balancer name and address using pattern: "+workerAddressPattern.pattern());
		}
		
		member.setHost(address);
		member.setPort(Integer.valueOf(workerPort));
		member.setAddress(address); 
		member.setActivation(matcher.group(_6STATUS).trim());
		member.setName(workerName);
		member.setRoute(matcher.group(_2ROUTE).trim());
		member.setRedirect(matcher.group(_3ROUTE_REDIR).trim());
		member.setElected(Integer.valueOf(matcher.group(_7ELECTED).trim()));
		member.setState(matcher.group(_6STATUS).trim());
		member.setRead(Integer.valueOf(matcher.group(_5SET).trim()));
		member.setBusy(Integer.valueOf(matcher.group(_8TO).trim()));
		member.setLbfactor(Integer.valueOf(matcher.group(_4FACTOR).trim()));
		float txFloat = Float.valueOf(matcher.group(_9FROM).replace('K', ' ').trim());
		int txInt = Math.round(txFloat);
		member.setTransferred(txInt);
		member.setType(context.trim());
		
		if(logger.isDebugEnabled()) { logger.debug("Parsed: host: "+member.getHost()+", address: "+member.getAddress()+", name: "+member.getName()+", port: "+member.getPort()+ ", read: "+member.getRead()+ "; route: "+member.getRoute()+ "; redirect: "+member.getRedirect()+ "; elected: "+member.getElected()+ "; state: "+member.getState()+ "; busy: "+member.getBusy()+ ", context: "+context); }
		
		jkBalancer.getMember().add(member);
		jkBalancer.setName(balancerName);
	}
	/**
	 * 
	 * @param body
	 * @return
	 */
	JkServer getJkServer(String body) {
		Pattern pattern = Pattern.compile(".*<address>.*erver at (.*) Port (.*)</address>.*");
		Matcher matcher = pattern.matcher(body);
		if(matcher.find()) {
        	JkServer jkServer = new JkServer();
        	jkServer.setName(matcher.group(1));
        	jkServer.setPort(Integer.parseInt(matcher.group(2)));
        	if(logger.isDebugEnabled()) { logger.debug("Parsed: server: "+jkServer.getName()+", port: "+jkServer.getPort()); }
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
package net.local.clustercontrol.core.http.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import net.local.clustercontrol.api.model.xml.Host;
import net.local.clustercontrol.api.model.xml.Hosts;
import net.local.clustercontrol.api.model.xml.ResponseError;
import net.local.clustercontrol.api.model.xml.WorkerResponse;
import net.local.clustercontrol.api.model.xml.WorkerResponses;
import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.logic.ControlCommandException;

@Component
public class HttpClient implements IHttpClient {

	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

	/**
	 * Executes urls
	 * @param hosts the hosts to request
	 * @param parameters the parameters to execute
	 * @return the workerlists, ie html bodys
	 * @throws MalformedURLException 
	 * @throws URISyntaxException 
	 */
	@Override
	public WorkerResponses performActionOnHosts(Hosts hosts) {
		int hostsCount = hosts.getHostList().size();
		WorkerResponses workerResponses = new WorkerResponses();
		
		for (int hostIdx = 0; hostIdx < hostsCount; hostIdx++) {
			Host host = hosts.getHostList().get(hostIdx);
			workerResponses.getResponseList().add(performActionOnHost(host));
		}
		return workerResponses;
	}
	/**
	 * Creates the request (GET) and receives a encapsulated response object, ie
	 * the html body. Returns a WorkerResponse for this request.
	 * 
	 * @param host
	 * 			  the host to request
	 * @param parameters
	 *            the parameters to execute
	 * @return the response, ie html body
	 * @throws MalformedURLException 
	 * @throws URISyntaxException 
	 */
	public WorkerResponse performActionOnHost(Host host) {
		URL url = createTargetUrl(host);
		if(logger.isDebugEnabled()) { logger.debug("executing request " + url.toExternalForm()); }
		// creates the response handler
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpRequestRetryHandler retryHandler = new RetryHandler();
		httpclient.setHttpRequestRetryHandler(retryHandler);

		WorkerResponse workerResponse = new WorkerResponse();
		String responseBody = null;
		try {
			HttpGet httpget = new HttpGet(url.toExternalForm());
			org.apache.http.client.ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = (String) httpclient.execute(httpget, responseHandler);
			workerResponse.setBody(responseBody);
			workerResponse.setHost(url.getHost());
		} catch (ClientProtocolException e) {
			logger.error(e.getClass().getCanonicalName() +" "+e.getMessage()+" "+e.getLocalizedMessage());
			if(e instanceof HttpResponseException) {
				logger.error("Failed to get response for: "+url.getHost()+", "+url.getPort()+", "+url.getPath());
			} else {
				logger.error("ClientProtocolException: Failed to connect to host: "+url.getHost()+", "+url.getPort());
			}
			ResponseError responseError = new ResponseError();
			responseError.setMessageKey(e.getClass().getCanonicalName());
			responseError.setMessage(e.getMessage());
			workerResponse.setError(responseError);
		} catch (IOException e) {
			logger.error(e.getClass() +" "+e.getMessage()+" "+e.getLocalizedMessage());
			if(e instanceof HttpHostConnectException) {
				logger.error("Failed to connect to host: "+url.getHost()+", "+url.getPort());
			} else {
				logger.error("IOException: Failed to connect to host: "+url.getHost()+", "+url.getPort());
			}
			ResponseError responseError = new ResponseError();
			responseError.setMessageKey(e.getClass().getCanonicalName());
			responseError.setMessage(e.getMessage());
			workerResponse.setError(responseError);
		}
		// shutdown the client
		httpclient.getConnectionManager().shutdown();
		return workerResponse;
	}
	/**
	 * Creates the target url to execute by the http client
	 * @param host the host with all info about the target, ie ipaddress, port, context
	 * @param parameters the control parameters, added as url parameters 
	 * @return the target url to execute by the http client
	 */
	URL createTargetUrl(Host host) {
		Integer port = host.getPort();
		String portPart = "";
		String targetHost = "";
		if (port!=null && port > 0) {
			portPart = ":"+port;
		}
		targetHost = "http://"+ host.getIpAddress() + portPart;
		String targetContext = "/"+host.getContext();
		String url = targetHost + targetContext;
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new ControlCommandException("Failed to execute url: "+url);
		}
	}
}

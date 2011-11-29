package net.local.clustercontrol.core.http;

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

import net.local.clustercontrol.api.model.Host;
import net.local.clustercontrol.api.model.Hosts;
import net.local.clustercontrol.api.model.ResponseError;
import net.local.clustercontrol.api.model.WorkerResponse;
import net.local.clustercontrol.api.model.WorkerResponses;

public class HttpClient {

	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

	/**
	 * Executes urls
	 * @param _hosts the hosts to request
	 * @param parameters the parameters to execute
	 * @return the workerlists, ie html bodys
	 * @throws MalformedURLException 
	 * @throws URISyntaxException 
	 */
	public static WorkerResponses executeUrls(Hosts _hosts, String parameters) {
		int hostsCount = _hosts.getHostList().size();
		WorkerResponses workerResponses = new WorkerResponses();
		
		for (int hostIdx = 0; hostIdx < hostsCount; hostIdx++) {
			Host host = _hosts.getHostList().get(hostIdx);
			try {
				workerResponses.getResponseList().add(executeUrl(host, parameters));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	public static WorkerResponse executeUrl(Host host, String parameters) throws MalformedURLException {
		String targetUrl = createTargetUrl(host, parameters);
		return executeUrl(new URL(targetUrl));
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
	public static WorkerResponse executeUrl(String url, String parameters) throws MalformedURLException {
		String targetUrl = url+"?"+parameters;
		return executeUrl(new URL(targetUrl));
	}
	/**
	 * Creates the request (GET) and receives a encapsulated response object, ie
	 * the html body. Returns a WorkerResponse for this request.
	 * 
	 * @param url
	 * 			  the url to request
	 * @param parameters
	 *            the parameters to execute
	 * @return the response, ie html body
	 * @throws URISyntaxException 
	 */
	public static WorkerResponse executeUrl(URL url) {
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
	public static String createTargetUrl(Host host, String parameters) {
		Integer port = host.getPort();
		String portPart = "";
		String targetHost = "";
		if (port!=null && port > 0) {
			portPart = ":"+port;
		}
		targetHost = "http://"+ host.getIpAddress() + portPart;
		String targetContext = "/"+host.getContext() + "?" + parameters;
		return targetHost + targetContext;
	}
}

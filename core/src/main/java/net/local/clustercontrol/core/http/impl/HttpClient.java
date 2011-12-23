package net.local.clustercontrol.core.http.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import net.local.clustercontrol.api.model.xml.Host;
import net.local.clustercontrol.api.model.xml.ResponseError;
import net.local.clustercontrol.api.model.xml.WorkerResponse;
import net.local.clustercontrol.api.model.xml.WorkerResponses;
import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.logic.ControlCommandException;
import net.local.clustercontrol.core.model.dto.Cluster;

@Component
public class HttpClient implements IHttpClient {

	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

	@Override
	public WorkerResponse getWorkerResponseForUrl(String url) {
		URL targetUrl = createTargetUrl(url);
		return performActionOnHost(targetUrl);
	}
	@Override
	public WorkerResponses getWorkerResponseForAction(Cluster cluster) {
		throw new IllegalArgumentException("not implemented");
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
	WorkerResponse performActionOnHost(URL url) {
		if(url == null) {
			throw new IllegalArgumentException("Url cannot be null");
		}
		if(logger.isDebugEnabled()) { logger.debug("Performing request " + url.toExternalForm()); }
		
		// creates the response handler
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpRequestRetryHandler retryHandler = new RetryHandler();
		httpclient.setHttpRequestRetryHandler(retryHandler);

		WorkerResponse workerResponse = new WorkerResponse();
		String responseBody = null;
		ResponseError responseError = new ResponseError();
		try {
			HttpGet httpget = new HttpGet(url.toExternalForm());
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = httpclient.execute(httpget, responseHandler);
			workerResponse.setBody(responseBody);
			workerResponse.setHost(url.getHost());
			responseError.setMessageKey("");
			responseError.setMessage("");
		} catch (ClientProtocolException e) {
			logger.error(e.getClass().getCanonicalName() +" "+e.getMessage()+" "+e.getLocalizedMessage());
			if(e instanceof HttpResponseException) {
				logger.error("Failed to get response for: "+url.getHost()+", "+url.getPort()+", "+url.getPath());
			} else {
				logger.error("ClientProtocolException: Failed to connect to host: "+url.getHost()+", "+url.getPort());
			}
			responseError.setMessageKey(e.getClass().getCanonicalName());
			responseError.setMessage(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getClass() +" "+e.getMessage()+" "+e.getLocalizedMessage());
			if(e instanceof HttpHostConnectException) {
				logger.error("Failed to connect to host: "+url.getHost()+", "+url.getPort());
			} else {
				logger.error("IOException: Failed to connect to host: "+url.getHost()+", "+url.getPort());
			}
			responseError.setMessageKey(e.getClass().getCanonicalName());
			responseError.setMessage(e.getMessage());
		}
		workerResponse.setError(responseError);
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
	URL createTargetUrl(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new ControlCommandException("Failed to execute url: "+url);
		}
		
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
		String targetContext = host.getContext(); 
		if(false == targetContext.startsWith("/")) {
			targetContext = "/"+targetContext;
		}
		String url = targetHost + targetContext;
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new ControlCommandException("Failed to execute url: "+url);
		}
	}
}

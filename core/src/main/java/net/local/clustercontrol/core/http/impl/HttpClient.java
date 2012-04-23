package net.local.clustercontrol.core.http.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import net.local.clustercontrol.api.model.xml.Host;
import net.local.clustercontrol.api.model.xml.ResponseError;
import net.local.clustercontrol.api.model.xml.WorkerResponse;
import net.local.clustercontrol.core.http.IHttpClient;
import net.local.clustercontrol.core.logic.ControlCommandException;

@Component
public class HttpClient implements IHttpClient {

	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

	@Override
	public WorkerResponse getWorkerResponseForUrl(String url) {
		URL targetUrl = createTargetUrl(url);
		return performActionOnHost(targetUrl);
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
	 * @throws IOException 
	 * @throws ClientProtocolException 
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
			
			HttpResponse response = httpclient.execute(httpget);
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 404) {
				responseError.setMessageKey("");
				responseError.setMessage(response.getStatusLine().getReasonPhrase());
				workerResponse.setError(responseError);
				return workerResponse;
			} else if(statusCode != 200) {
				responseError.setMessageKey("");
				responseError.setMessage(response.getStatusLine().getReasonPhrase());
				workerResponse.setError(responseError);
				return workerResponse;
			}
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				responseBody = EntityUtils.toString(entity);
			}
			
			workerResponse.setBody(responseBody);
			workerResponse.setHost(url.getHost());
		} catch (ClientProtocolException e) {
//			logger.error(e.getClass().getCanonicalName() +" "+e.getMessage()+" "+e.getLocalizedMessage());
//			if(e instanceof HttpResponseException) {
//				logger.error("Failed to get response for: "+body.getHost()+", "+body.getPort()+", "+body.getPath());
//			} else {
//				logger.error("ClientProtocolException: Failed to connect to host: "+body.getHost()+", "+body.getPort());
//			}
			responseError.setMessageKey(e.getClass().getCanonicalName());
			responseError.setMessage("Failed to connect to host: "+url.getHost()+": "+e.getMessage());
			workerResponse.setError(responseError);
		} catch (IOException e) {
//			logger.error(e.getClass() +" "+e.getMessage()+" "+e.getLocalizedMessage());
//			if(e instanceof HttpHostConnectException) {
//				logger.error("Failed to connect to host: "+body.getHost()+", "+body.getPort());
//			} else {
//				logger.error("IOException: Failed to connect to host: "+body.getHost()+", "+body.getPort());
//			}
			responseError.setMessageKey(e.getClass().getCanonicalName());
			responseError.setMessage("Failed to connect to host: "+url.getHost()+": "+e.getMessage());
			workerResponse.setError(responseError);
		} finally {
            // When HttpClient instance is no longer needed, shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
		return workerResponse;
	}
	
	/**
	 * Creates the target body to execute by the http client
	 * @param host the host with all info about the target, ie ipaddress, port, context
	 * @param parameters the control parameters, added as body parameters 
	 * @return the target body to execute by the http client
	 */
	URL createTargetUrl(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new ControlCommandException("Failed to execute body: "+url);
		}
		
	}
	/**
	 * Creates the target body to execute by the http client
	 * @param host the host with all info about the target, ie ipaddress, port, context
	 * @param parameters the control parameters, added as body parameters 
	 * @return the target body to execute by the http client
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
			throw new ControlCommandException("Failed to execute body: "+url);
		}
	}
}

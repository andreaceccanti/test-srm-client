package org.italiangrid.dav.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;

import javax.xml.rpc.ServiceException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.italiangrid.axis.CANLMessageLogger;
import org.italiangrid.voms.util.CertificateValidatorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.emi.security.authn.x509.X509CertChainValidatorExt;
import eu.emi.security.authn.x509.X509Credential;
import eu.emi.security.authn.x509.impl.PEMCredential;

public class WebDAVClient {

	Logger logger = LoggerFactory.getLogger(WebDAVClient.class);

	public static class Builder {

		private String url;
		private String proxyFilePath;
		private String trustAnchorDir = "/etc/grid-security/certificates";

		public Builder() {

		}

		public Builder serviceURL(String url) {

			this.url = url;
			return this;
		}

		public Builder proxyFilePath(String pfp) {

			proxyFilePath = pfp;
			return this;
		}

		public Builder trustAnchorsDir(String tad) {

			trustAnchorDir = tad;
			return this;
		}

		public WebDAVClient build() throws ServiceException, KeyStoreException,
			CertificateException, FileNotFoundException, IOException {

			return new WebDAVClient(this);
		}

	}

	private HttpClient httpClient;

	private WebDAVClient(Builder builder) throws ServiceException,
		KeyStoreException, CertificateException, FileNotFoundException, IOException {

		X509Credential credential = new PEMCredential(new FileInputStream(
			builder.proxyFilePath), (char[]) null);

		X509CertChainValidatorExt validator = 
		  new CertificateValidatorBuilder()
		  .trustAnchorsDir(builder.trustAnchorDir)
		  .storeUpdateListener(CANLMessageLogger.INSTANCE)
		  .validationErrorListener(CANLMessageLogger.INSTANCE)
		  .trustAnchorsUpdateInterval(0L)
		  .build();

		ProtocolSocketFactory sf =  
			new CustomSecureProtocolSocketFactory(credential, validator);

		Protocol protocol = new Protocol("https", sf, 443);
		Protocol.registerProtocol("https", protocol);

		HostConfiguration hostConfig = new HostConfiguration();
		hostConfig.setHost(new URI(builder.url, false));

		HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setMaxConnectionsPerHost(hostConfig, 100);
		params.setMaxTotalConnections(100);

		connectionManager.setParams(params);

		httpClient = new HttpClient(connectionManager);
		httpClient.setHostConfiguration(hostConfig);

	}

	/**
	 * Execute an HTTP HEAD and return the status code.
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public int head(String url) throws Exception {

		HeadMethod method = new HeadMethod(url);
		int statusCode = httpClient.executeMethod(method);

		
		method.releaseConnection();

		return statusCode;
	}

	public void mkcol(String url) throws IOException, DavException {

		DavMethod method = new MkColMethod(url);
		httpClient.executeMethod(method);

		method.checkSuccess();
		method.releaseConnection();
	}

	public void move(String srcUrl, String destUrl) throws DavException,
		IOException {

		DavMethod method = new MoveMethod(srcUrl, destUrl, true);
		httpClient.executeMethod(method);

		method.checkSuccess();
		method.releaseConnection();
	}

	public int get(String url) throws IOException, DavException {

		GetMethod method = new GetMethod(url);
		int statusCode = httpClient.executeMethod(method);

		method.releaseConnection();

		return statusCode;
	}
	
	public int put(String url, String filePath) throws IOException, DavException {

		RequestEntity requestEntity = new InputStreamRequestEntity(
			new FileInputStream(new File(filePath)));
		
		PutMethod method = new PutMethod(url);
		method.setRequestEntity(requestEntity);

		httpClient.executeMethod(method);

		method.checkSuccess();
		method.releaseConnection();
		
		return method.getStatusCode(); 
	}

}

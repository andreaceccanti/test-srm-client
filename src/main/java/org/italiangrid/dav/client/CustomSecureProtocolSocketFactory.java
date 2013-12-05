package org.italiangrid.dav.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

import eu.emi.security.authn.x509.X509CertChainValidatorExt;
import eu.emi.security.authn.x509.X509Credential;
import eu.emi.security.authn.x509.impl.SocketFactoryCreator;


/**
 * Custom SSL socket provider for commons http-client,
 * see http://hc.apache.org/httpclient-3.x/sslguide.html.
 * 
 * @author valerioventuri
 *
 */
public class CustomSecureProtocolSocketFactory implements
	SecureProtocolSocketFactory {

	/**
	 * The SSL context
	 * 
	 */
	private SSLContext context = null;
	
	/**
	 * The credential to use ofr authentication.
	 * 
	 */
	private X509Credential credential;
	
	/**
	 * The credential validator.
	 * 
	 */
	private X509CertChainValidatorExt validator;
	
	/**
	 * Constructor.
	 * 
	 * @param credential
	 * @param validator
	 */
	public CustomSecureProtocolSocketFactory(X509Credential credential, 
		X509CertChainValidatorExt validator) {

		this.credential = credential;
		this.validator = validator;
	}
	
	/**
	 * Create the {@link SSLContext}.
	 * 
	 * @return
	 */
	private SSLContext createSSLContext() {
		
		SSLContext context = null;
		
		try {

			context = SSLContext.getInstance("SSLv3");

		} catch (NoSuchAlgorithmException e) {

			throw new Error(e.getMessage(), e);
		}

		KeyManager[] keyManagers = new KeyManager[] { credential.getKeyManager() };

		X509TrustManager trustManager = SocketFactoryCreator.getSSLTrustManager(validator);

		TrustManager[] trustManagers = new TrustManager[] { trustManager };

		SecureRandom secureRandom = null;

		/* http://bugs.sun.com/view_bug.do?bug_id=6202721 */
		/* Use new SecureRandom instead of SecureRandom.getInstance("SHA1PRNG") to avoid
		 * unnecessary blocking
		 */
		secureRandom = new SecureRandom();

		try {

			context.init(keyManagers, trustManagers, secureRandom);

		} catch (KeyManagementException e) {

			throw new Error(e.getMessage(), e);
		}
		
		return context;
	}
	
	/**
	 * Getters for the SSLContext. Create the context at the first call.
	 * 
	 * @return
	 */
	private SSLContext getContext() {
		
		if (this.context == null) {
		
			this.context = createSSLContext();
		}
		
		return this.context;	
	}
	
	
	public Socket createSocket(String host, int port) throws IOException,
		UnknownHostException {

		return getContext().getSocketFactory().createSocket(host, port);
	}

	public Socket createSocket(String host, int port, InetAddress localAddress, int localPort)
		throws IOException, UnknownHostException {

		return getContext().getSocketFactory().createSocket(host, port, localAddress, localPort);
	}

	public Socket createSocket(String host, int port, InetAddress localAddress, int localPort,
		HttpConnectionParams params) throws IOException, UnknownHostException,
		ConnectTimeoutException {

		if (params == null) {
			
			throw new IllegalArgumentException("Parameters may not be null");
		}
		
		int timeout = params.getConnectionTimeout();
		
		SocketFactory socketfactory = getContext().getSocketFactory();
		
		if (timeout == 0) {
		
			return socketfactory.createSocket(host, port, localAddress, localPort);
		
		} else {
		
			Socket socket = socketfactory.createSocket();
			SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
			SocketAddress remoteaddr = new InetSocketAddress(host, port);
			socket.bind(localaddr);
			socket.connect(remoteaddr, timeout);
			
			return socket;
		}

	}

	public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
		throws IOException, UnknownHostException {
		
		 return getContext().getSocketFactory().createSocket(socket, host, port, autoClose);
	}

	public boolean equals(Object obj) {

		return ((obj != null) && obj.getClass().equals(
			CustomSecureProtocolSocketFactory.class));
	}

	public int hashCode() {

		return CustomSecureProtocolSocketFactory.class.hashCode();
	}

}

package org.italiangrid.dav.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;

import javax.xml.rpc.ServiceException;


public class WebDAVClientFactory {

	private WebDAVClientFactory() {}
	
	public static WebDAVClient newWebDAVClient(String endpoint, String proxy)
		throws ServiceException, KeyStoreException, CertificateException,
			FileNotFoundException, IOException {

		return new WebDAVClient.Builder().proxyFilePath(proxy).serviceURL(endpoint).build();
		
	}
	
}

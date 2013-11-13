package org.italiangrid.srm.client;

import java.net.MalformedURLException;

import javax.xml.rpc.ServiceException;


public class SRMClientFactory {

	private SRMClientFactory() {}
	
	public static SRMClient newSRMClient(String endpoint, String proxy) 
		throws MalformedURLException, ServiceException{
		return new SRMClient
			.Builder()
			.proxyFilePath(proxy)
			.serviceURL(endpoint).build();
	}

}

package org.italiangrid.srm.client;

import gov.lbl.srm.StorageResourceManager.SrmPrepareToGetResponse;
import gov.lbl.srm.StorageResourceManager.TGetRequestFileStatus;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Arrays;

import javax.xml.rpc.ServiceException;

import org.apache.axis.types.URI.MalformedURIException;

public class TestSRMClient {

	public static void main(String[] args) throws MalformedURLException,
		ServiceException, MalformedURIException, RemoteException {

		String url = "https://omii005-vm03.cnaf.infn.it:8444";
		String proxyFilePath = "/tmp/x509up_u501";

		SRMClient client = new SRMClient.Builder().serviceURL(url)
			.proxyFilePath(proxyFilePath).build();

		String surl = 
			"srm://omii005-vm03.cnaf.infn.it:8444/testers.eu-emi.eu/myfaketest";
		
		SrmPrepareToGetResponse response = client
			.srmPtG( Arrays.asList(surl) );

		System.out.println(response.getReturnStatus().getStatusCode() + ": "
			+ response.getReturnStatus().getExplanation());
		TGetRequestFileStatus[] statuses = response.getArrayOfFileStatuses()
			.getStatusArray();

		for (TGetRequestFileStatus s : statuses) {
			System.out.println(s.getSourceSURL() + ":"
				+ s.getStatus().getStatusCode());
		}
		System.out.println();

	}

}

package org.italiangrid.srm.client;

import gov.lbl.srm.StorageResourceManager.SrmPrepareToGetResponse;
import gov.lbl.srm.StorageResourceManager.TGetRequestFileStatus;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.xml.rpc.ServiceException;

import org.apache.axis.types.URI.MalformedURIException;

public class TestSRMClient {

	public static void main(String[] args) throws MalformedURLException,
		ServiceException, MalformedURIException, RemoteException {

		String url = "https://omii005-vm03.cnaf.infn.it:8444";
		String proxyFilePath = "/tmp/x509up_u501";

		SRMClient client = new SRMClient.Builder().serviceURL(url)
			.proxyFilePath(proxyFilePath).build();

		SrmPrepareToGetResponse response = client
			.srmPTG(
				Arrays
					.asList("srm://omii005-vm03.cnaf.infn.it:8444/testers.eu-emi.eu/myfaketest"),
				TimeUnit.SECONDS.toMillis(10));

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

package org.italiangrid.srm.client;

import gov.lbl.srm.StorageResourceManager.SrmPrepareToGetResponse;
import gov.lbl.srm.StorageResourceManager.TGetRequestFileStatus;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.xml.rpc.ServiceException;

import org.apache.axis.types.URI.MalformedURIException;

import gov.lbl.srm.StorageResourceManager.SrmPingResponse;
import gov.lbl.srm.StorageResourceManager.TExtraInfo;

import java.io.File;

public class TestSRMPing {

	public static void main(String[] args) throws MalformedURLException,
		ServiceException, MalformedURIException, RemoteException {
		
		Security.addProvider(new BouncyCastleProvider());

		if (args.length == 0) {
			System.err.println("Usage: TestSRMPing <endpoint-url>");
			return;
		}
		String url = args[0];

		String proxyFilePath = System.getenv("X509_USER_PROXY");
		File proxy = new File(proxyFilePath);
		if (!proxy.exists()) {
			System.err.println("TestSRMPing ERROR: " + proxy + " doesn't exist!");
			return;
		}
		SRMClient client = new SRMClient.Builder().serviceURL(url)
			.proxyFilePath(proxyFilePath).build();

		SrmPingResponse res = client.srmPing();
		System.out.println("SRM Version: " + res.getVersionInfo());
		System.out.println("OtherInfo: ");
		for (TExtraInfo info: res.getOtherInfo().getExtraInfoArray()) {
                	System.out.println(info.getKey() + " = " + info.getValue());
                }
		return;
	}

}

package org.italiangrid.srm.client;

import gov.lbl.srm.StorageResourceManager.ArrayOfTGetFileRequest;
import gov.lbl.srm.StorageResourceManager.ISRM;
import gov.lbl.srm.StorageResourceManager.SRMServiceLocator;
import gov.lbl.srm.StorageResourceManager.SrmPingRequest;
import gov.lbl.srm.StorageResourceManager.SrmPingResponse;
import gov.lbl.srm.StorageResourceManager.SrmPrepareToGetRequest;
import gov.lbl.srm.StorageResourceManager.SrmPrepareToGetResponse;
import gov.lbl.srm.StorageResourceManager.SrmStatusOfGetRequestRequest;
import gov.lbl.srm.StorageResourceManager.SrmStatusOfGetRequestResponse;
import gov.lbl.srm.StorageResourceManager.TDirOption;
import gov.lbl.srm.StorageResourceManager.TGetFileRequest;
import gov.lbl.srm.StorageResourceManager.TStatusCode;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisProperties;
import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.italiangrid.axis.CANLAxis1SocketFactory;
import org.italiangrid.axis.DefaultConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * A simple SRM client.
 *
 * @author andreaceccanti
 *
 */
public class SRMClient implements SRMHelper {

	Logger logger = LoggerFactory.getLogger(SRMClient.class);

	public static class Builder {

		private String serviceURL = "https://localhost:8444";
		private String proxyFilePath;
		private String trustAnchorDir = "/etc/grid-security/certificates";

		public Builder() {

		}

		public Builder serviceURL(String url) {

			serviceURL = url;
			return this;
		}

		public Builder proxyFilePath(String pfp) {

			proxyFilePath = pfp;
			return this;
		}

		public Builder trustAnchorDIr(String tad) {

			trustAnchorDir = tad;
			return this;
		}

		public SRMClient build() throws MalformedURLException, ServiceException {

			return new SRMClient(this);
		}

	}

	private static final String AXIS_SOCKET_FACTORY_PROPERTY = "axis.socketSecureFactory";

	private ISRM serviceEndpoint;

	private SRMClient(Builder builder) throws MalformedURLException,
		ServiceException {

		DefaultConfigurator configurator = new DefaultConfigurator();

		configurator.setEnableGSIHandshake(true);
		configurator.setProxyFile(builder.proxyFilePath);
		configurator.setTrustAnchorsDir(builder.trustAnchorDir);

		CANLAxis1SocketFactory.setConfigurator(configurator);

		AxisProperties.setProperty(AXIS_SOCKET_FACTORY_PROPERTY,
			CANLAxis1SocketFactory.class.getName());

		SRMServiceLocator locator = new SRMServiceLocator();
		serviceEndpoint = locator.getsrm(new URL(builder.serviceURL));

	}

	public SrmPingResponse srmPing() throws RemoteException {

		return serviceEndpoint.srmPing(new SrmPingRequest());
	}

	public SrmStatusOfGetRequestResponse srmPTG(List<String> surls,
		long maxWaitingTimeInMsec) throws MalformedURIException, RemoteException {

		if (maxWaitingTimeInMsec < 0)
			throw new IllegalArgumentException("Please specify a positive integer"
				+ " for the max waiting time");

		if (surls == null || surls.isEmpty())
			throw new IllegalArgumentException(
				"Please provide a non-null or not-empty" + " list of surls.");

		List<TGetFileRequest> requests = new ArrayList<TGetFileRequest>();

		for (String s : surls)
			requests.add(new TGetFileRequest(new URI(s), new TDirOption(false, false,
				0)));

		SrmPrepareToGetRequest ptg = new SrmPrepareToGetRequest();
		ptg.setArrayOfFileRequests(new ArrayOfTGetFileRequest(requests
			.toArray(new TGetFileRequest[requests.size()])));

		SrmPrepareToGetResponse resp = serviceEndpoint.srmPrepareToGet(ptg);
		if (!resp.getReturnStatus().getStatusCode()
			.equals(TStatusCode.SRM_REQUEST_QUEUED)) {
			throw new SRMError(resp.getReturnStatus().getStatusCode(), resp
				.getReturnStatus().getExplanation());
		}
		String token = resp.getRequestToken();
		SrmStatusOfGetRequestRequest sptg = new SrmStatusOfGetRequestRequest();
		sptg.setRequestToken(token);

		long cumulativeSleepTime = 0;
		long sleepInterval = 50;
		int requestCounter = 0;
		TStatusCode lastStatus;
		SrmStatusOfGetRequestResponse sptgResp = null;

		do {
			requestCounter++;
			sptgResp = serviceEndpoint.srmStatusOfGetRequest(sptg);

			lastStatus = sptgResp.getReturnStatus().getStatusCode();
			if (lastStatus.equals(TStatusCode.SRM_REQUEST_QUEUED)
				|| (lastStatus.equals(TStatusCode.SRM_REQUEST_INPROGRESS))) {

				try {
					Thread.sleep(sleepInterval);
					cumulativeSleepTime += sleepInterval;
					sleepInterval *= 2;
				} catch (InterruptedException e) {

				}

			} else
				return sptgResp;

		} while (cumulativeSleepTime < maxWaitingTimeInMsec);

		logger.warn(
			"PtG still in progress after {} status requests and {} waiting time.",
			requestCounter, maxWaitingTimeInMsec);

		return sptgResp;

	}
}

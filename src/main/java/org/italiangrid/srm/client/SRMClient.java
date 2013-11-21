package org.italiangrid.srm.client;

import gov.lbl.srm.StorageResourceManager.ArrayOfAnyURI;
import gov.lbl.srm.StorageResourceManager.ArrayOfString;
import gov.lbl.srm.StorageResourceManager.ArrayOfTGetFileRequest;
import gov.lbl.srm.StorageResourceManager.ISRM;
import gov.lbl.srm.StorageResourceManager.SRMServiceLocator;
import gov.lbl.srm.StorageResourceManager.SrmLsRequest;
import gov.lbl.srm.StorageResourceManager.SrmLsResponse;
import gov.lbl.srm.StorageResourceManager.SrmMkdirRequest;
import gov.lbl.srm.StorageResourceManager.SrmMkdirResponse;
import gov.lbl.srm.StorageResourceManager.SrmPingRequest;
import gov.lbl.srm.StorageResourceManager.SrmPingResponse;
import gov.lbl.srm.StorageResourceManager.SrmPrepareToGetRequest;
import gov.lbl.srm.StorageResourceManager.SrmPrepareToGetResponse;
import gov.lbl.srm.StorageResourceManager.SrmReleaseFilesRequest;
import gov.lbl.srm.StorageResourceManager.SrmReleaseFilesResponse;
import gov.lbl.srm.StorageResourceManager.SrmRmRequest;
import gov.lbl.srm.StorageResourceManager.SrmRmResponse;
import gov.lbl.srm.StorageResourceManager.SrmRmdirRequest;
import gov.lbl.srm.StorageResourceManager.SrmRmdirResponse;
import gov.lbl.srm.StorageResourceManager.SrmStatusOfGetRequestRequest;
import gov.lbl.srm.StorageResourceManager.SrmStatusOfGetRequestResponse;
import gov.lbl.srm.StorageResourceManager.TDirOption;
import gov.lbl.srm.StorageResourceManager.TGetFileRequest;
import gov.lbl.srm.StorageResourceManager.TStatusCode;
import gov.lbl.srm.StorageResourceManager.TTransferParameters;

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
	
	public SrmPrepareToGetResponse srmPTG(List<String> surls,
		long maxWaitingTimeInMsec) throws MalformedURIException, RemoteException {
		
		return srmPTG(surls, new ArrayList<String>(), maxWaitingTimeInMsec);
	}

	public SrmPrepareToGetResponse srmPTG(List<String> surls, List<String> transferProtocols,
		long maxWaitingTimeInMsec) throws MalformedURIException, RemoteException {

		checkMaxWaitingTimeInSecArgument(maxWaitingTimeInMsec);
		checkSurlsArgument(surls);
		checkTransferProtocolsArgument(transferProtocols);

		List<TGetFileRequest> requests = new ArrayList<TGetFileRequest>();

		for (String s : surls)
			requests.add(new TGetFileRequest(new URI(s), new TDirOption(false, false,
				0)));

		SrmPrepareToGetRequest ptg = new SrmPrepareToGetRequest();
		ptg.setArrayOfFileRequests(new ArrayOfTGetFileRequest(requests
			.toArray(new TGetFileRequest[requests.size()])));

		if (!transferProtocols.isEmpty()) {
			TTransferParameters transferParameters = new TTransferParameters();
			transferParameters.setArrayOfTransferProtocols(
				new ArrayOfString(transferProtocols.toArray(
					new String[transferProtocols.size()])));
			ptg.setTransferParameters(transferParameters);
		}
		
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

			} else {
				
				resp.setReturnStatus(sptgResp.getReturnStatus());
				resp.setArrayOfFileStatuses(sptgResp.getArrayOfFileStatuses());
				
				return resp;
			}
		} while (cumulativeSleepTime < maxWaitingTimeInMsec);

		logger.warn(
			"PtG still in progress after {} status requests and {} waiting time.",
			requestCounter, maxWaitingTimeInMsec);

		resp.setReturnStatus(sptgResp.getReturnStatus());
		resp.setArrayOfFileStatuses(sptgResp.getArrayOfFileStatuses());
		
		return resp;

	}
	
	public SrmLsResponse srmLs(List<String> surls,
		long maxWaitingTimeInMsec) throws MalformedURIException, RemoteException {

		checkMaxWaitingTimeInSecArgument(maxWaitingTimeInMsec);
		checkSurlsArgument(surls);
		
		SrmLsRequest request = new SrmLsRequest();
		
		request.setArrayOfSURLs(convertSurlsFromList(surls));
		
		return serviceEndpoint.srmLs(request);
	}
	
	public SrmMkdirResponse srmMkdir(String surl) throws MalformedURIException, RemoteException {

		checkSurlArgument(surl);
		
		SrmMkdirRequest request = new SrmMkdirRequest();
		request.setSURL(new URI(surl));
		
		return serviceEndpoint.srmMkdir(request);
	}
	
	private void checkMaxWaitingTimeInSecArgument(long maxWaitingTimeInMsec) {
		
		if (maxWaitingTimeInMsec < 0)
			throw new IllegalArgumentException("Please specify a positive integer"
				+ " for the max waiting time");

	}
	
	private void checkSurlArgument(String surl) {
		
		if (surl == null || surl.isEmpty())
			throw new IllegalArgumentException(
				"Please provide a non-null or not-empty surl.");
	}
	
	private void checkSurlsArgument(List<String> surls) {
		
		if (surls == null || surls.isEmpty())
			throw new IllegalArgumentException(
				"Please provide a non-null or not-empty" + " list of surls.");

	}
	
	private void checkTransferProtocolsArgument(List<String> protocols) {
		
		if (protocols == null)
			throw new IllegalArgumentException(
				"Please provide a non-null " + " list of protocols.");

	}

	private ArrayOfAnyURI convertSurlsFromList(List<String> surls) 
		throws MalformedURIException {
		
		List<URI> uris = new ArrayList<URI>();
		
		for(String surl : surls)
			uris.add(new URI(surl));
		
		return new ArrayOfAnyURI(uris.toArray(new URI[surls.size()]));
	}
	
	public SrmReleaseFilesResponse srmReleaseFiles(List<String> surls)
		throws MalformedURIException, RemoteException {
		
		return srmReleaseFiles(null, surls);
	}

	public SrmReleaseFilesResponse srmReleaseFiles(String requestToken)
		throws MalformedURIException, RemoteException {
		
		return srmReleaseFiles(requestToken, null);
	}

	public SrmReleaseFilesResponse srmReleaseFiles(String requestToken,
		List<String> surls) throws MalformedURIException, RemoteException {

		SrmReleaseFilesRequest srmReleaseFilesRequest = new SrmReleaseFilesRequest();
		
		if (requestToken != null && !requestToken.isEmpty())
			srmReleaseFilesRequest.setRequestToken(requestToken);
		
		if (surls != null && !surls.isEmpty())
			srmReleaseFilesRequest.setArrayOfSURLs(convertSurlsFromList(surls));
		
		return serviceEndpoint.srmReleaseFiles(srmReleaseFilesRequest);
	}

	public SrmRmResponse srmRm(List<String> surls) throws MalformedURIException,
		RemoteException {

		checkSurlsArgument(surls);
		
		SrmRmRequest srmRmRequest = new SrmRmRequest();
		srmRmRequest.setArrayOfSURLs(convertSurlsFromList(surls));
		
		return serviceEndpoint.srmRm(srmRmRequest);
	}

	public SrmRmdirResponse srmRmdir(String surl, boolean recursive)
		throws MalformedURIException, RemoteException {

		checkSurlArgument(surl);
		
		SrmRmdirRequest srmRmdirRequest = new SrmRmdirRequest();
		srmRmdirRequest.setSURL(new URI(surl));
		srmRmdirRequest.setRecursive(recursive);
		
		return serviceEndpoint.srmRmdir(srmRmdirRequest);
	}
	
}

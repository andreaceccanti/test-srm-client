package org.italiangrid.srm.client;

import gov.lbl.srm.StorageResourceManager.SrmMkdirResponse;
import gov.lbl.srm.StorageResourceManager.SrmPingResponse;
import gov.lbl.srm.StorageResourceManager.SrmPrepareToPutResponse;
import gov.lbl.srm.StorageResourceManager.SrmPutDoneResponse;
import gov.lbl.srm.StorageResourceManager.SrmStatusOfGetRequestResponse;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.axis.types.URI.MalformedURIException;

/**
 * Small wrapper interface to simplify srm request handling
 * from jython (grinder) scripts.
 * 
 * @author andreaceccanti
 *
 */
public interface SRMHelper {

	/**
	 * Runs an srm ping.
	 * 
	 * @return the {@link SrmPingResponse} in case success
	 * @throws RemoteException in case of errors
	 */
	public SrmPingResponse srmPing() throws RemoteException;
	
	/**
	 * Runs a srm prepare to get for a list of surls. 
	 * This call implements the first ptg request and a number of status-ptg
	 * until either the request is complete or a maximum waiting treshold is reached.
	 * 
	 * @param surls the list of surls
	 * @param maxWaitingTimeInMsec the total maximum waiting time for sptg requests. 
	 * 
	 * @return the {@link SrmStatusOfGetRequestResponse}
	 * @throws RemoteException in case of errors
	 * @throws MalformedURIException in case of malformed URIs
	 */
	public SrmStatusOfGetRequestResponse srmPTG(List<String> surls, 
		long maxWaitingTimeInMsec) 
		throws RemoteException, MalformedURIException;
	
	/**
	 * Runs an srm Mkdir.
	 * 
	 * @return the {@link SrmMkdirResponse} in case success
	 * @throws RemoteException in case of errors
	 */
	public SrmMkdirResponse srmMkdir(String surl) throws MalformedURIException, RemoteException;
	
	/**
	 * Runs a srm prepare to put for a list of surls.
	 * The status of the ptp is checked performing status-ptp calls until
	 * the request is completed or a maximum waiting treshold is reached.
	 * 
	 * @param surls
	 * @param maxWaitingTimeInMsec
	 * @return
	 * @throws RemoteException
	 * @throws MalformedURIException
	 */
	public SrmPrepareToPutResponse srmPtP(List<String> surls,long maxWaitingTimeInMsec)
			throws RemoteException, MalformedURIException;
	
	/**
	 * Runs a srm put done for a list of surls with the request token provided.
	 * 
	 * @param surls
	 * @param token
	 * @return
	 * @throws RemoteException
	 * @throws MalformedURIException
	 */
	public SrmPutDoneResponse srmPd(List<String> surls, String token)
			throws RemoteException, MalformedURIException;
	
}

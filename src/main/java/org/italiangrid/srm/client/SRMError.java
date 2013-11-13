package org.italiangrid.srm.client;

import gov.lbl.srm.StorageResourceManager.TStatusCode;


public class SRMError extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	TStatusCode srmStatus;

	public SRMError(TStatusCode status, String message) {
		super(message);
		srmStatus = status;
	}

	public TStatusCode getSRMStatus(){
		return srmStatus;
	}
	
	@Override
	public String getMessage() {
		return String.format("%s: %s\n", srmStatus.toString(), 
			super.getMessage());
	}

}

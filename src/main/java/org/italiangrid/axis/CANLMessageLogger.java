package org.italiangrid.axis;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.emi.security.authn.x509.StoreUpdateListener;
import eu.emi.security.authn.x509.ValidationError;
import eu.emi.security.authn.x509.ValidationErrorListener;
import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.impl.FormatMode;


public enum CANLMessageLogger implements ValidationErrorListener,
	StoreUpdateListener {
	
	INSTANCE;
	
	private final Logger logger = 
		LoggerFactory.getLogger(CANLMessageLogger.class);
		
	public void loadingNotification(String location, String type, Severity level,
		Exception cause) {

		if (location.startsWith("file:"))
      location = location.substring(5, location.length());

		if (level.equals(Severity.ERROR)){
				logger.error("Error for {} {}: {}.", 
					new Object[]{type, location, cause.getMessage()});
      
		}else if (level.equals(Severity.WARNING)){
      logger.debug("Warning for {} {}: {}.", 
      	new Object[]{type, location, cause.getMessage()});
      
		}else if (level.equals(Severity.NOTIFICATION)){
      logger.debug("Loading {} {}.", new Object[]{type, location});
		}
		
	}

	public boolean onValidationError(ValidationError error) {

		String certChainInfo = CertificateUtils.format(error.getChain(), 
			FormatMode.COMPACT_ONE_LINE);
    
		logger.warn("Certificate validation error for chain: {}", certChainInfo);
    logger.warn("Validation Error: {}", error.getMessage());
		return false;
	}

}

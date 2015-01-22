package org.bitcoin.authenticator.core.GcmUtil.Exceptions;

public class GooglePlayServicesNotFoundException extends Exception {
	public GooglePlayServicesNotFoundException(String message) {
        super("GCM Exception\n" + message);
    }
	
	public String getMessage()
    {
        return super.getMessage();
    }
}

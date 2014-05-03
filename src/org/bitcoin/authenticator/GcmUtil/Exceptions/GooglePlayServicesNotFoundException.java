package org.bitcoin.authenticator.GcmUtil.Exceptions;

import android.util.Log;

public class GooglePlayServicesNotFoundException extends Exception {
	public GooglePlayServicesNotFoundException(String message) {
        super("GCM Exception\n" + message);
    }
	
	public String getMessage()
    {
        return super.getMessage();
    }
}

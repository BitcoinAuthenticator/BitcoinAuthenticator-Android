package org.bitcoin.authenticator.core.GcmUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.bitcoin.authenticator.Main;
import org.bitcoin.authenticator.core.GcmUtil.Exceptions.GooglePlayServicesNotFoundException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMRegister {
	
	/* GCM Vars */
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    String regid;
    Context mContext;
	
    public GCMRegister(Context context)
    {
    	mContext = context;
    }
    
    public void runRegistrationSequence() throws GooglePlayServicesNotFoundException
    {
    	if (checkPlayServices()) 
		{
			gcm = GoogleCloudMessaging.getInstance(mContext);
	        regid = getRegistrationId(mContext);
	
	        if (regid.isEmpty()) {
	            registerInBackground();
	        }
	        else{
	        	copyRegistrationIdToSingleton(regid);
	        }
	    } 
		else {
			throw new GooglePlayServicesNotFoundException("Could Not Find Google Services");
		}
    }
    
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity)mContext,
	            		GcmUtilGlobal.PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.v(GcmUtilGlobal.TAG, "No Google Play Service Library Found.");
	            
	        }
	        return false;
	    }
	    return true;
	}
	
	private String getRegistrationId(Context context) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    String registrationId = prefs.getString(GcmUtilGlobal.PROPERTY_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i(GcmUtilGlobal.TAG, "Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    int registeredVersion = prefs.getInt(GcmUtilGlobal.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	        Log.i(GcmUtilGlobal.TAG, "App version changed.");
	        return "";
	    }
	    return registrationId;
	}
	
	public static SharedPreferences getGCMPreferences(Context context) {
	    // This sample app persists the registration ID in shared preferences, but
	    // how you store the regID in your app is up to you.
	    return context.getSharedPreferences(Main.class.getSimpleName(),
	            Context.MODE_PRIVATE);
	}
	
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void registerInBackground() 
	{
		new AsyncTask() {
	        @Override
	        protected Object doInBackground(Object... params)
	        {
	            String msg = "";
	            try {
	                if (gcm == null)
	                {
	                    gcm = GoogleCloudMessaging.getInstance(mContext);
	                }
	                regid = gcm.register(GcmUtilGlobal.API_CONSOLE_PROJECT_NUMBER);
	                msg = "Device registered, registration ID=" + regid;

	                // You should send the registration ID to your server over HTTP,
	                // so it can use GCM/HTTP or CCS to send messages to your app.
	                // The request to your server should be authenticated if your app
	                // is using accounts.
	                copyRegistrationIdToSingleton(regid);

	                // For this demo: we don't need to send it because the device
	                // will send upstream messages to a server that echo back the
	                // message using the 'from' address in the message.

	                // Persist the regID - no need to register again.
	                storeRegistrationId(mContext, regid);
	            } 
	            catch (IOException ex) 
	            {
	                msg = "Error :" + ex.getMessage();
	                // If there is an error, don't just keep trying to register.
	                // Require the user to click a button again, or perform
	                // exponential back-off.
	            }
	            return msg;
	        }

	    }.execute(null, null, null);
	}
	
	private void copyRegistrationIdToSingleton(String regId) {
		GcmUtilGlobal.gcmRegistrationToken = regId;
	    Log.v(GcmUtilGlobal.TAG,"GCM Registration ID - " + regId);
	}
	
	public static void storeRegistrationId(Context context, String regId) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    int appVersion = getAppVersion(context);
	    Log.i(GcmUtilGlobal.TAG, "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(GcmUtilGlobal.PROPERTY_REG_ID, regId);
	    editor.putInt(GcmUtilGlobal.PROPERTY_APP_VERSION, appVersion);
	    editor.commit();
	}
}

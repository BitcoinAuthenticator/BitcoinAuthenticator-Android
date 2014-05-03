package org.bitcoin.authenticator;

import org.bitcoin.authenticator.GcmUtil.GCMRegister;
import org.bitcoin.authenticator.GcmUtil.GcmUtilGlobal;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

/**
 * This class loads at startup. It figures out if the authenticator has already been paired.
 * If so, it loads the wallet_list activity. Otherwise it loads a welcome activity.
 */
public class Main extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/* GCM Registration 
		 * GCM depends on google play services library.
		 * Build Instructions - 
		 * 1) download the git submodule of this project
		 * 2) import the project from /libs/google-play-services/libproject/google-play-services_lib into eclipse
		 * via "file -> import -> Existing android code
		 * 3) in BitoinAuthenticator, right click properties -> android -> library (add)
		 * 4) you are done !
		 * */
		
		{ /*  */
			GCMRegister regClass = new GCMRegister(this);
			try {
				regClass.runRegistrationSequence();
			} catch (Exception e) {
				Log.v(GcmUtilGlobal.TAG, " Error - " + e.getMessage());
				//TODO - rise error //Utils.riseConfirmAlertToView(this, "GCM Error", e.getMessage());
				GcmUtilGlobal.gcmRegistrationToken = "";
			}
		}
		
		SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
	    Boolean paired = settings.getBoolean("paired", false);
	    if (paired==true){
	    	startActivity (new Intent(Main.this, Wallet_list.class));
	    }
	    else {
	    	startActivity (new Intent(Main.this, Welcome.class));
	    }		
	}
	
}

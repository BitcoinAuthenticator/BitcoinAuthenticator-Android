package org.bitcoin.authenticator;

import org.bitcoin.authenticator.BAPreferences.BAPreferences;
import org.bitcoin.authenticator.core.GcmUtil.GCMRegister;
import org.bitcoin.authenticator.core.GcmUtil.GcmUtilGlobal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * This class loads at startup. It figures out if the authenticator has already been paired.
 * If so, it loads the wallet_list activity. Otherwise it loads a welcome activity.
 */
public class Main extends Activity {

	@Override
	public void onNewIntent(Intent intent){
		gcmInit();
		
		// init preferencess
		new BAPreferences(this.getApplicationContext());
		
		//SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
	    Boolean paired = BAPreferences.ConfigPreference().getPaired(false);//settings.getBoolean("paired", false);
	    Boolean pendingReq = false;
	    
		{
			if(intent.getStringExtra("RequestID") != null){
				pendingReq = true;
			}
		}
		
	    if (paired==true){
	    	Intent in = new Intent(Main.this, Wallet_list.class);
	    	if(pendingReq)
	    	{
	    		in.putExtra("RequestID", intent.getStringExtra("RequestID"));
	    	}
	    	startActivity (in);
	    }
	    else {
	    	startActivity (new Intent(Main.this, Welcome.class));
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		onNewIntent(getIntent());
	}
	
	private void gcmInit()
	{
		{ 
			GCMRegister regClass = new GCMRegister(this);
			try {
				regClass.runRegistrationSequence();
			} catch (Exception e) {
				Log.v(GcmUtilGlobal.TAG, " Error - " + e.getMessage());
				//TODO - rise error //Utils.riseConfirmAlertToView(this, "GCM Error", e.getMessage());
				GcmUtilGlobal.gcmRegistrationToken = "";
			}
		}
	}
}

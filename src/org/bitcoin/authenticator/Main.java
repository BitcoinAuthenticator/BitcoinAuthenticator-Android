package org.bitcoin.authenticator;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

/**
 * This class loads at startup. It figures out if the authenticator has already been paired.
 * If so, it loads the wallet_list activity. Otherwise it loads a welcome activity.
 */
public class Main extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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

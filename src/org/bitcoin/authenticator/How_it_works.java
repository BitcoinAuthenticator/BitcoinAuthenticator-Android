package org.bitcoin.authenticator;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Creates an activity that explains to the user how the Bitcoin Authenticator works. 
 * This activity is pretty ugly. In the future I'm going to add some descriptive images/clip art and 
 * make it so the user can swipe to see the next image. 
 */
public class How_it_works extends Activity {

	Boolean paired;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_how_it_works);
		//The user can access this activity from two places ― the welcome activity and the menu in the 
		//wallet_list activity. If she's accessing it from the welcome activity (ie. the device isn't paired)
		//the button text should say "Begin Setup". If she's accessing it from the menu it should just say "Continue" 
		SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
	    paired = settings.getBoolean("paired", false);
	    Button btn = (Button) findViewById(R.id.btnBeginSetup);
	    if (paired==true){
	    	btn.setText("Continue");
	    }
		setupBeginSetupBtn();
	}
	
	/** 
	 * Like before, if the user is accessing this activity from the welcome activity, this button should take her
	 * to the Show_seed activity. Otherwise, it should just take her back to the main wallet_list activity.
	 */
	private void setupBeginSetupBtn(){
		Button BeginSetupButton = (Button) findViewById(R.id.btnBeginSetup);
		BeginSetupButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (paired==true){
			    	startActivity (new Intent(How_it_works.this, Wallet_list.class));
			    }
			    else {
			    	startActivity (new Intent(How_it_works.this, Show_seed.class));
			    }		
				
			}
		});
	}
	
}

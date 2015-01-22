package org.bitcoin.authenticator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

/**
 * Creates an activity welcoming the user to Bitcoin Autheticator and gives her the choice learn more about 
 * how it works or skip directly to pairing. 
 */
public class Welcome extends Activity {

	private ProgressDialog mProgressDialog;
	
	private Button howitworksButton;
	private Button NewWalletButton;
	private Button restoreButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		setupHowItWorksBtn();
		setupRestoreBtn();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {

	    if(keyCode == KeyEvent.KEYCODE_BACK)
	    {
	            moveTaskToBack(true);
	            return true;
	    }
		return false;
	}
	
	/**These methods set up the activity components*/
	private void setupHowItWorksBtn(){
		Button howitworksButton = (Button) findViewById(R.id.btnHowItWorks);
		howitworksButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity (new Intent(Welcome.this, How_it_works.class));
			}
		});
	}
	
	private void setupRestoreBtn(){
		Button restoreButton = (Button) findViewById(R.id.btnRestoreWallet);
		restoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity (new Intent(Welcome.this, Restore_Menu.class));
			}
		});
	}
	
}
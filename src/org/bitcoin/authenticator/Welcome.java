package org.bitcoin.authenticator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Creates an activity welcoming the user to Bitcoin Autheticator and gives her the choice learn more about 
 * how it works or skip directly to pairing. 
 */
public class Welcome extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		setupHowItWorksBtn();
		setupPairBtn();
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
	
	private void setupPairBtn(){
		Button PairButton = (Button) findViewById(R.id.btnPairWallet);
		PairButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity (new Intent(Welcome.this, Show_seed.class));
			}
		});
	}
	
}
package org.bitcoin.authenticator;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class PairingDetails extends Activity {

	private ImageView iv;
	private TextView txvName;
	private TextView txvPairingID;
	private TextView txvAccountID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pairing_details);
		
		txvName = (TextView) findViewById(R.id.pairing_details_name);
		txvPairingID = (TextView) findViewById(R.id.pairing_details_pairing_id);
		txvAccountID = (TextView) findViewById(R.id.pairing_details_account_id);
		
		String name = getIntent().getStringExtra("walletName");
		String pairID = getIntent().getStringExtra("fingerprint");
		String accID = getIntent().getStringExtra("accountID");
		int icon = getIntent().getIntExtra("icon", 0);
		
		txvName.setText(name);
		txvPairingID.setText(pairID);
		txvAccountID.setText(accID);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pairing_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

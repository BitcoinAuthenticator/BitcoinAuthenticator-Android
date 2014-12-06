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
	private TextView txvAccountID;
	private TextView txvExtIP;
	private TextView txvInternalIP;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pairing_details);
		
		txvName = (TextView) findViewById(R.id.pairing_details_name);
		txvAccountID = (TextView) findViewById(R.id.pairing_details_account_id);
		txvExtIP = (TextView) findViewById(R.id.pairing_details_ext_ip);
		txvInternalIP = (TextView) findViewById(R.id.pairing_details_int_ip);
		
		String name = getIntent().getStringExtra("walletName");
		String accID = getIntent().getStringExtra("accountID");
		String extIP = getIntent().getStringExtra("externalIP");
		String internalIP = getIntent().getStringExtra("internalIP");
		int icon = getIntent().getIntExtra("icon", 0);
		
		txvName.setText(name);
		txvAccountID.setText(accID);
		txvExtIP.setText(extIP);
		txvInternalIP.setText(internalIP);
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

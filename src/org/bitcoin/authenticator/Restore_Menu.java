package org.bitcoin.authenticator;

import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferences;
import org.bitcoin.authenticator.backup.PaperWalletQR;
import org.bitcoin.authenticator.backup.PaperWalletQR.SeedQRData;
import org.bitcoin.authenticator.core.WalletCore;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase.ReadyToScanQROnClickListener;
import org.bitcoin.authenticator.dialogs.ReadyToScanQRDialog;

import com.google.zxing.integration.android.IntentIntegrator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

public class Restore_Menu extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restore_menu);
		
		setupRestoreFromMnemonicBtn();
		setupRestoreFromQRBtn();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.restore__menu, menu);
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
	
	private void setupRestoreFromMnemonicBtn(){
		Button restoreButton = (Button) findViewById(R.id.restore_menu_btn_mnemonic);
		restoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity (new Intent(Restore_Menu.this, Restore_Mnemonic.class));
			}
		});
	}
	
	private void setupRestoreFromQRBtn(){
		Button restoreButton = (Button) findViewById(R.id.restore_menu_btn_qr);
		restoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ReadyToScanQRDialog d = new ReadyToScanQRDialog(Restore_Menu.this);
											d.setTitle("Scan QR");
											d.setSecondaryTitle("If you are ready to scan the QR, press the Ready button");
											d.requestWindowFeature(Window.FEATURE_NO_TITLE);
											d.setDialogCenterIcon(R.drawable.ic_qr_scan);
											d.setOkButtonListener(new ReadyToScanQROnClickListener(){
												@Override
												public void onClick(BAAlertDialogBase alert) {
													try {
														IntentIntegrator z = new IntentIntegrator(Restore_Menu.this);
														z.initiateScan();
													} catch (Exception e) {
														e.printStackTrace();
													}
													catch (Error e)
													{
														/* Download zxing */
														Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
														Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
														startActivity(marketIntent);
													}
												}
											});
											d.setCancelButtonListener(new ReadyToScanQROnClickListener(){
												@Override
												public void onClick(BAAlertDialogBase alert) { }
											});
						
						d.show();
			}
		});
	}
	
	/**
	 * This is the method which executes when the QR code is scanned. It parses the data, saves the relevant parts to
	 * memory and starts the pairing protocol. 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		String input;
		if (resultCode == RESULT_OK) {
            //
			input = intent.getStringExtra("SCAN_RESULT");
			new BuildSeedFromQRScan(input).execute("");
		} 
	}
	
	private ProgressDialog mProgressDialog;
	private class BuildSeedFromQRScan extends AsyncTask<String, Void, String> {

		private String data;
		
		public BuildSeedFromQRScan(String data) {
			this.data = data;
		}
		
		@Override
        protected void onPreExecute() { 
			mProgressDialog = new ProgressDialog(Restore_Menu.this, R.style.CustomDialogSpinner);
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.show();
            
			Button restoreQRButton = (Button) findViewById(R.id.restore_menu_btn_qr);
			restoreQRButton.setEnabled(false);
			Button restoreMnemonicButton = (Button) findViewById(R.id.restore_menu_btn_mnemonic);
			restoreMnemonicButton.setEnabled(false);
		}
		
		@Override
		protected String doInBackground(String... params) {
			PaperWalletQR qr = new PaperWalletQR(Restore_Menu.this);
			SeedQRData data = qr.parseSeedQR(this.data);
			
			WalletCore wc = new WalletCore();
			wc.saveSeedBytes(Restore_Menu.this, data.getSeedFromMnemonics());
			wc.saveMnemonic(Restore_Menu.this, data.toMnemonicArray());
			BAPreferences.ConfigPreference().setInitialized(true);
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			startActivity (new Intent(Restore_Menu.this, Show_seed.class));
	    }
	}
}

package org.bitcoin.authenticator;

import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferences;
import org.bitcoin.authenticator.GcmUtil.GcmUtilGlobal;
import org.bitcoin.authenticator.Pair_wallet.connectTask;
import org.bitcoin.authenticator.Wallet_list.WalletItem;
import org.bitcoin.authenticator.backup.PaperWalletQR;
import org.bitcoin.authenticator.backup.PaperWalletQR.SeedQRData;
import org.bitcoin.authenticator.core.WalletCore;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase.ReadyToScanQROnClickListener;
import org.bitcoin.authenticator.dialogs.ReadyToScanQRDialog;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase.SingleInputOnClickListener;
import org.json.JSONException;

import com.google.zxing.integration.android.IntentIntegrator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

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
		setupNewWalletBtn();
		setupRestoreBtn();
	}
	
	/**These methods set up the activity components*/
	private void setupHowItWorksBtn(){
		howitworksButton = (Button) findViewById(R.id.btnHowItWorks);
		howitworksButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity (new Intent(Welcome.this, How_it_works.class));
			}
		});
	}
	
	private void setupNewWalletBtn(){
		NewWalletButton = (Button) findViewById(R.id.btnNewWallet);
		NewWalletButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity (new Intent(Welcome.this, Show_seed.class));
			}
		});
	}
	
	private void setupRestoreBtn(){
		restoreButton = (Button) findViewById(R.id.btnRestoreWallet);
		restoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				askIfReadyToScanQR();
			}
		});
	}
	
	private void askIfReadyToScanQR(){
		ReadyToScanQRDialog d = new ReadyToScanQRDialog(this);
							d.setTitle("Scan QR");
							d.setSecondaryTitle("If you are ready to scan the QR, press the Ready button");
							d.requestWindowFeature(Window.FEATURE_NO_TITLE);
							d.setDialogCenterIcon(R.drawable.ic_qr_scan);
							d.setOkButtonListener(new ReadyToScanQROnClickListener(){
								@Override
								public void onClick(BAAlertDialogBase alert) {
									try {
										IntentIntegrator z = new IntentIntegrator(Welcome.this);
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
	
	/**
	 * This is the method which executes when the QR code is scanned. It parses the data, saves the relevant parts to
	 * memory and starts the pairing protocol. 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		String input;
		if (resultCode == RESULT_OK) {
            //
			input = intent.getStringExtra("SCAN_RESULT");
			new RestoreSeedTask(input) {
				@Override
		        protected void onPostExecute(String result) {
					mProgressDialog.hide();
					startActivity (new Intent(Welcome.this, Show_seed.class));
				}
				
			}.execute("");
		} 
		else
			Toast.makeText(getApplicationContext(), "Could Not ReadQR", Toast.LENGTH_LONG).show();
	}
	
	private class RestoreSeedTask extends AsyncTask<String, Void, String> {
		private SeedQRData seedData;
		private String inputQRString;
		
		public RestoreSeedTask(String inputQRString) {
			this.inputQRString = inputQRString;
		}
		
		@Override
        protected void onPreExecute() { 
			//Display a spinner while the device is pairing.
			mProgressDialog = new ProgressDialog(Welcome.this, R.style.CustomDialogSpinner);
			mProgressDialog.setIndeterminate(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
    		mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
            
//            howitworksButton.setVisibility(View.INVISIBLE);
        	NewWalletButton.setVisibility(View.INVISIBLE);
        	restoreButton.setVisibility(View.INVISIBLE);
        	
		}
		
		@Override
		protected String doInBackground(String... params) {
			
			PaperWalletQR qr = new PaperWalletQR(Welcome.this);
			SeedQRData data = qr.parseSeedQR(inputQRString);
			
			WalletCore wc = new WalletCore();
			wc.saveSeedBytes(Welcome.this, data.getSeedFromMnemonics());
			wc.saveMnemonic(Welcome.this, data.toMnemonicArray());
			BAPreferences.ConfigPreference().setInitialized(true);

			return null;
		}
		
	}
}
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
		Button howitworksButton = (Button) findViewById(R.id.btnHowItWorks);
		howitworksButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity (new Intent(Welcome.this, How_it_works.class));
			}
		});
	}
	
	private void setupNewWalletBtn(){
		Button NewWalletButton = (Button) findViewById(R.id.btnNewWallet);
		NewWalletButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity (new Intent(Welcome.this, Show_seed.class));
			}
		});
	}
	
	private void setupRestoreBtn(){
		Button restoreButton = (Button) findViewById(R.id.btnRestoreWallet);
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
			PaperWalletQR qr = new PaperWalletQR(Welcome.this);
			SeedQRData data = qr.parseSeedQR(input);
			
			WalletCore wc = new WalletCore();
			wc.saveSeedBytes(Welcome.this, data.getSeedFromMnemonics());
			wc.saveMnemonic(Welcome.this, data.toMnemonicArray());
			
			startActivity (new Intent(Welcome.this, Show_seed.class));
		} 
	}
}
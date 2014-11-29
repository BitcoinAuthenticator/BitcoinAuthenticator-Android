package org.bitcoin.authenticator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferences;
import org.bitcoin.authenticator.PairingProtocol.CouldNotPairToWalletException;
import org.bitcoin.authenticator.PairingProtocol.PairingQRData;
import org.bitcoin.authenticator.GcmUtil.GcmUtilGlobal;
import org.bitcoin.authenticator.net.Connection.CannotConnectToWalletException;
import org.bitcoin.authenticator.utils.EncodingUtils;

import com.google.zxing.integration.android.IntentIntegrator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Creates the activity which allows the user to pair the Authenticator with her wallet. The steps for pairing include:
 * 1) Entering a name for the wallet which will be added to the list view in the wallet_list activity.
 * 2) Opening a QR scanner and scanning the pairing QR code displayed by the wallet.
 * 3) Running the pairing protocol in background using data acquired from QR.
 * 4) Saving the wallet metadata to shared preferences and the AES key to internal storage.
 */
public class Pair_wallet extends Activity {
	
	ProgressDialog mProgressDialog;
	private CheckBox chkForceAccountID;
	private EditText accountID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pair_wallet);
		setupScanButton();	
	}
	
	/**Sets up the Scan button component*/
	private void setupScanButton(){
		ImageButton scanBtn = (ImageButton) findViewById(R.id.btnScan);
		
		//Check and make sure the user entered a name, if not display a warning dialog.
		scanBtn.setOnClickListener(new OnClickListener() {
			
			private void showError(String msg){
				AlertDialog alertDialog = new AlertDialog.Builder(
		                  Pair_wallet.this).create();
					// Setting Dialog Title
					alertDialog.setTitle("Error");
					// Setting Dialog Message
					alertDialog.setMessage(msg);
					// Setting Icon to Dialog
					alertDialog.setIcon(R.drawable.ic_error);
					// Setting OK Button
					alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					// Showing Alert Message
					alertDialog.show();
			}
						
			@SuppressLint("ShowToast")
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {			
				try {
					launchScanActivity();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), "ERROR:" + e, 1).show();
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
	}
	
	/**Launches the QR scanner*/
	public void launchScanActivity()
	{
		IntentIntegrator z = new IntentIntegrator(this);
		z.initiateScan();
	}

	/**
	 * This is the method which executes when the QR code is scanned. It parses the data, saves the relevant parts to
	 * memory and starts the pairing protocol. 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		String QRInput;
		//if (requestCode == ZXING_QR_SCANNER_REQUEST) {
			if (resultCode == RESULT_OK) {
				//Display a spinner while the device is pairing.
				mProgressDialog = new ProgressDialog(this, R.style.CustomDialogSpinner);
				mProgressDialog.setIndeterminate(false);
	            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            mProgressDialog.setCancelable(false);
	    		mProgressDialog.setCanceledOnTouchOutside(false);
	            mProgressDialog.show();
            //
			QRInput = intent.getStringExtra("SCAN_RESULT");
			PairingQRData qrData = PairingProtocol.parseQRString(QRInput);
			if(qrData == null) {
				runOnUiThread(new Runnable() {
        			public void run() {
						  Toast.makeText(getApplicationContext(), "Unrecognized QR", Toast.LENGTH_LONG).show();
					}
				});
				
				mProgressDialog.hide();
				mProgressDialog = null;
				
				return;
			}

			//Start the pairing protocol
			connectTask conx = new connectTask(qrData);
		    conx.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} 
			else if (resultCode == RESULT_CANCELED) {
				QRInput = "Scan canceled.";
			
			}
	}
	

	/**
	 * This class runs in the background. It creates a connection object which connects
	 * to the wallet and executes the core of the pairing protocol.
	 */
	public class connectTask extends AsyncTask<String,String,PairingProtocol> {
		
		private String IPAddress;
		private String walletType;
		private String LocalIP;
		private String AESKey;
		private String pairingName;
		private long walletIndex;
		private int networkType;
		
		public connectTask(PairingQRData qrData){
			this.IPAddress 		= qrData.IPAddress;
			this.walletType		= qrData.walletType;
			this.LocalIP 		= qrData.LocalIP;
			this.AESKey 		= qrData.AESKey;
			this.pairingName 	= qrData.pairingName;
			this.walletIndex 	= qrData.walletIndex;
			this.networkType 	= qrData.networkType;
		}
		
        @Override
        protected PairingProtocol doInBackground(String... message) {
            //Load the seed from file
        	byte [] seed = null;
    		String FILENAME = "seed";
    		File file = new File(getFilesDir(), FILENAME);
    		int size = (int)file.length();
    		if (size != 0)
    		{
    			FileInputStream inputStream = null;
    			try {inputStream = openFileInput(FILENAME);} 
    			catch (FileNotFoundException e1) {e1.printStackTrace();}
    			seed = new byte[size];
    			try {inputStream.read(seed, 0, size);} 
    			catch (IOException e) {e.printStackTrace();}
    			try {inputStream.close();} 
    			catch (IOException e) {e.printStackTrace();}
    		}
    		
    		String[] ips = new String[] { IPAddress, LocalIP};
    		PairingProtocol pair2wallet = new PairingProtocol(ips);
    		

    		//Run pairing protocol
            SecretKey secretkey = new SecretKeySpec(Utils.hexStringToByteArray(AESKey), "AES");
            byte[] regID = (GcmUtilGlobal.gcmRegistrationToken).getBytes();
			try {
				pair2wallet.run(seed, 
						secretkey, 
						regID, 
						walletIndex);
				completePairing(AESKey, 
						IPAddress, 
						LocalIP, 
						walletType, 
						pairingName, 
						walletIndex, 
						networkType);
			} 
			catch (CouldNotPairToWalletException e) {
				e.printStackTrace();
				runOnUiThread(new Runnable() {
        			public void run() {
						  Toast.makeText(getApplicationContext(), "Unable to pair", Toast.LENGTH_LONG).show();
					}
				});
			}

            return null;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
        protected void onPostExecute(PairingProtocol result) {
            super.onPostExecute(result);
    		startActivity(new Intent(Pair_wallet.this, Wallet_list.class));
        }
        
        /**
    	 * This method executes the final step in pairing. It saves the following metadata to shared preferences: 
    	 * Wallet ID, Fingerprint of the AES key, Type (for wallet_list icon), External IP, Local IP
    	 * Saves the AES key to private internal storage.
    	 */
		private void completePairing(String AESKey, 
    			String IPAddress, 
    			String LocalIP, 
    			String walletType, 
    			String pairingName,
    			long walletIndex, 
    			int networkType) {	
			/*
			 * check the pairing name does not exist, if it does add a (2) ending to the name
			 */
			Set<Long> walletIndexSet= BAPreferences.ConfigPreference().getWalletIndexList();
			for (Long i:walletIndexSet) {
				String name = BAPreferences.WalletPreference().getName(Long.toString(i), null);
				boolean isDeleted = BAPreferences.WalletPreference().getDeleted(Long.toString(i), true);
				if(name.equals(pairingName) && isDeleted == false) {
					pairingName = pairingName + " (2)";
					break;
				}
			}
			
    	    String walletData = Long.toString(walletIndex);
    	    BAPreferences.WalletPreference().setWallet(walletData,
    	    		pairingName, 
    	    		walletType, 
    	    		IPAddress, 
    	    		LocalIP,
    	    		networkType,
    	    		false);
    	    //
    	    BAPreferences.ConfigPreference().addWalletIndex(walletIndex);
    	    BAPreferences.ConfigPreference().setPaired(true);
    	    
    	    //Save the AES key to internal storage.
    	    String FILENAME = "AESKey" + walletIndex;
    	    byte[] keyBytes = Utils.hexStringToByteArray(AESKey);
    	    FileOutputStream outputStream = null;
    		try {outputStream = openFileOutput(FILENAME, Context.MODE_PRIVATE);} 
    		catch (FileNotFoundException e1) {e1.printStackTrace();}
    		try {outputStream.write(keyBytes);} 
    		catch (IOException e) {e.printStackTrace();}
    		try {outputStream.close();} 
    		catch (IOException e) {e.printStackTrace();} 		
    	}
    }
	
	static public class CannotScanExceptionTemp extends Exception {
		public CannotScanExceptionTemp(String str) {
			super(str);
		}
	}
}




	


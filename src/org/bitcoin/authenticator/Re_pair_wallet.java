package org.bitcoin.authenticator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferences;
import org.bitcoin.authenticator.Connection.CannotConnectToWalletException;
import org.bitcoin.authenticator.PairingProtocol.CouldNotPairToWalletException;
import org.bitcoin.authenticator.PairingProtocol.PairingQRData;
import org.bitcoin.authenticator.GcmUtil.GcmUtilGlobal;

import com.google.zxing.integration.android.IntentIntegrator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class Re_pair_wallet extends Activity{
	long walletNum;
	String AESKey;
	String IPAddress;
	String LocalIP;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		walletNum = getIntent().getLongExtra("walletNum", -1);
		if (walletNum == -1) throw new AssertionError("Wrong wallet number !");
		try {launchScanActivity();} 
		catch (Exception e) {e.printStackTrace();}
		catch (Error e){
			/* Download zxing */
			Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
			Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
			startActivity(marketIntent);
		}
	}
	
	public void launchScanActivity()
	{
		IntentIntegrator z = new IntentIntegrator(this);
		z.initiateScan();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		String QRInput;
		//if (requestCode == ZXING_QR_SCANNER_REQUEST) {
			if (resultCode == RESULT_OK) {
				QRInput = intent.getStringExtra("SCAN_RESULT");
				PairingQRData qrData = PairingProtocol.parseQRString(QRInput);
				if(qrData == null) {
					runOnUiThread(new Runnable() {
	        			public void run() {
							  Toast.makeText(getApplicationContext(), "Unrecognized QR", Toast.LENGTH_LONG).show();
						}
					});
					
					return;
				}
				
				qrData.fingerprint = PairingProtocol.getPairingIDDigest(walletNum, GcmUtilGlobal.gcmRegistrationToken);
				String walletData = Long.toString(walletNum);
				BAPreferences.WalletPreference().setFingerprint(walletData, qrData.fingerprint);
				BAPreferences.WalletPreference().setExternalIP(walletData, IPAddress);
				BAPreferences.WalletPreference().setLocalIP(walletData, LocalIP);
				
				//Save the AES key to internal storage.
			    String FILENAME = "AESKey" + walletNum;
			    byte[] keyBytes = Utils.hexStringToByteArray(AESKey);
			    FileOutputStream outputStream = null;
				try {outputStream = openFileOutput(FILENAME, Context.MODE_PRIVATE);} 
				catch (FileNotFoundException e1) {e1.printStackTrace();}
				try {outputStream.write(keyBytes);} 
				catch (IOException e) {e.printStackTrace();}
				try {outputStream.close();} 
				catch (IOException e) {e.printStackTrace();} 
				//Start the pairing protocol
				connectToWallet();
				startActivity(new Intent(Re_pair_wallet.this, Wallet_list.class));
			}
			else if (resultCode == RESULT_CANCELED) {
				QRInput = "Scan canceled.";
				Log.i("asdf", "Cannot Read QR");
				Toast.makeText(getApplicationContext(), "Cannot Read QR", Toast.LENGTH_LONG).show();
			}
			
	}
	
	/**
	 * This class runs in the background. It creates a connection object which connects
	 * to the wallet and executes the core of the pairing protocol.
	 */
	private void connectToWallet() {
		new Thread() {
			@Override
			public void run() {
				//Load the seed from file
				Log.i("asdf", "Reading seed");
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

				PairingProtocol pair2wallet = new PairingProtocol(new String[]{ IPAddress, LocalIP  } );

		        SecretKey secretkey = new SecretKeySpec(Utils.hexStringToByteArray(AESKey), "AES");
		        
		        byte[] regID = (GcmUtilGlobal.gcmRegistrationToken).getBytes();
		        
		        try {
					pair2wallet.run(seed, 
							secretkey, 
							PairingProtocol.getPairingIDDigest(walletNum,GcmUtilGlobal.gcmRegistrationToken), 
							regID, walletNum);
				} catch (CouldNotPairToWalletException e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
		    			public void run() {
							  Toast.makeText(getApplicationContext(), "Unable to pair", Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}.start();
	}
	
}

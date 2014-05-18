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

import org.bitcoin.authenticator.Pair_wallet.connectTask;
import org.bitcoin.authenticator.Pair_wallet.getIPtask;

import com.google.zxing.integration.android.IntentIntegrator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

public class Re_pair_wallet extends Activity{
	static int walletNum;
	String PublicIP;
	String AESKey;
	String IPAddress;
	String LocalIP;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		//TODO
		//if (requestCode == ZXING_QR_SCANNER_REQUEST) {
			if (resultCode == RESULT_OK) {
				QRInput = intent.getStringExtra("SCAN_RESULT");
				//Checking to see what type of data was included in the QR code.
				AESKey = QRInput.substring(QRInput.indexOf("AESKey=")+7, QRInput.indexOf("&PublicIP="));
				IPAddress = QRInput.substring(QRInput.indexOf("&PublicIP=")+10, QRInput.indexOf("&LocalIP="));
				LocalIP = QRInput.substring(QRInput.indexOf("&LocalIP=")+9, QRInput.indexOf("&WalletType="));
				String walletType = QRInput.substring(QRInput.indexOf("&WalletType=")+12, QRInput.length());
				//Calculate the fingerprint of the AES key to serve as the wallet identifier.
				MessageDigest md = null;
				try {md = MessageDigest.getInstance("SHA-1");} 
				catch (NoSuchAlgorithmException e) {e.printStackTrace();}
			    String fingerprint=Utils.bytesToHex(md.digest(Utils.hexStringToByteArray(AESKey)));
				String walletData = "WalletData" + walletNum;
				SharedPreferences data = getSharedPreferences(walletData, 0);
				SharedPreferences.Editor editor = data.edit();	
				//Save the metadata for this wallet to shared preferences
				editor.putString("Fingerprint", fingerprint.substring(32,40));
				editor.putString("ExternalIP", IPAddress);
				editor.putString("LocalIP", LocalIP);
				editor.commit();
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
				//Start the pairing protocol by first getting the device IP address.
				getIPtask ip = new getIPtask();
				ip.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				startActivity(new Intent(Re_pair_wallet.this, Wallet_list.class));
			}
			else if (resultCode == RESULT_CANCELED) {
				QRInput = "Scan canceled.";
			}
	}
	
	/**
	 * This class runs in the background and gets the device's IP address so that we can determine 
	 * if the device is on the same WiFi network as the wallet. If it is, we will need to connect to
	 * the wallet using the local IP address rather than the external IP address. 
	 */
	public class getIPtask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... message) {
        	String ip = Utils.getPublicIP();
            return ip;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
        /**After the task is finished we will launch the AsyncTask which connects to the wallet*/
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            PublicIP = result;
            connectTask conx = new connectTask();
            conx.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }    
    }
	
	/**
	 * Another class which runs in the background. This one creates a connection object which connects
	 * to the wallet and executes the core of the pairing protocol.
	 */
	public class connectTask extends AsyncTask<String,String,PairingProtocol> {
        @Override
        protected PairingProtocol doInBackground(String... message) {
            //Figure out if the mobile device is connected to the same network as the wallet and pass the
        	//appropriate IP address to the connection class. 
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
        	PairingProtocol pair2wallet = null;
            if (IPAddress.equals(PublicIP)){
            	try {pair2wallet = new PairingProtocol(LocalIP);} 
            	catch (IOException e) {e.printStackTrace();}
            }
            else {
            	try {pair2wallet = new PairingProtocol(IPAddress);}
            	catch (IOException e) {e.printStackTrace();}
            }
            SecretKey secretkey = new SecretKeySpec(Utils.hexStringToByteArray(AESKey), "AES");
			try {pair2wallet.run(seed, secretkey, walletNum);} 
			catch (InvalidKeyException e) {e.printStackTrace();} 
			catch (NoSuchAlgorithmException e) {e.printStackTrace();} 
			catch (IOException e) {e.printStackTrace();}
            return null;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
        protected void onPostExecute(PairingProtocol result) {
            super.onPostExecute(result);
        }
    }
}

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

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bitcoin.authenticator.GcmUtil.GcmUtilGlobal;

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
import android.widget.EditText;
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
	private EditText txtID;
	private String IPAddress;
	private String fingerprint;
	private String walletType;
	private String LocalIP;
	private String AESKey;
	public static int num;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pair_wallet);
		setupScanButton();
	}
	
	/**Sets up the Scan button component*/
	private void setupScanButton(){
		Button scanBtn = (Button) findViewById(R.id.btnScan);
		txtID = (EditText) findViewById(R.id.txtLabel);
		//Check and make sure the user entered a name, if not display a warning dialog.
		scanBtn.setOnClickListener(new OnClickListener() {
			@SuppressLint("ShowToast")
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				String check = txtID.getText().toString();
				if (check.matches("")){
					AlertDialog alertDialog = new AlertDialog.Builder(
		                  Pair_wallet.this).create();
					// Setting Dialog Title
					alertDialog.setTitle("Alert");
					// Setting Dialog Message
					alertDialog.setMessage("Please enter a label for this wallet");
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
				//If a name has been entered then open the QR code scanner.
				else {
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
				QRInput = intent.getStringExtra("SCAN_RESULT");
				//Checking to see what type of data was included in the QR code.
					AESKey = QRInput.substring(QRInput.indexOf("AESKey=")+7, QRInput.indexOf("&PublicIP="));
					IPAddress = QRInput.substring(QRInput.indexOf("&PublicIP=")+10, QRInput.indexOf("&LocalIP="));
					LocalIP = QRInput.substring(QRInput.indexOf("&LocalIP=")+9, QRInput.indexOf("&WalletType="));
					walletType = QRInput.substring(QRInput.indexOf("&WalletType=")+12, QRInput.length());
				//Display a spinner while the device is pairing.
					mProgressDialog = new ProgressDialog(this, R.style.CustomDialogSpinner);
					mProgressDialog.setIndeterminate(false);
		            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		            mProgressDialog.show();
				try {
					//Increment the counter for the number of paired wallet in shared preferences
					SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
					SharedPreferences.Editor settingseditor = settings.edit();	
				    num = (settings.getInt("numwallets", 0))+1;
					completePairing();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				//Start the pairing protocol
				connectTask conx = new connectTask();
	            conx.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} 
			else if (resultCode == RESULT_CANCELED) {
				QRInput = "Scan canceled.";
			}
	}
	
	/**
	 * This method executes the final step in pairing. It saves the following metadata to shared preferences: 
	 * Wallet ID, Fingerprint of the AES key, Type (for wallet_list icon), External IP, Local IP
	 * Saves the AES key to private internal storage.
	 */
	private void completePairing() throws NoSuchAlgorithmException{
		//Calculate the fingerprint of the AES key to serve as the wallet identifier.
	    fingerprint = getPairingIDDigest(num, GcmUtilGlobal.gcmRegistrationToken);
	  	SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
	  	SharedPreferences.Editor settingseditor = settings.edit();	
	    String walletData = "WalletData" + num;
	    SharedPreferences data = getSharedPreferences(walletData, 0);
	    SharedPreferences.Editor editor = data.edit();	
	    String wID = "ID";
	    String wFP = "Fingerprint";	
	    String wTP = "Type";
	    String wEIP = "ExternalIP";
	    String wLIP = "LocalIP";
	    //Save the metadata for this wallet to shared preferences
	    editor.putString(wID, txtID.getText().toString());
	    editor.putString(wFP, fingerprint);
	    editor.putString(wTP, walletType);
	    editor.putString(wEIP, IPAddress);
	    editor.putString(wLIP, LocalIP);
	    settingseditor.putInt("numwallets", num);
	    //Set paired to true so that the Authenticator knows to display the wallet_list activity at startup.
	    settingseditor.putBoolean("paired", true);
	    editor.commit();
	    settingseditor.commit();
	    //Save the AES key to internal storage.
	    String FILENAME = "AESKey" + num;
	    byte[] keyBytes = Utils.hexStringToByteArray(AESKey);
	    FileOutputStream outputStream = null;
		try {outputStream = openFileOutput(FILENAME, Context.MODE_PRIVATE);} 
		catch (FileNotFoundException e1) {e1.printStackTrace();}
		try {outputStream.write(keyBytes);} 
		catch (IOException e) {e.printStackTrace();}
		try {outputStream.close();} 
		catch (IOException e) {e.printStackTrace();} 		
	}
	
	/**
	 * This class runs in the background. It creates a connection object which connects
	 * to the wallet and executes the core of the pairing protocol.
	 */
	public class connectTask extends AsyncTask<String,String,PairingProtocol> {
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
    		//Try to connect to wallet using either IP
    		PairingProtocol pair2wallet = null;
    		try {pair2wallet = new PairingProtocol(IPAddress);}
        	catch (IOException e1) {
        		try {pair2wallet = new PairingProtocol(LocalIP);} 
            	catch (IOException e2) {
            		runOnUiThread(new Runnable() {
            			public void run() {
    						  Toast.makeText(getApplicationContext(), "Unable to connect to wallet", Toast.LENGTH_LONG).show();
    					}
    				});
            	}
        	}
    		//Run pairing protocol
            SecretKey secretkey = new SecretKeySpec(Utils.hexStringToByteArray(AESKey), "AES");
            byte[] regID = (GcmUtilGlobal.gcmRegistrationToken).getBytes();
			try {pair2wallet.run(seed, secretkey, getPairingIDDigest(num, GcmUtilGlobal.gcmRegistrationToken), regID, num);} 
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
    		startActivity(new Intent(Pair_wallet.this, Wallet_list.class));
        }
    }
	
	public static String getPairingIDDigest(int num, String gcmRegID)
	 {
		MessageDigest md = null;
		try {md = MessageDigest.getInstance("SHA-1");}
		catch(NoSuchAlgorithmException e) {e.printStackTrace();} 
	    byte[] digest = md.digest((gcmRegID + "_" + Integer.toString(num)).getBytes());
	    String ret = new BigInteger(1, digest).toString(16);
	    //Make sure it is 40 chars, if less pad with 0, if more substringit
	    if(ret.length() > 40)
	    {
    	ret = ret.substring(0, 39);
	    }
	    else if(ret.length() < 40)
	    {
	    	int paddingNeeded = 40 - ret.length();
	    	String padding = "";
	    	for(int i=0;i<paddingNeeded;i++)
	    		padding = padding + "0";
	    	ret = padding + ret;
	    }
	    //Log.v("ASDF","Reg id: " + ret);
	    return ret;
	}
	
}




	


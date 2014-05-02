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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
	private String walletID;
	private String walletType;
	private String LocalIP;
	private String PublicIP;
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
						Intent intent = new Intent(
								"com.google.zxing.client.android.SCAN");
						intent.putExtra("SCAN_MODE", "QR_CODE_MODE,PRODUCT_MODE");
						startActivityForResult(intent, 0);
						walletID = txtID.getText().toString();  
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(getApplicationContext(), "ERROR:" + e, 1).show();
					}
				}
			}
		});
	}
	
	/**
	 * This method executes the final step in pairing. It saves the following metadata to shared preferences: 
	 * Wallet ID, Fingerprint of the AES key, Type (for wallet_list icon), External IP, Local IP
	 * Saves the AES key to private internal storage.
	 */
	private void completePairing() throws NoSuchAlgorithmException{
		//Calculate the fingerprint of the AES key to serve as the wallet identifier.
		MessageDigest md = MessageDigest.getInstance("SHA-1");
	    fingerprint=Utils.bytesToHex(md.digest(Utils.hexStringToByteArray(AESKey)));
	    //Increment the counter for the number of paired wallet in shared preferences
		SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
		SharedPreferences.Editor settingseditor = settings.edit();	
	    num = (settings.getInt("numwallets", 0))+1;
	    String walletData = "WalletData" + num;
	    SharedPreferences data = getSharedPreferences(walletData, 0);
	    SharedPreferences.Editor editor = data.edit();	
	    String wID = "ID";
	    String wFP = "Fingerprint";	
	    String wTP = "Type";
	    String wEIP = "ExternalIP";
	    String wLIP = "LocalIP";
	    //Save the metadata for this wallet to shared preferences
	    editor.putString(wID, walletID);
	    editor.putString(wFP, fingerprint.substring(32,40));
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
		try {
			outputStream = openFileOutput(FILENAME, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			outputStream.write(keyBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}

	/**
	 * This is the method which executes when the QR code is scanned. It parses the data, saves the relevant parts to
	 * memory and starts the pairing protocol. 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		String QRInput;
		if (requestCode == 0) {
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
					completePairing();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				//Start the pairing protocol by first getting the device IP address.
				new getIPtask().execute("");
			} 
			else if (resultCode == RESULT_CANCELED) {
				QRInput = "Scan cancelled.";
			}
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
            new connectTask().execute("");
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
    			try {
    				inputStream = openFileInput(FILENAME);
    			} catch (FileNotFoundException e1) {
    				e1.printStackTrace();
    			}
    			seed = new byte[size];
    			try {
    				inputStream.read(seed, 0, size);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    			try {
    				inputStream.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
        	
        	PairingProtocol pair2wallet = null;
            if (IPAddress.equals(PublicIP)){try {
				pair2wallet = new PairingProtocol(LocalIP);
			} catch (IOException e) {
				e.printStackTrace();
			}}
            else {try {
				pair2wallet = new PairingProtocol(IPAddress);
			} catch (IOException e) {
				e.printStackTrace();
			}}
            SecretKey secretkey = new SecretKeySpec(Utils.hexStringToByteArray(AESKey), "AES");
			try {
				pair2wallet.run(seed, secretkey);
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            return null;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
        protected void onPostExecute(PairingProtocol result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
            //Open the wallet_list activity.
    		startActivity(new Intent(Pair_wallet.this, Wallet_list.class));
        }
    }
	
}




	


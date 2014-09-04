package org.bitcoin.authenticator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferences;
import org.bitcoin.authenticator.GcmUtil.GcmIntentService;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase.ConfirmTxOnClickListener;
import org.bitcoin.authenticator.dialogs.BAConfirmTxDialog;
import org.bitcoin.authenticator.Message.CouldNotSendEncryptedException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.crypto.DeterministicKey;
import com.google.bitcoin.crypto.HDKeyDerivation;
import com.google.bitcoin.crypto.TransactionSignature;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.params.TestNet3Params;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptBuilder;
import com.google.common.collect.ImmutableList;

/**
 * Creates a dialog box to show to the user when the Authenticator receives a transaction from the wallet.
 * Loads the seed from internal storage to derive the private key needed for signing.
 * Asks user for authorization. If yes, it signs the transaction and sends it back to the wallet.
 */
public class ConfirmTxDialog {

	/**
	 * Will close socket after process
	 * 
	 * @param s
	 * @param tx
	 * @param activity
	 * @param walletnum
	 * @param responseListener
	 * @throws InterruptedException
	 */
	public ConfirmTxDialog(final Socket s, 
			final TxData tx, 
			Activity activity, 
			final int walletnum,
			final TxDialogResponse responseListener) throws InterruptedException{
	
		//Load walletID from Shared Preferences
		//SharedPreferences data = activity.getSharedPreferences("WalletData"+ walletnum, 0);
		String name = BAPreferences.WalletPreference().getID(Integer.toString(walletnum),"Null");//data.getString("ID", "null");
		
		//load wallet's ips
		final String[] ips = new String[] 
				{ BAPreferences.WalletPreference().getExternalIP(Integer.toString(walletnum),"Null"),
				  BAPreferences.WalletPreference().getLocalIP(Integer.toString(walletnum),"Null")};
		
		
		//Load AES Key from internal storage
    	byte [] key = null;
		String FILENAME = "AESKey" + walletnum;
		File file = new File(activity.getFilesDir(), FILENAME);
		int size = (int)file.length();
		if (size != 0)
		{
			FileInputStream inputStream = null;
			try {inputStream = activity.openFileInput(FILENAME);} 
			catch (FileNotFoundException e1) {e1.printStackTrace();}
			key = new byte[size];
			try {inputStream.read(key, 0, size);} 
			catch (IOException e) {e.printStackTrace();}
			try {inputStream.close();} 
			catch (IOException e) {e.printStackTrace();}
		}
		final byte[] AESKey = key;
		final SecretKey sharedsecret = new SecretKeySpec(AESKey, "AES");
		//Load the seed from internal storage
		byte [] seed = null;
		String FILENAME2 = "seed";
		File file2 = new File(activity.getFilesDir(), FILENAME2);
		int size2 = (int)file2.length();
		if (size2 != 0)
		{
			FileInputStream inputStream = null;
			try {inputStream = activity.openFileInput(FILENAME2);} 
			catch (FileNotFoundException e1) {e1.printStackTrace();}
			seed = new byte[size2];
			try {inputStream.read(seed, 0, size2);} 
			catch (IOException e) {e.printStackTrace();}
			try {inputStream.close();} 
			catch (IOException e) {e.printStackTrace();}
		}
		final byte[] authseed = seed;
		//Load network parameters from shared preferences
		//SharedPreferences settings = activity.getSharedPreferences("ConfigFile", 0);
        Boolean testnet = BAPreferences.ConfigPreference().getTestnet(false);//settings.getBoolean("testnet", false);
        NetworkParameters params = null;
        if (testnet==false){
        	params = MainNetParams.get();
        } 
        else {
        	params = TestNet3Params.get();
        }
		//Parse through the transaction message and rebuild the transaction.
		byte[] transaction = tx.getTransaction();
		final Transaction unsignedTx = new Transaction(params, transaction);
		//Get the output address and the amount from the transaction so we can display it to the user.
		List<TransactionOutput> outputs = unsignedTx.getOutputs();
		String display = "";
		for (int i = 0; i<outputs.size(); i++){
			TransactionOutput multisigOutput = unsignedTx.getOutput(i);
			String strOutput = multisigOutput.toString();
			String addr = strOutput.substring(strOutput.indexOf("to ")+3, strOutput.indexOf(" script"));
			String amount = strOutput.substring(9, strOutput.indexOf(" to"));
			display = display + addr + " <font color='#98d947'>" + amount + "</font>";
			if (i<outputs.size()-1){display = display + "<br>";}
		}
		//Create the dialog box
		AlertDialog alertDialog = new AlertDialog.Builder(
				activity, R.style.CustomAlertDialogStyle).create();
		
			TextView tv = new TextView(activity);
			tv.setText("Authorize Transaction");
			tv.setTextSize(25);	
			tv.setPadding(50, 50, 0, 50);
			tv.setTextColor(Color.parseColor("#33b5e5"));
			
			//Set title
			alertDialog.setCustomTitle(tv);
			
			//Set dialog message
			alertDialog.setMessage(Html.fromHtml("Bitcoin Authenticator has received a transaction<br><br>From: <br>" + name + "<br><br>To:<br>" + display));
			
			alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Authorize", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int id) {
					try {
						//Prep the JSON object we will fill with the signatures.
						Map obj=new LinkedHashMap();
						obj.put("version", 1);
						obj.put("sigs_n", tx.numInputs);
						JSONArray siglist = new JSONArray();
						//Loop creating a signature for each input
						for (int j=0; j<tx.numInputs; j++){
							//Derive the private key needed to sign the transaction
							ArrayList<Integer> index = tx.getIndexes();
							ArrayList<String> walpubkeys = tx.getPublicKeys();
							HDKeyDerivation HDKey = null;
							DeterministicKey masterKey = HDKey.createMasterPrivateKey(authseed);
							DeterministicKey walletMasterKey = HDKey.deriveChildKey(masterKey, walletnum);
							DeterministicKey childKey = HDKey.deriveChildKey(walletMasterKey,index.get(j));
							byte[] privKey = childKey.getPrivKeyBytes();
							byte[] pubKey = childKey.getPubKey();
							ECKey authenticatorKey = new ECKey(privKey, pubKey);
							ECKey walletPubKey = new ECKey(null, Utils.hexStringToByteArray(walpubkeys.get(j))); 							
							List<ECKey> keys = ImmutableList.of(authenticatorKey, walletPubKey);
							//Create the multisig script we will be using for signing. 
							Script scriptpubkey = ScriptBuilder.createMultiSigOutputScript(2,keys);
							//Create the signature.
							TransactionSignature sig2 = unsignedTx.calculateSignature(j, authenticatorKey, scriptpubkey, Transaction.SigHash.ALL, false);
							byte[] signature = sig2.encodeToBitcoin();
							JSONObject sigobj = new JSONObject();
							try {sigobj.put("signature", Utils.bytesToHex(signature));} 
							catch (JSONException e) {e.printStackTrace();}
							//Add key object to array
							siglist.add(sigobj);					
						}
						obj.put("siglist", siglist);
						StringWriter jsonOut = new StringWriter();
						JSONValue.writeJSONString(obj, jsonOut);						

						String jsonText = jsonOut.toString();
						System.out.println(jsonText);
						byte[] jsonBytes = jsonText.getBytes();
						//Create a new message object
						Message msg = new Message(ips);
						
			        	//Send the signature
						msg.sendEncrypted(jsonBytes, sharedsecret, s);
						responseListener.onAuthorizedTx();
					}
					catch(Exception e) {
						responseListener.OnError();
					}
					
				}
		});
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Decline", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				try {
					JSONObject obj = new JSONObject();
					obj.put("result", "0");
					obj.put("reason", "Authenticator refused to autherize the transaction");
					//
					Message msg = new Message(ips);
					msg.sendEncrypted(obj.toString().getBytes(), sharedsecret, s);

					responseListener.onNotAuthorizedTx();
				}
				catch (Exception e) {
					responseListener.OnError();
				}
				
			}
		});
		
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save", new DialogInterface.OnClickListener() {

		      public void onClick(DialogInterface dialog, int id) {
		    	  try {
		    		JSONObject obj = new JSONObject();
		    		obj.put("justCancelled", "0");
						//
					Message msg = new Message(ips);
					msg.sendEncrypted(obj.toString().getBytes(), sharedsecret, s);
					responseListener.onCancel();
		    	  }
		    	  catch (Exception e) {
					responseListener.OnError();
				  }
				
			}
		});
		alertDialog.show();
		
	}


	public interface TxDialogResponse{
		/**
		 * Will notify the wallet the transaction was authorized
		 */
		public void onAuthorizedTx();
		/**
		 * Will notify the wallet the transaction was not authorized
		 */
		public void onNotAuthorizedTx();
		/**
		 * Will not do anything
		 */
		public void onCancel();
		
		public void OnError();
	}



}


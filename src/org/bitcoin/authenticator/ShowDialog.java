package org.bitcoin.authenticator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bitcoin.authenticator.GcmUtil.GcmIntentService;
import org.bitcoin.authenticator.Wallet_list.ConnectToWallets;
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
import android.os.AsyncTask;
import android.text.Html;
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
public class ShowDialog {

	public ShowDialog(final Connection conn, final TxData tx, Activity activity, final int walletnum) throws InterruptedException{
		//Close the notification if it is still open
		/*NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel((int)GcmIntentService.uniqueId);
		if (tx.equals("error")){
			Toast.makeText(getApplicationContext(), "Unable to connect to wallet", Toast.LENGTH_LONG).show();
		}*/
		//else {
		
		//Load walletID from Shared Preferences
		SharedPreferences data = activity.getSharedPreferences("WalletData"+ walletnum, 0);
		String name = data.getString("ID", "null");
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
		SharedPreferences settings = activity.getSharedPreferences("ConfigFile", 0);
        Boolean testnet = settings.getBoolean("testnet", false);
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
			display = display + addr + " <font color='#009933'>" + amount + " BTC</font>";
			if (i<outputs.size()-1){display = display + "<br>";}
		}
		//Create the dialog box
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
			//Set title
		alertDialogBuilder.setTitle("Authorize Transaction");
		//Set dialog message
		alertDialogBuilder
			.setMessage(Html.fromHtml("Bitcoin Authenticator has received a transaction<br><br>From: <br>" + name + "<br><br>To:<br>" + display))
			.setCancelable(false)
			.setPositiveButton("Authorize",new DialogInterface.OnClickListener() {
				@SuppressWarnings("unchecked")
				public void onClick(DialogInterface dialog,int id) {
					//Close the dialog box first since the signature operations will create a little lag
					//and we can do them in the background.
					dialog.cancel();
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
					try {JSONValue.writeJSONString(obj, jsonOut);} 
					catch (IOException e1) {e1.printStackTrace();}
					String jsonText = jsonOut.toString();
					System.out.println(jsonText);
					byte[] jsonBytes = jsonText.getBytes();
					//Create a new message object
					Message msg = null;
		        	try {msg = new Message(conn);} 
		        	catch (IOException e) {e.printStackTrace();}
		        	//Send the signature
					try {msg.sendEncrypted(jsonBytes, sharedsecret);} 
					catch (InvalidKeyException e) {e.printStackTrace();} 
					catch (NoSuchAlgorithmException e) {e.printStackTrace();} 
					catch (IOException e) {e.printStackTrace();							}
					//Reload the ConnectionToWallets task to set up to receive another transaction.
					try {conn.close();} 
					catch (IOException e) {e.printStackTrace();}
					//new ConnectToWallets().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
			  })
			.setNegativeButton("Cancel",new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog,int id) {
					JSONObject obj = new JSONObject();
					try {
						obj.put("result", "0");
						obj.put("reason", "Authenticator refused to autherize the transaction");
						//
						Message msg = new Message(conn);
						msg.sendEncrypted(obj.toString().getBytes(), sharedsecret);
					} 
					catch (JSONException e) { e.printStackTrace(); } 
					catch (IOException e) { e.printStackTrace(); } 
					catch (InvalidKeyException e) { e.printStackTrace(); } 
					catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
					finally{
						try { conn.close(); } catch (IOException e) { e.printStackTrace(); }
					}
					dialog.cancel();
				}
			});
			// create and show the alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		
	}






}


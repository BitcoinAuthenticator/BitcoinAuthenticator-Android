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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

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

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class creates the Wallet_list activity which serves as the main activity after the user pairs a wallet.
 * It loads the wallet metadata from shared preferences and creates a listview of paired wallets.
 * In the background it opens a connection to the wallet and waits for transactions. When a transaction is received
 * it displays a dialog box to the user asking for authorization. If it's authorized, it calculates the signature
 * and sends it back to the wallet.
 */
public class Wallet_list extends Activity {
	public static String PublicIP;
	public static String LocalIP;
	public static String IPAddress;
	ListView lv1;
	public static JSONObject req;
	public static Boolean hasPendingReq;
	public static Boolean GCM;
	public static Connection conn;
	public static int walletnum;
	public static int numpairs;
 

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_list);
        //Create the list view
        setListView();
        //Load pending request Boolean and GCM boolean
        SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
        hasPendingReq = settings.getBoolean("request", false);
        GCM = settings.getBoolean("GCM", true);
        numpairs = settings.getInt("numwallets", 0);
        //Start the AsyncTask which waits for new transactions
        if (GCM){new GCMConnect().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);}
        else {for (int b=1; b<=numpairs; b++){new ManualConnect().execute(b);}}
    }

	/**Creates the listview component and defines behavior to a long press on a list item.*/
	void setListView(){
        ArrayList walletList = getListData();
        lv1 = (ListView) findViewById(R.id.custom_list);
        lv1.setLongClickable(true);
        lv1.setAdapter(new CustomListAdapter(this, walletList));
        registerForContextMenu(lv1);
	}

	/**Inflates the menu and adds it to the action bar*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**This method handles the clicks in the option menu*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_pair_wallet){
			startActivity (new Intent(Wallet_list.this, Pair_wallet.class));;
		}
		if (id == R.id.action_how_it_works){
			startActivity (new Intent(Wallet_list.this, How_it_works.class));;
		}
		if (id == R.id.action_show_seed){
			startActivity (new Intent(Wallet_list.this, Show_seed.class));
		}
		if (id == R.id.action_settings){
			startActivity (new Intent(Wallet_list.this, Settings.class));
		}
		return super.onOptionsItemSelected(item);
	}

	/**Creates the context menu that pops up on a long click in the list view*/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
	super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Select Action");
		menu.add(0, v.getId(), 0, "Re-pair");
		menu.add(0, v.getId(), 0, "Rename");
		menu.add(0, v.getId(), 0, "Delete");
	}
    
    /**Handles the clicks in the context menu*/
    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final int index = info.position;
        //Re-pairs with the wallet
       	if(item.getTitle()=="Re-pair"){
       		Object o = lv1.getItemAtPosition(index);
			WalletItem Data = (WalletItem) o;
			Re_pair_wallet.walletNum = Data.getWalletNum();
			startActivity (new Intent(Wallet_list.this, Re_pair_wallet.class));
       	}
       	//Displays a dialog allowing the user to rename the wallet in the listview
    	else if(item.getTitle()=="Rename"){
    		AlertDialog.Builder alert = new AlertDialog.Builder(this);
    		alert.setTitle("Rename");
    		alert.setMessage("Enter a name for this wallet:");
    		// Set an EditText view to get user input 
    		final EditText input = new EditText(this);
    		alert.setView(input);
    		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			String value = input.getText().toString();
    			Object o = lv1.getItemAtPosition(index);
    			WalletItem Data = (WalletItem) o;
    			String wdata = "WalletData" + Data.getWalletNum();
    			SharedPreferences data = getSharedPreferences(wdata, 0);
    			SharedPreferences.Editor editor = data.edit();	
    			editor.putString("ID", value);
    			editor.commit();
    			setListView();
    			}
    		});
    		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {
    		    // Canceled.
    			}
    		});
    		alert.show();
    	}
       	//Displays a dialog prompting the user to confirm they want to delete a wallet from the listview
    	else if(item.getTitle()=="Delete"){
    		AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	    alert.setTitle("Delete");
    	    alert.setMessage(Html.fromHtml("Are you sure you want to delete this wallet?<br><br> Do not continue if this wallet has a positive balance as you will not be able to sign any more transactions."));
    	    alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
    	        public void onClick(DialogInterface dialog, int which) { 
    	        	Object o = lv1.getItemAtPosition(index);
        			WalletItem Data = (WalletItem) o;
        			String wdata = "WalletData" + Data.getWalletNum();
        			SharedPreferences data = getSharedPreferences(wdata, 0);
        			SharedPreferences.Editor editor = data.edit();	
        			editor.clear();
        			editor.putBoolean("Deleted", true);
        			editor.commit();
        			setListView();
    	        }
    	     })
    	    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	        public void onClick(DialogInterface dialog, int which) { 
    	            // do nothing
    	        }
    	     })
    	    .setIcon(android.R.drawable.ic_dialog_alert)
    	     .show();
    	}
    	else {return false;}
	return true;
	}

	/**
	 * Disable the back button because the device is now paired and any activity the user needs can be 
	 * accessed from the menu. 
	 */
	@Override
	public void onBackPressed() {
	}

	/**
	 * Creates a dialog box to show to the user when the Authenticator receives a transaction from the wallet.
	 * Loads the seed from internal storage to derive the private key needed for signing.
	 * Asks user for authorization. If yes, it signs the transaction and sends it back to the wallet.
	 */
	public void showDialogBox(final TxData tx) throws InterruptedException{
		//Close the notification if it is still open
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel((int)GcmIntentService.uniqueId);
		if (tx.equals("error")){
			Toast.makeText(getApplicationContext(), "Unable to connect to wallet", Toast.LENGTH_LONG).show();
		}
		else {
			//Load walletID from Shared Preferences
			SharedPreferences data = getSharedPreferences("WalletData"+ walletnum, 0);
			String name = data.getString("ID", "null");
			//Load AES Key from internal storage
        	byte [] key = null;
    		String FILENAME = "AESKey" + walletnum;
    		File file = new File(getFilesDir(), FILENAME);
    		int size = (int)file.length();
    		if (size != 0)
    		{
    			FileInputStream inputStream = null;
    			try {inputStream = openFileInput(FILENAME);} 
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
    		File file2 = new File(getFilesDir(), FILENAME2);
    		int size2 = (int)file2.length();
    		if (size2 != 0)
    		{
    			FileInputStream inputStream = null;
    			try {inputStream = openFileInput(FILENAME2);} 
    			catch (FileNotFoundException e1) {e1.printStackTrace();}
    			seed = new byte[size2];
    			try {inputStream.read(seed, 0, size2);} 
    			catch (IOException e) {e.printStackTrace();}
    			try {inputStream.close();} 
    			catch (IOException e) {e.printStackTrace();}
    		}
    		final byte[] authseed = seed;
    		//Load network parameters from shared preferences
    		Boolean testnet = tx.getParams();
            NetworkParameters params = null;
            if (testnet==false){params = MainNetParams.get();} 
            else {params = TestNet3Params.get();}
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
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);
			//Set title
			alertDialogBuilder.setTitle("Authorize Transaction");
			//Set dialog message
			alertDialogBuilder
				.setMessage(Html.fromHtml("Bitcoin Authenticator has received a transaction<br><br>From: <br>" + name + "<br><br>To:<br>" + display))
				.setCancelable(false)
				.setPositiveButton("Authorize",new DialogInterface.OnClickListener() {
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
						try {msg.sendSig(jsonBytes, sharedsecret);} 
						catch (InvalidKeyException e) {e.printStackTrace();} 
						catch (NoSuchAlgorithmException e) {e.printStackTrace();} 
						catch (IOException e) {e.printStackTrace();							}
						//Reload the ConnectionToWallets task to set up to receive another transaction.
						try {conn.close();} 
						catch (IOException e) {e.printStackTrace();}
						if (GCM){new GCMConnect().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);}
				        else {for (int b=1; b<=numpairs; b++){new ManualConnect().execute(b);}}
					}
				  })
				.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						//TODO if the user doesn't approve the transaction, send a message decline message back to the wallet.
						//Reload the ConnectionToWallets task to set up to receive another transaction.
						try {conn.close();} 
						catch (IOException e) {e.printStackTrace();}
						if (GCM){new GCMConnect().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);}
				        else {for (int b=1; b<=numpairs; b++){new ManualConnect().execute(b);}}
						dialog.cancel();
					}
				});
				// create and show the alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();}
	}

	/**This method loads the metadata from Shared Preferences needed to display in the listview*/
    private ArrayList getListData() {
    	//Open shared preferences and get the number of wallets
    	SharedPreferences settings = getSharedPreferences("ConfigFile", 0);	
	    int num = settings.getInt("numwallets", 0);
    	ArrayList results = new ArrayList();
    	//Load the data for each wallet and add it to a WalletItem object
    	for (int i=1; i<num+1; i+=1){
    		String wdata = "WalletData" + i;
    		SharedPreferences data = getSharedPreferences(wdata, 0);	
    		WalletItem walletData = new WalletItem();
    		Boolean deleted = data.getBoolean("Deleted", false);
    		String wID = ("ID");
    		String wFP = ("Fingerprint");
    		String wTP = ("Type");
    		walletData.setWalletNum(i);
    		walletData.setWalletLabel(data.getString(wID, "null"));
    		String fingerprint = data.getString(wFP, "null");
    		if (!fingerprint.equals("null")) walletData.setFingerprint(fingerprint.substring(32,40).toUpperCase());
    		//Decide which icon to display
    		if (data.getString(wTP, "null").equals("blockchain")){walletData.setIcon(R.drawable.blockchain_info_logo);}
    		else if (data.getString(wTP, "null").equals("electrum")){walletData.setIcon(R.drawable.electrum_logo);}
    		else if (data.getString(wTP, "null").equals("hive")){walletData.setIcon(R.drawable.hive_logo);}
    		else if (data.getString(wTP, "null").equals("multibit")){walletData.setIcon(R.drawable.multibit_logo);}
    		else if (data.getString(wTP, "null").equals("bitcoincore")){walletData.setIcon(R.drawable.bitcoin_core_logo);}
    		else if (data.getString(wTP, "null").equals("armory")){walletData.setIcon(R.drawable.armory_logo);}
        	else if (data.getString(wTP, "null").equals("darkwallet")){walletData.setIcon(R.drawable.darkwallet_logo);}
        	else {walletData.setIcon(R.drawable.authenticator_logo);}
    		if (!deleted){results.add(walletData);}
    	}
        return results;
    }
    
    /**Creates an object that holds the metadata for each wallet to include in the listview*/
    public class WalletItem {
    	 
        private String walletLabel;
        private String fingerprint;
        private int icon;
        private int WalletNum;
        
        public int getWalletNum() {
        	return WalletNum;
        }
        
        public void setWalletNum(int num) {
        	this.WalletNum = num;
        }
     
        public String getWalletLabel() {
            return walletLabel;
        }
     
        public void setWalletLabel(String label) {
            this.walletLabel = label;
        }
     
        public String getFingerprint() {
            return fingerprint;
        }
     
        public void setFingerprint(String fp) {
            this.fingerprint = fp;
        }
     
        public int getIcon() {
            return icon;
        }
     
        public void setIcon(int blockchainInfoLogo) {
            this.icon = blockchainInfoLogo;
        }
     
        @Override
        public String toString() {
            return "[ Wallet Label=" + walletLabel + ", Fingerprint=" + 
                    fingerprint + " , Icon=" + icon + "]";
        }
    }

    /**Creates a custom adapter object for the listview*/
    public class CustomListAdapter extends BaseAdapter {
    	
    	private ArrayList listData;
    	private LayoutInflater layoutInflater;
 
    	public CustomListAdapter(Context context, ArrayList listData) {
    		this.listData = listData;
    		layoutInflater = LayoutInflater.from(context);
    	}
 
    	@Override
    	public int getCount() {
    		return listData.size();
    	}
 
    	@Override
    	public Object getItem(int position) {
    		return listData.get(position);
    	}
    	
    	@Override
    	public long getItemId(int position) {
    		return position;
    	}
 
    	public View getView(int position, View convertView, ViewGroup parent) {
    		ViewHolder holder;
    		if (convertView == null) {
    			convertView = layoutInflater.inflate(R.layout.list_item, null);
    			holder = new ViewHolder();
    			holder.walletLabelView = (TextView) convertView.findViewById(R.id.wallet_label);
    			holder.walletFingerprintView = (TextView) convertView.findViewById(R.id.wallet_fingerprint);
    			holder.walletIcon = (ImageView) convertView.findViewById(R.id.wallet_icon);
    			convertView.setTag(holder);
    		} else {
    			holder = (ViewHolder) convertView.getTag();
    		}
    		holder.walletLabelView.setText(((WalletItem) listData.get(position)).getWalletLabel());
    		holder.walletFingerprintView.setText(((WalletItem) listData.get(position)).getFingerprint());
    		holder.walletIcon.setImageResource(((WalletItem) listData.get(position)).getIcon());
 
    		return convertView;
    	}
 
    	class ViewHolder {
    		TextView walletLabelView;
    		TextView walletFingerprintView;
    		ImageView walletIcon;
    	}
    }
    
    /**
     * This is a class that runs in the background and connects to the wallet and waits to receive a transaction.
     * When one is received, it loads the dialog box.
     */
    public class GCMConnect extends AsyncTask<String,String,Connection> {
    	TxData tx;
        @Override
        protected Connection doInBackground(String... message) {
    		Log.v("ASDF", "hasPendingReq " + hasPendingReq.toString());
    		//Load the settings from shared preferences
            SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
            int numwallets = settings.getInt("numwallets", 0);
            //Wait for pending requests via GCM\
            while (!hasPendingReq){
           		SharedPreferences settings2 = getSharedPreferences("ConfigFile", 0);
           		hasPendingReq = settings2.getBoolean("request", false);
           	}
           	//Handle pending requests from GCM
           	req = GcmIntentService.getMessage();
           	try {
           		JSONObject reqPayload = new JSONObject();
           		reqPayload = req.getJSONObject("ReqPayload");
           		IPAddress =  reqPayload.getString("ExternalIP");
           		LocalIP = reqPayload.getString("LocalIP");
           		String pairID = req.getString("PairingID");
           		for (int y=1; y<=numwallets; y++){
            		SharedPreferences data = getSharedPreferences("WalletData" + y, 0);
            		String fingerprint = data.getString("Fingerprint", "null");
            		if (fingerprint.equals(pairID)){
            			walletnum = y;
            		}
            	}
            	SharedPreferences prefs = getSharedPreferences("WalletData" + walletnum, 0);
           		SharedPreferences.Editor editor = prefs.edit();
           		editor.putString("ExternalIP", IPAddress);
           		editor.putString("LocalIP", LocalIP);
           		editor.commit();
           		Log.v("ASDF", "Changed wallet ip address from GCM to: " + IPAddress + "\n" +
           				"Changed wallet local ip address from GCM to: " + LocalIP);
           	} catch (JSONException e) {e.printStackTrace();} 
           	hasPendingReq=false;
           	SharedPreferences.Editor editor = settings.edit();	
           	editor.putBoolean("request", false);
           	editor.commit();
           	//Open a new connection
           	try {conn = new Connection(IPAddress);} 
           	catch (IOException e1) {
            	try {conn = new Connection(LocalIP);} 
            	catch (IOException e2) {
            		runOnUiThread(new Runnable() {
            			public void run() {
            				Toast.makeText(getApplicationContext(), "Unable to connect to wallet", Toast.LENGTH_LONG).show();
            			}
            		});
            	}
            }
           
          //Load AES Key from file
        	byte [] key = null;
    		String FILENAME = "AESKey" + walletnum;
    		File file = new File(getFilesDir(), FILENAME);
    		int size = (int)file.length();
    		if (size != 0)
    		{
    			FileInputStream inputStream = null;
    			try {inputStream = openFileInput(FILENAME);} 
    			catch (FileNotFoundException e1) {e1.printStackTrace();}
    			key = new byte[size];
    			try {inputStream.read(key, 0, size);} 
    			catch (IOException e) {e.printStackTrace();}
    			try {inputStream.close();} 
    			catch (IOException e) {e.printStackTrace();}
    		}
    		final byte[] AESKey = key;
    		SecretKey sharedsecret = new SecretKeySpec(AESKey, "AES");
        	//Create a new message object for receiving the transaction.
        	Message msg = null;
			try {msg = new Message(conn);} 
			catch (IOException e) {e.printStackTrace();}
			try {tx = msg.receiveTX(sharedsecret);} 
			catch (Exception e) {e.printStackTrace();}
        	
			return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
        
        /**On finish show the transaction in a dialog box*/
        protected void onPostExecute(Connection result) {
           super.onPostExecute(result);
           if (tx != null) {
        	   try {showDialogBox(tx);} 
        	   catch (InterruptedException e) {e.printStackTrace();}
           }
        }
    }
    
    public class ManualConnect extends AsyncTask<Integer,String,Connection> {
    	TxData tx;
        protected Connection doInBackground(Integer... n) {
        	walletnum = n[0];
        	SharedPreferences prefs = getSharedPreferences("WalletData" + walletnum, 0);	
            IPAddress = prefs.getString("ExternalIP", "null");
            LocalIP = prefs.getString("LocalIP","null");
        	Boolean connected = false;
        	while(!connected){
        		connected = true;
        		try {conn = new Connection(LocalIP);} 
            	catch (IOException e1) {
            		try {conn = new Connection(IPAddress);}
            		catch (IOException e2) {
            			connected = false;
            		}
            	}	
        	}
        	//Load AES Key from file
        	byte [] key = null;
    		String FILENAME = "AESKey" + walletnum;
    		File file = new File(getFilesDir(), FILENAME);
    		int size = (int)file.length();
    		if (size != 0)
    		{
    			FileInputStream inputStream = null;
    			try {inputStream = openFileInput(FILENAME);} 
    			catch (FileNotFoundException e1) {e1.printStackTrace();}
    			key = new byte[size];
    			try {inputStream.read(key, 0, size);} 
    			catch (IOException e) {e.printStackTrace();}
    			try {inputStream.close();} 
    			catch (IOException e) {e.printStackTrace();}
    		}
    		final byte[] AESKey = key;
    		SecretKey sharedsecret = new SecretKeySpec(AESKey, "AES");
        	//Create a new message object for receiving the transaction.
        	Message msg = null;
			try {msg = new Message(conn);} 
			catch (IOException e) {e.printStackTrace();}
			try {tx = msg.receiveTX(sharedsecret);} 
			catch (Exception e) {e.printStackTrace();}
			return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
        
        /**On finish show the transaction in a dialog box*/
        protected void onPostExecute(Connection result) {
        	super.onPostExecute(result);
            if (tx != null) {
         	   try {showDialogBox(tx);} 
         	   catch (InterruptedException e) {e.printStackTrace();}
            }
        }
    }

}
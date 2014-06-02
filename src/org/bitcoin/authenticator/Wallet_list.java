package org.bitcoin.authenticator;

import java.io.IOException;
import java.util.ArrayList;
<<<<<<< HEAD
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
=======
>>>>>>> multi_gcm

import javax.crypto.SecretKey;

import org.bitcoin.authenticator.Events.GlobalEvents;
import org.bitcoin.authenticator.GcmUtil.GcmIntentService;
import org.bitcoin.authenticator.GcmUtil.ProcessGCMRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
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
	ListView lv1;
	public static JSONObject req;
<<<<<<< HEAD
	public static Boolean hasPendingReq;
	public static Boolean GCM;
	public static Connection conn;
	public static int walletnum;
	public static int numpairs;
	ArrayList<String> InternalAddrs;
	ArrayList<String> ExternalAddrs;
	public static ArrayList<Integer> IndexArr = new ArrayList<Integer>();
	public static String address;
 
=======
	public static Connection conn; 
	private CustomListAdapter listAdapter;
	public GlobalEvents singletonEvents;
>>>>>>> multi_gcm

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_list);
        //Create the list view
<<<<<<< HEAD
        setListView();
        //Load pending request Boolean and GCM boolean
        SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
        hasPendingReq = settings.getBoolean("request", false);
        GCM = settings.getBoolean("GCM", true);
        numpairs = settings.getInt("numwallets", 0);
        //If GCM is turned on start the AsyncTask which waits for a new GCM message
        if (GCM){new GCMConnect().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);}
        //If GCM is off load the data needed to connect to the wallets and start the Manual Connect AsyncTask
        else {
        	InternalAddrs = new ArrayList<String>();
        	ExternalAddrs = new ArrayList<String>();
        	//Attempt to connect to each IP address (both internal and external) only once
        	for (int b=1; b<=numpairs; b++){
        		SharedPreferences prefs = getSharedPreferences("WalletData" + b, 0);	
        		String Internal = prefs.getString("LocalIP", "null");
        		String External = prefs.getString("ExternalIP", "null");
        		if (!ExternalAddrs.contains(Internal)) {new ManualConnect().execute(Internal);}
        		if (!ExternalAddrs.contains(External)) {new ManualConnect().execute(External);}
        		ExternalAddrs.add(Internal);
        		InternalAddrs.add(External);
        	}
        }
=======
        try { setListView(); } catch (InterruptedException e) { e.printStackTrace(); } catch (JSONException e) { e.printStackTrace(); }
        //Start the AsyncTask which waits for new transactions
  		new ConnectToWallets().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); 
  		
  		/**
  		 * Events
  		 */
  		this.singletonEvents = GlobalEvents.SharedGlobal();
  		singletonEvents.onSetPendingGCMRequestToSeen.AddListener(this, "onSetPendingGCMRequestToSeen");
>>>>>>> multi_gcm
    }

	/**Creates the listview component and defines behavior to a long press on a list item.
	 * @throws JSONException 
	 * @throws InterruptedException */
	void setListView() throws InterruptedException, JSONException{
        ArrayList walletList = getListData();
        lv1 = (ListView) findViewById(R.id.custom_list);
        lv1.setLongClickable(true);
        listAdapter = new CustomListAdapter(this, walletList);
        lv1.setAdapter(listAdapter);
        registerForContextMenu(lv1);
        new ProcessGCMInBackground().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	public void updatePendingGCMRequest(){
		try {
			listAdapter.updateData(getGCMPendingRequests(listAdapter.listData));
		} catch (InterruptedException e) { e.printStackTrace(); } catch (JSONException e) { e.printStackTrace(); }
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
		menu.add(0, v.getId(), 0, "Show Pending Requests");
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
        if(item.getTitle() == "Show Pending Requests"){
        	Intent i = new Intent(Wallet_list.this, ActivityPendingRequests.class);
        	WalletItem wi = (WalletItem)lv1.getItemAtPosition(index);
        	i.putExtra("fingerprint", wi.getFingerprint());
        	startActivity (i);
        }
        else if(item.getTitle()=="Re-pair"){
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
    	        try { setListView(); } catch (InterruptedException e) { e.printStackTrace(); } catch (JSONException e) { e.printStackTrace(); }
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
        	        try { setListView(); } catch (InterruptedException e) { e.printStackTrace(); } catch (JSONException e) { e.printStackTrace(); }
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

<<<<<<< HEAD
	/**
	 * Creates a dialog box to show to the user when the Authenticator receives a transaction from the wallet.
	 * Loads the seed from internal storage to derive the private key needed for signing.
	 * Asks user for authorization. If yes, it signs the transaction and sends it back to the wallet.
	 */
	public void showDialogBox(final TxData tx) throws InterruptedException{
		IndexArr = new ArrayList<Integer>();
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
				        else {new ManualConnect().execute(address);}
					}
				  })
				.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						//TODO if the user doesn't approve the transaction, send a message decline message back to the wallet.
						//Reload the ConnectionToWallets task to set up to receive another transaction.
						try {conn.close();} 
						catch (IOException e) {e.printStackTrace();}
						if (GCM){new GCMConnect().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);}
				        else {new ManualConnect().execute(address);}
						dialog.cancel();
					}
				});
				// create and show the alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();}
=======
	@SuppressWarnings("unchecked")
	public void removePendingRequestFromListAndThenUpdate(String requestID) throws JSONException{
	   SharedPreferences settings2 = getSharedPreferences("ConfigFile", 0);
	   SharedPreferences.Editor editor = settings2.edit();
	   JSONArray pja;
	   JSONArray newPja = new JSONArray();
	   if(settings2.getString("pendingList", null) != null){
		   pja = new JSONArray(settings2.getString("pendingList", null));
		   for(int i=0;i<pja.length();i++){
			   if(!getIntent().getStringExtra("RequestID").equals(pja.get(i)))
				   newPja.put(pja.get(i));
		   }
		}
		editor.putString("pendingList", newPja.toString());
		editor.commit();
	
		// update adapter
		try {
			listAdapter.updateData(getGCMPendingRequests(listAdapter.listData));
		} catch (InterruptedException e) { e.printStackTrace(); } catch (JSONException e) { e.printStackTrace(); }
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList getGCMPendingRequests(ArrayList wallets) throws InterruptedException, JSONException{
		// poll pending requests
    	ArrayList<String> pending = new ArrayList<String>();
    	//load from preference
		SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
		JSONArray arr;
		if(settings.getString("pendingList", null) != null){
			arr = new JSONArray(settings.getString("pendingList", null));
			for (int i = 0; i < arr.length(); i++)
				pending.add(arr.getString(i));
		}
		
		for(WalletItem walletData:(ArrayList<WalletItem>)wallets){
			walletData.pendingGCMRequests = new ArrayList<JSONObject>();
			// Load pending request
			for(String req:pending){
				JSONObject o = new JSONObject(settings.getString(req, null));
				String fingerPrintFromPairingID = o.getString("PairingID").substring(32,40).toUpperCase();
				if(fingerPrintFromPairingID.equals(walletData.getFingerprint()))
				if(o.getBoolean("seen") == false)
					walletData.addPendingGCMRequest(o);
			}
		}
		return wallets;
>>>>>>> multi_gcm
	}

	/**This method loads the metadata from Shared Preferences needed to display in the listview
	 * @throws InterruptedException 
	 * @throws JSONException */
    private ArrayList getListData() throws InterruptedException, JSONException {
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
        return getGCMPendingRequests(results);
    }
    
    /**Creates an object that holds the metadata for each wallet to include in the listview*/
    public class WalletItem {
    	 
        private String walletLabel;
        private String fingerprint;
        private ArrayList<JSONObject> pendingGCMRequests;
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
        
        // pending gcm requests
        public ArrayList<JSONObject> getPendingGCMRequests() {
        	if(this.pendingGCMRequests == null)
        		this.pendingGCMRequests = new ArrayList<JSONObject>();
            return this.pendingGCMRequests;
        }
     
        public void addPendingGCMRequest(JSONObject req) {
        	getPendingGCMRequests().add(req);
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
    	
    	public void updateData(ArrayList data){
    		this.listData = data;
    		this.notifyDataSetChanged();
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
    			holder.walletPendingRequestCntView = (TextView) convertView.findViewById(R.id.wallet_new_requests);
    			holder.walletIcon = (ImageView) convertView.findViewById(R.id.wallet_icon);
    			convertView.setTag(holder);
    		} else {
    			holder = (ViewHolder) convertView.getTag();
    		}
    		holder.walletLabelView.setText(((WalletItem) listData.get(position)).getWalletLabel());
    		holder.walletFingerprintView.setText(((WalletItem) listData.get(position)).getFingerprint());
    		holder.walletPendingRequestCntView.setText(Integer.toString(((WalletItem) listData.get(position)).getPendingGCMRequests().size()));
    		holder.walletIcon.setImageResource(((WalletItem) listData.get(position)).getIcon());
 
    		return convertView;
    	}
 
    	class ViewHolder {
    		TextView walletLabelView;
    		TextView walletFingerprintView;
    		TextView walletPendingRequestCntView;
    		ImageView walletIcon;
    	}
    }
    
    /**
     * This class runs in the background. It waits to receive a new GCM message, parses it to figure out which 
     * wallet it came from, then opens a TCP connection to that wallet and receives the tx. Assuming it decrypts 
     * properly, it loads the dialog box asking the user for approval. 
     */
    public class GCMConnect extends AsyncTask<String,String,Connection> {
    	TxData tx;
    	ProcessGCMRequest.ProcessReturnObject ret;
        @Override
        protected Connection doInBackground(String... message) {
<<<<<<< HEAD
    		Log.v("ASDF", "hasPendingReq " + hasPendingReq.toString());
    		//Load the settings from shared preferences
=======
        	System.out.println("a1");
    		//Load the GCM settings from shared preferences
>>>>>>> multi_gcm
            SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
            int numwallets = settings.getInt("numwallets", 0);
<<<<<<< HEAD
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
=======
            if(GCM){
            	System.out.println("a2");
            	// Handle a request that was pressed by the user
            	String reqString = null;
            	if(getIntent().getStringExtra("RequestID") != null){
            		SharedPreferences settings2 = getSharedPreferences("ConfigFile", 0);
            		reqString = settings2.getString(getIntent().getStringExtra("RequestID"), null);
            		ProcessGCMRequest processor = new ProcessGCMRequest(getApplicationContext());
            		ret = processor.ProcessRequest(reqString);
            		// Connect
                	SharedPreferences.Editor editor = settings.edit();	
                	editor.putBoolean("request", false);
                	editor.commit();
                	//Open a new connection
                	System.out.println("a4");
                	try {conn = new Connection(ret.IPAddress);} 
                	catch (IOException e1) {
                		try {conn = new Connection(ret.LocalIP);} 
                		catch (IOException e2) {
                			runOnUiThread(new Runnable() {
                				public void run() {
                					Toast.makeText(getApplicationContext(), "Unable to connect to wallet", Toast.LENGTH_LONG).show();
                				}
                			});
                		}
                	}
                	
                	SecretKey sharedsecret = Utils.getAESSecret(getApplicationContext(), ret.walletnum); 
                	//Create a new message object for receiving the transaction.
            		System.out.println("a5");
                	Message msg = null;
        			try {
        				msg = new Message(conn);
        				//send request id
        				msg.sentRequestID(getIntent().getStringExtra("RequestID"));
        			} 
        			catch (IOException e) {e.printStackTrace();}
        			try {tx = msg.receiveTX(sharedsecret);} 
        			catch (Exception e) {e.printStackTrace();}
            	}            		
            }
            else { // TODO !
            	//Load the IPs for each wallet from shared preferences.
                SharedPreferences prefs = getSharedPreferences("WalletData1", 0);	
                String IPAddress = prefs.getString("ExternalIP", "null");
                String LocalIP = prefs.getString("LocalIP","null");
                
            	Boolean connected = false;
            	while(!connected){
                	System.out.println("#1");
            		connected = true;
            		try {conn = new Connection(IPAddress);} 
                	catch (IOException e1) {
                		System.out.println("#2");
                		try {conn = new Connection(LocalIP);}
                		catch (IOException e2) {
                			connected = false;
                        	System.out.println("#3");
                			runOnUiThread(new Runnable() {
                				public void run() {
                					Toast.makeText(getApplicationContext(), "Unable to connect to wallet", Toast.LENGTH_LONG).show();
                				}
                			});
                		}
                	}	
>>>>>>> multi_gcm
            	}
            }
<<<<<<< HEAD
           
          //Load AES Key from file
        	byte [] key = null;
        	ArrayList<SecretKey> keys = new ArrayList<SecretKey>();
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
    		keys.add(sharedsecret);
        	//Create a new message object for receiving the transaction.
        	Message msg = null;
			try {msg = new Message(conn);} 
			catch (IOException e) {e.printStackTrace();}
			try {tx = msg.receiveTX(keys);} 
			catch (Exception e) {e.printStackTrace();}
=======
>>>>>>> multi_gcm
        	
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
<<<<<<< HEAD
        	   try {showDialogBox(tx);} 
        	   catch (InterruptedException e) {e.printStackTrace();}
=======
        	   try 
        	   {
        		   new ShowDialog(conn, tx, Wallet_list.this, ret.walletnum);
        		   System.out.println("a6");
        		   SharedPreferences settings2 = getSharedPreferences("ConfigFile", 0);
        		   // set request as seen
           		   String req = settings2.getString(getIntent().getStringExtra("RequestID"), null);
           		   SharedPreferences.Editor editor = settings2.edit();
           		   JSONObject jo = new JSONObject(req);
           		   jo.put("seen", true);
	           	   editor.putString(jo.getString("RequestID"), jo.toString());
	    		   editor.commit();
	    		   // remove from pending requests
	    		   removePendingRequestFromListAndThenUpdate(getIntent().getStringExtra("RequestID"));
        	   } 
        	   catch (InterruptedException e) {e.printStackTrace();} catch (JSONException e) { e.printStackTrace(); }
>>>>>>> multi_gcm
           }
        }
    }
    
    /**
<<<<<<< HEAD
     * This class is similar to GCMConnect but it works with GCM off. It runs a loop trying to connect to the IP
     * address of the wallet. When the wallet opens a port, it connects, receives the tx, and loads the dialog box
     * for the user. 
     */
    public class ManualConnect extends AsyncTask<String,String,Connection> {
    	TxData tx;
        protected Connection doInBackground(String... IP) {
        	Boolean connected = false;
        	ArrayList<Integer> arr = null;
        	String ip = IP[0];
        	//Attempt to connect to the wallet
        	while(!connected){
        		connected = true;
        		try {
        			arr = new ArrayList<Integer>();
        			conn = new Connection(ip);
        			//If we connect, add the index of each instance of the IP address in the address array to an index array
        			//We will need these indices for loading the AES keys
        			for (int j=0; j<InternalAddrs.size(); j++){
        				if (InternalAddrs.get(j).equals(ip)){arr.add(j+1);}
        				if (ExternalAddrs.get(j).equals(ip)){arr.add(j+1);}
        			}
        		} catch (IOException e1) {connected = false;}
        	}
        	address = ip;
        	for (int z=0; z<arr.size(); z++){IndexArr.add(arr.get(z));}
        	System.out.println("x1");
        	ArrayList<SecretKey> keys = new ArrayList<SecretKey>();
        	//Load AES key for each index in the index array
        	for (int j=0; j<IndexArr.size(); j++){
        		//Load AES Key from file
        		byte [] key = null;
        		String FILENAME = "AESKey" + IndexArr.get(j);
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
        		keys.add(sharedsecret);
        	}
        	//Create a new message object for receiving the transaction.
        	System.out.println("x2");
        	Message msg = null;
			try {msg = new Message(conn);} 
			catch (IOException e) {e.printStackTrace();}
			try {tx = msg.receiveTX(keys);} 
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
         	   try {showDialogBox(tx);
         	  System.out.println("x6");} 
         	   catch (InterruptedException e) {e.printStackTrace();}
            }
        }
    }
=======
     * This is a class that runs in the background and connects to the wallet and waits to receive a transaction.
     * When one is received, it loads the dialog box.
     */
    public class ProcessGCMInBackground extends AsyncTask<String,String,Connection> {
>>>>>>> multi_gcm

		@Override
		protected Connection doInBackground(String... params) {
			while(true)
			{
				// blocking
				try {
					String reqID = GcmIntentService.takeRequest();
					if(reqID != null)
						runOnUiThread(new Runnable() {
	        				public void run() {
	        					updatePendingGCMRequest();
	        				}
						});
						
				} catch (InterruptedException e) { e.printStackTrace(); }
			}
		}
    	
    }
    
    /**
     * Events
     */
    public void onSetPendingGCMRequestToSeen(Object Sender, Object Arguments)
	{
    	updatePendingGCMRequest();
	}
}
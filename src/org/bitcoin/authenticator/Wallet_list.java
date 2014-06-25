package org.bitcoin.authenticator;

import java.io.IOException;
import java.util.ArrayList;

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
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
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
	private PopupMenu popupMenu;
	public static JSONObject req;
	public static Connection conn; 
	private CustomListAdapter listAdapter;
	public GlobalEvents singletonEvents;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_list);
        //Create the list view
        try { setListView(); } catch (InterruptedException e) { e.printStackTrace(); } catch (JSONException e) { e.printStackTrace(); }
        //Start the AsyncTask which waits for new transactions
  		new ConnectToWallets().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); 
  		
  		/**
  		 * Events
  		 */
  		this.singletonEvents = GlobalEvents.SharedGlobal();
  		singletonEvents.onSetPendingGCMRequestToSeen.AddListener(this, "onSetPendingGCMRequestToSeen");
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
        //registerForContextMenu(lv1);
        lv1.setOnItemClickListener(new OnItemClickListener()
        {
           @Override
           public void onItemClick(AdapterView<?> adapter, View v, int position,
                 long arg3) 
           {
        	   final int index = position;
        	   ArrayList<BAPopupMenu.PopupButton> buttons = new ArrayList<BAPopupMenu.PopupButton>();
        	   int cnt = ((WalletItem)listAdapter.getItem(index)).getPendingGCMRequests().size();
        	   boolean showPending = cnt>0? true:false;
        	   buttons.add(new BAPopupMenu.PopupButton("Show Pending Requests",showPending));
        	   buttons.add(new BAPopupMenu.PopupButton("Re-pair",true));
        	   buttons.add(new BAPopupMenu.PopupButton("Rename",true));
        	   buttons.add(new BAPopupMenu.PopupButton("Delete",true));
        	   
                new BAPopupMenu(getApplicationContext(),v)
                .setButtons(buttons)
                .setActionsListener(new BAPopupMenu.ActionsListener(){
					@Override
					public void pressed(MenuItem item) {
						onPopupMenuItemSelected(item.getTitle().toString(),index);
					}
                }).show();
           }
        });
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
    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
	super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Select Action");
		menu.add(0, v.getId(), 0, "Show Pending Requests");
		menu.add(0, v.getId(), 0, "Re-pair");
		menu.add(0, v.getId(), 0, "Rename");
		menu.add(0, v.getId(), 0, "Delete");
	}*/
    
    /**Handles the clicks in the context menu*/
    /*@Override
	public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final int index = info.position;*/
	public void onPopupMenuItemSelected(String title, final int index){
        //Re-pairs with the wallet
        if(title == "Show Pending Requests"){
        	Intent i = new Intent(Wallet_list.this, ActivityPendingRequests.class);
        	WalletItem wi = (WalletItem)lv1.getItemAtPosition(index);
        	i.putExtra("fingerprint", wi.getFingerprint());
        	i.putExtra("walletName", wi.getWalletLabel());
        	startActivity (i);
        }
        else if(title=="Re-pair"){
       		Object o = lv1.getItemAtPosition(index);
			WalletItem Data = (WalletItem) o;
			Re_pair_wallet.walletNum = Data.getWalletNum();
			startActivity (new Intent(Wallet_list.this, Re_pair_wallet.class));
       	}
       	//Displays a dialog allowing the user to rename the wallet in the listview
    	else if(title=="Rename"){
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
    	else if(title=="Delete"){
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
    	
	}

	/**
	 * Disable the back button because the device is now paired and any activity the user needs can be 
	 * accessed from the menu. 
	 */
	@Override
	public void onBackPressed() {
	}

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
    		if (data.getString(wTP, "null").equals("blockchain")){walletData.setIcon(R.drawable.ic_bitcoin_logo);}
    		else if (data.getString(wTP, "null").equals("electrum")){walletData.setIcon(R.drawable.ic_electrum_logo);}
    		else if (data.getString(wTP, "null").equals("hive")){walletData.setIcon(R.drawable.hive_logo);}
    		else if (data.getString(wTP, "null").equals("multibit")){walletData.setIcon(R.drawable.multibit_logo);}
    		else if (data.getString(wTP, "null").equals("bitcoincore")){walletData.setIcon(R.drawable.ic_bitcoin_logo);}
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
    		int cnt = ((WalletItem) listData.get(position)).getPendingGCMRequests().size();
    		if( cnt > 0)
    			holder.walletPendingRequestCntView.setText(Integer.toString(cnt) + " Pending Requests");
    		else
    			holder.walletPendingRequestCntView.setText("");
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
     * This is a class that runs in the background and connects to the wallet and waits to receive a transaction.
     * When one is received, it loads the dialog box.
     */
    public class ConnectToWallets extends AsyncTask<String,String,Connection> {
    	TxData tx;
    	ProcessGCMRequest.ProcessReturnObject ret;
        @Override
        protected Connection doInBackground(String... message) {
        	System.out.println("a1");
    		//Load the GCM settings from shared preferences
            SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
            Boolean GCM = settings.getBoolean("GCM", true);
            int numwallets = settings.getInt("numwallets", 0);
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
            	}
            	System.out.println("#4");
            }
        	
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
           }
        }
    }
    
    /**
     * This is a class that runs in the background and connects to the wallet and waits to receive a transaction.
     * When one is received, it loads the dialog box.
     */
    public class ProcessGCMInBackground extends AsyncTask<String,String,Connection> {

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
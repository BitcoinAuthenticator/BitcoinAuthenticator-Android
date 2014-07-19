package org.bitcoin.authenticator;

import java.io.IOException;
import java.util.ArrayList;

import javax.crypto.SecretKey;

import org.bitcoin.authenticator.ConfirmTxDialog.TxDialogResponse;
import org.bitcoin.authenticator.Wallet_list.ConnectToWallets;
import org.bitcoin.authenticator.Wallet_list.WalletItem;
import org.bitcoin.authenticator.Wallet_list.CustomListAdapter.ViewHolder;
import org.bitcoin.authenticator.dialogs.BAPopupMenu;
import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferences;
import org.bitcoin.authenticator.Events.GlobalEvents;
import org.bitcoin.authenticator.GcmUtil.ProcessGCMRequest;
import org.bitcoin.authenticator.GcmUtil.RequestType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Build;

public class ActivityPendingRequests extends Activity {

	ListView lv1;
	TextView walletName;
	public Adapter adapter;
	public GlobalEvents singletonEvents;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pending_requests);			
		
		walletName = (TextView) findViewById(R.id.lblPendingWalletName);
		walletName.setText(getIntent().getStringExtra("walletName"));
		
		try {
			String fingerprint = getIntent().getStringExtra("fingerprint");
			ArrayList<JSONObject> pendingReq = getGCMPendingRequests(fingerprint);
			ArrayList<dataClass> data;
			lv1 = (ListView) findViewById(R.id.lstPendingReq);
			data = getData(pendingReq);
			adapter = new Adapter(getApplicationContext(),data);
			lv1.setAdapter(adapter);
			//registerForContextMenu(lv1);
			lv1.setOnItemClickListener(new OnItemClickListener()
	        {
	           @Override
	           public void onItemClick(AdapterView<?> adapter, View v, int position,
	                 long arg3) 
	           {
	        	   final int index = position;
	        	   ArrayList<BAPopupMenu.PopupButton> buttons = new ArrayList<BAPopupMenu.PopupButton>();
	        	   buttons.add(new BAPopupMenu.PopupButton("Open",true));
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
		} catch (JSONException e) { e.printStackTrace(); } catch (InterruptedException e) { e.printStackTrace(); }
		
		/**
		 * Events
		 */
		this.singletonEvents = GlobalEvents.SharedGlobal();
	}

	private ArrayList<dataClass> getData(ArrayList<JSONObject> jsonobj) throws JSONException{
		ArrayList<dataClass> ret = new ArrayList<dataClass>();
		int index = 1;
		for(JSONObject o:jsonobj){
			dataClass n = new dataClass(o,index);
			ret.add(n);
			index++;
		}
		return ret;
	}
	
	private ArrayList<JSONObject> getGCMPendingRequests(String fingerprint) throws InterruptedException, JSONException{
		// poll pending requests
		ArrayList<JSONObject> pending = new ArrayList<JSONObject>();
		ArrayList<String> allPending = new ArrayList<String>();
    	//load from preference
		SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
		JSONArray arr;
		if(settings.getString("pendingList", null) != null){
			arr = new JSONArray(settings.getString("pendingList", null));
			for (int i = 0; i < arr.length(); i++)
				allPending.add(arr.getString(i));
		}
		
		// Load pending request
		for(String req:allPending){
			JSONObject o = new JSONObject(settings.getString(req, null));
			String fingerPrintFromPairingID = o.getString("PairingID").substring(32,40).toUpperCase();
			if(fingerPrintFromPairingID.equals(fingerprint))
			if(o.getBoolean("seen") == false)
				pending.add(o);
		}
		
		return pending;
	}
	
	private void markPendingRequestAsSeen(String reqID) throws JSONException{
	   //SharedPreferences settings2 = getSharedPreferences("ConfigFile", 0);
	   //String req = settings2.getString(reqID, null);
	   //SharedPreferences.Editor editor = settings2.edit();
	   JSONObject jo = BAPreferences.ConfigPreference().getPendingRequestAsJsonObject(reqID);//new JSONObject(req);
	   jo.put("seen", true);
	   BAPreferences.ConfigPreference().setPendingRequest(reqID, jo);
	   //editor.putString(jo.getString("RequestID"), jo.toString());
	   //editor.commit();
	   
	   //
	   this.singletonEvents.onSetPendingGCMRequestToSeen.Raise(this, null);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_pending_requests, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**Creates the context menu that pops up on a long click in the list view*/
    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
	super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Select Action");
		menu.add(0, v.getId(), 0, "Open");
		menu.add(0, v.getId(), 0, "Delete");
	}*/
    
    /**Handles the clicks in the context menu*/
    //@Override
	//public boolean onContextItemSelected(MenuItem item) {
    //	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    //    final int index = info.position;
        //Re-pairs with the wallet
    public void onPopupMenuItemSelected(String title, final int index){
        if(title == "Open"){
        	new ConnectToWallet(index).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); 
        }
        else if(title == "Delete"){
        	dataClass data = (dataClass)lv1.getItemAtPosition(index);
        	try {
				markPendingRequestAsSeen(data.getReqID());
				adapter.removePendigRequestAt(index);
			} catch (JSONException e) { e.printStackTrace(); }
        	
        }
    }
    
    public class ConnectToWallet extends AsyncTask<String,String,Connection> {
    	public ConnectToWallet(int index){
    		this.index = index;
    	}
    	int index;
    	TxData tx;
    	ProcessGCMRequest.ProcessReturnObject ret;
    	Connection conn = null;
    	dataClass data;
		@Override
		protected Connection doInBackground(String... params) {
			//SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
            Boolean GCM = BAPreferences.ConfigPreference().getGCM(true);//settings.getBoolean("GCM", true);
            data = (dataClass)lv1.getItemAtPosition(index);
            if(GCM){
            	//SharedPreferences settings2 = getSharedPreferences("ConfigFile", 0);
        		String reqString = BAPreferences.ConfigPreference().getPendingRequestAsString(data.getReqID());//settings2.getString(data.getReqID(), null);
        		ProcessGCMRequest processor = new ProcessGCMRequest(getApplicationContext());
        		ret = processor.ProcessRequest(reqString);
        		//Open a new connection
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
            	
            	
            	//Receive Tx
            	SecretKey sharedsecret = Utils.getAESSecret(getApplicationContext(), ret.walletnum); 
            	//Create a new message object for receiving the transaction.
            	Message msg = null;
    			try {
    				msg = new Message(conn);
    				//send request id
    				msg.sentRequestID(data.reqID);
    			} 
    			catch (IOException e) {e.printStackTrace();}
    			try {tx = msg.receiveTX(sharedsecret);} 
    			catch (Exception e) {e.printStackTrace();}
            }
			return null;
		}
    	
		/**On finish show the transaction in a dialog box*/
        protected void onPostExecute(Connection result) {
        	// Show Tx dialog
			try {
				new ConfirmTxDialog(conn, 
						tx, 
						ActivityPendingRequests.this, 
						ret.walletnum, 
						new TxDialogResponse(){
								@Override
								public void onAuthorizedTx() {
									 try {
									   markPendingRequestAsSeen(data.getReqID());
									   adapter.removePendigRequestAt(index);
									 } catch (JSONException e) { e.printStackTrace(); }
								}
			
								@Override
								public void onNotAuthorizedTx() {
									try {
									   markPendingRequestAsSeen(data.getReqID());
									   adapter.removePendigRequestAt(index);
									} catch (JSONException e) { e.printStackTrace(); }
								}
			
								@Override
								public void onCancel() {
									// do nothing
								}
							});
			} catch (InterruptedException e) { e.printStackTrace(); }
 		   // Update pending tx    			
		  
		}
    }
	
	public class dataClass
	{
		public String tmp;
		public String pairingID;
		public String reqID;
		public RequestType ReqType;
		public String customMsg;
		public int index;
		
		public dataClass(JSONObject jObj, int index) throws JSONException{
			this.tmp = jObj.getString("tmp");
			this.pairingID = jObj.getString("PairingID");
			this.reqID = jObj.getString("RequestID");
			// type
			if(Integer.parseInt( jObj.getString("RequestType") ) == RequestType.test.getValue()){
				
			}
			else if(Integer.parseInt( jObj.getString("RequestType") ) == RequestType.signTx.getValue()){
				this.ReqType = RequestType.signTx;
			}
			//
			this.customMsg =  jObj.getString("CustomMsg");
			this.index = index;
		}
		
		public String getTmp(){ return this.tmp; }
		public String getPairingID(){ return this.pairingID; }
		public String getReqID(){ return this.reqID; }
		public RequestType getRequestType(){ return this.ReqType; }
		public String getCustomMsg(){ return this.customMsg; }
		public int getIndex(){ return this.index; }
		public String getIndexString(){ return Integer.toString(getIndex()); }
	}
	
	public class Adapter extends BaseAdapter {
		private ArrayList<dataClass> listData;
    	private LayoutInflater layoutInflater;
		
    	public Adapter(Context context, ArrayList<dataClass> listData) {
    		this.listData = listData;
    		layoutInflater = LayoutInflater.from(context);
    	}
    	
    	public void removePendigRequestAt(int index)
    	{
    		this.listData.remove(index);
    		this.notifyDataSetChanged();
    	}
    	
		@Override
		public int getCount() {
			return this.listData.size();
		}

		@Override
		public Object getItem(int position) {
			return this.listData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
    		if (convertView == null) {
    			convertView = layoutInflater.inflate(R.layout.pending_list_item, null);
    			holder = new ViewHolder();
    			holder.customMsg = (TextView) convertView.findViewById(R.id.pending_custom_msg_label);
    			holder.tmp = (TextView) convertView.findViewById(R.id.pending_tmp);
    			holder.index = (TextView) convertView.findViewById(R.id.pending_number);
    			convertView.setTag(holder);
    		} else {
    			holder = (ViewHolder) convertView.getTag();
    		}
    		
    		holder.customMsg.setText(((dataClass) listData.get(position)).getCustomMsg());
    		holder.tmp.setText(((dataClass) listData.get(position)).getTmp());
    		holder.index.setText(((dataClass) listData.get(position)).getIndexString() + ")");
 
    		return convertView;
		}
		class ViewHolder {
			TextView customMsg;
			TextView tmp;
			TextView index;
    	}
		
	}
}

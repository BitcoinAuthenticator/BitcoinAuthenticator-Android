package org.bitcoin.authenticator;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import javax.crypto.SecretKey;

import org.bitcoin.authenticator.ConfirmTxDialog.TxDialogResponse;
import org.bitcoin.authenticator.core.TxData;
import org.bitcoin.authenticator.dialogs.BAPopupMenu;
import org.bitcoin.authenticator.core.net.Connection;
import org.bitcoin.authenticator.core.net.Message;
import org.bitcoin.authenticator.BAPreferences.BAPreferences;
import org.bitcoin.authenticator.Events.GlobalEvents;
import org.bitcoin.authenticator.core.GcmUtil.ProcessGCMRequest;
import org.bitcoin.authenticator.core.GcmUtil.RequestType;
import org.bitcoin.authenticator.core.net.exceptions.CouldNotGetTransactionException;
import org.bitcoin.authenticator.core.net.exceptions.CouldNotSendRequestIDException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ActivityPendingRequests extends Activity {

	ListView lv1;
	TextView walletName;
	public Adapter adapter;
	public GlobalEvents singletonEvents;
	
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pending_requests);			
		
		walletName = (TextView) findViewById(R.id.lblPendingWalletName);
		walletName.setText(getIntent().getStringExtra("walletName"));
		
		try {
			String walletID = getIntent().getStringExtra("walletID");
			ArrayList<JSONObject> pendingReq = getGCMPendingRequests(walletID);
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
		
		/**
		 * Waiting indicator
		 */
		mProgressDialog = new ProgressDialog(this, R.style.CustomDialogSpinner);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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
	
	private ArrayList<JSONObject> getGCMPendingRequests(String walletID) throws InterruptedException, JSONException{
		// poll pending requests
		ArrayList<JSONObject> pending = new ArrayList<JSONObject>();
		ArrayList<String> allPending = new ArrayList<String>();
    	//load from preference
		allPending = BAPreferences.ConfigPreference().getPendingList();
		for(String req:allPending){
			JSONObject o = BAPreferences.ConfigPreference().getPendingRequestAsJsonObject(req);
			String pendingReqWalletID = Long.toString(PairingProtocol.getWalletIndexFromString(o.getString("WalletID")));
			if(pendingReqWalletID.equals(walletID))
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

    
    /**Handles the clicks in the context menu*/
    public void onPopupMenuItemSelected(String title, final int index){
        if(title == "Open"){
        	new ConnectToWallet(index, getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); 
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
    	Context context;
    	
    	private Socket persistentSocketForTheProcess;
    	
    	public ConnectToWallet(int index, Context context){
    		this.index = index;
    		this.context = context;
    	}
    	int index;
    	TxData tx;
    	ProcessGCMRequest.ProcessReturnObject ret;
    	Connection conn = null;
    	dataClass data;
		@Override
		protected Connection doInBackground(String... params) {
			//Display a spinner while connecting
			runOnUiThread(new Runnable() {
				public void run() {
		            mProgressDialog.show();
				}
			});
			
            Boolean GCM = BAPreferences.ConfigPreference().getGCM(true);
            data = (dataClass)lv1.getItemAtPosition(index);
            if(GCM){
        		String reqString = BAPreferences.ConfigPreference().getPendingRequestAsString(data.getReqID());
        		ProcessGCMRequest processor = new ProcessGCMRequest(getApplicationContext());
        		ret = processor.ProcessRequest(reqString);
        		String[] ips = new String[] { ret.IPAddress, ret.LocalIP};
            	
            	//Receive Tx
            	SecretKey sharedsecret = Utils.getAESSecret(getApplicationContext(), ret.walletnum); 
            	//Create a new message object for receiving the transaction.
            	Message msg = null;
            	persistentSocketForTheProcess = null;
    			try {
    				msg = new Message(ips);
    				//send request id
    				persistentSocketForTheProcess = msg.sendRequestID(data.reqID, data.getWalletID());
    			} 
    			catch (CouldNotSendRequestIDException e) {
					e.printStackTrace();
					riseError("Failed to get transaction");
					try { persistentSocketForTheProcess.close();}
					catch (Exception ex) { }
				}
    			
    			if(persistentSocketForTheProcess != null)
	    			try {
	    				tx = msg.receiveTX(sharedsecret, persistentSocketForTheProcess);
	    			} 
	    			catch (CouldNotGetTransactionException e) {
	    				e.printStackTrace(); 
	    				riseError(e.getMessage());
	    				try { persistentSocketForTheProcess.close();
						} catch (IOException ex) { }
	    			}
	    			
            }
			return null;
		}
		
		private void riseError(final String msg) {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
				}
			});
		}
    	
		/**On finish show the transaction in a dialog box*/
        protected void onPostExecute(Connection result) {
        	mProgressDialog.hide();
        	
        	// Show Tx dialog
        	if(tx != null)
			try {
				new ConfirmTxDialog(persistentSocketForTheProcess,
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

								@Override
								public void OnError() {
									runOnUiThread(new Runnable() {
			            				public void run() {
			            					Toast.makeText(getApplicationContext(), "Failed !!", Toast.LENGTH_LONG).show();
			            				}
			            			});
								}
							});
			} catch (InterruptedException e) { e.printStackTrace(); }
 		   // Update pending tx    			
		  
		}
    }
	
	public class dataClass
	{
		public String tmp;
		public String WalletID;
		public String reqID;
		public RequestType ReqType;
		public String customMsg;
		public int index;
		
		public dataClass(JSONObject jObj, int index) throws JSONException{
			this.tmp = jObj.getString("tmp");
			this.WalletID = jObj.getString("WalletID");
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
		public String getWalletID(){ return this.WalletID; }
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

package org.bitcoin.authenticator.GcmUtil;

import java.io.IOException;

import org.bitcoin.authenticator.Connection;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class ProcessGCMRequest {
	private  JSONObject req;
	private Context mContext;
	public ProcessGCMRequest(Context context){ mContext = context; }
	
	public ProcessReturnObject ProcessRequest(String msg){
		ProcessReturnObject ret = new ProcessReturnObject();
		//Wait for pending requests via GCM\
    	
    	// Search for a new request
    	/*while (!hasPendingReq){
    		try {
    			SharedPreferences settings2 = getSharedPreferences("ConfigFile", 0);
        		hasPendingReq = settings2.getBoolean("request", false);
        		//In case app was launched by clicking the notification
        		reqString = settings2.getString(getIntent().getStringExtra("pairingID"), null);
        		// case activity is already running
        		if(reqString == null)
        			reqString = GcmIntentService.takeRequest();
        		
				Thread.sleep(50);
			} catch (InterruptedException e) { e.printStackTrace(); }
    	}*/
		
		SharedPreferences settings = mContext.getSharedPreferences("ConfigFile", 0);
		int numwallets = settings.getInt("numwallets", 0);
		
       	if(msg != null)
    	try {
    		req = new JSONObject(msg);//GcmIntentService.getMessage();
    		JSONObject reqPayload = new JSONObject();
    		reqPayload = req.getJSONObject("ReqPayload");
    		ret.IPAddress =  reqPayload.getString("ExternalIP");
    		ret.LocalIP = reqPayload.getString("LocalIP");
    		String pairID = req.getString("PairingID");
    		for (int y=1; y<=numwallets; y++){
    			SharedPreferences data = mContext.getSharedPreferences("WalletData" + y, 0);
    			String fingerprint = data.getString("Fingerprint", "null");
    			if (fingerprint.equals(pairID)){
    				ret.walletnum = y;
    			}
    		}
    		SharedPreferences prefs = mContext.getSharedPreferences("WalletData" + ret.walletnum, 0);
    		SharedPreferences.Editor editor = prefs.edit();
    		editor.putString("ExternalIP", ret.IPAddress);
    		editor.putString("LocalIP", ret.LocalIP);
    		editor.commit();
    		Log.v("ASDF", "Changed wallet ip address from GCM to: " + ret.IPAddress + "\n" +
    				"Changed wallet local ip address from GCM to: " + ret.LocalIP);
    	} catch (JSONException e) {e.printStackTrace();} 
    	
       	return ret;
	}

	public class ProcessReturnObject{
		public  String PublicIP;
		public  String LocalIP;
		public  String IPAddress;
		public int walletnum;
	}
}

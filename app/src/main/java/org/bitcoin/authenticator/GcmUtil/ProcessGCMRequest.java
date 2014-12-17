package org.bitcoin.authenticator.GcmUtil;

import java.util.Set;

import org.bitcoin.authenticator.PairingProtocol;
import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferences;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ProcessGCMRequest {
	private  JSONObject req;
	private Context mContext;
	public ProcessGCMRequest(Context context){ mContext = context; }
	
	public ProcessReturnObject ProcessRequest(String msg){
		ProcessReturnObject ret = new ProcessReturnObject();
		//Wait for pending requests via GCM\
    	
//		SharedPreferences settings = mContext.getSharedPreferences("ConfigFile", 0);
//		int numwallets = settings.getInt("numwallets", 0);
//		
       	if(msg != null)
    	try {
    		req = new JSONObject(msg);//GcmIntentService.getMessage();
    		JSONObject reqPayload = new JSONObject();
    		reqPayload = req.getJSONObject("ReqPayload");
    		ret.IPAddress =  reqPayload.getString("ExternalIP");
    		ret.LocalIP = reqPayload.getString("LocalIP");
    		String walletID = req.getString("WalletID");
    		
    		// search wallet index
    		Set<Long> wallets = BAPreferences.ConfigPreference().getWalletIndexList();
    		ret.walletnum = PairingProtocol.getWalletIndexFromString(walletID);
    		
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
		public long walletnum;
	}
}

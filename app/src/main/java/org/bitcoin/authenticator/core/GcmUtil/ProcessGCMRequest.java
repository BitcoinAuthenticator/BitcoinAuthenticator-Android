package org.bitcoin.authenticator.core.GcmUtil;

import java.util.Set;

import org.bitcoin.authenticator.PairingProtocol;
import org.bitcoin.authenticator.BAPreferences.BAPreferences;
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

       	if(msg != null)
    	try {
    		req = new JSONObject(msg);//GcmIntentService.getMessage();
    		JSONObject reqPayload = new JSONObject();
    		reqPayload = req.getJSONObject("ReqPayload");
    		ret.publicIP =  reqPayload.getString("ExternalIP");
    		ret.localIP = reqPayload.getString("LocalIP");
    		String walletID = req.getString("WalletID");
    		
    		// search wallet index
    		Set<Long> wallets = BAPreferences.ConfigPreference().getWalletIndexList();
    		ret.walletIdx = PairingProtocol.getWalletIndexFromString(walletID);
    		
    		SharedPreferences prefs = mContext.getSharedPreferences("WalletData" + ret.walletIdx, 0);
    		SharedPreferences.Editor editor = prefs.edit();
    		editor.putString("ExternalIP", ret.publicIP);
    		editor.putString("LocalIP", ret.localIP);
    		editor.commit();
    		Log.v("ASDF", "Changed wallet ip address from GCM to: " + ret.publicIP + "\n" +
                    "Changed wallet local ip address from GCM to: " + ret.localIP);
    	} catch (JSONException e) {e.printStackTrace();} 
    	
       	return ret;
	}

	public class ProcessReturnObject{
		public  String localIP;
		public  String publicIP;
		public long walletIdx;
	}
}

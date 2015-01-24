package org.bitcoin.authenticator.core.GcmUtil;

import org.bitcoin.authenticator.core.net.PairingProtocol;
import org.bitcoin.authenticator.BAPreferences.BAPreferences;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class ParseSignTxNotification {

    public static SignTxNotificationPayload ProcessRequest(BAPreferences preferences, String msg){
		SignTxNotificationPayload ret = new SignTxNotificationPayload();

       	if(msg != null)
    	try {
            JSONObject req = new JSONObject(msg);
    		JSONObject reqPayload = req.getJSONObject("ReqPayload");
    		ret.publicIP =  reqPayload.getString("ExternalIP");
    		ret.localIP = reqPayload.getString("LocalIP");
    		String walletID = req.getString("WalletID");
    		
    		// search wallet index
    		ret.walletIdx = PairingProtocol.getWalletIndexFromString(walletID);

            preferences.WalletPreference().setExternalIP(ret.walletIdx.toString(), ret.publicIP);
            preferences.WalletPreference().setLocalIP(ret.walletIdx.toString(), ret.localIP);

    		Log.i("ASDF", "Changed wallet ip address from GCM to: " + ret.publicIP + "\n" +
                    "Changed wallet local ip address from GCM to: " + ret.localIP);
    	} catch (JSONException e) { e.printStackTrace(); ret = null;}
    	
       	return ret;
	}

	public static class SignTxNotificationPayload {
		public  String localIP;
		public  String publicIP;
		public  Long walletIdx;
	}
}

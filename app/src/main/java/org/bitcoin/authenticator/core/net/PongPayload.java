package org.bitcoin.authenticator.core.net;

import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class PongPayload extends JSONObject{
	
	static public boolean isValidPongPayload(byte[] payload) {
		try {
			String strJson = new String(payload);
            Log.i("asdf","Received PONG msg: " + strJson);
            JSONParser parser=new JSONParser();
			Object obj = parser.parse(strJson);
			JSONObject jsonObject = (JSONObject) obj;
			
			if(jsonObject.containsKey("WELCOME_BACK_AUTHENTICATOR")) {
                Log.i("asdf","Received PONG msg valid");
                return true;
            }
		} catch (Exception e) {
			e.printStackTrace();
		}

        Log.i("asdf","Received PONG msg not valid");
		return false;
	}
	
}

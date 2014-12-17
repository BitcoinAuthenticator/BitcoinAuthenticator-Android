package org.bitcoin.authenticator.net;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PongPayload extends JSONObject{
	
	static public boolean isValidPongPayload(byte[] payload) {
		try {
			String strJson = new String(payload);
			JSONParser parser=new JSONParser();	  
			Object obj = parser.parse(strJson);
			JSONObject jsonObject = (JSONObject) obj;
			
			if(jsonObject.containsKey("WELCOME_BACK_AUTHENTICATOR"))
				return true;
			return false;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}
	
}

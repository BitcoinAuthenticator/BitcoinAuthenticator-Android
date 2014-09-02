package org.bitcoin.authenticator.net;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CannotProcessRequestPayload {
	
	static public boolean isCannotBeProcessedPayload(byte[] payload) {
		try {
			String strJson = new String(payload);
			JSONParser parser=new JSONParser();	  
			Object obj = parser.parse(strJson);
			JSONObject jsonObject = (JSONObject) obj;
			
			if(jsonObject.containsKey("CANNOT_PROCESS_REQUEST"))
				return true;
			else
				return false;
		} catch (ParseException e) {
			e.printStackTrace();
			
		}
		return true;
	}
}

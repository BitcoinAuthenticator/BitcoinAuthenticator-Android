package org.bitcoin.authenticator.net;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CannotProcessRequestPayload {
	
	/**
	 * return null in case the payload could be processed, a description string in case not.
	 * 
	 * @param payload
	 * @return
	 */
	static public String isCannotBeProcessedPayload(byte[] payload) {
		String ret = "Cannot process operation";
		try {
			String strJson = new String(payload);
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(strJson);
			JSONObject jsonObject = (JSONObject) obj;
			
			if(jsonObject.containsKey("CANNOT_PROCESS_REQUEST")) {
				ret = jsonObject.containsKey("WHY")? jsonObject.get("WHY").toString():"Partial payload";
			}
			else
				ret = null;
		} catch (ParseException e) { e.printStackTrace(); }
		return ret;
	}
}

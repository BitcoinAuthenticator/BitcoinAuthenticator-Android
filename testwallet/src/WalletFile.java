import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class WalletFile {
	
	String filePath = null;

	public WalletFile(){
		try {
			filePath = new java.io.File( "." ).getCanonicalPath() + "/wallet.json";
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void writeToFile(String privkey, String addr){
		//Load the existing json file
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(filePath));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		JSONObject jsonObject = (JSONObject) obj;
		String mpubkey = null;
		mpubkey = (String) jsonObject.get("master_public_key");
		String chaincode = null;
		chaincode = (String) jsonObject.get("chain_code");
		long numkeys;
		numkeys = (Long) jsonObject.get("keys_n");
		JSONArray msg = (JSONArray) jsonObject.get("keys");
		Iterator<JSONObject> iterator = msg.iterator();
		JSONArray jsonlist = new JSONArray();
		while (iterator.hasNext()) {
			jsonlist.add(iterator.next());
		}
		//Create the key json object
		numkeys ++;
		JSONObject keyobj = new JSONObject();
		keyobj.put("index",new Integer((int) numkeys));
		keyobj.put("priv_key", privkey);
		keyobj.put("address", addr);
		//Add key object to array
		jsonlist.add(keyobj);
		//Save the new json file
		Map newobj=new LinkedHashMap();
		newobj.put("master_public_key", mpubkey);
		newobj.put("chain_code", chaincode);
		newobj.put("keys_n", numkeys);
		newobj.put("keys", jsonlist);
		StringWriter jsonOut = new StringWriter();
		try {
			JSONValue.writeJSONString(newobj, jsonOut);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String jsonText = jsonOut.toString();
			try {
				FileWriter file = new FileWriter(filePath);
				file.write(jsonText);
				file.flush();
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	void writePairingData(String mpubkey, String chaincode){
		JSONArray jsonlist = new JSONArray();
		Map obj=new LinkedHashMap();
		obj.put("master_public_key", mpubkey);
		obj.put("chain_code", chaincode);
		obj.put("keys_n", new Integer(0));
		obj.put("keys", jsonlist);
		StringWriter jsonOut = new StringWriter();
		try {
			JSONValue.writeJSONString(obj, jsonOut);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String jsonText = jsonOut.toString();
		try {
			FileWriter file = new FileWriter(filePath);
			file.write(jsonText);
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

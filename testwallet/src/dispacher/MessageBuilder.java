package dispacher;

import org.json.JSONException;
import org.json.JSONObject;


public class MessageBuilder extends JSONObject{
	public MessageBuilder(MessageType type,String[] ... arg) throws JSONException
	{
		switch (type){
			case test:
				this.append("data","Hello World");
				break;
			case signTx:
				this.append("PairingID", "1"); // TODO
				this.append("RequestID", "1"); // TODO
				JSONObject reqPayload = new JSONObject();
				reqPayload.append("ExternalIP", arg[0]);
				reqPayload.append("LocalIP", arg[1]);
				this.append("ReqPayload", reqPayload.toString());
				this.append("CustomMsg", "Hello");
				break;
		}
	}
}

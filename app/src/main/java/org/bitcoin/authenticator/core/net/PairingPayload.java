package org.bitcoin.authenticator.core.net;

import org.json.JSONException;
import org.spongycastle.util.encoders.Hex;
import org.json.JSONObject;

/**
 * Created by alonmuroch on 1/24/15.
 */
public class PairingPayload extends JSONObject {
    public PairingPayload(int version, byte[] mpubkey, byte[] chaincode, byte[] regID) throws JSONException {
        put("version", version);
        put("mpubkey", Hex.toHexString(mpubkey));
        put("chaincode", Hex.toHexString(chaincode));
        put("gcmID", new String(regID));
    }
}

package org.bitcoin.authenticator.tests.net;

import junit.framework.TestCase;

import org.bitcoin.authenticator.net.PongPayload;
import org.json.simple.JSONObject;
import org.junit.Test;

/**
 * Created by alonmuroch on 12/22/14.
 */
public class PongTest extends TestCase{
    @Test
    public void testIsValidPongPayload() {
        // null payload
        boolean result = PongPayload.isValidPongPayload(null);
        assertFalse(result);

        // payload not a json string
        result = PongPayload.isValidPongPayload("not a json".getBytes());
        assertFalse(result);

        // payload a json string but wrong one
        JSONObject jo = new JSONObject();
        jo.put("Im not the authenitcator", "");
        result = PongPayload.isValidPongPayload(jo.toString().getBytes());
        assertFalse(result);

        //correct payload
        jo = new JSONObject();
        jo.put("WELCOME_BACK_AUTHENTICATOR", "");
        result = PongPayload.isValidPongPayload(jo.toString().getBytes());
        assertTrue(result);
    }
}

package org.bitcoin.authenticator.tests.core.net;

import junit.framework.TestCase;

import org.bitcoin.authenticator.core.net.CannotProcessRequestPayload;
import org.json.simple.JSONObject;
import org.junit.Test;

/**
 * Created by alonmuroch on 1/22/15.
 */
public class CannotBeProcessedPayloadTest extends TestCase {
    @Test
    public void testCannotBeProcessed() {
        JSONObject j = new JSONObject();
        j.put("CANNOT_PROCESS_REQUEST", "");
        j.put("WHY","reason");

        String ret = CannotProcessRequestPayload.isCannotBeProcessedPayload(j.toString().getBytes());
        assertTrue(ret.equals("reason"));
    }

    @Test
    public void testCanBeProcessed() {
        JSONObject j = new JSONObject();
        j.put("CAN_PROCESS_REQUEST", "");

        String ret = CannotProcessRequestPayload.isCannotBeProcessedPayload(j.toString().getBytes());
        assertTrue(ret == null);
    }

    @Test
    public void testIvalidPayload() {
        JSONObject j = new JSONObject();
        j.put("CANNOT_PROCESS_REQUEST", "");

        String ret = CannotProcessRequestPayload.isCannotBeProcessedPayload(j.toString().getBytes());
        assertTrue(ret.equals("Partial payload"));
    }
}

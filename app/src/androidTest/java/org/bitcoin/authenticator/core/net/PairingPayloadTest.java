package org.bitcoin.authenticator.core.net;

import junit.framework.TestCase;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by alonmuroch on 1/24/15.
 */
public class PairingPayloadTest extends TestCase {
    @Test
    public void test() {
        try {
            byte[] pubKey = "pub key".getBytes();
            byte[] chainCode = "chain code".getBytes();
            byte[] reg = "reg".getBytes();
            PairingPayload p = new PairingPayload(1, pubKey, chainCode, reg);
            assertEquals(p.getInt("version"), 1);
            assertEquals(p.getString("mpubkey"), Hex.toHexString(pubKey));
            assertEquals(p.getString("chaincode"), Hex.toHexString(chainCode));
            assertEquals(p.getString("gcmID"), new String(reg));
        }
        catch (Exception e) {
            assertTrue(false);
        }
    }
}

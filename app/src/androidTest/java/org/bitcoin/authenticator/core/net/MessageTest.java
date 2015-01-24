package org.bitcoin.authenticator.core.net;

import junit.framework.TestCase;

import org.bitcoin.authenticator.core.TxData;
import org.bitcoin.authenticator.core.utils.CryptoUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.SecretKey;

public class MessageTest extends TestCase {

	@Test
	public void testConstructorWithIPs() {
        try {
            new Message(null);
        }
        catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getMessage().equals("No ips were provided"));
        }

        try {

            new Message(new String[] {});
        }
        catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getMessage().equals("No ips were provided"));
        }
	}

    @Test
    public void testSendRequestIDPayload() {
        String requestID = "req id";
        String walletID = "wallet id";
        JSONObject jo = new JSONObject();
        jo.put("requestID", requestID);
        jo.put("pairingID", walletID); // the walletID in the authenticator is the pairing id in the wallet
        String expected = Hex.toHexString(jo.toString().getBytes());
        Message m = new Message(new String[]{ "127.0.0.1"});

        byte[] ret = m.getSendRequestIDPayload(requestID, walletID);
        assertTrue(Hex.toHexString(ret).equals(expected));
    }

    @Test
    public void testParseTxPayload() {
        JSONObject j = new JSONObject();

        j.put("version", 12);
        j.put("ins_n", 1);
        j.put("testnet", false);
        j.put("tx",Hex.toHexString("the tx".getBytes()));

        JSONArray arr = new JSONArray();
        for(int i=0; i<4; i++){
            JSONObject tmp = new JSONObject();
            tmp.put("index", i);
            tmp.put("pubkey", "key " + i);
            arr.add(tmp);
        }
        j.put("keylist", arr);

        SecretKey sk = CryptoUtils.deriveSecretKeyFromPasswordString("password");
        byte[] payload = null;
        try {
            payload = CryptoUtils.encryptPayloadWithChecksum(sk, j.toString().getBytes());
            Message m = new Message(new String[]{ "127.0.0.1"});
            TxData ret = m.parseTxPayload(sk, payload);

            assertTrue(ret.getVersion() == 12);
            assertTrue(ret.getInputCount() == 1);
            assertTrue(ret.getIsTestnet() == false);
            assertTrue(Arrays.equals(ret.getTransaction(), "the tx".getBytes()));

            ArrayList<Integer> expectedIdx = new ArrayList<Integer>();
            expectedIdx.add(0);expectedIdx.add(1);expectedIdx.add(2);expectedIdx.add(3);
            ArrayList<String> expectedPubKeys = new ArrayList<String>();
            expectedPubKeys.add("key 0");expectedPubKeys.add("key 1");expectedPubKeys.add("key 2");expectedPubKeys.add("key 3");
            assertTrue(ret.getIndexes().equals(expectedIdx));
            assertTrue(ret.getPublicKeys().equals(expectedPubKeys));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }


    }
}

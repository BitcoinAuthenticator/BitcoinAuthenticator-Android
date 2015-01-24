package org.bitcoin.authenticator.core.GcmUtil;

import junit.framework.TestCase;

import org.bitcoin.authenticator.BAPreferences.BAPreferences;
import org.bitcoin.authenticator.BAPreferences.Preferences.WalletPreference;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;

/**
 * Created by alonmuroch on 1/23/15.
 */
public class ParseSignTxNotificationTest extends TestCase {
    @Test
    public void testValid() {
        JSONObject j = new JSONObject();
        j.put("tmp", new java.util.Date().getTime());
        j.put("WalletID", Hex.toHexString(ByteBuffer.allocate(8).putLong(123456).array()));
        j.put("RequestType", GCMRequestType.signTx.getValue());
        JSONObject reqPayload = new JSONObject();
        reqPayload.put("ExternalIP", "external IP");
        reqPayload.put("LocalIP", "internal IP");
        j.put("ReqPayload", reqPayload);
        j.put("CustomMsg", "custom msg");
        j.put("RequestID", Hex.toHexString("req ID".getBytes()));

        WalletPreference wp = Mockito.spy(new WalletPreference(null));
        Mockito.doNothing().when(wp).setExternalIP("123456", "external IP");
        Mockito.doNothing().when(wp).setLocalIP("123456", "internal IP");
        BAPreferences bp = Mockito.mock(BAPreferences.class);
        Mockito.when(bp.WalletPreference()).thenReturn(wp);

        //test
        ParseSignTxNotification.SignTxNotificationPayload result = ParseSignTxNotification.ProcessRequest(bp, j.toString());
        assertTrue(result.publicIP.equals("external IP"));
        assertTrue(result.localIP.equals("internal IP"));
        assertTrue(result.walletIdx == 123456);

        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);
        Mockito.verify(wp, Mockito.atMost(1)).setExternalIP(arg1.capture(), arg2.capture());
        assertTrue(arg1.getValue().equals("123456"));
        assertTrue(arg2.getValue().equals("external IP"));

        arg1 = ArgumentCaptor.forClass(String.class);
        arg2 = ArgumentCaptor.forClass(String.class);
        Mockito.verify(wp, Mockito.atMost(1)).setLocalIP(arg1.capture(), arg2.capture());
        assertTrue(arg1.getValue().equals("123456"));
        assertTrue(arg2.getValue().equals("internal IP"));
    }

    @Test
    public void testInValidPayload() {
        JSONObject j = new JSONObject();
        j.put("tmp", new java.util.Date().getTime());
        j.put("WalletID", Hex.toHexString(ByteBuffer.allocate(8).putLong(123456).array()));
        j.put("RequestType", GCMRequestType.signTx.getValue());
        JSONObject reqPayload = new JSONObject();
        reqPayload.put("ExternalIP", "external IP");
        reqPayload.put("LocalIP", "internal IP");
        j.put("ReqPayloadWrong!", reqPayload);
        j.put("CustomMsg", "custom msg");
        j.put("RequestID", Hex.toHexString("req ID".getBytes()));

        //test
        ParseSignTxNotification.SignTxNotificationPayload result = ParseSignTxNotification.ProcessRequest(null, j.toString());
        assertNull(result);
    }
}

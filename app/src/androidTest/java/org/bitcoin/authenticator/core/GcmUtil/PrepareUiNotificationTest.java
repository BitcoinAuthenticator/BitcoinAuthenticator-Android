package org.bitcoin.authenticator.core.GcmUtil;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import org.bitcoin.authenticator.BAPreferences.BAPreferences;
import org.bitcoin.authenticator.BAPreferences.Preferences.ConfigPreference;
import org.bitcoin.authenticator.BAPreferences.Preferences.WalletPreference;
import org.bitcoin.authenticator.R;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by alonmuroch on 1/23/15.
 */
public class PrepareUiNotificationTest extends AndroidTestCase {
    @Test
    public void testCoinsReceivedRemoteNotification() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("CustomMsg","custom msg");
            obj.put("WalletID", Hex.toHexString(ByteBuffer.allocate(8).putLong(123456).array()));

            Notification ret = PrepareUiNotification.getInstance().forCoinsReceivedRemoteNotification(getContext(), mockPreferences(), obj.toString(), null);
            assertTrue(ret.icon == R.drawable.ic_icon_action_bar);
            assertTrue(ret.largeIcon == null);
            assertTrue(ret.tickerText.equals("wallet name: custom msg"));
        } catch (JSONException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testUpdateIpAddressesRemoteNotificationDoAlert() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("WalletID", Hex.toHexString(ByteBuffer.allocate(8).putLong(123456).array()));
            JSONObject reqPayload = new JSONObject();
            reqPayload.put("ExternalIP","ext IP");
            reqPayload.put("LocalIP","inter IP");
            obj.put("ReqPayload", reqPayload.toString());

            BAPreferences preferences = mockPreferences();

            Notification ret = PrepareUiNotification.getInstance().forUpdateIpAddressesRemoteNotification(getContext(), preferences, obj.toString(), null);
            assertTrue(ret.icon == R.drawable.ic_icon_action_bar);
            assertTrue(ret.largeIcon == null);
            assertTrue(ret.tickerText.equals("Pending Notification Update"));

            JSONObject expected = new JSONObject();
            expected.put("seen",false);
            expected.put("WalletID", Hex.toHexString(ByteBuffer.allocate(8).putLong(123456).array()));
            expected.put("ReqPayload", reqPayload);

            ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<JSONObject> arg2 = ArgumentCaptor.forClass(JSONObject.class);
            Mockito.verify(preferences.ConfigPreference(), Mockito.atMost(1)).setPendingRequest(arg1.capture(), arg2.capture());
            String id = arg1.getValue();
            JSONObject req = arg2.getValue();
            assertTrue(id.equals("pending1"));
            assertTrue(req.getBoolean("seen") == expected.getBoolean("seen"));
            assertTrue(req.getString("WalletID").equals(expected.getString("WalletID")));

            JSONObject reqPayloadResult = new JSONObject(req.getString("ReqPayload"));
            assertTrue(reqPayloadResult.getString("ExternalIP").equals(expected.getJSONObject("ReqPayload").getString("ExternalIP")));
            assertTrue(reqPayloadResult.getString("LocalIP").equals(expected.getJSONObject("ReqPayload").getString("LocalIP")));
        } catch (JSONException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testUpdateIpAddressesRemoteNotificationDoNotAlert() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("WalletID", Hex.toHexString(ByteBuffer.allocate(8).putLong(123456).array()));
            obj.put("RequestID", "req ID");
            obj.put("CustomMsg","custom msg");

            BAPreferences preferences = mockPreferences();
            BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

            Notification ret = PrepareUiNotification.getInstance().forSigningRequestRemoteNotification(getContext(), preferences, queue, obj.toString(), null);
            assertTrue(ret.icon == R.drawable.ic_icon_action_bar);
            assertTrue(ret.largeIcon == null);
            assertTrue(ret.tickerText.equals("custom msg"));

            ArgumentCaptor<Boolean> arg1 = ArgumentCaptor.forClass(Boolean.class);
            Mockito.verify(preferences.ConfigPreference(), Mockito.atMost(1)).setRequest(arg1.capture());
            assertTrue(arg1.getValue());

            ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<JSONObject> arg3 = ArgumentCaptor.forClass(JSONObject.class);
            Mockito.verify(preferences.ConfigPreference(), Mockito.atMost(1)).setPendingRequest(arg2.capture(), arg3.capture());
            String id = arg2.getValue();
            JSONObject req = arg3.getValue();
            assertEquals(id, "req ID");
            assertEquals(req.getBoolean("seen"), false);
            assertEquals(req.getString("WalletID"), Hex.toHexString(ByteBuffer.allocate(8).putLong(123456).array()));
            assertEquals(req.getString("RequestID"), "req ID");
            assertEquals(req.getString("CustomMsg"), "custom msg");

            arg2 = ArgumentCaptor.forClass(String.class);
            Mockito.verify(preferences.ConfigPreference(), Mockito.atMost(1)).addPendingRequestToList(arg2.capture());
            assertEquals(arg2.getValue(), "req ID");
        } catch (JSONException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    private BAPreferences mockPreferences() throws JSONException {
        WalletPreference wp = Mockito.spy(new WalletPreference(null));
        //
        Mockito.doReturn("wallet name").when(wp).getName(Long.toString(123456), "XXX");

        ConfigPreference cp = Mockito.spy(new ConfigPreference(null));
        //
        Mockito.doNothing().when(cp).setPendingRequest(Mockito.any(String.class), Mockito.any(JSONObject.class));
        Mockito.doNothing().when(cp).addPendingRequestToList(Mockito.any(String.class));
        Mockito.doNothing().when(cp).setRequest(Mockito.any(Boolean.class));
        //
        List<String> pendingReqs = new ArrayList<String>(); pendingReqs.add("pending1"); pendingReqs.add("pending2");
        // pending 1
        Mockito.doReturn(pendingReqs).when(cp).getPendingList();
        JSONObject pending1 = new JSONObject();
        pending1.put("seen", false);
        pending1.put("WalletID", Hex.toHexString(ByteBuffer.allocate(8).putLong(123456).array()));
        JSONObject reqPayload = new JSONObject();
        reqPayload.put("ExternalIP","old ext IP");
        reqPayload.put("LocalIP","old inter IP");
        pending1.put("ReqPayload", reqPayload.toString());
        Mockito.doReturn(pending1).when(cp).getPendingRequestAsJsonObject("pending1");
        // pending 2
        Mockito.doReturn(pendingReqs).when(cp).getPendingList();
        JSONObject pending2 = new JSONObject();
        pending2.put("seen", true);
        pending2.put("WalletID", Hex.toHexString(ByteBuffer.allocate(8).putLong(123457).array()));
        reqPayload = new JSONObject();
        reqPayload.put("ExternalIP","old ext IP");
        reqPayload.put("LocalIP","old inter IP");
        pending2.put("ReqPayload", reqPayload.toString());
        Mockito.doReturn(pending2).when(cp).getPendingRequestAsJsonObject("pending2");
        //



        BAPreferences preferences = Mockito.mock(BAPreferences.class);
        Mockito.when(preferences.WalletPreference()).thenReturn(wp);
        Mockito.when(preferences.ConfigPreference()).thenReturn(cp);
        return preferences;
    }
}

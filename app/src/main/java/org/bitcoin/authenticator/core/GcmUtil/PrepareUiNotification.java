package org.bitcoin.authenticator.core.GcmUtil;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.bitcoin.authenticator.BAPreferences.BAPreferences;
import org.bitcoin.authenticator.Main;
import org.bitcoin.authenticator.core.net.PairingProtocol;
import org.bitcoin.authenticator.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by alonmuroch on 1/22/15.
 */
public class PrepareUiNotification {

    private static PrepareUiNotification instance;
    public static PrepareUiNotification getInstance() {
        if(instance == null)
            instance = new PrepareUiNotification();
        return instance;
    }

     Notification forCoinsReceivedRemoteNotification(Context context, BAPreferences preferences, String payload, Bitmap logo) throws JSONException {
        JSONObject obj = new JSONObject(payload);

        //
        String customMsg = obj.getString("CustomMsg");
        long walletID = PairingProtocol.getWalletIndexFromString(obj.getString("WalletID"));
        String accountName = preferences.WalletPreference().getName(Long.toString(walletID), "XXX");
        customMsg = accountName + ": " + customMsg;

         Notification notif = generateNotification(context, null, logo, customMsg, "Coins Received");

        return notif;
    }

    Notification forUpdateIpAddressesRemoteNotification(Context context, BAPreferences preferences, String payload, Bitmap logo) throws JSONException {
        JSONObject obj = new JSONObject(payload);
        JSONObject reqPayload = new JSONObject(obj.getString("ReqPayload"));
        /**
         * Update all pending requests IPs from the received WalletID
         */
        boolean didFind = false;
        List<String> pendingReqs = preferences.ConfigPreference().getPendingList();
        if(pendingReqs !=null){
            for(String pendingID: pendingReqs) {
                JSONObject pendingObj = preferences.ConfigPreference().getPendingRequestAsJsonObject(pendingID);

                if(pendingObj.getString("WalletID").equals(obj.getString("WalletID")) && pendingObj.getBoolean("seen") == false){
                    didFind = true;
                    // update
                    JSONObject pendingPayload = new JSONObject(pendingObj.getString("ReqPayload"));
                    pendingPayload.put("ExternalIP", reqPayload.getString("ExternalIP"));
                    pendingPayload.put("LocalIP", reqPayload.getString("LocalIP"));
                    pendingObj.put("ReqPayload",pendingPayload );

                    preferences.ConfigPreference().setPendingRequest(pendingID, pendingObj);

                    Log.v(GcmUtilGlobal.TAG, "Updated pending request: " + pendingID);
                }
            }
        }

        /**
         * Notify user if found a relevant pending request
         */
        if(didFind) {
            //
            Notification notif = generateNotification(context, null, logo, "Pending Notification Update", "Unsigned Transactions");
            return notif;
        }
        return null;
    }

    Notification forSigningRequestRemoteNotification(Context context, BAPreferences preferences, BlockingQueue<String> queue, String payload, Bitmap logo) throws JSONException {
        JSONObject obj = new JSONObject(payload);
        // Those flags would serve to create a list of pending requests
        obj.put("seen", false);
        /**
         * When a new notification enters we have 2 options:
         * 1) If the app is not running, put the request ID as an extra so it will pull it on launch.
         * 2) If app is already running we add it to a request queue so {@link org.bitcoin.authenticator.Wallet_list}<br>
         * 	  activity will pull it in the while loop
         */
        // 1)
        Intent mainIntent = new Intent(context, Main.class);
        mainIntent.putExtra("WalletID", obj.getString("WalletID"));
        mainIntent.putExtra("RequestID", obj.getString("RequestID"));
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // 2)
        queue.add(obj.getString("RequestID"));

        String customMsg = obj.getString("CustomMsg");
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Notification notif = generateNotification(context, contentIntent, logo, customMsg, "New Transaction To Sign");

        // update preference
        preferences.ConfigPreference().setRequest(true);
        preferences.ConfigPreference().setPendingRequest(obj.getString("RequestID"), obj);
        preferences.ConfigPreference().addPendingRequestToList(obj.getString("RequestID"));

        Log.i(GcmUtilGlobal.TAG, "Added pending request: " + obj.getString("RequestID"));

        return notif;
    }

    Notification generateNotification(Context context, PendingIntent contentIntent, Bitmap logo, String customMsg, String title) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_icon_action_bar)
                        .setLargeIcon(logo)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(customMsg))
                        .setContentText(customMsg)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setTicker(customMsg).setWhen(System.currentTimeMillis());

        if(contentIntent != null)
            mBuilder.setContentIntent(contentIntent);
        Notification notif = mBuilder.build();
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        return notif;
    }
}

package org.bitcoin.authenticator.core.GcmUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.bitcoin.authenticator.BAPreferences.BAPreferences;
import org.bitcoin.authenticator.Main;
import org.bitcoin.authenticator.PairingProtocol;
import org.bitcoin.authenticator.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by alonmuroch on 1/22/15.
 */
public class PrepareUiNotification {
    public static Notification forCoinsReceivedRemoteNotification(Context context, String msg, Bitmap logo) throws JSONException {
        JSONObject obj = new JSONObject(msg);

        //
        String customMsg = obj.getString("CustomMsg");
        long walletID = PairingProtocol.getWalletIndexFromString(obj.getString("WalletID"));
        String accountName = BAPreferences.WalletPreference().getName(Long.toString(walletID), "XXX");
        customMsg = accountName + ": " + customMsg;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_icon_action_bar)
                        .setLargeIcon(logo)
                        .setContentTitle("Coins Received")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(customMsg))
                        .setContentText(customMsg)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setTicker(customMsg).setWhen(System.currentTimeMillis());

        Notification notif = mBuilder.build();
        notif.flags |= Notification.FLAG_AUTO_CANCEL;

        return notif;
    }

    public static Notification forUpdateIpAddressesRemoteNotification(Context context, String msg, Bitmap logo) throws JSONException {
        JSONObject obj = new JSONObject(msg);
        JSONObject payload = new JSONObject(obj.getString("ReqPayload"));
        /**
         * Update all pending requests IPs from the received WalletID
         */
        JSONArray o;
        boolean didFind = false;
        List<String> pendingReqs = BAPreferences.ConfigPreference().getPendingList();
        if(pendingReqs !=null){
            //o = new JSONArray(settings.getString("pendingList", ""));
            //for(int i = 0 ; i < o.length(); i++){
            for(String pendingID: pendingReqs) {
                //String pendingID = o.get(i).toString();
                //JSONObject pendingObj = new JSONObject(settings.getString(pendingID, null));

                JSONObject pendingObj = BAPreferences.ConfigPreference().getPendingRequestAsJsonObject(pendingID);

                if(pendingObj.getString("WalletID").equals(obj.getString("WalletID")) && pendingObj.getBoolean("seen") == false){
                    didFind = true;
                    // update
                    JSONObject pendingPayload = new JSONObject(pendingObj.getString("ReqPayload"));
                    pendingPayload.put("ExternalIP", payload.getString("ExternalIP"));
                    pendingPayload.put("LocalIP", payload.getString("LocalIP"));
                    pendingObj.put("ReqPayload",pendingPayload );

                    BAPreferences.ConfigPreference().setPendingRequest(pendingID, pendingObj);

                    Log.v(GcmUtilGlobal.TAG, "Updated pending request: " + pendingID);


                }
            }
        }


        /**
         * Notify user if found a relevant pending request
         */
        if(didFind){
            //
            String customMsg = "Pending Notification Update";
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_icon_action_bar)
                            .setLargeIcon(logo)
                            .setContentTitle("Unsigned Transactionsi")
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(customMsg))
                            .setContentText(customMsg)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setDefaults(Notification.DEFAULT_VIBRATE)
                            .setTicker(customMsg).setWhen(System.currentTimeMillis());

            //mBuilder.setContentIntent(contentIntent);
            Notification notif = mBuilder.build();
            notif.flags |= Notification.FLAG_AUTO_CANCEL;
            return notif;
        }
        return null;
    }

    public static Notification forSigningRequestRemoteNotification(Context context, BlockingQueue<String> queue, String msg, Bitmap logo) throws JSONException {
        Intent mainIntent = new Intent(context, Main.class);
        String customMsg = "";

        JSONObject obj = new JSONObject(msg);
        // Those flags would serve to create a list of pending requests
        obj.put("seen", false);
        /**
         * When a new notification enters we have 2 options:
         * 1) If the app is not running, put the request ID as an extra so it will pull it on launch.
         * 2) If app is already running we add it to a request queue so {@link org.bitcoin.authenticator.Wallet_list}<br>
         * 	  activity will pull it in the while loop
         */
        // 1)
        mainIntent.putExtra("WalletID", obj.getString("WalletID"));
        mainIntent.putExtra("RequestID", obj.getString("RequestID"));
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // 2)
        queue.add(obj.getString("RequestID"));

        customMsg = obj.getString("CustomMsg");

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_icon_action_bar)
                        .setLargeIcon(logo)
                        .setContentTitle("New Transaction To Sign")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(customMsg))
                        .setContentText(customMsg)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setTicker(customMsg).setWhen(System.currentTimeMillis());

        mBuilder.setContentIntent(contentIntent);
        Notification notif = mBuilder.build();
        notif.flags |= Notification.FLAG_AUTO_CANCEL;

        // update preference
        BAPreferences.ConfigPreference().setRequest(true);
        BAPreferences.ConfigPreference().setPendingRequest(obj.getString("RequestID"), obj);
        BAPreferences.ConfigPreference().addPendingRequestToList(obj.getString("RequestID"));

        Log.i(GcmUtilGlobal.TAG, "Added pending request: " + obj.getString("RequestID"));

        return notif;
    }
}

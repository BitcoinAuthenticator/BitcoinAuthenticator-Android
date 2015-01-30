package org.bitcoin.authenticator.core.GcmUtil;

import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bitcoin.authenticator.R;
import org.bitcoin.authenticator.BAPreferences.BAPreferences;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

public class GcmIntentService extends IntentService {
    private NotificationManager mNotificationManager;
    public long uniqueId;
    public static BlockingQueue<String> requestQueue;

    Bitmap logo;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        InputStream is = this.getResources().openRawResource(R.drawable.authenticator_logo);
        logo = BitmapFactory.decodeStream(is);

        if(GcmUtilGlobal.API_CONSOLE_PROJECT_NUMBER == null)
            GcmUtilGlobal.API_CONSOLE_PROJECT_NUMBER = this.getResources().getString(R.string.api_console_project_number);
        
        // init preferencess
     	BAPreferences.init(getApplicationContext());
        
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        try {
		    if (!extras.isEmpty()) {
                JSONObject obj = new JSONObject(extras.getString("data"));
                int idx = obj.getInt("RequestType");
		    	GCMRequestType messageType = GCMRequestType.fromInt(idx);

		        if (messageType == GCMRequestType.signTx) {
		        	processNewSigningNotification(extras.getString("data"));
		            Log.v(GcmUtilGlobal.TAG, "Received: " + extras.getString("data"));
		        }
		        else if(messageType == GCMRequestType.updateIpAddressesForPreviousMessage){
		        	processUpdateIpAddresses(extras.getString("data"));
		        	Log.v(GcmUtilGlobal.TAG, "Received: " + extras.getString("data"));
		        }
		        else if(messageType == GCMRequestType.CoinsReceived){
		        	processCoinsReceived(extras.getString("data"));
		        	Log.v(GcmUtilGlobal.TAG, "Received: " + extras.getString("data"));
		        }
		        else
		        	Log.v(GcmUtilGlobal.TAG, "Received Uknown: " + extras.getString("data"));
		    }
        } catch (JSONException e) { e.printStackTrace(); }
        
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    
    private void processCoinsReceived(String msg) throws JSONException {
//        JSONObject obj = new JSONObject(msg);
//
//    	InputStream is = this.getResources().openRawResource(R.drawable.authenticator_logo);
//        Bitmap logo = BitmapFactory.decodeStream(is);
//		mNotificationManager = (NotificationManager)
//                this.getSystemService(Context.NOTIFICATION_SERVICE);
//		//
//		String customMsg = obj.getString("CustomMsg");
//		long walletID = PairingProtocol.getWalletIndexFromString(obj.getString("WalletID"));
//		String accountName = BAPreferences.WalletPreference().getName(Long.toString(walletID), "XXX");
//		customMsg = accountName + ": " + customMsg;
//
//		NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//        .setSmallIcon(R.drawable.ic_icon_action_bar)
//        .setLargeIcon(logo)
//        .setContentTitle("Coins Received")
//        .setStyle(new NotificationCompat.BigTextStyle()
//        .bigText(customMsg))
//        .setContentText(customMsg)
//        .setDefaults(Notification.DEFAULT_SOUND)
//        .setDefaults(Notification.DEFAULT_VIBRATE)
//        .setTicker(customMsg).setWhen(System.currentTimeMillis());
//
//        Notification notif = mBuilder.build();
//        notif.flags |= Notification.FLAG_AUTO_CANCEL;
//        mNotificationManager.notify((int)uniqueId, notif);

        Notification notif = PrepareUiNotification.getInstance().forCoinsReceivedRemoteNotification(this, BAPreferences.getInstance(), msg, logo);
        if(notif != null) {
            Date now = new Date();
            uniqueId = now.getTime();//use date to generate an unique id to differentiate the notifications.
            mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify((int)uniqueId, notif);
        }
    }

    private void processUpdateIpAddresses(String msg) throws JSONException {
//        JSONObject obj = new JSONObject(msg);
//    	JSONObject payload = new JSONObject(obj.getString("ReqPayload"));
//		/**
//		 * Update all pending requests IPs from the received WalletID
//		 */
//		JSONArray o;
//		boolean didFind = false;
//		List<String> pendingReqs = BAPreferences.ConfigPreference().getPendingList();
//		if(pendingReqs !=null){
//			//o = new JSONArray(settings.getString("pendingList", ""));
//			//for(int i = 0 ; i < o.length(); i++){
//			for(String pendingID: pendingReqs) {
//				//String pendingID = o.get(i).toString();
//				//JSONObject pendingObj = new JSONObject(settings.getString(pendingID, null));
//
//				JSONObject pendingObj = BAPreferences.ConfigPreference().getPendingRequestAsJsonObject(pendingID);
//
//				if(pendingObj.getString("WalletID").equals(obj.getString("WalletID")) && pendingObj.getBoolean("seen") == false){
//					didFind = true;
//					// update
//					JSONObject pendingPayload = new JSONObject(pendingObj.getString("ReqPayload"));
//					pendingPayload.put("ExternalIP", payload.getString("ExternalIP"));
//					pendingPayload.put("LocalIP", payload.getString("LocalIP"));
//					pendingObj.put("ReqPayload",pendingPayload );
//
//					BAPreferences.ConfigPreference().setPendingRequest(pendingID, pendingObj);
//
//					Log.v(GcmUtilGlobal.TAG, "Updated pending request: " + pendingID);
//
//
//				}
//			}
//		}
//
//
//		/**
//		 * Notify user if found a relevant pending request
//		 */
//		if(didFind){
//			InputStream is = this.getResources().openRawResource(R.drawable.authenticator_logo);
//	        Bitmap logo = BitmapFactory.decodeStream(is);
//			mNotificationManager = (NotificationManager)
//	                this.getSystemService(Context.NOTIFICATION_SERVICE);
//			//
//			String customMsg = "Pending Notification Update";
//			NotificationCompat.Builder mBuilder =
//	                new NotificationCompat.Builder(this)
//	        .setSmallIcon(R.drawable.ic_icon_action_bar)
//	        .setLargeIcon(logo)
//	        .setContentTitle("Unsigned Transactionsi")
//	        .setStyle(new NotificationCompat.BigTextStyle()
//	        .bigText(customMsg))
//	        .setContentText(customMsg)
//	        .setDefaults(Notification.DEFAULT_SOUND)
//	        .setDefaults(Notification.DEFAULT_VIBRATE)
//	        .setTicker(customMsg).setWhen(System.currentTimeMillis());
//
//			//mBuilder.setContentIntent(contentIntent);
//	        Notification notif = mBuilder.build();
//	        notif.flags |= Notification.FLAG_AUTO_CANCEL;
//	        mNotificationManager.notify((int)uniqueId, notif);
//		}

        Notification notif = PrepareUiNotification.getInstance().forUpdateIpAddressesRemoteNotification(this, BAPreferences.getInstance(), msg, logo);
        if(notif != null) {
            Date now = new Date();
            uniqueId = now.getTime();//use date to generate an unique id to differentiate the notifications.
            mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify((int)uniqueId, notif);
        }
    }
    
    private void processNewSigningNotification(String msg) throws JSONException {
//    	mNotificationManager = (NotificationManager)
//                this.getSystemService(Context.NOTIFICATION_SERVICE);
//
//    	Intent mainIntent = new Intent(this, Main.class);
//    	String customMsg = "";
//
//        JSONObject obj = new JSONObject(msg);
//		// Those flags would serve to create a list of pending requests
//		obj.put("seen", false);
//		/**
//		 * When a new notification enters we have 2 options:
//		 * 1) If the app is not running, put the request ID as an extra so it will pull it on launch.
//		 * 2) If app is already running we add it to a request queue so {@link org.bitcoin.authenticator.Wallet_list}<br>
//		 * 	  activity will pull it in the while loop
//		 */
//		// 1)
//		mainIntent.putExtra("WalletID", obj.getString("WalletID"));
//		mainIntent.putExtra("RequestID", obj.getString("RequestID"));
//		mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		// 2)
//		addRequestToQueue(obj.getString("RequestID"));
//
//		customMsg = obj.getString("CustomMsg");
//
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//        		mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        InputStream is = this.getResources().openRawResource(R.drawable.authenticator_logo);
//        Bitmap logo = BitmapFactory.decodeStream(is);
//
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//        .setSmallIcon(R.drawable.ic_icon_action_bar)
//        .setLargeIcon(logo)
//        .setContentTitle("New Transaction To Sign")
//        .setStyle(new NotificationCompat.BigTextStyle()
//        .bigText(customMsg))
//        .setContentText(customMsg)
//        .setDefaults(Notification.DEFAULT_SOUND)
//        .setDefaults(Notification.DEFAULT_VIBRATE)
//        .setTicker(customMsg).setWhen(System.currentTimeMillis());
//
//        mBuilder.setContentIntent(contentIntent);
//        Notification notif = mBuilder.build();
//        notif.flags |= Notification.FLAG_AUTO_CANCEL;
//        mNotificationManager.notify((int)uniqueId, notif);
//
//        // update preference
//        BAPreferences.ConfigPreference().setRequest(true);
//        BAPreferences.ConfigPreference().setPendingRequest(obj.getString("RequestID"), obj);
//        BAPreferences.ConfigPreference().addPendingRequestToList(obj.getString("RequestID"));
//
//        Log.v(GcmUtilGlobal.TAG, "Added pending request: " + obj.getString("RequestID"));


        Notification notif = PrepareUiNotification.getInstance().forSigningRequestRemoteNotification(this, BAPreferences.getInstance(), getQueue(), msg, logo);
        if(notif != null) {
            Date now = new Date();
            uniqueId = now.getTime();//use date to generate an unique id to differentiate the notifications.
            mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify((int)uniqueId, notif);
        }
    }
        
    //########################
    //
    //			QUEUE
    //
    //########################
    
    private static BlockingQueue<String> getQueue(){
    	if(requestQueue == null){
    		requestQueue = new LinkedBlockingQueue<String>();
    	}
    	return requestQueue;
    }
    
    private void addRequestToQueue(String reqID)
    {
    	getQueue().add(reqID);
    }
    
    public static String pollRequest() throws InterruptedException{

    	return getQueue().poll();
    }
    
    /**
     * block until a new gcm request is available
     * @return - top request ID
     * 
     * @throws InterruptedException
     */
    public static String takeRequest() throws InterruptedException{
    	return getQueue().take();
    }
}
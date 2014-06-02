package org.bitcoin.authenticator.GcmUtil;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bitcoin.authenticator.Main;
import org.bitcoin.authenticator.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    public long uniqueId;
    public JSONObject obj;
    public static BlockingQueue<String> requestQueue;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        try {
		    if (!extras.isEmpty()) { 
		    	RequestType messageType = getRequestType(extras.getString("data"));
				
		        if (messageType == RequestType.signTx) {
		        	processNewSigningNotification(extras.getString("data"));
		            Log.v(GcmUtilGlobal.TAG, "Received: " + extras.getString("data"));
		        }
		        else if(messageType == RequestType.updateIpAddressesForPreviousMessage){
		        	processUpdateIpAddressesForPreviousMessage(extras.getString("data"));
		        	Log.v(GcmUtilGlobal.TAG, "Received: " + extras.getString("data"));
		        }
		    }
        } catch (JSONException e) { e.printStackTrace(); }
        
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
    
    public RequestType getRequestType(String msg) throws JSONException{
    	obj = new JSONObject(msg);
    	int objInt = obj.getInt("RequestType");
    	switch(objInt){
    	case 1:
    		return RequestType.test;
    	case 2:
    		return RequestType.signTx;
    	case 4:
    		return RequestType.updateIpAddressesForPreviousMessage;
    	}
    	return null;
    }

    private void processUpdateIpAddressesForPreviousMessage(String msg) throws JSONException {
    	obj = new JSONObject(msg);
    	JSONObject payload = new JSONObject(obj.getString("ReqPayload"));
    	// search notification
    	SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
		SharedPreferences.Editor editor = settings.edit();	
		// update RequestID list
		JSONArray o;
		if(settings.getString("pendingList", null) !=null){
			o = new JSONArray(settings.getString("pendingList", ""));
			 for(int i = 0 ; i < o.length(); i++){
				String pendingID = o.get(i).toString();
				if(pendingID.equals(obj.getString("RequestID"))){
					JSONObject pendingObj = new JSONObject(settings.getString(pendingID, null));
					// update
					JSONObject pendingPayload = new JSONObject(pendingObj.getString("ReqPayload"));
					pendingPayload.put("ExternalIP", payload.getString("ExternalIP"));
					pendingPayload.put("LocalIP", payload.getString("LocalIP"));
					pendingObj.put("ReqPayload",pendingPayload );
					editor.putString(pendingObj.getString("RequestID"), pendingObj.toString());
					editor.commit();
					//
					break;
				}
			 }
		}
		
		Intent intent = new Intent(this, Main.class);
		intent.putExtra("RequestID", obj.getString("RequestID"));
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
		//
		String customMsg = "Pending Notification Update";
		NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.authenticator_logo)
        .setContentTitle("New Message To Sign")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(customMsg))
        .setContentText(customMsg)
        .setDefaults(Notification.DEFAULT_SOUND)
        .setDefaults(Notification.DEFAULT_VIBRATE)
        .setTicker(customMsg).setWhen(System.currentTimeMillis());

		mBuilder.setContentIntent(contentIntent);
        Notification notif = mBuilder.build();
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify((int)uniqueId, notif);
    }
    
    private void processNewSigningNotification(String msg) throws JSONException {
    	Date now = new Date();
    	uniqueId = now.getTime();//use date to generate an unique id to differentiate the notifications.
    	
    	mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

    	Intent intent = new Intent(this, Main.class);
    	String customMsg = "";
	
		obj = new JSONObject(msg);
		// Those flags would serve to create a list of pending requests
		obj.put("seen", false);
		/**
		 * When a new notification enters we have 2 options:
		 * 1) If the app is not running, put the request ID as an extra so it will pull it on launch.
		 * 2) If app is already running we add it to a request queue so {@link org.bitcoin.authenticator.Wallet_list}<br>
		 * 	  activity will pull it in the while loop 
		 */
		// 1) 
		intent.putExtra("RequestID", obj.getString("RequestID"));
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// 2) 
		addRequestToQueue(obj.getString("RequestID"));
		
		customMsg = obj.getString("CustomMsg");
  
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
         
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.authenticator_logo)
        .setContentTitle("New Message To Sign")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(customMsg))
        .setContentText(customMsg)
        .setDefaults(Notification.DEFAULT_SOUND)
        .setDefaults(Notification.DEFAULT_VIBRATE)
        .setTicker(customMsg).setWhen(System.currentTimeMillis());

        mBuilder.setContentIntent(contentIntent);
        Notification notif = mBuilder.build();
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify((int)uniqueId, notif);

        // update preference
		SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
		SharedPreferences.Editor editor = settings.edit();	
    	editor.putBoolean("request", true);
		editor.putString(obj.getString("RequestID"), obj.toString());
		// update RequestID list
		JSONArray o;
		if(settings.getString("pendingList", null) !=null)
			o = new JSONArray(settings.getString("pendingList", ""));
		else
			o = new JSONArray();
		o.put(obj.getString("RequestID"));
		editor.putString("pendingList", o.toString());
		editor.commit();
    	
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
package org.bitcoin.authenticator.core.GcmUtil;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
	
	static private Map<String,String> recMsg;
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	if(recMsg == null)
    		recMsg = new HashMap<String,String>();

    	//Workaround for the multiple notification problem
    	String msg = intent.getStringExtra("data");
    	if(recMsg.get(msg) != null)
    		return;
    	recMsg.put(msg, "");
    	
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
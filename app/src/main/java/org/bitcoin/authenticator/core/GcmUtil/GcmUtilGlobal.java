package org.bitcoin.authenticator.core.GcmUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class GcmUtilGlobal {
	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String TAG = "GCM";
    public static String API_CONSOLE_PROJECT_NUMBER = null;
    public static String gcmRegistrationToken;
}

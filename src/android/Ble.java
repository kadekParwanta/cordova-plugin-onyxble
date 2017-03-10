package com.cordova.ble;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.Gson;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPush;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushException;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPSimplePushNotification;
import com.onyxbeacon.OnyxBeaconApplication;
import com.onyxbeacon.OnyxBeaconManager;
import com.onyxbeacon.listeners.OnyxBeaconsListener;
import com.onyxbeacon.listeners.OnyxCouponsListener;
import com.onyxbeacon.listeners.OnyxPushListener;
import com.onyxbeacon.listeners.OnyxTagsListener;
import com.onyxbeacon.rest.auth.util.AuthData;
import com.onyxbeacon.rest.auth.util.AuthenticationMode;
import com.onyxbeacon.rest.model.account.BluemixApp;
import com.onyxbeacon.rest.model.content.Coupon;
import com.onyxbeacon.rest.model.content.Tag;
import com.onyxbeacon.service.logging.LoggingStrategy;
import com.onyxbeaconservice.Beacon;
import com.onyxbeaconservice.IBeacon;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Ble extends CordovaPlugin implements BleStateListener {

    private static final String TAG = "ONYXBLE";
    private static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int REQUEST_INIT_SDK = 0;

    private static final String ACTION_INIT_SDK = "initSDK";
    private static final String ACTION_STOP_SCAN = "stop";
    private static final String ACTION_ADD_ONYX_BEACONS_LISTENER = "addOnyxBeaconsListener";
    private static final String ACTION_ADD_WEB_LISTENER = "addWebListener";
    private static final String ACTION_ADD_TAGS_LISTENER = "addTagsListener";
    private static final String ACTION_ADD_COUPON_LISTENER = "addCouponsListener";
    private static final String ACTION_ADD_DELIVERED_COUPON_LISTENER = "addDeliveredCouponsListener";
    private static final String ACTION_GET_DELIVERED_COUPONS = "getDeliveredCoupons";
    private static final String ACTION_SET_ERROR_LISTENER = "setErrorListener";
    private static final String ACTION_ENTER_BACKGROUND = "enterBackground";
    private static final String ACTION_ENTER_FOREGROUND = "enterForeground";
    private static final String ACTION_SHOW_COUPON = "showCoupon";
    private static final String ACTION_GET_TAGS = "getTags";
    private static final String ACTION_BUZZ_BEACON = "buzzBeacon";
    private static final String ACTION_Add_PUSH_LISTENER = "addPushListener";

    private static final String PROPERTY_BLUEMIX_DEVICE_ID = "bluemix_device_id";
    private static final String PROPERTY_BLUEMIX_CREDENTIALS = "bluemix_credentials";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private CallbackContext messageChannel;
    // OnyxBeacon SDK
    private OnyxBeaconManager beaconManager;
    private String CONTENT_INTENT_FILTER;
    private ContentReceiver mContentReceiver;
    private BleStateReceiver mBleReceiver;
    private boolean receiverRegistered = false;
    private boolean bleStateRegistered = false;
    private boolean sendfalse;
    private Ble instance;
    private Gson gson = new Gson();

    private BleErrorListener mBleErrorListener;
    private ArrayList<CallbackContext> mCouponReceivers = new ArrayList<CallbackContext>();
    private ArrayList<CallbackContext> mDeliveredCouponReceivers = new ArrayList<CallbackContext>();
    private MFPPush push;
    private Boolean isPushRegistered = false;
    private String pushError;

    interface BleErrorListener {
        void onError(int errorCode, Exception e);
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        instance = this;
        beaconManager = OnyxBeaconApplication.getOnyxBeaconManager(cordova.getActivity());

        mContentReceiver = ContentReceiver.getInstance();
        mContentReceiver.setOnyxCouponsListener(mOnyxCouponListener);
        mContentReceiver.setOnyxPushListener(mOnyxPushListener);
        //Register for BLE events
        mBleReceiver = BleStateReceiver.getInstance();
        mBleReceiver.setBleStateListener(this);

        String BLE_INTENT_FILTER = cordova.getActivity().getPackageName() + ".scan";
        cordova.getActivity().registerReceiver(mBleReceiver, new IntentFilter(BLE_INTENT_FILTER));
        bleStateRegistered = true;

        CONTENT_INTENT_FILTER = cordova.getActivity().getPackageName() + ".content";
        cordova.getActivity().registerReceiver(mContentReceiver, new IntentFilter(CONTENT_INTENT_FILTER));
        receiverRegistered = true;
    }
    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArry of arguments for the plugin.
     * @param callbackContext The callback id used when calling back into JavaScript.
     * @return True if the action was valid, false if not.
     */
    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext)   {
        messageChannel = callbackContext;
        Log.v(TAG, "action=" + action);
        try {
            if (action.equalsIgnoreCase(ACTION_INIT_SDK)) {
                enableLocation();
            } else if (action.equalsIgnoreCase(ACTION_ADD_ONYX_BEACONS_LISTENER)) {
                addOnyxBeaconsListener(callbackContext);
            } else if (action.equalsIgnoreCase(ACTION_ADD_WEB_LISTENER)) {
                addWebListener(callbackContext);
            } else if (action.equalsIgnoreCase(ACTION_ADD_TAGS_LISTENER)) {
                addTagsListener(callbackContext);
            } else if (action.equalsIgnoreCase(ACTION_SET_ERROR_LISTENER)) {
                setBleErrorListener(callbackContext);
            } else if (action.equalsIgnoreCase(ACTION_STOP_SCAN)) {
                beaconManager.stopScan();
            }  else if (action.equalsIgnoreCase(ACTION_ENTER_BACKGROUND)) {
                enterBackground();
            }  else if (action.equalsIgnoreCase(ACTION_ENTER_FOREGROUND)) {
                enterForeground();
            } else if (action.equalsIgnoreCase(ACTION_ADD_COUPON_LISTENER)) {
                addCouponReceiver(callbackContext);
            }   else if (action.equalsIgnoreCase(ACTION_ADD_DELIVERED_COUPON_LISTENER)) {
                addDeliveredCouponReceiver(callbackContext);
            }   else if (action.equalsIgnoreCase(ACTION_GET_DELIVERED_COUPONS)) {
                getDeliveredCoupons(callbackContext);
            }   else if (action.equalsIgnoreCase(ACTION_SHOW_COUPON)) {
                showCoupon(args, callbackContext);
            }  else if (action.equalsIgnoreCase(ACTION_GET_TAGS)) {
                beaconManager.getTags();
                callbackContext.success("getTags is invoked");
            }  else if (action.equalsIgnoreCase(ACTION_BUZZ_BEACON)) {
                buzzBeacon(args, callbackContext);
            }  else if (action.equalsIgnoreCase(ACTION_Add_PUSH_LISTENER)) {
                addPushListener(callbackContext);
            } else if (action.equals("sendGenericUserProfile")) {
                beaconManager.sendGenericUserProfile(jsonToMap(args.getJSONObject(0)));
                callbackContext.success("Success");
            } else if (action.equals("setForegroundMode")) {
                beaconManager.setForegroundMode(args.getBoolean(0));
                callbackContext.success("Success");
            } else if (action.equals("setBackgroundBetweenScanPeriod")) {
                beaconManager.setBackgroundBetweenScanPeriod(args.getLong(0));
                callbackContext.success("Success");
            } else if (action.equals("startScan")) {
                beaconManager.startScan();
                callbackContext.success("Success");
            } else if (action.equals("stopScan")) {
                beaconManager.stopScan();
                callbackContext.success("Success");
            } else if (action.equals("isInForeground")) {
                Boolean isInForeground = beaconManager.isInForeground();
                callbackContext.success(isInForeground ? 1 : 0);
            } else if (action.equals("setCouponEnabled")) {
                beaconManager.setCouponEnabled(args.getBoolean(0));
                callbackContext.success("Success");
            } else if (action.equals("setLocationTrackingEnabled")) {
                beaconManager.setLocationTrackingEnabled(args.getBoolean(0));
                callbackContext.success("Success");
            } else if (action.equals("logOut")) {
                beaconManager.logOut();
                callbackContext.success("Success");
            } else if (action.equals("restartLocationTracking")) {
                beaconManager.restartLocationTracking();
                callbackContext.success("Success");
            } else if (action.equals("enableGeofencing")) {
                beaconManager.enableGeofencing(args.getBoolean(0));
                callbackContext.success("Success");
            } else if (action.equals("setAPIContentEnabled")) {
                beaconManager.setAPIContentEnabled(args.getBoolean(0));
                callbackContext.success("Success");
            } else if (action.equals("sendDeviceToken")) {
                beaconManager.sendDeviceToken(args.getString(0), args.getString(1));
                callbackContext.success("Success");
            } else if (action.equals("onPause")) {
                beaconManager.setForegroundMode(false);
                // Unregister content receiver
                if (bleStateRegistered) {
                    cordova.getActivity().unregisterReceiver(mBleReceiver);
                    bleStateRegistered = false;
                }

                if (receiverRegistered){
                    cordova.getActivity().unregisterReceiver(mContentReceiver);
                    receiverRegistered = false;
                }

                callbackContext.success("Success");
            } else if (action.equals("onResume")) {
                if (mBleReceiver == null) mBleReceiver = BleStateReceiver.getInstance();
                cordova.getActivity().registerReceiver(mContentReceiver, new IntentFilter(CONTENT_INTENT_FILTER));
                receiverRegistered = true;
                mBleReceiver.setBleStateListener(instance);

                if (mContentReceiver == null)
                    mContentReceiver = ContentReceiver.getInstance();
                cordova.getActivity().registerReceiver(mContentReceiver, new IntentFilter(CONTENT_INTENT_FILTER));
                receiverRegistered = true;
                if (BluetoothAdapter.getDefaultAdapter() == null) {
                    Log.e(TAG, "Device does not support Bluetooth");
                } else {
                    if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                        Log.e(TAG, "Please turn on bluetooth");
                    } else {
                        // Enable scanner in foreground mode and register receiver
                        beaconManager = OnyxBeaconApplication.getOnyxBeaconManager(cordova.getActivity());
                        beaconManager.setForegroundMode(true);
                    }
                }
                callbackContext.success("Success");
            } else if (action.equals("setTagsFilterForCoupons")) {


                ArrayList<Tag> tagsFilter = new ArrayList<Tag>();

                for (int i = 0; i < args.length(); i++) {

                    JSONArray arg = args.getJSONArray(i);
                    Tag rtag = new Tag();
                    rtag.id = arg.getInt(0);
                    rtag.name = arg.getString(1);
                    rtag.state = arg.getString(2);

                    tagsFilter.add(rtag);
                }
                beaconManager.setTagsFilterForCoupons(tagsFilter);
                callbackContext.success("Success");
            }
            else if (action.equals("sendReport")) {
                String reporter = args.getString(0);
                beaconManager.sendLogs(reporter);
                callbackContext.success("sendReport Invoked");
            } else {
                Log.e("Ble","Unknown action " + action);
                sendfalse = true;
                callbackContext.error(action);
            }
        } catch (JSONException e) {
            callbackContext.success(e.getMessage());
            Log.e("Ble","Unknown JSONException " + e.getMessage());

            sendfalse = true;
        }

        return !sendfalse;
    }

    private void buzzBeacon(JSONArray args, CallbackContext callbackContext) {
        Gson gson = new Gson();
        try {
            IBeacon beacon = gson.fromJson(args.getString(0), IBeacon.class);
            beaconManager.buzz(beacon);
            callbackContext.success("buzzBeacon is invoked");
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error(e.getLocalizedMessage());
        }
    }

    private void showCoupon(JSONArray args, CallbackContext callbackContext) {
        Context context = this.cordova.getActivity();
        Gson gson = new Gson();
        try {
            Coupon coupon = gson.fromJson(args.getString(0), Coupon.class);
            OnyxBeaconApplication.startCouponDetailActivity(context, coupon);
            callbackContext.success("showCoupon is invoked");
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error(e.getLocalizedMessage());
        }
    }

    private void getDeliveredCoupons(CallbackContext callbackContext) {
        beaconManager.getDeliveredCoupons();
        callbackContext.success("getDeliveredCoupons is invoked");
    }

    private OnyxCouponsListener mOnyxCouponListener = new OnyxCouponsListener() {
        @Override
        public void onCouponReceived(Coupon coupon, IBeacon iBeacon) {
            //create arraylist because the iOS SDK use an array.
            ArrayList<Coupon> coupons = new ArrayList<Coupon>();
            coupons.add(coupon);
            for (CallbackContext callbackContext : mCouponReceivers) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, gson.toJson(coupons));
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        }

        @Override
        public void onDeliveredCouponsReceived(ArrayList<Coupon> arrayList) {
            for (CallbackContext callbackContext : mDeliveredCouponReceivers) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, gson.toJson(arrayList));
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        }
    };

    private void addCouponReceiver(CallbackContext callbackContext) {
        mCouponReceivers.add(callbackContext);
    }

    private void addDeliveredCouponReceiver(CallbackContext callbackContext) {
        mDeliveredCouponReceivers.add(callbackContext);
    }

    private void addTagsListener(final CallbackContext callbackContext) {
        OnyxTagsListener mOnyxTagsListener = new OnyxTagsListener() {
            @Override
            public void onTagsReceived(List<Tag> list) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, gson.toJson(list));
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        };

        if (mContentReceiver != null) {
            mContentReceiver.setOnyxTagsListener(mOnyxTagsListener);
        } else {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Failed to add listener");
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }
    }

    private void addWebListener(final CallbackContext callbackContext) {
        BleWebRequestListener mBleWebRequestListener = new BleWebRequestListener() {
            @Override
            public void onRequested(String info) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, info);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        };

        if (mContentReceiver != null) {
            mContentReceiver.setBlewWebRequestListener(mBleWebRequestListener);
        } else {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Failed to add listener");
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }
    }

    private void addOnyxBeaconsListener(final CallbackContext callbackContext) {
        OnyxBeaconsListener mOnyxBeaconsListener = new OnyxBeaconsListener() {
            @Override
            public void didRangeBeaconsInRegion(List<Beacon> list) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, gson.toJson(list));
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        };

        if (mContentReceiver != null) {
            mContentReceiver.setOnyxBeaconsListener(mOnyxBeaconsListener);
        } else {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Failed to add listener");
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }
    }

    private void setBleErrorListener(final CallbackContext callbackContext) {
        mBleErrorListener = new BleErrorListener() {
            @Override
            public void onError(int errorCode, Exception e) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, errorCode);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        };

    }

    private OnyxPushListener mOnyxPushListener = new OnyxPushListener() {
        @Override
        public void onBluemixCredentialsReceived(BluemixApp bluemixApp) {
            storeBluemixCredentials(cordova.getActivity(), bluemixApp);
            registerDeviceAtBluemix(bluemixApp);
        }
    };

    private void addPushListener(final CallbackContext callbackContext) {
        MFPPushNotificationListener notificationListener = new MFPPushNotificationListener() {
            @Override
            public void onReceive(MFPSimplePushNotification message) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("payload",message.getPayload());
                    jsonObject.put("url",message.getUrl());
                    jsonObject.put("alert",message.getAlert());
                    PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject.toString());
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                } catch (JSONException e) {
                    PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Message is empty");
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                }
            }
        };

        if (isPushRegistered && push != null) {
            push.listen(notificationListener);
        } else {
            String errorMessage = "Not registered to Bluemix";
            if (pushError != null) {
                errorMessage = pushError;
            }
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, errorMessage);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }

    }

    private void registerDeviceAtBluemix(BluemixApp bluemixApp) {
        final Activity mActivity = cordova.getActivity();
        final Context context = mActivity.getApplicationContext();
        try {
            BMSClient.getInstance().initialize(context, bluemixApp.route, bluemixApp.app_key);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        push = MFPPush.getInstance();
        push.initialize(mActivity);

        push.register(new MFPPushResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                Pattern p = Pattern.compile("\"deviceId\":\"([0-9a-z\\-]+)");
                Matcher m = p.matcher(response);
                if (m.find()){
                    storeBluemixDeviceId(context, m.group(1));
                    beaconManager.sendDeviceToken("", m.group(1));
                    isPushRegistered = true;
                }
            }

            @Override
            public void onFailure(MFPPushException exception) {
                isPushRegistered = false;
                pushError = exception.getErrorMessage();
            }
        });
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(cordova.getActivity().getPackageName(),Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void storeBluemixDeviceId(Context context, String bluemixDeviceId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_BLUEMIX_DEVICE_ID, bluemixDeviceId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    private void storeBluemixCredentials(Context context, BluemixApp bluemixApp) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_BLUEMIX_CREDENTIALS, gson.toJson(bluemixApp));
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    private void enterBackground() {
        beaconManager.setForegroundMode(false);
        // Unregister content receiver
        if (bleStateRegistered) {
            cordova.getActivity().unregisterReceiver(mBleReceiver);
            bleStateRegistered = false;
        }

        if (receiverRegistered){
            cordova.getActivity().unregisterReceiver(mContentReceiver);
            receiverRegistered = false;
        }
    }

    private void enterForeground() {
        if (mBleReceiver == null) mBleReceiver = BleStateReceiver.getInstance();
        cordova.getActivity().registerReceiver(mContentReceiver, new IntentFilter(CONTENT_INTENT_FILTER));
        receiverRegistered = true;
        mBleReceiver.setBleStateListener(instance);

        if (mContentReceiver == null)
            mContentReceiver = ContentReceiver.getInstance();
        cordova.getActivity().registerReceiver(mContentReceiver, new IntentFilter(CONTENT_INTENT_FILTER));
        receiverRegistered = true;
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Log.e(TAG, "Device does not support Bluetooth");
        } else {
            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                Log.e(TAG, "Please turn on bluetooth");
            } else {
                // Enable scanner in foreground mode and register receiver
                beaconManager = OnyxBeaconApplication.getOnyxBeaconManager(cordova.getActivity());
                beaconManager.setForegroundMode(true);
            }
        }
    }

    private static HashMap<String, Object> jsonToMap(JSONObject json) throws JSONException {
        HashMap<String, Object> retMap = new HashMap<String, Object>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    private static HashMap<String, Object> toMap(JSONObject object) throws JSONException {
        HashMap<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    private static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    private void enableLocation() {
        if (cordova.hasPermission(ACCESS_FINE_LOCATION)) {
            initSDK();
        } else {
            getPermission(REQUEST_INIT_SDK, ACCESS_FINE_LOCATION);
        }
    }

    private void getPermission(int requestCode, String permission)
    {
        cordova.requestPermission(this, requestCode, permission);
    }

    private void initSDK() {
        beaconManager.setDebugMode(LoggingStrategy.DEBUG);
        beaconManager.setAPIEndpoint("https://connect.onyxbeacon.com");
        beaconManager.setCouponEnabled(true);
        beaconManager.setAPIContentEnabled(true);
        beaconManager.enableGeofencing(true);
        beaconManager.setLocationTrackingEnabled(true);
        String clientId, secret;
        if (preferences.contains("com-cordova-ble-clientId")) {
            clientId = preferences.getString("com-cordova-ble-clientId", "");
        } else {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR,"missing clientId");
            result.setKeepCallback(true);
            messageChannel.sendPluginResult(result);
            return;
        }
        if (preferences.contains("com-cordova-ble-secret")) {
            secret = preferences.getString("com-cordova-ble-secret", "");
        } else {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR,"missing secret");
            result.setKeepCallback(true);
            messageChannel.sendPluginResult(result);
            return;
        }
        AuthData authData = new AuthData();
        authData.setAuthenticationMode(AuthenticationMode.CLIENT_SECRET_BASED);
        authData.setSecret(secret);
        authData.setClientId(clientId);
        beaconManager.setAuthData(authData);
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR,"Device does not support Bluetooth");
            result.setKeepCallback(true);
            messageChannel.sendPluginResult(result);
        } else {
            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR,"Please turn on bluetooth");
                result.setKeepCallback(true);
                messageChannel.sendPluginResult(result);
            } else {
                // Enable scanner in foreground mode and register receiver
                beaconManager.setForegroundMode(true);
                beaconManager.startScan();
                PluginResult result = new PluginResult(PluginResult.Status.OK,"Success");
                result.setKeepCallback(true);
                messageChannel.sendPluginResult(result);
            }
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for (int r: grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                if (requestCode == REQUEST_INIT_SDK) {
                    if (messageChannel != null) {
                        messageChannel.error(requestCode);
                    }
                }

                return;
            }
        }

        switch (requestCode) {
            case REQUEST_INIT_SDK:
                initSDK();
                break;
        }
    }

    private void onError(int errorCode, Exception e) {
        if (mBleErrorListener!=null) mBleErrorListener.onError(errorCode, e);
    }


    @Override
    public void onBleStackEvent(int event) {
        System.out.println(event);
        switch (event) {
            case 1:
                onError(event,new Exception("Probably your bluetooth stack has crashed. Please restart your bluetooth"));
                break;
            case 2:
                onError(event, new Exception("Beacons with invalid RSSI detected. Please restart your bluetooth."));
                break;

            default:onError(event, new Exception("This Error is unknown"));
                break;

        }
    }
}



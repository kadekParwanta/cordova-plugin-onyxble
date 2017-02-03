package com.cordova.ble;


import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;
import android.util.Log;

import com.onyxbeacon.OnyxBeaconApplication;
import com.onyxbeacon.OnyxBeaconManager;
import com.onyxbeacon.rest.auth.util.AuthData;
import com.onyxbeacon.rest.auth.util.AuthenticationMode;
import com.onyxbeacon.rest.model.content.Tag;
import com.onyxbeacon.service.logging.LoggingStrategy;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class Ble extends CordovaPlugin implements BleStateListener  {

    private CallbackContext messageChannel;
    // OnyxBeacon SDK
    private OnyxBeaconManager beaconManager;
    private String CONTENT_INTENT_FILTER;
    private String BLE_INTENT_FILTER;
    private ContentReceiver mContentReceiver;
    private BleStateReceiver mBleReceiver;
    private boolean receiverRegistered = false;
    private boolean bleStateRegistered = false;
    public static final String TAG = "Device";
    private          boolean sendfalse;
    Ble instance;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {

        Log.v(TAG, "initialized BLE=" );

        super.initialize(cordova, webView);
        instance = this;
        beaconManager = OnyxBeaconApplication.getOnyxBeaconManager(cordova.getActivity());

        mContentReceiver = ContentReceiver.getInstance(this);
        //Register for BLE events
        mBleReceiver = BleStateReceiver.getInstance();
        mBleReceiver.setBleStateListener(this);

        BLE_INTENT_FILTER = cordova.getActivity().getPackageName() + ".scan";
        Log.d("Ble","initialize scan intent filter = " + BLE_INTENT_FILTER);
        cordova.getActivity().registerReceiver(mBleReceiver, new IntentFilter(BLE_INTENT_FILTER));
        bleStateRegistered = true;

        CONTENT_INTENT_FILTER = cordova.getActivity().getPackageName() + ".content";
        Log.d("Ble","initialize content intent filter = " + CONTENT_INTENT_FILTER);
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
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (action.equals("initSDK")) {
                        beaconManager.setDebugMode(LoggingStrategy.DEBUG);
                        beaconManager.setAPIEndpoint("https://connect.onyxbeacon.com");
                        beaconManager.setCouponEnabled(true);
                        beaconManager.setAPIContentEnabled(true);
                        beaconManager.enableGeofencing(true);
                        beaconManager.setLocationTrackingEnabled(true);
                        String clientId = args.getString(0);
                        String secret = args.getString(1);
                        AuthData authData = new AuthData();
                        authData.setAuthenticationMode(AuthenticationMode.CLIENT_SECRET_BASED);
                        authData.setSecret(secret);
                        authData.setClientId(clientId);
                        beaconManager.setAuthData(authData);
                        if (BluetoothAdapter.getDefaultAdapter() == null) {
                            Log.e(TAG, "Device does not support Bluetooth");
                        } else {
                            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                                Log.e(TAG, "Please turn on bluetooth");
                            } else {
                                // Enable scanner in foreground mode and register receiver
                                beaconManager.setForegroundMode(true);
                            }
                        }
                        callbackContext.success("Success");
                    } else if (action.equals("isBluetoothAvailable")) {
                        // TODO-Deprecated
//                        Boolean isBluetooth = beaconManager.isBluetoothAvailable();
//                        callbackContext.success(isBluetooth ? 1 : 0);
                        callbackContext.success("Deprecated");
                    } else if (action.equals("enableBluetooth")) {
                        // TODO-Deprecated
//                        beaconManager.enableBluetooth();
//                        callbackContext.success("Success");
                        callbackContext.success("Deprecated");
                    } else if (action.equals("getTags")) {
                        beaconManager.getTags();
                        callbackContext.success("Success");
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
                    } else if (action.equals("isCouponEnabled")) {
                        // TODO-Deprecated
//                        Boolean isCouponEnabled = beaconManager.isCouponEnabled();
//                        callbackContext.success(isCouponEnabled ? 1 : 0);
                        callbackContext.success("Deprecated");
                    } else if (action.equals("isAPIEnabled")) {
                        // TODO-Deprecated
//                        Boolean isAPIEnabled = beaconManager.isAPIEnabled();
//                        callbackContext.success(isAPIEnabled ? 1 : 0);
                        callbackContext.success("Deprecated");
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
                    } else if (action.equals("setAuthExtraData")) {
                        // TODO-Deprecated
//                        beaconManager.setAuthExtraData(args.getString(0));
//                        callbackContext.success("Success");
                        callbackContext.success("Deprecated");
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
                            mContentReceiver = ContentReceiver.getInstance(instance);
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
        /* Unknown methods */
                    else if (action.equals("sendReport")) {
                        String reporter = args.getString(0);
                        beaconManager.sendLogs(reporter);
                        callbackContext.success("sendReport Invoked");
                    } else {
                        Log.e("Ble","Unknown action " + action);
                        sendfalse = true;
                    }
                } catch (JSONException e) {
                    callbackContext.success(e.getMessage());
                    Log.e("Ble","Unknown JSONException " + e.getMessage());

                    sendfalse = true;
                }
            }

        });

        return !sendfalse;
    }

    public static HashMap<String, Object> jsonToMap(JSONObject json) throws JSONException {
        HashMap<String, Object> retMap = new HashMap<String, Object>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static HashMap<String, Object> toMap(JSONObject object) throws JSONException {
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

    public static List<Object> toList(JSONArray array) throws JSONException {
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

    public void onError(int errorCode, Exception e) {
        String js = String.format(
                "window.cordova.plugins.Ble.onyxBeaconError('%d:%s');",
                errorCode,e.getMessage());
        webView.loadUrl("javascript:"+js);
    }


    public void onTagsReceived(String tags) {
        String js = String.format(
                "window.cordova.plugins.Ble.onTagsReceived('%s');",
                tags);
        webView.loadUrl("javascript:"+js);
    }



    public void didRangeBeaconsInRegion( String  beacons) {
        String js = String.format(
                "window.cordova.plugins.Ble.didRangeBeaconsInRegion('%s');",
                beacons);
        webView.loadUrl("javascript:"+js);
    }


    public void deleteCoupon( long  var1,int var2) {
        String js = String.format(
                "window.cordova.plugins.Ble.deleteCoupon('%d,%d');",
                var1,var2);
        webView.loadUrl("javascript:"+js);
    }

    public void markAsTapped( long  var1) {
        String js = String.format(
                "window.cordova.plugins.Ble.markAsTapped('%d');",
                var1);
        webView.loadUrl("javascript:"+js);
    }

    public void markAsOpened( long  var1) {
        String js = String.format(
                "window.cordova.plugins.Ble.markAsOpened('%d');",
                var1);
        webView.loadUrl("javascript:"+js);
    }


    public void onCouponsReceived(String coupons, String  beacon) {
        String js = String.format(
                "window.cordova.plugins.Ble.onCouponsReceived('%s,%s');",
                coupons,beacon);
        webView.loadUrl("javascript:"+js);
    }

    public void onBluemixCredentialsReceived( String blueMix ) {
        String js = String.format(
                "window.cordova.plugins.Ble.onBluemixCredentialsReceived('%s');",
                blueMix);
        webView.loadUrl("javascript:"+js);
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



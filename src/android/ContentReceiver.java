package com.cordova.ble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.onyxbeacon.OnyxBeaconApplication;
import com.onyxbeacon.rest.model.account.BluemixApp;
import com.onyxbeacon.rest.model.content.Coupon;
import com.onyxbeacon.rest.model.content.Tag;
import com.onyxbeaconservice.IBeacon;

import java.util.ArrayList;

/**
 * Created by Work 2 on 4/2/2015.
 */
public class ContentReceiver extends BroadcastReceiver {


    private static ContentReceiver sInstance;
    static Ble blePlugin;
    private Gson gson = new Gson();

    public ContentReceiver() {}

    public static ContentReceiver getInstance(Ble bp) {
        if(blePlugin == null){
            blePlugin = bp;
        }
        if (sInstance == null) {
            sInstance = new ContentReceiver();
            return sInstance;
        } else {
            return sInstance;
        }
    }

    public void onReceive(Context context, Intent intent) {
        String payloadType = intent.getStringExtra(OnyxBeaconApplication.PAYLOAD_TYPE);
        Log.d("ContentReceiver","onReceive " + payloadType);

        if (payloadType.equals(OnyxBeaconApplication.TAG_TYPE)) {
            ArrayList<Tag> tagsList = intent.getParcelableArrayListExtra(OnyxBeaconApplication.EXTRA_TAGS);
            blePlugin.onTagsReceived(gson.toJson(tagsList));
        }
        else if (payloadType.equals(OnyxBeaconApplication.BEACON_TYPE)) {
                ArrayList<IBeacon> beacons = intent.getParcelableArrayListExtra(OnyxBeaconApplication.EXTRA_BEACONS);
                blePlugin.didRangeBeaconsInRegion(gson.toJson(beacons));
        }
        else if (payloadType.equals(OnyxBeaconApplication.COUPON_TYPE)) {
            ArrayList<Coupon> coupons = intent.getParcelableArrayListExtra(OnyxBeaconApplication.EXTRA_COUPONS);
            IBeacon beacon = intent.getParcelableExtra(OnyxBeaconApplication.EXTRA_BEACON);
            blePlugin.onCouponsReceived(gson.toJson(coupons), gson.toJson(beacon));
        }
        else if (payloadType.equals(OnyxBeaconApplication.PUSH_TYPE)) {
            BluemixApp bluemixApp = intent.getParcelableExtra(OnyxBeaconApplication.EXTRA_BLUEMIX);
            Log.d("ContentReceiver","PUSH Received bluemix credentials " + gson.toJson(bluemixApp));
            blePlugin.onBluemixCredentialsReceived(gson.toJson(bluemixApp));

        }
        else if (payloadType.equals(OnyxBeaconApplication.WEB_REQUEST_TYPE)) {
            String extraInfo = intent.getStringExtra(OnyxBeaconApplication.EXTRA_INFO);
            Log.d("ContentReceiver","AUTH Web reguest info " + extraInfo);
            if (extraInfo.equals(OnyxBeaconApplication.REQUEST_UNAUTHORIZED)) {
                // Pin based session expired
            }
        }
    }
}

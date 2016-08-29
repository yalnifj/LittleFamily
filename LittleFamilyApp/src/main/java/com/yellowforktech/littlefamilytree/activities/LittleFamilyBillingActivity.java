package com.yellowforktech.littlefamilytree.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import java.util.ArrayList;

/**
 * Created by jfinlay on 8/29/2016.
 */
public class LittleFamilyBillingActivity extends LittleFamilyActivity {
    private IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    public void queryItems() {
        Runnable offMain = new Runnable() {
            @Override
            public void run() {
                ArrayList<String> skuList = new ArrayList<String>();
                skuList.add("premiumUpgrade");
                Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

                Bundle skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);
            }
        };

        Thread thread = new Thread(offMain);
        thread.start();
    }
}

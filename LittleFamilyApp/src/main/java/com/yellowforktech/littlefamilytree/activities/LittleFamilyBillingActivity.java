package com.yellowforktech.littlefamilytree.activities;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.db.FireHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jfinlay on 8/29/2016.
 */
public class LittleFamilyBillingActivity extends LittleFamilyActivity {
    protected final String SKU_PREMIUM = "LFTPremium";
    protected IInAppBillingService mService;

    private ServiceConnection mServiceConn;
    private boolean connecting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mService != null) {
            unbindService(mServiceConn);
        }
        mService = null;
    }

    public void connectToStore() {
        connecting = true;
        mServiceConn = new ServiceConnection() {
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

        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    //alert("You have bought the " + sku + ". Excellent choice,
                    //        adventurer!");
                    if (sku != null && sku.equals(SKU_PREMIUM)) {
                        this.hasPremium = true;
                        DataService.getInstance().getDBHelper().saveProperty(LittleFamilyActivity.PROP_HAS_PREMIUM, "true");
                        String username = DataService.getInstance().getDBHelper().getProperty(DataService.SERVICE_USERNAME);
                        String serviceType = PreferenceManager.getDefaultSharedPreferences(this).getString(DataService.SERVICE_TYPE, null);
                        if (username!=null && serviceType!=null) {
                            FireHelper.getInstance().createOrUpdateUser(username, serviceType, true);
                        }
                        return;
                    }
                }
                catch (Exception e) {
                    //alert("Failed to parse purchase data.");
                    Log.e(this.getClass().getName().toString(), "Error reading products from store", e);
                    finish();
                }
            }
            finish();
        }
    }

    public void queryItems() {
        Runnable offMain = new Runnable() {
            @Override
            public void run() {
                waitForConnect();
                ArrayList<String> skuList = new ArrayList<String>();
                skuList.add(SKU_PREMIUM);
                Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

                Bundle skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);

                int response = skuDetails.getInt("RESPONSE_CODE");
                if (response == 0) {
                    ArrayList<String> responseList
                            = skuDetails.getStringArrayList("DETAILS_LIST");

                    for (String thisResponse : responseList) {
                        try {
                            JSONObject object = new JSONObject(thisResponse);
                            String sku = object.getString("productId");
                            String price = object.getString("price");
                            //if (sku.equals("premiumUpgrade")) mPremiumUpgradePrice = price;
                            //else if (sku.equals("gas")) mGasPrice = price;
                        } catch (JSONException e) {
                            Log.e(this.getClass().getName().toString(), "Error reading products from store", e);
                        }
                    }
                }
            }
        };

        Thread thread = new Thread(offMain);
        thread.start();
    }

    public void buyUpgrade() {
        waitForConnect();
        Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                SKU_PREMIUM, "inapp", "");
        int response = buyIntentBundle.getInt("RESPONSE_CODE");
        if (response == 0) {
            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
            try {
                startIntentSenderForResult(pendingIntent.getIntentSender(),
                        1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                        Integer.valueOf(0));
            } catch (IntentSender.SendIntentException e) {
                Log.e(this.getClass().getName().toString(), "Error starting purchase intent", e);
            }
        }
    }

    protected void waitForConnect() {
        if (mService==null) {
            if (!connecting) {
                connectToStore();
            }
            while(mService==null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void checkPremium(String gameCode) {
        userHasPremium(premium -> {
            if (!premium) {
                int tries = getTries(gameCode);
                showBuyTryDialog(tries, new PremiumDialog.ActionListener() {
                    @Override
                    public void onBuy() {
                        hideBuyTryDialog();
                        buyUpgrade();
                    }

                    @Override
                    public void onTry() {
                        int newTries = tries-1;
                        try {
                            DataService.getInstance().getDBHelper().saveProperty(gameCode, String.valueOf(newTries));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        hideBuyTryDialog();
                    }

                    @Override
                    public void onClose() {
                        hideBuyTryDialog();
                        finish();
                    }
                });
            }
        });
    }
}

package com.yellowforktech.littlefamilytree.activities;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.UserDataResponse;
import com.android.vending.billing.IInAppBillingService;
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
    private boolean isAmazon = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PackageManager pkgManager = this.getPackageManager();
        String installerPackageName = pkgManager.getInstallerPackageName(this.getPackageName());
        if(installerPackageName!=null && installerPackageName.startsWith("com.amazon")) {
            isAmazon = true;
        }
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

        if (isAmazon) {

            Log.d(this.getClass().getName().toString(), "onCreate: registering PurchasingListener");

            PurchasingService.registerListener(this.getApplicationContext(), new LFTPurchasingListener());

            Log.d(this.getClass().getName().toString(), "IS_SANDBOX_MODE:" + PurchasingService.IS_SANDBOX_MODE);
        } else {
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
                        grantPremium();
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

    private void grantPremium() {
        this.hasPremium = true;
        try {
            DataService.getInstance().getDBHelper().saveProperty(LittleFamilyActivity.PROP_HAS_PREMIUM, "true");
            String username = DataService.getInstance().getDBHelper().getProperty(DataService.SERVICE_USERNAME);
            String serviceType = DataService.getInstance().getServiceType();
            if (username != null && serviceType != null) {
                FireHelper.getInstance().createOrUpdateUser(username, serviceType, true);
            }
        } catch (Exception e) {
            Log.e(this.getClass().getName().toString(), "Error reading products from store", e);
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

                try {
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
                } catch (RemoteException e) {
                    Log.e(this.getClass().getName().toString(), "Error reading products from store", e);
                }
            }
        };

        Thread thread = new Thread(offMain);
        thread.start();
    }

    public void buyUpgrade() {
        if(isAmazon) {
            // Amazon
            PurchasingService.purchase("LFTPremium");
        } else {
            // Google Play
            waitForConnect();
            try {
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
            } catch (RemoteException e) {
                Log.e(this.getClass().getName().toString(), "Error purchasing upgrade from store", e);
            }
        }
    }

    protected void waitForConnect() {
        if (isAmazon) {
            connectToStore();
            return;
        }
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

    public void checkPremium(final String gameCode) {
        userHasPremium(new FireHelper.PremiumListener() {
            @Override
            public void results(boolean premium) {
                if (!premium) {
                    final int tries = getTries(gameCode);
                    showBuyTryDialog(tries, new PremiumDialog.ActionListener() {
                        @Override
                        public void onBuy() {
                            hideBuyTryDialog();
                            showLoadingDialog();
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
            }
        });
    }

    class LFTPurchasingListener implements PurchasingListener {
        @Override
        public void onUserDataResponse(UserDataResponse userDataResponse) {

        }

        @Override
        public void onProductDataResponse(ProductDataResponse productDataResponse) {

        }

        @Override
        public void onPurchaseResponse(PurchaseResponse purchaseResponse) {
            switch (purchaseResponse.getRequestStatus()) {
                case ALREADY_PURCHASED:
                case SUCCESSFUL:
                    grantPremium();
                    break;
                default:
                    finish();
                    Toast.makeText(LittleFamilyBillingActivity.this, "Unable to complete purchase", Toast.LENGTH_LONG);
                    break;
            }
        }

        @Override
        public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse purchaseUpdatesResponse) {

        }
    }
}

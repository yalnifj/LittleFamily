package com.yellowforktech.littlefamilytree.activities;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserDataResponse;
import com.android.vending.billing.IInAppBillingService;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.db.FireHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jfinlay on 8/29/2016.
 */
public class LittleFamilyBillingActivity extends LittleFamilyActivity {
    protected final String SKU_PREMIUM = "lftpremium";
    protected final String AMAZON_PREMIUM = "LFTPremium";
    protected IInAppBillingService mService;

    private ServiceConnection mServiceConn;
    private LFTPurchasingListener amazonListener;
    private boolean connecting;
    protected boolean isAmazon = false;
    protected boolean noBilling = false;

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

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        connectToStore();
    }

    public void connectToStore() {
        connecting = true;

        if (isAmazon) {
            if (amazonListener!=null) {
                connecting = false;
                return;
            }

            Log.d(this.getClass().getName().toString(), "onCreate: registering PurchasingListener");

            amazonListener = new LFTPurchasingListener();
            PurchasingService.registerListener(this.getApplicationContext(), amazonListener);

            Log.d(this.getClass().getName().toString(), "IS_SANDBOX_MODE:" + PurchasingService.IS_SANDBOX_MODE);
            connecting = false;
        } else {
            if (mService!=null) {
                connecting = false;
                return;
            }

            mServiceConn = new ServiceConnection() {
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.w(getClass().getName(), "Disconnected from billing service");
                    mService = null;
                    connecting = false;
                }

                @Override
                public void onServiceConnected(ComponentName name,
                                               IBinder service) {
                    Log.w(getClass().getName(), "Connected to billing service "+service);
                    mService = IInAppBillingService.Stub.asInterface(service);
                    connecting = false;
                }
            };

            Intent serviceIntent =
                    new Intent("com.android.vending.billing.InAppBillingService.BIND");
            serviceIntent.setPackage("com.android.vending");
            List<ResolveInfo> intentServices = getPackageManager().queryIntentServices(serviceIntent, 0);
            if (intentServices != null && !intentServices.isEmpty()) {
                bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
            } else {
                Log.e(this.getClass().getName(), "No billing services availabe on device.");
                noBilling = true;
            }
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
                    hideLoadingDialog();
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
        if (isAmazon) {
            waitForConnect();
            final Set<String> productSkus =  new HashSet<>();
            productSkus.add( "com.amazon.example.iap.entitlement" );
            PurchasingService.getProductData(productSkus);
        } else {
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

                                    Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                                            sku, "inapp", "");
                                    int response2 = buyIntentBundle.getInt("RESPONSE_CODE");
                                    if (response2 == 0) {
                                        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                                        try {
                                            startIntentSenderForResult(pendingIntent.getIntentSender(),
                                                    1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                                                    Integer.valueOf(0));
                                        } catch (IntentSender.SendIntentException e) {
                                            Log.e(this.getClass().getName().toString(), "Error starting purchase intent", e);
                                        }
                                    }

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
    }

    public void buyUpgrade() {
        waitForConnect();
        showLoadingDialog();
        queryItems();
    }

    public void restorePurchases() {
        waitForConnect();
        if (isAmazon) {
            PurchasingService.getPurchaseUpdates(false);
        } else {
            try {
                if (mService != null) {
                    Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                    int response = ownedItems.getInt("RESPONSE_CODE");
                    if (response == 0) {
                        ArrayList<String> ownedSkus =
                                ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                        ArrayList<String> purchaseDataList =
                                ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                        ArrayList<String> signatureList =
                                ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                        //String continuationToken =
                        //        ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                        boolean found = false;
                        for (int i = 0; i < purchaseDataList.size(); ++i) {
                            String purchaseData = purchaseDataList.get(i);
                            String signature = signatureList.get(i);
                            String sku = ownedSkus.get(i);

                            if (sku != null && sku.equals(SKU_PREMIUM)) {
                                try {
                                    grantPremium();
                                } catch (Exception e) {
                                    Log.e(this.getClass().getName().toString(), "Error starting purchase intent", e);
                                }
                            }

                            // do something with this purchase information
                            // e.g. display the updated list of products owned by user
                        }

                        // if continuationToken != null, call getPurchases again
                        // and pass in the token to retrieve more items
                    }
                }
            } catch (RemoteException e) {
                Log.e(this.getClass().getName().toString(), "Error starting purchase intent", e);
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
                            showAdultAuthDialog(new AdultsAuthDialog.AuthCompleteAction() {
                                @Override
                                public void doAction(boolean success) {
                                    hideAdultAuthDialog();
                                    buyUpgrade();
                                }

                                public void onClose() {
                                    finish();
                                }
                            });
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
            switch (productDataResponse.getRequestStatus()) {
                case SUCCESSFUL:
                    for (final String s : productDataResponse.getUnavailableSkus()) {
                        Log.v(getClass().getName(), "Unavailable SKU:" + s);
                    }

                    final Map<String, Product> products = productDataResponse.getProductData();
                    for (final String key : products.keySet()) {
                        Product product = products.get(key);
                        Log.v(getClass().getName(), String.format("Product: %s\n Type: %s\n SKU: %s\n Price: %s\n Description: %s\n", product.getTitle(), product.getProductType(), product.getSku(), product.getPrice(), product.getDescription()));
                        if (product.getSku().equals(AMAZON_PREMIUM)) {
                            PurchasingService.purchase(AMAZON_PREMIUM);
                        }
                    }
                    break;

                case FAILED:
                    Log.v(getClass().getName(), "ProductDataRequestStatus: FAILED");
                    break;
            }
        }

        @Override
        public void onPurchaseResponse(PurchaseResponse purchaseResponse) {
            switch (purchaseResponse.getRequestStatus()) {
                case ALREADY_PURCHASED:
                case SUCCESSFUL:
                    final Receipt receipt = purchaseResponse.getReceipt();
                    if (!receipt.isCanceled() && receipt.getSku().equals(AMAZON_PREMIUM)) {
                        hideLoadingDialog();
                        PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);
                        grantPremium();
                    }
                    break;
                default:
                    finish();
                    Toast.makeText(LittleFamilyBillingActivity.this, "Unable to complete purchase", Toast.LENGTH_LONG);
                    break;
            }
        }

        @Override
        public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse purchaseUpdatesResponse) {
            switch (purchaseUpdatesResponse.getRequestStatus()) {
                case SUCCESSFUL:
                    for ( final Receipt receipt : purchaseUpdatesResponse.getReceipts()) {
                        if (receipt.getSku().equals(AMAZON_PREMIUM)) {
                            grantPremium();
                        }
                    }
                    break;
                default:
                    finish();
                    Toast.makeText(LittleFamilyBillingActivity.this, "Unable to restore purchases", Toast.LENGTH_LONG);
                    break;
            }
        }
    }
}

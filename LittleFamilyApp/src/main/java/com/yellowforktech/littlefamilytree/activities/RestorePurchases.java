package com.yellowforktech.littlefamilytree.activities;

import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amazon.device.iap.PurchasingService;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.tasks.WaitTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.db.FireHelper;

import java.util.ArrayList;

public class RestorePurchases extends LittleFamilyBillingActivity {

    private TextView statusText;
    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_purchases);

        spinner = (ProgressBar) findViewById(R.id.spinner);
        statusText = (TextView) findViewById(R.id.status);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bundle logBundle = new Bundle();
        logBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, getLocalClassName());
        FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.VIEW_ITEM, logBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();

        WaitTask task = new WaitTask(new WaitTask.WaitTaskListener() {
            @Override
            public void onProgressUpdate(Integer progress) {
            }

            @Override
            public void onComplete(Integer progress) {
                restorePurchases();
            }
        });
        task.execute(3000L);
    }

    public void restorePurchases() {
        Log.d(this.getClass().getSimpleName(), "Starting restore purchases ");
        waitForConnect();
        try {
            if (isAmazon) {
                PurchasingService.getPurchaseUpdates(false);
            } else {
                Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                int response = ownedItems.getInt("RESPONSE_CODE");
                Log.d(this.getClass().getSimpleName(), "Received response code "+response);
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
                    Log.d(this.getClass().getSimpleName(), "Found "+purchaseDataList.size()+" purchases.");
                    for (int i = 0; i < purchaseDataList.size(); ++i) {
                        String purchaseData = purchaseDataList.get(i);
                        String signature = signatureList.get(i);
                        String sku = ownedSkus.get(i);

                        Log.d(this.getClass().getSimpleName(), "Found SKU "+sku);
                        if (sku != null && sku.equals(SKU_PREMIUM)) {
                            try {
                                String premStr = DataService.getInstance().getDBHelper().getProperty(LittleFamilyActivity.PROP_HAS_PREMIUM);
                                if (premStr == null || !premStr.equals("true")) {
                                    DataService.getInstance().getDBHelper().saveProperty(LittleFamilyActivity.PROP_HAS_PREMIUM, "true");
                                    String username = DataService.getInstance().getDBHelper().getProperty(DataService.SERVICE_USERNAME);
                                    String serviceType = PreferenceManager.getDefaultSharedPreferences(this).getString(DataService.SERVICE_TYPE, null);
                                    if (username != null && serviceType != null) {
                                        FireHelper.getInstance().createOrUpdateUser(username, serviceType, true);
                                    }
                                    found = true;
                                }
                                statusText.setText(getResources().getString(R.string.purchases_restore));
                            } catch (Exception e) {
                                Log.e(this.getClass().getName().toString(), "Error starting purchase intent", e);
                            }
                        }

                        // do something with this purchase information
                        // e.g. display the updated list of products owned by user
                    }

                    if (!found) {
                        statusText.setText(getResources().getString(R.string.purchase_not_found));
                    }
                    spinner.setVisibility(View.GONE);

                    // if continuationToken != null, call getPurchases again
                    // and pass in the token to retrieve more items
                }
            }
        } catch (RemoteException e) {
            Log.e(this.getClass().getName().toString(), "Error starting purchase intent", e);
        }
    }
}

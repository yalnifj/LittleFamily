package com.yellowforktech.littlefamilytree.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.adapters.SkinListAdapter;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

import java.util.HashMap;

public class NewSettingsActivity extends Activity {

    private TextView remoteTreeTypeTxt;
    private CheckBox chkBackgroundSync;
    private CheckBox chkCellularSync;
    private TextView txtSyncDelay;
    private CheckBox chkNotifications;
    private CheckBox chkShowStepChildren;
    private CheckBox chkQuietMode;
    private TextView txtVersion;
    private ImageView imgSkinView;

    private LittlePerson selectedPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        selectedPerson = (com.yellowforktech.littlefamilytree.data.LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);

        setContentView(R.layout.activity_new_settings);

        remoteTreeTypeTxt = (TextView) findViewById(R.id.remoteTreeTypeTxt);
        String serviceType = PreferenceManager.getDefaultSharedPreferences(this).getString(DataService.SERVICE_TYPE, "");
        remoteTreeTypeTxt.setText(serviceType);

        chkBackgroundSync = (CheckBox) findViewById(R.id.chkBackgroundSync);
        Boolean syncBackground = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("sync_background", true);
        chkBackgroundSync.setChecked(syncBackground);

        chkCellularSync = (CheckBox) findViewById(R.id.chkCellularSync);
        Boolean syncCell = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("sync_cellular", false);
        chkCellularSync.setChecked(syncCell);

        txtSyncDelay = (TextView) findViewById(R.id.txtSyncDelay);
        String syncDelayStr = PreferenceManager.getDefaultSharedPreferences(this).getString("sync_delay", "1");
        String[] syncTitles = getResources().getStringArray(R.array.pref_sync_frequency_titles);
        String[] syncValues = getResources().getStringArray(R.array.pref_sync_frequency_values);
        HashMap<String, String> valueMap = new HashMap<>();
        for(int i=0; i<syncTitles.length; i++) {
            valueMap.put(syncValues[i], syncTitles[i]);
        }
        String syncDelayTitle = valueMap.get(syncDelayStr);
        txtSyncDelay.setText(syncDelayTitle);

        chkNotifications = (CheckBox) findViewById(R.id.chkNotifications);
        Boolean enableNotifications = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("enable_notifications", true);
        chkNotifications.setChecked(enableNotifications);

        chkShowStepChildren = (CheckBox) findViewById(R.id.chkShowStepChildren);
        Boolean showStepChildren = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("show_step_children", true);
        chkShowStepChildren.setChecked(showStepChildren);

        chkQuietMode = (CheckBox) findViewById(R.id.chkQuietMode);
        Boolean quietMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("quiet_mode", false);
        chkQuietMode.setChecked(quietMode);

        imgSkinView = (ImageView) findViewById(R.id.skinView);
        String skinColor = PreferenceManager.getDefaultSharedPreferences(this).getString("skin_color", "light");
        int skinViewResourceId = R.drawable.boy;
        switch (skinColor) {
            case "mid":
                skinViewResourceId = R.drawable.boy_mid;
                break;
            case "dark":
                skinViewResourceId = R.drawable.boy_dark;
                break;
        }
        imgSkinView.setImageResource(skinViewResourceId);


        txtVersion = (TextView) findViewById(R.id.txtVersion);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            String premStr = DataService.getInstance().getDBHelper().getProperty(LittleFamilyActivity.PROP_HAS_PREMIUM);
            if ("true".equals(premStr)) {
                versionName += " Premium";
            }
            txtVersion.setText(versionName);

            Bundle logBundle = new Bundle();
            logBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, getLocalClassName());
            logBundle.putString("Version", versionName);
            FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.VIEW_ITEM, logBundle);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Error getting version number", e);
        }
    }

    public void showManagePeople(View view) {
        Intent intent = new Intent( this, PersonSearchActivity.class );
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void showParentsGuide(View view) {
        Intent intent = new Intent( this, ParentsGuideActivity.class );
        startActivity(intent);
    }

    public void showWebsite(View view) {
        Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse("http://www.littlefamilytree.com"));
        startActivity(intent);
    }

    public void onSyncBackground(View view) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean("sync_background", chkBackgroundSync.isChecked());
        editor.commit();
    }

    public void onSyncCellular(View view) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean("sync_cellular", chkCellularSync.isChecked());
        editor.commit();
    }

    public void onEnableNotifications(View view) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean("enable_notifications", chkNotifications.isChecked());
        editor.commit();
    }

    public void onShowStepChildren(View view) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean("show_step_children", chkShowStepChildren.isChecked());
        editor.commit();
    }

    public void onQuietMode(View view) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean("quiet_mode", chkQuietMode.isChecked());
        editor.commit();
    }

    public void onChooseSync(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pref_title_sync_delay)
                .setItems(R.array.pref_sync_frequency_titles, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        String[] values = getResources().getStringArray(R.array.pref_sync_frequency_values);
                        String value = values[which];
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(NewSettingsActivity.this).edit();
                        editor.putString("sync_delay", value);
                        editor.commit();

                        String[] titles = getResources().getStringArray(R.array.pref_sync_frequency_titles);
                        String title = titles[which];
                        txtSyncDelay.setText(title);

                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onChooseSkin(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final ListAdapter adapter = new SkinListAdapter(this);
        builder.setTitle(R.string.pref_title_skin_color)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String skinColor = (String) adapter.getItem(which);
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(NewSettingsActivity.this).edit();
                        editor.putString("skin_color", skinColor);
                        editor.commit();

                        int skinViewResourceId = R.drawable.boy;
                        switch (skinColor) {
                            case "mid":
                                skinViewResourceId = R.drawable.boy_mid;
                                break;
                            case "dark":
                                skinViewResourceId = R.drawable.boy_dark;
                                break;
                        }
                        imgSkinView.setImageResource(skinViewResourceId);

                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}

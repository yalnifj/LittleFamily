package com.yellowforktech.littlefamilytree.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

/**
 * Created by jfinlay on 4/28/2016.
 */
public class AutoStartNotifyReceiver extends BroadcastReceiver {
    private final String BOOT_COMPLETED_ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(BOOT_COMPLETED_ACTION)){
            Boolean enableNotifications = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enable_notifications", true);
            if (enableNotifications) {
                Intent myIntent = new Intent(context, LittleFamilyNotificationService.class);
                context.startService(myIntent);
            }
        }

    }

}

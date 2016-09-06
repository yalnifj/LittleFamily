package com.yellowforktech.littlefamilytree.events;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by jfinlay on 4/28/2016.
 */
public class AutoStartNotifyReceiver extends BroadcastReceiver {
    private final String BOOT_COMPLETED_ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(BOOT_COMPLETED_ACTION)){
            scheduleAlarms(context);
        }
    }

    public static void scheduleAlarms(Context context) {
        Intent myIntent = new Intent(context, LittleFamilyNotificationService.class);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, myIntent, 0);
        alarmManager.cancel(pendingIntent);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 00);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000 , pendingIntent);
        Log.d("AutoStartNotifyReceiver", "scheduleAlarms: ");
    }

    public static void cancelAlarms(Context context) {
        Intent myIntent = new Intent(context, LittleFamilyNotificationService.class);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, myIntent, 0);
        alarmManager.cancel(pendingIntent);
        Log.d("AutoStartNotifyReceiver", "cancelAlarms: ");
    }

}

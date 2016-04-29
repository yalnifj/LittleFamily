package com.yellowforktech.littlefamilytree.events;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.BirthdayCardActivity;
import com.yellowforktech.littlefamilytree.activities.SplashActivity;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.db.DBHelper;

import java.util.Calendar;
import java.util.List;

public class LittleFamilyNotificationService extends IntentService {
    private DBHelper dbHelper = null;
    NotificationManager mNotificationManager;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public LittleFamilyNotificationService() {
        super("LittleFamilyNotificationService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Boolean enableNotifications = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("enable_notifications", true);
        if (!enableNotifications) {
            return;
        }

        if (dbHelper==null) {
            dbHelper = new DBHelper(this);
        }

        if (mNotificationManager == null) {
            mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        Log.d("NotificationService", "onStartCommand: ");

        Calendar now = Calendar.getInstance();
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DATE);

        List<LittlePerson> people = dbHelper.getNextBirthdays(10, 4);
        if (people!=null) {
            int count = 0;
            for(LittlePerson person : people) {
                Calendar birthCal = Calendar.getInstance();
                birthCal.setTime(person.getBirthDate());
                birthCal.set(Calendar.YEAR, now.get(Calendar.YEAR));

                Log.d("NotificationService", "Checking birthdate for "+person.getName() + " "+person.getBirthDate());

                if ((birthCal.get(Calendar.DATE)==day && birthCal.get(Calendar.MONTH)==month)) {
                    //-- create notification
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(LittleFamilyNotificationService.this)
                            .setSmallIcon(R.drawable.logo)
                            .setContentTitle("Today is "+person.getName()+"'s birthday!")
                            .setContentText("Decorate a birthday card in Little Family Tree for "+person.getGivenName());
                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    mBuilder.setSound(alarmSound);
                    Intent resultIntent = new Intent(LittleFamilyNotificationService.this, BirthdayCardActivity.class);
                    resultIntent.putExtra(BirthdayCardActivity.BIRTHDAY_PERSON, person);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(LittleFamilyNotificationService.this);
                    stackBuilder.addParentStack(SplashActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(pendingIntent);
                    mBuilder.setDefaults(Notification.DEFAULT_ALL);
                    mBuilder.setAutoCancel(true);

                    mNotificationManager.notify(person.getId(), mBuilder.build());
                }
                count++;
            }
        }
    }
}

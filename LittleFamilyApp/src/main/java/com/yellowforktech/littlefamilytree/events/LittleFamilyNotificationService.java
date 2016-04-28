package com.yellowforktech.littlefamilytree.events;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.SplashActivity;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.db.DBHelper;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LittleFamilyNotificationService extends Service {
    public final static String ACTION = "NotifyServiceAction";
    public final static int RQS_STOP_SERVICE = 1;

    NotifyServiceReceiver notifyServiceReceiver;
    Timer timer;
    BirthdayTimer timerTask;
    private DBHelper dbHelper = null;
    NotificationManager mNotificationManager;


    public LittleFamilyNotificationService() {
    }

    @Override
    public void onCreate() {
        notifyServiceReceiver = new NotifyServiceReceiver();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(notifyServiceReceiver, intentFilter);

        if (dbHelper==null) {
            dbHelper = new DBHelper(this);
        }

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        timer = new Timer();
        timerTask = new BirthdayTimer();
        timer.schedule(timerTask, 1000, 10*60*1000);

        Log.d("NotificationService", "onStartCommand: service started");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(notifyServiceReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class NotifyServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            int rqs = arg1.getIntExtra("RQS", 0);
            if (rqs == RQS_STOP_SERVICE){
                stopSelf();
            }
        }
    }

    public class BirthdayTimer extends TimerTask {

        @Override
        public void run() {
            Log.d("BirthdayTimer", "timer started");

            Calendar now = Calendar.getInstance();
            int month = now.get(Calendar.MONTH);
            int day = now.get(Calendar.DATE);

            List<LittlePerson> people = dbHelper.getNextBirthdays(10, 4);
            if (people!=null) {
                for(LittlePerson person : people) {
                    Calendar birthCal = Calendar.getInstance();
                    birthCal.setTime(person.getBirthDate());
                    birthCal.set(Calendar.YEAR, now.get(Calendar.YEAR));

                    if (birthCal.get(Calendar.DATE)==day && birthCal.get(Calendar.MONTH)==month) {
                        //-- create notification
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(LittleFamilyNotificationService.this)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Today is "+person.getName()+"'s birthday!")
                                .setContentText("Decorate a birthday card for "+person.getGivenName());
                        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        mBuilder.setSound(alarmSound);
                        Intent resultIntent = new Intent(LittleFamilyNotificationService.this, SplashActivity.class);
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(LittleFamilyNotificationService.this);
                        stackBuilder.addParentStack(SplashActivity.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(pendingIntent);
                        mBuilder.setDefaults(Notification.DEFAULT_ALL);

                        mNotificationManager.notify(person.getId(), mBuilder.build());
                    }
                }
            }
        }
    }
}

package com.jeevesandroid.actions;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.jeevesandroid.ApplicationContext;
import com.jeevesandroid.R;

import java.util.Map;

/**
 * Created by Daniel on 26/05/15.
 */
public class PromptAction extends FirebaseAction {

    public PromptAction(Map<String,Object> params){
        setparams(params);
    }
    private static int count = 0;
    @Override
    public void execute(){
        int notificationId = Integer.parseInt("8" + count++);
        Context app = ApplicationContext.getContext();
        if(!getparams().containsKey("msgtext"))return;
        String text = getparams().get("msgtext").toString();
        NotificationManager notificationManager =
                (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "default_channel_id";
        String channelDescription = "Default Channel";
        //Check if notification channel exists and if not create one
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);
            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelId, channelDescription, importance);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(app)
                .setVibrate(new long[]{0, 1000})
                .setSmallIcon(R.drawable.ic_action_search)
                .setContentTitle("Jeeves")
                .setChannelId(channelId)
                .setContentText(text);

        notificationManager.notify(notificationId,mBuilder.build());
        PowerManager pm = (PowerManager)ApplicationContext.getContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        Log.e("screen on....", ""+isScreenOn);
        if(!isScreenOn)
        {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"app:locktag");
            wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"app:cpulock");

            wl_cpu.acquire(10000);
        }
    }
}

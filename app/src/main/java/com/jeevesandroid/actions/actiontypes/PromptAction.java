package com.jeevesandroid.actions.actiontypes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.jeevesandroid.AppContext;
import com.jeevesandroid.R;

import java.util.Map;

/**
 * Class representing the action block for prompting the participant
 * (i.e., sending a notification for anything other than a survey to complete)
 */
public class PromptAction extends FirebaseAction {

    public PromptAction(Map<String,Object> params){
        setparams(params);
    }
    private static int count = 0;

    /**
     * The execution involves building a notification (with the message text specified
     * in the 'msgtext' action parameter) and displaying it to the user, ensuring insofar
     * as possible that it is noticed by vibrating if the user is not in silent mode, and
     * turning on the screen if it is off
     */
    @Override
    public void execute(){
        //First check we're not snoozing
        SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());
        if(prefs.getBoolean(AppContext.SNOOZE,false)){
            return;
        }
        int notificationId = Integer.parseInt("8" + count++);
        Context app = AppContext.getContext();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mBuilder.setSound(
                    RingtoneManager
                            .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            );
        }
        notificationManager.notify(notificationId,mBuilder.build());

        //Turn the screen on if it's not already.
        PowerManager pm = (PowerManager) AppContext.getContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if(!isScreenOn)
        {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"app:locktag");
            wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,"app:cpulock");

            wl_cpu.acquire(10000);
        }
    }
}

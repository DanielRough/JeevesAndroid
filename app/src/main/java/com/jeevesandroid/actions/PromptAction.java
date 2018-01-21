package com.jeevesandroid.actions;

import android.app.NotificationManager;
import android.content.Context;
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
    static int count = 0;
    @Override
    public boolean execute(){
        Log.d("GET HERE","DID I GET TO EXECUTE");
        int notificationId = Integer.parseInt("8" + count++);
        Context app = ApplicationContext.getContext();
        String text = getparams().get("msgtext").toString();
        NotificationManager notificationManager =
                (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(app)
                .setVibrate(new long[]{0, 1000})
                .setSmallIcon(R.drawable.ic_action_search)
                .setContentTitle("Jeeves")
                .setContentText(text);
        notificationManager.notify(notificationId,mBuilder.build());
        PowerManager pm = (PowerManager)ApplicationContext.getContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        Log.e("screen on....", ""+isScreenOn);
        if(isScreenOn==false)
        {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
            wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");

            wl_cpu.acquire(10000);
        }
        return true;
    }
}

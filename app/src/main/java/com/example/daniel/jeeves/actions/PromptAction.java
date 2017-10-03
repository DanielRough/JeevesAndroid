package com.example.daniel.jeeves.actions;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.R;

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
        return true;
    }
}

package com.example.daniel.jeeves.actions;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.R;

/**
 * Created by Daniel on 26/05/15.
 */
public class PromptAction extends FirebaseAction {

    static int count = 0;
    @Override
    public void execute(){
        Log.d("ACTIONPROMPT", "PROMPTED AN ACTON");
        Context app = ApplicationContext.getContext();
        String text = params.get("prompttext").toString();
        NotificationManager notificationManager =
                (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(app)
                .setVibrate(new long[]{0, 1000})
                .setSmallIcon(R.drawable.ic_action_search)
                .setContentText(text);
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Jeeves");
        mBuilder.setStyle(inboxStyle);
        notificationManager.notify(count++,mBuilder.build());
    }
}

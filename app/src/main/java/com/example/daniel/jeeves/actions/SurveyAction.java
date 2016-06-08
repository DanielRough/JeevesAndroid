package com.example.daniel.jeeves.actions;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.R;
import com.example.daniel.jeeves.SurveyActivity;


/**
 * Created by Daniel on 26/05/15.
 */
public class SurveyAction extends FirebaseAction {

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void execute(){
        Log.d("ACTIONSURVEY","SENT A SURVEY");
        Context app = ApplicationContext.getContext();

        String surveyname = params.get("survey").toString();
        NotificationManager notificationManager =
                (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(app)
                .setContentTitle("Survey")
                .setVibrate(new long[] {0,1000})
                .setSmallIcon(R.drawable.ic_action_search)
                .setContentText("Ready to take a survey?");
        Intent resultIntent = new Intent(app, SurveyActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(app);

        stackBuilder.addNextIntent(resultIntent);
        resultIntent.putExtra("name",surveyname);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("New Survey Available");
        mBuilder.setStyle(inboxStyle);
        notificationManager.notify(0,mBuilder.build());
    }
}

package com.example.daniel.jeeves.actions;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.DebugUtils;
import android.util.Log;

import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.R;
import com.example.daniel.jeeves.SurveyActivity;
import com.example.daniel.jeeves.firebase.FirebaseSurvey;
import com.firebase.client.Firebase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Daniel on 26/05/15.
 */
public class SurveyAction extends FirebaseAction {
    public static int NOTIFICATION_ID = 1;
    public static final String ACTION_1 = "action_1";
    public static final String ACTION_2 = "action_2";

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void execute() {
        Log.d("ACTIONSURVEY", "SENT A SURVEY");
        Context app = ApplicationContext.getContext();
        SharedPreferences prefs = app.getSharedPreferences("userprefs", Context.MODE_PRIVATE);
        String userid = prefs.getString("userid", "null");
        Firebase incompleteSurveys = new Firebase("https://incandescent-torch-8695.firebaseio.com/patients/" + userid + "/incomplete");
        HashMap<String, Object> surveyDetails = new HashMap<String, Object>();

        String surveyname = getparams().get("survey").toString();

        FirebaseSurvey currentsurvey = null;
        List<FirebaseSurvey> surveys = ApplicationContext.getProject().getsurveys();
        for (FirebaseSurvey survey : surveys) {
            Log.d("Here", "SURVEY NAME IS " + survey.getname());
            if (survey.getname().equals(surveyname)) {
                currentsurvey = survey;
                break;
            }
        }
        Firebase newPostRef = incompleteSurveys.push();
        newPostRef.setValue(currentsurvey); //Maybe this needs tobe made explicit?
        String newPostRefId = newPostRef.getKey();
        Log.d("REFID", "New postrefid is " + newPostRefId);
        Intent action1Intent = new Intent(app, NotificationActionService.class)
                .setAction(ACTION_1);

        action1Intent.putExtra("name", surveyname);
        action1Intent.putExtra("surveyid", newPostRefId);
        long timeSent = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dateString = formatter.format(new Date(timeSent));
        Log.d("CURRENT TIME ", dateString);
        action1Intent.putExtra("timeSent",timeSent);
        PendingIntent action1PendingIntent = PendingIntent.getService(app, 0,
                action1Intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent action2Intent = new Intent(app, NotificationActionService.class)
                .setAction(ACTION_2);

        PendingIntent action2PendingIntent = PendingIntent.getService(app, 0,
                action2Intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationManager notificationManager =
                (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(app)
                .setContentTitle("Survey")
                .setVibrate(new long[]{0, 1000})
                .setSmallIcon(R.drawable.ic_action_search)
                .setContentText("Ready to take a survey?");
        long timeAlive = currentsurvey.gettimeAlive()*1000;
        newPostRef.child("expiryTime").setValue(System.currentTimeMillis() + (timeAlive)); //Put the expiry time in
        newPostRef.child("timeSent").setValue(System.currentTimeMillis());
        mBuilder.setAutoCancel(true);
        mBuilder.setOngoing(true);
        mBuilder.addAction(R.drawable.ic_create_black_24dp, "Start survey", action1PendingIntent);
        mBuilder.addAction(R.drawable.ic_block_black_24dp, "Ignore", action2PendingIntent);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(app, AlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(app, 0, intent, 0);
        Log.d("STARTED","Set an alarm for " + timeAlive + "milliseconds time");
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+timeAlive, pi);


        //   firebaseSurvey.push().setValue(surveyDetails);
    }

//    public void setOnetimeTimer(Context context) {
//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//
//
//    }
//
    public static class AlarmManagerBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Context app = ApplicationContext.getContext();
            Log.d("HELLOOOOO","whyisthis");
            NotificationManager manager = (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);
            manager.cancel(NOTIFICATION_ID);
        }
    }
    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Context app = ApplicationContext.getContext();
            String action = intent.getAction();
            Log.d("WOOHOO","ACTION IS " + action);
            if (ACTION_1.equals(action)) {
                // TODO: handle action 1.
                NotificationManager manager = (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);
                manager.cancel(NOTIFICATION_ID);
                Intent resultIntent = new Intent(app, SurveyActivity.class);
                resultIntent.putExtra("surveyid",intent.getStringExtra("surveyid"));
                Log.d("SURVEY ID, ", intent.getStringExtra("surveyid"));
                resultIntent.putExtra("name",intent.getStringExtra("name"));
                resultIntent.putExtra("timeSent",intent.getLongExtra("timeSent",0));
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String dateString = formatter.format(new Date(intent.getLongExtra("timeSent",0)));
                Log.d("THIS SURVEY WAS ", dateString);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                this.startActivity(resultIntent);

                // If you want to cancel the notification: NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
            }
            if (ACTION_2.equals(action)) {
                NotificationManager manager = (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);
                manager.cancel(NOTIFICATION_ID);
            }
        }
    }
}

package com.example.daniel.jeeves.actions;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.R;
import com.example.daniel.jeeves.SurveyActivity;
import com.example.daniel.jeeves.firebase.FirebaseSurvey;
import com.example.daniel.jeeves.firebase.FirebaseUtils;
import com.google.firebase.database.DatabaseReference;
import com.ubhave.triggermanager.config.TriggerManagerConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Daniel on 26/05/15.
 */
public class SurveyAction extends FirebaseAction {

    public static final String ACTION_1 = "action_1";
    public static final String ACTION_2 = "action_2";
    public static int NOTIFICATION_ID = 0;
    public int thisActionsId = 0;
    private int triggertype;
    private BroadcastReceiver mReceiver;

    public SurveyAction(Map<String, Object> params) {
        setparams(params);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean execute() {
        thisActionsId = Integer.parseInt("9" + NOTIFICATION_ID++);
        final Context app = ApplicationContext.getContext();

        final String surveyname = getparams().get("survey").toString();

        FirebaseSurvey currentsurvey = null;
        List<FirebaseSurvey> surveys = ApplicationContext.getProject().getsurveys();
        for (FirebaseSurvey survey : surveys) {
            if (survey.gettitle().equals(surveyname)) {
                currentsurvey = survey;
                break;
            }
        }

        final long timeSent = System.currentTimeMillis();

        IntentFilter intentFilter = new IntentFilter(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER);
        //We need to keep the notification alive for as long as the survey isn't completed
        //Whenever this receiver gets a notification that our survey was completed, it cancels
        //the timeout alarm previously set
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                long completedtimesent = intent.getLongExtra("timeSent", 0);
                if (completedtimesent == timeSent) { //Then this survey was completed woohoo!
                    AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
                    Intent cancelIntent = new Intent(app, MissedSurveyReceiver.class);
                    cancelIntent.setType(timeSent+surveyname);
                    PendingIntent pi = PendingIntent.getBroadcast(app, 0, cancelIntent, 0);
                    am.cancel(pi);

                }
            }
        };
        app.registerReceiver(mReceiver, intentFilter);


        DatabaseReference myRef = FirebaseUtils.PATIENT_REF.child("incomplete");
        DatabaseReference newPostRef = myRef.push();
        currentsurvey.settimeSent(timeSent);
        newPostRef.setValue(currentsurvey);
        String newPostRefId = newPostRef.getKey();

        Intent action1Intent = new Intent(app, NotificationActionService.class).setAction(ACTION_1);
        action1Intent.setType(Integer.toString(thisActionsId) + "start"); //gives intent a unique action to stop it overwriting previous notifications
        action1Intent.putExtra("name", surveyname);
        action1Intent.putExtra("surveyid", newPostRefId);
        action1Intent.putExtra("notificationid", thisActionsId);
        action1Intent.putExtra("triggertype",(int)getparams().get("TRIGGER_TYPE"));
        action1Intent.putExtra("timeSent", timeSent);
        PendingIntent action1PendingIntent = PendingIntent.getService(app, 0, action1Intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationManager notificationManager = (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(app)
                .setContentTitle("Ready to take a survey?")
                .setVibrate(new long[]{0, 1000})
                .setSmallIcon(R.drawable.ic_action_search)
                .setPriority(Notification.PRIORITY_HIGH)
                .setWhen(0);
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        //If this has an expiry time, we set our 'time to go', i.e. how long the user has to complete the survey
        long expiryTime = currentsurvey.getexpiryTime();
        long expiryMillis = expiryTime * 60 * 1000;
        long deadline = currentsurvey.gettimeSent() + expiryMillis;
        long timeToGo = deadline - System.currentTimeMillis();

        mBuilder.setAutoCancel(true);
        mBuilder.setOngoing(true);
        mBuilder.addAction(R.drawable.ic_create_black_24dp, "Start survey", action1PendingIntent);
        mBuilder.setStyle(inboxStyle);

        // notificationManager.notify(thisActionsId, mBuilder.build());
        AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        if (timeToGo > 0) {
            Intent intent = new Intent(app, MissedSurveyReceiver.class);
            intent.putExtra("name", surveyname);
            intent.putExtra("notificationid", thisActionsId);

            intent.putExtra("triggertype",(int)getparams().get("TRIGGER_TYPE"));
            intent.setType(timeSent + surveyname); //Unique type to distinguish intents
            Log.d("ALARM TYPE ", intent.getType());
            PendingIntent pi = PendingIntent.getBroadcast(app, 0, intent, 0);

            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeToGo, pi);
        }
        //This requires some explanation...
        //If the user has triggered the survey manually (i.e., with a button press), then there's no need for the prompting dialog.
        //The ycan just skip straight to the survey

        //We still want to have a dialog to keep the expiry stuff alive (at least for now)
        if (getmanual()) {
            mBuilder.setContentTitle("Started a survey!");
            mBuilder.mActions.clear();
            Intent resultIntent = new Intent(app, SurveyActivity.class);
            resultIntent.putExtra("surveyid", newPostRefId);
            resultIntent.putExtra("name", surveyname);
            resultIntent.putExtra("initTime", System.currentTimeMillis());

            resultIntent.putExtra("timeSent", System.currentTimeMillis());
            resultIntent.putExtra("manual", true);
            resultIntent.putExtra("triggertype",(int)getparams().get("TRIGGER_TYPE"));
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app.startActivity(resultIntent);
            notificationManager.notify(thisActionsId, mBuilder.build());

            return true;

        } else {
            notificationManager.notify(thisActionsId, mBuilder.build());
        }
        return true;
    }


    //This receives the notification that the user missed the survey WITHOUT EVEN TRIGGERING THE NOTIFICATION
    public static class MissedSurveyReceiver extends BroadcastReceiver {

        public MissedSurveyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
            Intent intended = new Intent();
            int id = intent.getIntExtra("notificationid", 0);
            intended.setAction(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER);
            intended.putExtra("surveyName", intent.getStringExtra("name"));
            intended.putExtra("result", false);

            SharedPreferences.Editor editor = preferences.edit();
            long missedSurveyCount = preferences.getLong("Missed Surveys", 0);
            missedSurveyCount++;
            editor.putLong("Missed Surveys", missedSurveyCount);

            long thisMissedSurveyCount = preferences.getLong(intent.getStringExtra("name") + "-Missed", 0);
            thisMissedSurveyCount++;
            editor.putLong(intent.getStringExtra("name") + "-Missed", thisMissedSurveyCount);
            editor.commit();

            intended.putExtra("missed", thisMissedSurveyCount);
            context.sendBroadcast(intended);
            Context app = ApplicationContext.getContext();
            NotificationManager manager = (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);
            manager.cancel(id);


            Map<String,Object> surveymap = new HashMap<String,Object>();
            surveymap.put("status",0);
            surveymap.put("inittime",0);
            surveymap.put("trigger",intent.getIntExtra("triggertype",0));
            FirebaseUtils.SURVEY_REF.child(intent.getStringExtra("name")).push().setValue(surveymap);
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
            int id = intent.getIntExtra("notificationid", 0);

            if (ACTION_1.equals(action)) {
                NotificationManager manager = (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);
                manager.cancel(id);
                Intent resultIntent = new Intent(app, SurveyActivity.class);
                resultIntent.putExtra("surveyid", intent.getStringExtra("surveyid"));
                resultIntent.putExtra("name", intent.getStringExtra("name"));
                resultIntent.putExtra("timeSent", intent.getLongExtra("timeSent", 0));
                resultIntent.putExtra("initTime", System.currentTimeMillis());
                resultIntent.putExtra("triggertype",intent.getIntExtra("triggertype",0));
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                this.startActivity(resultIntent);

            }
            if (ACTION_2.equals(action)) {
                NotificationManager manager = (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);
                manager.cancel(id);
            }
        }
    }
}

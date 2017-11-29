package com.jeeves.actions;

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

import com.jeeves.ApplicationContext;
import com.jeeves.R;
import com.jeeves.SurveyActivity;
import com.jeeves.firebase.FirebaseSurvey;
import com.jeeves.firebase.FirebaseUtils;
import com.google.firebase.database.DatabaseReference;
import com.ubhave.triggermanager.config.TriggerManagerConstants;
import com.ubhave.triggermanager.triggers.TriggerUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jeeves.ApplicationContext.DEADLINE;
import static com.jeeves.ApplicationContext.INCOMPLETE;
import static com.jeeves.ApplicationContext.INIT_TIME;
import static com.jeeves.ApplicationContext.MISSED_SURVEYS;
import static com.jeeves.ApplicationContext.NOTIF_ID;
import static com.jeeves.ApplicationContext.STATUS;
import static com.jeeves.ApplicationContext.SURVEY_ID;
import static com.jeeves.ApplicationContext.SURVEY_NAME;
import static com.jeeves.ApplicationContext.TIME_SENT;
import static com.jeeves.ApplicationContext.TRIG_TYPE;
import static com.jeeves.ApplicationContext.WAS_INIT;


/**
 * Created by Daniel on 26/05/15.
 */
public class SurveyAction extends FirebaseAction {

    public static final String ACTION_1 = "action_1";
    public static final String ACTION_2 = "action_2";
    public static int NOTIFICATION_ID = 0;

    //This receives the notification that the user missed the survey WITHOUT EVEN TRIGGERING THE NOTIFICATION
    public static class MissedSurveyReceiver extends BroadcastReceiver {

        public MissedSurveyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
            Intent intended = new Intent();
            int id = intent.getIntExtra(NOTIF_ID, 0);
            long initTime = intent.getLongExtra(INIT_TIME,0);
            long timeSent = intent.getLongExtra(TIME_SENT,0);

            intended.setAction(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER);
            intended.putExtra(SURVEY_NAME, intent.getStringExtra(SURVEY_NAME));
            intended.putExtra("result", false);

            SharedPreferences.Editor editor = preferences.edit();
            long missedSurveyCount = preferences.getLong(MISSED_SURVEYS, 0);
            missedSurveyCount++;
            editor.putLong(MISSED_SURVEYS, missedSurveyCount);

            long thisMissedSurveyCount = preferences.getLong(intent.getStringExtra("name") + "-Missed", 0);
            thisMissedSurveyCount++;
            editor.putLong(intent.getStringExtra("name") + "-Missed", thisMissedSurveyCount);
            editor.commit();

            intended.putExtra("missed", thisMissedSurveyCount);
            context.sendBroadcast(intended);
            Context app = ApplicationContext.getContext();
            NotificationManager manager = (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);
            manager.cancel(id);


            //Now we push the missed result to the database.
            Map<String,Object> surveymap = new HashMap<String,Object>();
            surveymap.put(STATUS,0);
            surveymap.put(INIT_TIME,initTime-timeSent);
            surveymap.put(TRIG_TYPE,intent.getIntExtra(TRIG_TYPE,0));
            FirebaseUtils.SURVEY_REF.child(intent.getStringExtra(SURVEY_NAME)).child("missed").push().setValue(surveymap);
        }
    }

    //We need to have a way to uniquely identify each survey
    public int thisActionsId = 0;

    private BroadcastReceiver mReceiver; //Receives notifications from SurveyActivity that we finished
    public SurveyAction(Map<String, Object> params) {
        setparams(params);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean execute() {
        final Context app = ApplicationContext.getContext();
        final String surveyname = getparams().get("survey").toString();


        int triggerType = (int)getparams().get(TRIG_TYPE);
        FirebaseSurvey currentsurvey = null;

        List<FirebaseSurvey> surveys = ApplicationContext.getProject().getsurveys();
        for (FirebaseSurvey survey : surveys) {
            thisActionsId++; //So two of the same survey have the same actionsID...I hope
            if (survey.gettitle().equals(surveyname)) {
                currentsurvey = survey;
                break;
            }
        }
        final String surveyId = currentsurvey.getsurveyId();
        //We need to store that the last survey with this name to be sent out has not been completed. This will help keep
        //track in the Missed Surveys screen of which surveys to display
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(currentsurvey.getsurveyId(),true);
        editor.commit();
        final long timeSent = System.currentTimeMillis();

        //We need to keep the notification alive for as long as the survey isn't completed
        //Whenever this receiver gets a notification that our survey was completed, it cancels
        //the timeout alarm previously set



        DatabaseReference myRef = FirebaseUtils.PATIENT_REF.child(INCOMPLETE);
        DatabaseReference newPostRef = myRef.push();
        currentsurvey.settimeSent(timeSent);
        currentsurvey.settriggerType((int)getparams().get(TRIG_TYPE));
        newPostRef.setValue(currentsurvey);
        final String newPostRefId = newPostRef.getKey();
        //If this has an expiry time, we set our 'time to go', i.e. how long the user has to complete the survey
        long expiryTime = currentsurvey.getexpiryTime();
        long expiryMillis = expiryTime * 60 * 1000;
        long deadline = currentsurvey.gettimeSent() + expiryMillis;
        long timeToGo = deadline - System.currentTimeMillis();

        IntentFilter intentFilter = new IntentFilter(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER);
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("HELLO","DID I PICK THIS UP");
                //This gets rid of the notification icon and cancels the alarm because the survey is completed
                long completedtimesent = intent.getLongExtra(TIME_SENT, 0);
                if (completedtimesent == timeSent) { //Then this survey was completed woohoo!
                    AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
                    Intent cancelIntent = new Intent(app, MissedSurveyReceiver.class);
                    cancelIntent.setType(timeSent+surveyname);
                    PendingIntent pi = PendingIntent.getBroadcast(app, 0, cancelIntent, 0);
                    am.cancel(pi);
                    NotificationManager manager = (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);
                    manager.cancel(thisActionsId);

                    //We need to store that the last survey with this name to be sent out has been completed. This will help keep
                    //track in the Missed Surveys screen of which surveys to display
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(surveyId,false);
                    editor.commit();
                }
                //This gets rid of the notification icon without actually cancelling the alarm
                else {
                    String surveyid = intent.getStringExtra(SURVEY_ID);
                    Log.d("surveyid","surveyid is " + surveyid + " and newpostrefid is " + newPostRefId);
                    if (surveyid != null && surveyid.equals(newPostRefId)) {
                        NotificationManager manager = (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);
                        manager.cancel(thisActionsId);
                    }
                }
            }
        };
        app.registerReceiver(mReceiver, intentFilter);

        Intent action1Intent = new Intent(app, NotificationActionService.class).setAction(ACTION_1);
        Intent action2Intent = new Intent(app, NotificationActionService.class).setAction(ACTION_2);

        action1Intent.setType(surveyname+"start"); //gives intent a unique action to stop it overwriting previous notifications
        action1Intent.putExtra(SURVEY_NAME, surveyname);                      //scrapped that because actually we do want it to overwrite rather than piling up
        action1Intent.putExtra(SURVEY_ID, newPostRefId);
        action1Intent.putExtra(NOTIF_ID, thisActionsId);
        action1Intent.putExtra(TRIG_TYPE,triggerType);
        action1Intent.putExtra(TIME_SENT, timeSent);
        action1Intent.putExtra(DEADLINE,deadline);
        PendingIntent action1PendingIntent = PendingIntent.getService(app, 0, action1Intent, PendingIntent.FLAG_UPDATE_CURRENT);

        action2Intent.putExtra(NOTIF_ID, thisActionsId);
        PendingIntent action2PendingIntent = PendingIntent.getService(app, 0, action2Intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationManager notificationManager = (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(app)
                .setContentTitle("You have a new survey available")
                .setVibrate(new long[]{0, 1000})
                .setSmallIcon(R.drawable.ic_action_search)
                .setPriority(Notification.PRIORITY_HIGH)
                .setWhen(0);
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        mBuilder.setAutoCancel(true);
        mBuilder.setOngoing(true);
        mBuilder.addAction(R.drawable.ic_create_black_24dp, "Start survey", action1PendingIntent);
        mBuilder.addAction(R.drawable.ic_create_black_24dp, "I'll do it later!", action2PendingIntent);
        mBuilder.setStyle(inboxStyle);

        //This fires when we've ran out of time to initiate the survey
        // notificationManager.notify(thisActionsId, mBuilder.build());
        AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        if (timeToGo > 0) {
            Intent intent = new Intent(app, MissedSurveyReceiver.class);
            intent.putExtra(SURVEY_NAME, surveyname);
            intent.putExtra(NOTIF_ID, thisActionsId);
            //because we never actually initiated the survey
            intent.putExtra(WAS_INIT,false);
            intent.putExtra(INIT_TIME,new Long(0));
            intent.putExtra(TRIG_TYPE,triggerType);
            intent.setType(timeSent + surveyname); //Unique type to distinguish intents
            PendingIntent pi = PendingIntent.getBroadcast(app, 0, intent, 0);

            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeToGo, pi);
        }
        //This requires some explanation...
        //If the user has triggered the survey manually (i.e., with a button press), then there's no need for the prompting dialog.
        //The ycan just skip straight to the survey

        //We still want to have a dialog to keep the expiry stuff alive (at least for now)
        if (triggerType == TriggerUtils.TYPE_SENSOR_TRIGGER_BUTTON || triggerType == TriggerUtils.TYPE_CLOCK_TRIGGER_BEGIN) {
//            mBuilder.setContentTitle("Started a survey!");
//             mBuilder.setVibrate(new long[]{0});
//            mBuilder.mActions.clear();
            Intent resultIntent = new Intent(app, SurveyActivity.class);
            resultIntent.putExtra(SURVEY_ID, newPostRefId);
            resultIntent.putExtra(SURVEY_NAME, surveyname);
            resultIntent.putExtra(INIT_TIME, System.currentTimeMillis());
            resultIntent.putExtra(TIME_SENT, System.currentTimeMillis());
            resultIntent.putExtra(TRIG_TYPE,triggerType);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app.startActivity(resultIntent);
      //      notificationManager.notify(thisActionsId, mBuilder.build());

            return true;

        } else {
            notificationManager.notify(thisActionsId, mBuilder.build());
        }
        return true;
    }

    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Context app = ApplicationContext.getContext();
            String action = intent.getAction();
            int id = intent.getIntExtra(NOTIF_ID, 0);

            if (ACTION_1.equals(action)) {
                int triggertype = intent.getIntExtra(TRIG_TYPE,0);
                String surveyname = intent.getStringExtra(SURVEY_NAME);
                long timeSent = intent.getLongExtra(TIME_SENT, 0);
                long initTime = System.currentTimeMillis();
                int notificationId = intent.getIntExtra(NOTIF_ID,0);
                long deadline = intent.getLongExtra(DEADLINE,0);
                long timeToGo = deadline-System.currentTimeMillis();
                //We first need to send an intent to reset our incomplete alarm
                AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
                if (timeToGo > 0) {
                    Log.d("YUP","STILL GO TTIME TO GO");
                    Intent resetIntent = new Intent(app, MissedSurveyReceiver.class);
                    resetIntent.putExtra(SURVEY_NAME, surveyname);
                    resetIntent.putExtra(NOTIF_ID, notificationId);
                    //Because we've just initiated the survey!
                      resetIntent.putExtra(WAS_INIT,true);
                    resetIntent.putExtra(TIME_SENT,timeSent);
                    resetIntent.putExtra(INIT_TIME,initTime);
                    resetIntent.putExtra(TRIG_TYPE,triggertype);
                    resetIntent.setType(timeSent + surveyname); //Unique type to distinguish intents
                    Log.d("NEW TYPE","New type is " + timeSent + surveyname);
                    Log.d("ALARM TYPE ", resetIntent.getType());
                    PendingIntent pi = PendingIntent.getBroadcast(app, 0, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                    PendingIntent.F
                    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeToGo, pi);
                }

                //Followed by an intent to actually start our survey!
                NotificationManager manager = (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);
                manager.cancel(id);
                Intent resultIntent = new Intent(app, SurveyActivity.class);
                resultIntent.putExtra(SURVEY_ID, intent.getStringExtra(SURVEY_ID));
                resultIntent.putExtra(SURVEY_NAME, surveyname);
                resultIntent.putExtra(TIME_SENT, timeSent);
                resultIntent.putExtra(INIT_TIME, initTime);
                resultIntent.putExtra(TRIG_TYPE,triggertype);
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

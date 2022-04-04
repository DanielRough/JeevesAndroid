package com.jeeves.actions.actiontypes;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.jeeves.AppContext;
import com.jeeves.R;
import com.jeeves.mainscreens.SurveyActivity;
import com.jeeves.firebase.FirebaseSurvey;
import com.jeeves.firebase.FirebaseUtils;
import com.google.firebase.database.DatabaseReference;
import com.jeeves.triggers.config.TriggerConstants;
import com.jeeves.triggers.triggers.TriggerUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Class representing the 'send survey' action block. It's pretty large in comparison to
 * the other actions as the survey might be opened immediately or prompted, and it may
 * also be missed entirely.
 */
public class SurveyAction extends FirebaseAction {

    private static final String ACTION_1 = "action_1";
    private static final String ACTION_2 = "action_2";

    //A field to uniquely identify each survey
    private int thisActionsId = 0;

    private FirebaseSurvey currentsurvey = null;

    public SurveyAction(Map<String, Object> params) {
        setparams(params);
    }


    //This class receives the notification that a user missed a prompted survey
    //without opening the notification
    public static class MissedSurveyReceiver extends BroadcastReceiver {

        public MissedSurveyReceiver() {
        }

        /**
         * When the user misses a survey, we need to push this information to the database
         * and also store this info in SharedPreferences
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(AppContext.getContext());
            int id = intent.getIntExtra(AppContext.NOTIF_ID, 0);
            long initTime = intent.getLongExtra(AppContext.INIT_TIME,0);
            long timeSent = intent.getLongExtra(AppContext.TIME_SENT,0);

            //Increase the total number of surveys the user has missed
            SharedPreferences.Editor editor = preferences.edit();
            long userMissed = preferences.getLong(AppContext.MISSED_SURVEYS, 0);
            userMissed++;
            editor.putLong(AppContext.MISSED_SURVEYS, userMissed);

            //Store the number of times this specific survey has been missed
            long surveyMissed = preferences
                .getLong(intent.getStringExtra("name") + "-Missed", 0);
            surveyMissed++;
            editor.putLong(intent.getStringExtra(AppContext.SURVEY_ID)+"-Missed", surveyMissed);
            editor.apply();

            //Send a broadcast that gets picked up somewhere. I can't remember where.
            //In the SURVEY TRIGGER
            Intent intended = new Intent();
            intended.setAction(TriggerConstants.ACTION_NAME_SURVEY_TRIGGER);
            intended.putExtra(AppContext.SURVEY_ID, intent.getStringExtra(AppContext.SURVEY_ID));
            intended.putExtra("result", false);
            intended.putExtra("missed", surveyMissed);
            intended.putExtra(AppContext.SURVEY_NAME,intent.getStringExtra(AppContext.SURVEY_NAME));
            context.sendBroadcast(intended);
            Context app = AppContext.getContext();

            //Get rid of the notification now that the survey is expired
            NotificationManager manager =
                (NotificationManager)app.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(id);

            //Now we push the missed result to the database.
            Map<String,Object> surveymap = new HashMap<>();
            surveymap.put(AppContext.STATUS,0);
            surveymap.put(AppContext.INIT_TIME,initTime-timeSent);
            surveymap.put(AppContext.TRIG_TYPE,
                intent.getIntExtra(AppContext.TRIG_TYPE,0));
            String surveyid = intent.getStringExtra(AppContext.SURVEY_ID);
            SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(AppContext.getContext());
            //Reset these if the app has been closed and then opened again
            FirebaseDatabase database = FirebaseUtils.getDatabase();
            FirebaseUtils.SURVEY_REF = database
                .getReference(FirebaseUtils.PROJECTS_KEY)
                .child(prefs.getString(AppContext.STUDY_NAME, ""))
                .child(FirebaseUtils.SURVEYDATA_KEY);
            if(surveyid == null)surveyid = "null";
            FirebaseUtils.SURVEY_REF.child(surveyid)
                .child("missed").push().setValue(surveymap);
        }
    }


    /**
     * Builds the survey notification and the actions that occur when the user starts/dismisses
     * the survey
     * @param newPostRefId String key of survey results in the Firebase database
     * @param timeSent Time in ms since the epoch that survey was sent
     * @param deadline Time in ms since the epoch that survye will expire
     */
    private void buildSurveyNotification(String newPostRefId,long timeSent, long deadline){
        final Context app = AppContext.getContext();
        if(getparams().get("survey") == null) {
            return;
        }
        final String surveyname = getparams().get("survey").toString();
        int triggerType = (int)getparams().get(AppContext.TRIG_TYPE);

        Intent action1Intent = new Intent(app, NotificationActionService.class).setAction(ACTION_1);
        Intent action2Intent = new Intent(app, NotificationActionService.class).setAction(ACTION_2);

        action1Intent.setType(surveyname+"start");
        action1Intent.putExtra(AppContext.SURVEY_NAME, surveyname);
        action1Intent.putExtra(AppContext.SURVEY_ID, newPostRefId);
        action1Intent.putExtra(AppContext.NOTIF_ID, thisActionsId);
        action1Intent.putExtra(AppContext.TRIG_TYPE,triggerType);
        action1Intent.putExtra(AppContext.TIME_SENT, timeSent);
        action1Intent.putExtra(AppContext.DEADLINE,deadline);
        PendingIntent startIntent = PendingIntent.getService(app,
            0, action1Intent, PendingIntent.FLAG_UPDATE_CURRENT);

        action2Intent.putExtra(AppContext.NOTIF_ID, thisActionsId);
        PendingIntent laterIntent = PendingIntent.getService(app,
            0, action2Intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationManager nm = (NotificationManager) app
            .getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "default_channel_id";
        AudioAttributes attributes = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        }
        //Check if notification channel exists and if not create one
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel nchannel = nm.getNotificationChannel(channelId);
            if (nchannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                nchannel = new NotificationChannel(channelId, "default", importance);
                nchannel.setLightColor(Color.GREEN);
                nchannel.enableVibration(true);
                nchannel.setSound(RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),attributes);
                nm.createNotificationChannel(nchannel);
            }
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(app)
            .setContentTitle("You have a new survey available")
            .setVibrate(new long[]{0, 1000})
            .setSmallIcon(R.drawable.ic_action_search)
            .setContentText(AppContext.getContext().getString(R.string.app_name))
            .setPriority(Notification.PRIORITY_HIGH)
            .setChannelId(channelId)
            .setWhen(0);
        NotificationCompat.InboxStyle inboxStyle =
            new NotificationCompat.InboxStyle();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mBuilder.setSound(
                    RingtoneManager
                            .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            );
        }
        mBuilder.setAutoCancel(true);
        mBuilder.setOngoing(true);
        mBuilder.setStyle(inboxStyle);
        //If survey is triggered by a button or app initiation, no need for survey dialog
        //Also should start straight away if this is a 'fast transition' survey
        if (triggerType == TriggerUtils.TYPE_SENSOR_TRIGGER_BUTTON ||
            triggerType == TriggerUtils.TYPE_CLOCK_TRIGGER_BEGIN
            ) {
        }
        else{
            mBuilder.addAction(R.drawable.ic_create_black_24dp, "Start survey", startIntent);
            mBuilder.addAction(R.drawable.ic_create_black_24dp,"I'll do it later", laterIntent);
            nm.notify(thisActionsId, mBuilder.build());

        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void execute() {

        final Context app = AppContext.getContext();
        if(getparams().get("survey") == null) {
            return;
        }
        final String surveyname = getparams().get("survey").toString();
        int triggerType = (int)getparams().get(AppContext.TRIG_TYPE);

        //Check we're not snoozing (button-triggered surveys and prompts can still be done)
        SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());
        if(prefs.getBoolean(AppContext.SNOOZE,false)){
            if (triggerType != TriggerUtils.TYPE_SENSOR_TRIGGER_BUTTON &&
                triggerType != TriggerUtils.TYPE_CLOCK_TRIGGER_BEGIN)
                return;
        }
        List<FirebaseSurvey> surveys = AppContext.getProject().getsurveys();
        //Find the actual JSON survey that corresponds to the given name
        for (FirebaseSurvey survey : surveys) {
            thisActionsId++;
            if (survey.gettitle().equals(surveyname)) {
                currentsurvey = survey;
                break;
            }
        }
        final String surveyId = currentsurvey.getsurveyId();

        //Stores that the last survey with this name to be
        //sent out has not been completed. This will help keep track
        //in the Missed Surveys screen of which surveys to display
        prefs = PreferenceManager.getDefaultSharedPreferences(app);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(surveyId,true);
        editor.apply();

        final long timeSent = System.currentTimeMillis();
        FirebaseDatabase database = FirebaseUtils.getDatabase();
        FirebaseUtils.PATIENT_REF = database
            .getReference(FirebaseUtils.PATIENTS_KEY)
            .child(prefs.getString(AppContext.UID, ""));
        DatabaseReference myRef = FirebaseUtils.PATIENT_REF.child(AppContext.INCOMPLETE);
        DatabaseReference newPostRef = myRef.push();
        currentsurvey.settimeSent(timeSent);
        currentsurvey.settriggerType((int)getparams().get(AppContext.TRIG_TYPE));
        newPostRef.setValue(currentsurvey);
        final String newPostRefId = newPostRef.getKey();

        //If this has an expiry time, set the 'time to go', i.e. how long
        // the user has to complete the survey
        long expiryTime = currentsurvey.getexpiryTime();
        long expiryMillis = expiryTime * 60 * 1000;
        long deadline = currentsurvey.gettimeSent() + expiryMillis;
        long timeToGo = deadline - System.currentTimeMillis();

        IntentFilter intentFilter = new IntentFilter(TriggerConstants.ACTION_NAME_SURVEY_TRIGGER);

        //Receives notifications from SurveyActivity that we finished
        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            //This removes notification icon and cancels alarm because the survey is completed
            long completedtimesent = intent.getLongExtra(AppContext.TIME_SENT, 0);
            if (completedtimesent == timeSent) { //Then this survey was completed woohoo!
                AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
                Intent cancelIntent = new Intent(app, MissedSurveyReceiver.class);
                cancelIntent.setType(timeSent + surveyname);
                PendingIntent pi = PendingIntent.getBroadcast(app, 0, cancelIntent, 0);
                am.cancel(pi);
                NotificationManager manager = (NotificationManager) app
                    .getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(thisActionsId);

                //Store that the last survey with this name to be sent out has been completed.
                //To keep track in the Missed Surveys screen of which surveys to display
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(surveyId, false);
                editor.apply();
            }
            //This gets rid of the notification icon without actually cancelling the alarm
            else {
                String surveyid = intent.getStringExtra(AppContext.SURVEY_ID);
                Log.d("surveyid", "surveyid is " + surveyid);
                if (surveyid != null && surveyid.equals(currentsurvey.getsurveyId())) {
                    NotificationManager manager = (NotificationManager) app
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(thisActionsId);
                }
            }
            }
        };
        app.registerReceiver(mReceiver, intentFilter);

        buildSurveyNotification(newPostRefId,timeSent,deadline);

        //If this survey has a time limit...
        if (timeToGo > 0) {
            AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(app, MissedSurveyReceiver.class);
            intent.putExtra(AppContext.SURVEY_NAME, surveyname);
            intent.putExtra(AppContext.NOTIF_ID, thisActionsId);
            Log.d("NEWPOSTREF","Noew postref is " + newPostRefId);
            intent.putExtra(AppContext.SURVEY_ID, newPostRefId);

            intent.putExtra(AppContext.WAS_INIT,false);
            intent.putExtra(AppContext.INIT_TIME,Long.valueOf(0));
            intent.putExtra(AppContext.TRIG_TYPE,triggerType);
            intent.setType(timeSent + surveyname); //Unique type to distinguish intents
            PendingIntent pi = PendingIntent.getBroadcast(app, 0, intent, 0);
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeToGo, pi);
        }


        PowerManager pm = (PowerManager) AppContext.getContext()
            .getSystemService(Context.POWER_SERVICE);
        if(!pm.isScreenOn()) {
            @SuppressLint("InvalidWakeLockTag")
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
            wl.acquire(10000);
            @SuppressLint("InvalidWakeLockTag")
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");
            wl_cpu.acquire(10000);
        }

        //If user doesn't need to start the survey manually
        if (triggerType == TriggerUtils.TYPE_SENSOR_TRIGGER_BUTTON ||
            triggerType == TriggerUtils.TYPE_CLOCK_TRIGGER_BEGIN
            || currentsurvey.getfastTransition()) {
            Intent resultIntent = new Intent(app, SurveyActivity.class);
            resultIntent.putExtra(AppContext.SURVEY_ID, newPostRefId);
            resultIntent.putExtra(AppContext.SURVEY_NAME, surveyname);
            resultIntent.putExtra(AppContext.INIT_TIME, System.currentTimeMillis());
            resultIntent.putExtra(AppContext.TIME_SENT, System.currentTimeMillis());
            resultIntent.putExtra(AppContext.TRIG_TYPE,triggerType);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app.startActivity(resultIntent);
        }
    }

    public SurveyAction getInstance(){
        return this;
    }
    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }
        @Override
        protected void onHandleIntent(Intent intent) {
            Context app = AppContext.getContext();
            String action = intent.getAction();
            int id = intent.getIntExtra(AppContext.NOTIF_ID, 0);

            if (ACTION_1.equals(action)) {
                int triggertype = intent.getIntExtra(AppContext.TRIG_TYPE,0);
                String surveyid = intent.getStringExtra(AppContext.SURVEY_ID);
                long timeSent = intent.getLongExtra(AppContext.TIME_SENT, 0);
                long initTime = System.currentTimeMillis();
                int notificationId = intent.getIntExtra(AppContext.NOTIF_ID,0);
                long deadline = intent.getLongExtra(AppContext.DEADLINE,0);
                long timeToGo = deadline-System.currentTimeMillis();


                //Send an intent to reset our incomplete alarm (user's time to complete is reset
                //as soon as they hit 'Start Survey'
                if (timeToGo > 0) {
                    AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
                    Intent resetIntent = new Intent(app, MissedSurveyReceiver.class);
                    resetIntent.putExtra(AppContext.NOTIF_ID, notificationId);
                    resetIntent.putExtra(AppContext.WAS_INIT,true);
                    resetIntent.putExtra(AppContext.TIME_SENT,timeSent);
                    resetIntent.putExtra(AppContext.INIT_TIME,initTime);
                    resetIntent.putExtra(AppContext.TRIG_TYPE,triggertype);
                    resetIntent.setType(timeSent + surveyid); //Unique type to distinguish intents
                    PendingIntent pi = PendingIntent.getBroadcast(app, 0, resetIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeToGo, pi);

                }

                //Followed by an intent to actually start our survey!
                NotificationManager manager = (NotificationManager) app
                    .getSystemService(NOTIFICATION_SERVICE);
                manager.cancel(id);
                Intent sIntent = new Intent(app, SurveyActivity.class);
                sIntent.putExtra(AppContext.SURVEY_ID, intent.getStringExtra(AppContext.SURVEY_ID));
                sIntent.putExtra(AppContext.TIME_SENT, timeSent);
                sIntent.putExtra(AppContext.INIT_TIME, initTime);
                sIntent.putExtra(AppContext.TRIG_TYPE,triggertype);
                sIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                this.startActivity(sIntent);

            }
            //User has elected to do survey later
            if (ACTION_2.equals(action)) {
                NotificationManager manager = (NotificationManager) app
                    .getSystemService(NOTIFICATION_SERVICE);
                manager.cancel(id);
            }
        }
    }
}

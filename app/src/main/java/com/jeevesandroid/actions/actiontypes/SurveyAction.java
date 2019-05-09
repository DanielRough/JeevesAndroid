package com.jeevesandroid.actions.actiontypes;

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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.jeevesandroid.ApplicationContext;
import com.jeevesandroid.R;
import com.jeevesandroid.mainscreens.SurveyActivity;
import com.jeevesandroid.firebase.FirebaseSurvey;
import com.jeevesandroid.firebase.FirebaseUtils;
import com.google.firebase.database.DatabaseReference;
import com.jeevesandroid.triggers.config.TriggerManagerConstants;
import com.jeevesandroid.triggers.triggers.TriggerUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Daniel on 26/05/15.
 */
public class SurveyAction extends FirebaseAction {

    private static final String ACTION_1 = "action_1";
    private static final String ACTION_2 = "action_2";
    public static int NOTIFICATION_ID = 0;

    //This receives the notification that the user missed the survey WITHOUT EVEN TRIGGERING THE NOTIFICATION
    public static class MissedSurveyReceiver extends BroadcastReceiver {

        public MissedSurveyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
            Intent intended = new Intent();
            Log.d("MISSED!?","MISSED IT !?!?!?");
            int id = intent.getIntExtra(ApplicationContext.NOTIF_ID, 0);
            long initTime = intent.getLongExtra(ApplicationContext.INIT_TIME,0);
            long timeSent = intent.getLongExtra(ApplicationContext.TIME_SENT,0);

            intended.setAction(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER);
            intended.putExtra(ApplicationContext.SURVEY_ID, intent.getStringExtra(ApplicationContext.SURVEY_ID));
            intended.putExtra("result", false);

            SharedPreferences.Editor editor = preferences.edit();
            long missedSurveyCount = preferences.getLong(ApplicationContext.MISSED_SURVEYS, 0);
            missedSurveyCount++;
            editor.putLong(ApplicationContext.MISSED_SURVEYS, missedSurveyCount);

            long thisMissedSurveyCount = preferences.getLong(intent.getStringExtra("name") + "-Missed", 0);
            thisMissedSurveyCount++;
            editor.putLong(intent.getStringExtra(ApplicationContext.SURVEY_ID) + "-Missed", thisMissedSurveyCount);
            editor.apply();

            intended.putExtra("missed", thisMissedSurveyCount);
            context.sendBroadcast(intended);
            Context app = ApplicationContext.getContext();
            NotificationManager manager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(id);


            //Now we push the missed result to the database.
            Map<String,Object> surveymap = new HashMap<>();
            surveymap.put(ApplicationContext.STATUS,0);
            surveymap.put(ApplicationContext.INIT_TIME,initTime-timeSent);
            surveymap.put(ApplicationContext.TRIG_TYPE,intent.getIntExtra(ApplicationContext.TRIG_TYPE,0));
            FirebaseUtils.SURVEY_REF.child(intent.getStringExtra(ApplicationContext.SURVEY_ID)).child("missed").push().setValue(surveymap);
        }
    }

    //We need to have a way to uniquely identify each survey
    private int thisActionsId = 0;

    public SurveyAction(Map<String, Object> params) {
        setparams(params);
    }
    private FirebaseSurvey currentsurvey = null;

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void execute() {
        final Context app = ApplicationContext.getContext();
        if(getparams().get("survey") == null)return;
        final String surveyname = getparams().get("survey").toString();


        int triggerType = (int)getparams().get(ApplicationContext.TRIG_TYPE);

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
        editor.apply();
        final long timeSent = System.currentTimeMillis();

        //We need to keep the notification alive for as long as the survey isn't completed
        //Whenever this receiver gets a notification that our survey was completed, it cancels
        //the timeout alarm previously set
        DatabaseReference myRef = FirebaseUtils.PATIENT_REF.child(ApplicationContext.INCOMPLETE);
        DatabaseReference newPostRef = myRef.push();
        currentsurvey.settimeSent(timeSent);
        currentsurvey.settriggerType((int)getparams().get(ApplicationContext.TRIG_TYPE));
        newPostRef.setValue(currentsurvey);
        final String newPostRefId = newPostRef.getKey();
        //If this has an expiry time, we set our 'time to go', i.e. how long the user has to complete the survey
        long expiryTime = currentsurvey.getexpiryTime();
        long expiryMillis = expiryTime * 60 * 1000;
        long deadline = currentsurvey.gettimeSent() + expiryMillis;
        long timeToGo = deadline - System.currentTimeMillis();

        IntentFilter intentFilter = new IntentFilter(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER);
        //Receives notifications from SurveyActivity that we finished
        BroadcastReceiver mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("HELLO", "DID I PICK THIS UP");
                //This gets rid of the notification icon and cancels the alarm because the survey is completed
                long completedtimesent = intent.getLongExtra(ApplicationContext.TIME_SENT, 0);
                if (completedtimesent == timeSent) { //Then this survey was completed woohoo!
                    AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
                    Intent cancelIntent = new Intent(app, MissedSurveyReceiver.class);
                    cancelIntent.setType(timeSent + surveyname);
                    PendingIntent pi = PendingIntent.getBroadcast(app, 0, cancelIntent, 0);
                    am.cancel(pi);
                    NotificationManager manager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(thisActionsId);

                    //We need to store that the last survey with this name to be sent out has been completed. This will help keep
                    //track in the Missed Surveys screen of which surveys to display
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(surveyId, false);
                    editor.apply();
                }
                //This gets rid of the notification icon without actually cancelling the alarm
                else {
                    String surveyid = intent.getStringExtra(ApplicationContext.SURVEY_ID);
                    Log.d("surveyid", "surveyid is " + surveyid);
                    if (surveyid != null && surveyid.equals(currentsurvey.getsurveyId())) {
                        NotificationManager manager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.cancel(thisActionsId);
                    }
                }
            }
        };
        app.registerReceiver(mReceiver, intentFilter);

        Intent action1Intent = new Intent(app, NotificationActionService.class).setAction(ACTION_1);
        Intent action2Intent = new Intent(app, NotificationActionService.class).setAction(ACTION_2);

        action1Intent.setType(surveyname+"start"); //gives intent a unique action to stop it overwriting previous notifications
        action1Intent.putExtra(ApplicationContext.SURVEY_NAME, surveyname);                      //scrapped that because actually we do want it to overwrite rather than piling up
        action1Intent.putExtra(ApplicationContext.SURVEY_ID, newPostRefId);
        action1Intent.putExtra(ApplicationContext.NOTIF_ID, thisActionsId);
        action1Intent.putExtra(ApplicationContext.TRIG_TYPE,triggerType);
        action1Intent.putExtra(ApplicationContext.TIME_SENT, timeSent);
        action1Intent.putExtra(ApplicationContext.DEADLINE,deadline);
        PendingIntent action1PendingIntent = PendingIntent.getService(app, 0, action1Intent, PendingIntent.FLAG_UPDATE_CURRENT);

        action2Intent.putExtra(ApplicationContext.NOTIF_ID, thisActionsId);
        PendingIntent action2PendingIntent = PendingIntent.getService(app, 0, action2Intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationManager notificationManager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "default_channel_id";
        String channelDescription = "Default Channel";
        AudioAttributes attributes = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
             attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
        }
        //Check if notification channel exists and if not create one
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);
            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelId, channelDescription, importance);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationChannel.setSound(RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),attributes);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(app)
                .setContentTitle("You have a new survey available")
                .setVibrate(new long[]{0, 1000})
                .setSmallIcon(R.drawable.ic_action_search)
                .setContentText(ApplicationContext.getContext().getString(R.string.app_name))
                .setPriority(Notification.PRIORITY_HIGH)
                .setChannelId(channelId)
                .setWhen(0);
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        mBuilder.setAutoCancel(true);
        mBuilder.setOngoing(true);

        mBuilder.setStyle(inboxStyle);

        //This fires when we've ran out of time to initiate the survey
        // notificationManager.notify(thisActionsId, mBuilder.build());
        AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);

        if (timeToGo > 0) {
            Log.d("TIMETOGO","Time to go s " + timeToGo);
            Intent intent = new Intent(app, MissedSurveyReceiver.class);
            intent.putExtra(ApplicationContext.SURVEY_NAME, surveyname);
            intent.putExtra(ApplicationContext.NOTIF_ID, thisActionsId);
            //because we never actually initiated the survey
            intent.putExtra(ApplicationContext.SURVEY_ID, newPostRefId);

            intent.putExtra(ApplicationContext.WAS_INIT,false);
            intent.putExtra(ApplicationContext.INIT_TIME,Long.valueOf(0));
            intent.putExtra(ApplicationContext.TRIG_TYPE,triggerType);
            intent.setType(timeSent + surveyname); //Unique type to distinguish intents
            PendingIntent pi = PendingIntent.getBroadcast(app, 0, intent, 0);

            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeToGo, pi);
        }

        if (triggerType == TriggerUtils.TYPE_SENSOR_TRIGGER_BUTTON || triggerType == TriggerUtils.TYPE_CLOCK_TRIGGER_BEGIN
            || currentsurvey.getfastTransition()) {
            mBuilder.setOngoing(false);
            mBuilder.setTimeoutAfter(3000);
        }
        else{
            mBuilder.addAction(R.drawable.ic_create_black_24dp, "Start survey", action1PendingIntent);
            mBuilder.addAction(R.drawable.ic_create_black_24dp, "I'll do it later!", action2PendingIntent);
        }
            //This requires some explanation...
        //If the user has triggered the
            // survey manually (i.e., with a button press), then there's no need for the prompting dialog.
        //The ycan just skip straight to the survey
        notificationManager.notify(thisActionsId, mBuilder.build());
        PowerManager pm = (PowerManager)ApplicationContext.getContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        Log.e("screen on....", ""+isScreenOn);
        if(!isScreenOn)
        {

            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
            wl.acquire(10000);
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");

            wl_cpu.acquire(10000);
        }

        //We still want to have a dialog to keep the expiry stuff alive (at least for now)
        if (triggerType == TriggerUtils.TYPE_SENSOR_TRIGGER_BUTTON || triggerType == TriggerUtils.TYPE_CLOCK_TRIGGER_BEGIN
            || currentsurvey.getfastTransition()) {
//            mBuilder.setContentTitle("Started a survey!");
//             mBuilder.setVibrate(new long[]{0});
//            mBuilder.mActions.clear();
            Log.d("YUP","YUUUUP");
            Intent resultIntent = new Intent(app, SurveyActivity.class);
            resultIntent.putExtra(ApplicationContext.SURVEY_ID, newPostRefId);
            resultIntent.putExtra(ApplicationContext.SURVEY_NAME, surveyname);
            resultIntent.putExtra(ApplicationContext.INIT_TIME, System.currentTimeMillis());
            resultIntent.putExtra(ApplicationContext.TIME_SENT, System.currentTimeMillis());
            resultIntent.putExtra(ApplicationContext.TRIG_TYPE,triggerType);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app.startActivity(resultIntent);
      //      notificationManager.notify(thisActionsId, mBuilder.build());

        }
        else {

            Log.d("NOPE","NOOOOPE");
        }
    }

// --Commented out by Inspection START (5/8/2019 4:26 PM):
    public SurveyAction getInstance(){
        return this;
    }
// --Commented out by Inspection STOP (5/8/2019 4:26 PM)
    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }
        @Override
        protected void onHandleIntent(Intent intent) {
            Context app = ApplicationContext.getContext();
            String action = intent.getAction();
            int id = intent.getIntExtra(ApplicationContext.NOTIF_ID, 0);

            if (ACTION_1.equals(action)) {
                int triggertype = intent.getIntExtra(ApplicationContext.TRIG_TYPE,0);
                String surveyid = intent.getStringExtra(ApplicationContext.SURVEY_ID);
                long timeSent = intent.getLongExtra(ApplicationContext.TIME_SENT, 0);

                long initTime = System.currentTimeMillis();
                int notificationId = intent.getIntExtra(ApplicationContext.NOTIF_ID,0);
                long deadline = intent.getLongExtra(ApplicationContext.DEADLINE,0);
                long timeToGo = deadline-System.currentTimeMillis();
                //We first need to send an intent to reset our incomplete alarm
                AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
                if (timeToGo > 0) {
                    Log.d("YUP","STILL GO TTIME TO GO");
                    Intent resetIntent = new Intent(app, MissedSurveyReceiver.class);
                 //   resetIntent.putExtra(SURVEY_NAME, surveyname);
                    resetIntent.putExtra(ApplicationContext.NOTIF_ID, notificationId);
                    //Because we've just initiated the survey!
                      resetIntent.putExtra(ApplicationContext.WAS_INIT,true);
                    resetIntent.putExtra(ApplicationContext.TIME_SENT,timeSent);
                    resetIntent.putExtra(ApplicationContext.INIT_TIME,initTime);
                    resetIntent.putExtra(ApplicationContext.TRIG_TYPE,triggertype);
                    resetIntent.setType(timeSent + surveyid); //Unique type to distinguish intents
              //      Log.d("NEW TYPE","New type is " + timeSent + surveyname);
                    Log.d("ALARM TYPE ", resetIntent.getType());
                    PendingIntent pi = PendingIntent.getBroadcast(app, 0, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                    PendingIntent.F
                    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeToGo, pi);
                }

                //Followed by an intent to actually start our survey!
                NotificationManager manager = (NotificationManager) app.getSystemService(NOTIFICATION_SERVICE);
                manager.cancel(id);
                Intent resultIntent = new Intent(app, SurveyActivity.class);
                resultIntent.putExtra(ApplicationContext.SURVEY_ID, intent.getStringExtra(ApplicationContext.SURVEY_ID));
             //   resultIntent.putExtra(SURVEY_NAME, surveyname);
                resultIntent.putExtra(ApplicationContext.TIME_SENT, timeSent);
                resultIntent.putExtra(ApplicationContext.INIT_TIME, initTime);
                resultIntent.putExtra(ApplicationContext.TRIG_TYPE,triggertype);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                this.startActivity(resultIntent);

            }
            if (ACTION_2.equals(action)) {
                NotificationManager manager = (NotificationManager) app.getSystemService(NOTIFICATION_SERVICE);
                manager.cancel(id);
            }
        }
    }
}

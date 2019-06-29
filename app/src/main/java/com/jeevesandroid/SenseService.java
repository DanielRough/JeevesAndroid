package com.jeevesandroid;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationResult;
import com.jeevesandroid.actions.ActionExecutorService;
import com.jeevesandroid.actions.ActionUtils;
import com.jeevesandroid.actions.actiontypes.FirebaseAction;
import com.jeevesandroid.firebase.FirebaseExpression;
import com.jeevesandroid.firebase.FirebaseProject;
import com.jeevesandroid.firebase.FirebaseTrigger;
import com.jeevesandroid.firebase.FirebaseUtils;
import com.jeevesandroid.firebase.FirebaseVariable;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jeevesandroid.sensing.ActivityListener;
import com.jeevesandroid.sensing.ActivityService;
import com.jeevesandroid.sensing.GeofenceListener;
import com.jeevesandroid.sensing.SensorListener;

import com.jeevesandroid.sensing.sensormanager.ESException;
import com.jeevesandroid.sensing.sensormanager.ESSensorManager;
import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.triggers.TriggerUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SenseService extends Service implements
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final HashMap<String, TriggerListener> triggerlisteners = new HashMap<>();
    public static final HashMap<Integer, SensorListener> sensorlisteners = new HashMap<>();
    private static final HashMap<String, GeofenceListener> geofencelisteners = new HashMap<>();
    private List<String> geofenceTriggerIds = new ArrayList<>();
    private List<String> activityTriggerIds = new ArrayList<>();
    private static final HashMap<String, ActivityListener> activitylisteners = new HashMap<>();
    public static final HashMap<Integer, Integer> subscribedSensors = new HashMap<>();
    //private static final String ACTION_1 = "action_1";
    //private static final int NOTIF_ID = 1337;
    private static final int ACTIVE = 1234;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient locClient;
    private GoogleApiClient mGoogleApiClient;
    SharedPreferences.OnSharedPreferenceChangeListener mListener; //Keeps it alive

    private void createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(120000);
        mLocationRequest.setFastestInterval(60000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    HashMap<String, Object> locData = new HashMap<>();
                    String mLastUpdateTime = new Date().toString();
                    locData.put("senseStartTimeMillis", mLastUpdateTime);
                    locData.put("latitude", location.getLatitude());
                    locData.put("longitude", location.getLongitude());
                    DatabaseReference patientRef = FirebaseUtils.PATIENT_REF
                        .child("sensordata").child("Location").push();
                    patientRef.setValue(locData);
                }
            }


        };
        locClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private SenseService getInstance(){
        return this;
    }
    private final BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(AppContext.STARTACTIVITY)) {
                mGoogleApiClient = new GoogleApiClient.Builder(AppContext.getContext())
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(getInstance())
                    .addOnConnectionFailedListener(getInstance())
                    .build();
                mGoogleApiClient.connect();
                Log.d("Activity","Starting activity sensing");
            }
            else {
                mGoogleApiClient.disconnect();
                Log.d("Activity","Stopping activity sensing");
            }
        }
    };
    private final BroadcastReceiver sensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int sensorType = intent.getIntExtra("sensortype",0);
                int subid = intent.getIntExtra("subid",0);
                ESSensorManager.getSensorManager(AppContext.getContext())
                    .unsubscribeFromSensorData(SenseService.subscribedSensors.get(sensorType));
                SenseService.sensorlisteners.remove(subid);
            } catch (ESException e) {
                e.printStackTrace();
            }
        }
    };
    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AppContext.STARTLOC)) {
                createLocationRequest();
                Log.d("LOCATION","Starting location updates");
            }
            else{
                locClient.removeLocationUpdates(mLocationCallback);
                Log.d("LOCATIONSTOP","Stopping location updates");
            }
        }
    };

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, ActivityService.class );
        PendingIntent pendingIntent = PendingIntent
            .getService( this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi
            .requestActivityUpdates( mGoogleApiClient, 60000, pendingIntent );
    }
    @Override
    public void onConnectionSuspended(int i) { }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }


/*
    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(SenseService.NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Context app = AppContext.getContext();
            String action = intent.getAction();
            if (ACTION_1.equals(action)) {
                //Followed by an intent to actually start our survey
                NotificationManager manager = (NotificationManager) app
                    .getSystemService(NOTIFICATION_SERVICE);
                manager.cancel(NOTIF_ID);
                SharedPreferences varPrefs = PreferenceManager.getDefaultSharedPreferences(app);
                SharedPreferences.Editor editor = varPrefs.edit();
                editor.putBoolean("active",false);
                editor.apply();
                //
                Intent resultIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                this.startActivity(resultIntent);

            }

        }
    }
*/
    private static final String NOTIFICATION_Service_CHANNEL_ID = "service_channel";
    @Override
    public void onCreate() {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(AppContext.getContext()));

        //Make the 'always-on' foreground notification that stops the service being killed
        Notification n = buildForegroundNotification();
        //New code to cope with specifying the channel ID
        if(Build.VERSION.SDK_INT>=26) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_Service_CHANNEL_ID,
                "Sync Service", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Service Name");
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

            n = new Notification.Builder(this,NOTIFICATION_Service_CHANNEL_ID)
                .setContentTitle("Jeeves - Running")
                .setSmallIcon(R.drawable.jeeves)
                .setTicker("Jeeves - Running")
                .setOngoing(true)
                .build();
        }
        startForeground(ACTIVE, n);

        final FirebaseDatabase database = FirebaseUtils.getDatabase();
        SharedPreferences varPrefs = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());
        String studyname = varPrefs.getString(AppContext.STUDY_NAME, "");
        //String researcherno = varPrefs.getString(AppContext.DEVELOPER_ID, "");
        DatabaseReference projectRef = database
           // .getReference(FirebaseUtils.PUBLIC_KEY)
           // .child(researcherno)
            .getReference(FirebaseUtils.PROJECTS_KEY)
            .child(studyname);

        DatabaseReference scheduleRef = FirebaseUtils.PATIENT_REF.child("schedule");
        //Set up the receivers for location, activity, and other sensor info
        IntentFilter mIntentFilter = new IntentFilter(AppContext.STARTLOC);
        mIntentFilter.addAction(AppContext.STOPLOC);
        IntentFilter sensIntentFilter = new IntentFilter(AppContext.STOPSENSOR);
        IntentFilter activityIntentFilter = new IntentFilter(AppContext.STARTACTIVITY);
        activityIntentFilter.addAction(AppContext.STOPACTIVITY);
        //Activity and location detection are done through the Google API (not SensorManager)
        AppContext.getContext().registerReceiver(sensorReceiver, sensIntentFilter);
        AppContext.getContext().registerReceiver(locationReceiver, mIntentFilter);
        AppContext.getContext().registerReceiver(activityReceiver, activityIntentFilter);

        //We also listen on the project ref in here (this is a constant listener as opposed to
        //the one in the MainActivity
        projectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FirebaseProject post = snapshot.getValue(FirebaseProject.class);
                AppContext.setCurrentproject(post);
                if (post == null) {
                    return;
                }
                try {
                    updateConfig(post);
                } catch (TriggerException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        //Listen for schedule updates, then update the relevant attributes
        scheduleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("SCHED","Schedule changed");
                List<String> schedule = (List<String>)dataSnapshot.getValue();
                if(schedule == null)return;
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
                SharedPreferences.Editor editor = prefs.edit();
                int scheduleDay = prefs.getInt(AppContext.SCHEDULE_DAY, 1);
                for(int i = 1; i <= schedule.size(); i++){
                    editor.putString(AppContext.SCHEDULE_PREF + i,schedule.get(i-1));
                    //If the current day's wake/sleep times have changed, need to update the
                    //wake/sleep attributes
                    String[] wakeSleep = schedule.get(i-1).split(":");
                    if(i == scheduleDay){
                        Map<String,Object> scheduleVars = AppContext.getProject().getscheduleAttrs();
                        String wakeVarName = scheduleVars.get(AppContext.WAKE_TIME).toString();
                        String sleepVarName = scheduleVars.get(AppContext.SLEEP_TIME).toString();
                        editor.putString(wakeVarName,wakeSleep[0]);
                        editor.putString(sleepVarName,wakeSleep[1]);
                    }
                }
                editor.commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * Make the foreground notification that always runs to stop this SenseService being killed
     * @return A Notification to be displayed
     */
    private Notification buildForegroundNotification() {
        NotificationCompat.Builder b=new NotificationCompat.Builder(this);

        b.setOngoing(true)
            .setContentTitle("Jeeves - Running")
            .setSmallIcon(R.drawable.jeeves)
            .setTicker("Jeeves - Running");
        return(b.build());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * This updates any Triggers that are dependent on variables when the value of this variable has
     * changed. For example, if the value of a time variable has changed, any trigger that is
     * contingent on this time must be relaunched
     * @param triggers List of current Triggers
     * @param variables List of current variables
     * @param key String name of the variable that has changed
     * @throws TriggerException
     */
    private void changeVarValue(List<FirebaseTrigger> triggers, List<FirebaseVariable> variables,
                                String key) throws TriggerException {
        Log.d("CHANGEVAR","Var that's changed is " + key);
        for (FirebaseVariable var : variables) {
            if (var.getname().equals(key)) {
                switch (var.getvartype()) {
                    case FirebaseUtils.LOCATION:
                        if (geofencelisteners.containsKey(var.getname())) {
                            GeofenceListener locListener = geofencelisteners.get(var.getname());
                            locListener.updateLocation();
                            Log.d("UPDATELOC", "UPDATING LOCATION");
                        }
                        break;
                    case FirebaseUtils.TIME:
                    case FirebaseUtils.DATE:
                        for (FirebaseTrigger trig : triggers) {
                            if (trig.getvariables() != null && trig.getvariables().contains(key)) {
                                removeTrigger(trig.gettriggerId());
                                Log.d("RELAUNCH", "Relaunching " + trig.gettriggerId());
                                launchTrigger(trig);
                            }
                        }
                        break;
                }
            }
        }
    }

    /**
     * Updates the Shared Preferences with any new variables and values added in the Jeeves spec
     * @param variables List of updated variables in the Jeeves specification
     */
    private void updateVariables(List<FirebaseVariable> variables){
        SharedPreferences varPrefs = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());
        SharedPreferences.Editor prefseditor = varPrefs.edit();
        prefseditor.apply();
        for(FirebaseVariable var : variables){
            String type = var.getvartype();
            switch(type){
                case FirebaseUtils.TIME:
                case FirebaseUtils.DATE:
                case FirebaseUtils.NUMERIC:
                    if(var.getisRandom()){
                        List<String> randomVals = var.getrandomOptions();
                        double lowest = Double.parseDouble(randomVals.get(0));
                        double highest = Double.parseDouble(randomVals.get(1));
                        double range = (highest-lowest)+1;
                        String answer = Long.toString((long)(Math.random()*range + lowest));
                        prefseditor.putString(var.getname(),answer);
                        break;
                    }
                case FirebaseUtils.LOCATION:
                case FirebaseUtils.TEXT:
                    prefseditor.putString(var.getname(),""); break;
                case FirebaseUtils.BOOLEAN:
                    boolean defaultVal = false;
                    if(var.getisRandom()){
                        defaultVal = (Math.random() >=0.5);
                    }
                    prefseditor.putString(var.getname(),Boolean.toString(defaultVal)); break;
            }
        }
    }
    /**
     * This method is called whenever the project has been updated. It interprets what has been
     * added and removed and updates the necessary triggers, sensors, and user interface elements
     * @param app The new JSON representation of the updated project
     */
    private void updateConfig(FirebaseProject app) throws TriggerException {

        final List<FirebaseTrigger> triggers = app.gettriggers();
        final List<FirebaseVariable> variables = app.getvariables();

        SharedPreferences varPrefs = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());
        SharedPreferences.Editor prefseditor = varPrefs.edit();
        prefseditor.apply();

        mListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                try {
                    changeVarValue(triggers,variables,key);
                } catch (TriggerException e) {
                    e.printStackTrace();
                }
            }
        };
        varPrefs.registerOnSharedPreferenceChangeListener(mListener);
        updateVariables(variables);

        //This reloads any existing set of triggerids we have
        HashSet<String> s = new HashSet<>(varPrefs.getStringSet("triggerids",new HashSet()));
        ArrayList<String> triggerids = new ArrayList<>(s);

        Toast.makeText(AppContext.getContext(),"Updated app configuration",Toast.LENGTH_SHORT).show();
        ArrayList<String> newIds = new ArrayList<>();
        for (int i = 0; i < triggers.size(); i++) {
            FirebaseTrigger triggerconfig = triggers.get(i);
            String tId = triggerconfig.gettriggerId();
            newIds.add(tId);
            if (!triggerids.contains(tId)) { //Don't relaunch an already-existing trigger
                launchTrigger(triggerconfig);
            }
            else if(triggerids.contains(tId) && (triggerlisteners.get(tId) == null)) {
                if(!geofenceTriggerIds.contains(tId) && !activityTriggerIds.contains(tId)) {
                    launchTrigger(triggerconfig);
                }
            }
        }
        //Find all the old Trigger IDs that are not in the 'new' Trigger IDs, and get rid of them
        for (String toRemove : triggerids) {
            if(!newIds.contains(toRemove))
                removeTrigger(toRemove);
        }
        //This saves our current set of trigger IDs
        Set<String> triggerset = new HashSet<>(newIds);
        prefseditor.putStringSet("triggerids",triggerset);
        prefseditor.commit();
    }

    /**
     * Creates a new location-based trigger based on Google's Geofencing API
     * @param trigger JSON representation of the trigger
     * @param actions List of JSON actions to execute
     */
    private void makeLocationTrigger(FirebaseTrigger trigger, List<FirebaseAction> actions){
        String triggerId = trigger.gettriggerId();
        Map<String, Object> params = trigger.getparams();
        ArrayList<FirebaseAction> toExecute = new ArrayList<>(actions);
        GeofenceListener newListener;
        String changes = params.get("change").toString();
        FirebaseExpression locexpr = trigger.getlocation();
        String locationName = "";
        if (locexpr != null) {
            locationName = locexpr.getname();
            newListener = new GeofenceListener(this, locationName, triggerId, changes, toExecute);
            Log.d("Location added", "New location added to " + locexpr.getname());
            geofencelisteners.put(locationName, newListener);
            geofenceTriggerIds.add(triggerId);
            newListener.addLocationTrigger();
        }
        return;
    }

    /**
     * Creates a new Activity-based trigger using Google's Activity Recognition API
     * @param trigger JSON representation of the trigger
     * @param actions List of JSON actions to execute
     */
    private void makeActivityTrigger(FirebaseTrigger trigger, List<FirebaseAction> actions){
        ActivityListener newListener;
        String triggerId = trigger.gettriggerId();
        Map<String, Object> params = trigger.getparams();
        ArrayList<FirebaseAction> toExecute = new ArrayList<>(actions);
        String activityType = params.get("result").toString();
        newListener = new ActivityListener(this,activityType,triggerId,toExecute);
        Log.d("New activity","Added a new activity trigger");
        activitylisteners.put(activityType,newListener);
        activityTriggerIds.add(triggerId);
        newListener.addActivityTrigger();
        return;
    }

    /**
     * Checks the JSON to determine what type of Trigger has been passed to it, and calls the
     * appropriate subscriber to listen on the Trigger's conditions
     * @param trigger JSON representation of Trigger to launch
     * @throws TriggerException
     */
    private void launchTrigger(FirebaseTrigger trigger) throws TriggerException {
        String triggerType = trigger.getname();
        String triggerId = trigger.gettriggerId();
        Log.d("Trigger","Launching trigger " + trigger.getname() + ", " + triggerId);
        SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());

        //If we have already launched a Begin Trigger, don't do it again
        if(prefs.getBoolean(AppContext.FINISHED_INTRODUCTION,false)
            && TriggerUtils.getTriggerType(triggerType) == TriggerUtils.TYPE_CLOCK_TRIGGER_BEGIN)
            return;

        Map<String, Object> params = trigger.getparams();
        List<FirebaseAction> actions = new ArrayList<>();
        if (trigger.getactions() != null) {
            actions = trigger.getactions();
        }
        if (TriggerUtils.getTriggerType(triggerType) == TriggerUtils.TYPE_SENSOR_TRIGGER_LOCATION) {
            makeLocationTrigger(trigger,actions);
            return;
        }
        if (TriggerUtils.getTriggerType(triggerType) == TriggerUtils.TYPE_SENSOR_TRIGGER_IMMEDIATE
            && params.get("selectedSensor").equals("Activity")) {
            makeActivityTrigger(trigger,actions);
            return;
        }
        //Otherwise carry on as normal
        TriggerListener newListener = new TriggerListener(
            TriggerUtils.getTriggerType(triggerType), this);
        triggerlisteners.put(triggerId, newListener);

        //Here we make the list of actions that this trigger executes when it's activated
        ArrayList<FirebaseAction> toExecute = new ArrayList<>();
        for (int i = 0; i < actions.size(); i++) {
            toExecute.add( actions.get(i));
        }
        //If this is our begin trigger, just start it from here rather than going through the
        //rigmarole of subscribing and unsubscribing
//        if(TriggerUtils.getTriggerType(triggerType) == TriggerUtils.TYPE_CLOCK_TRIGGER_BEGIN) {
//            Intent actionIntent = new Intent(this, ActionExecutorService.class);
//            actionIntent.putExtra(ActionUtils.ACTIONS, toExecute);
//            actionIntent.putExtra(AppContext.TRIG_TYPE, TriggerUtils.getTriggerType(triggerType));
//            startService(actionIntent);
//            Log.d("BEGIN","Beginning");
//            return;
//        }
        newListener.subscribeToTrigger(trigger, toExecute);
    }

    /**
     * Unsubscribes from an existing trigger - occurs when the trigger has been removed entirely
     * or simply needs to be updated (in which case it is removed and re-added)
     * @param triggerId String ID of the trigger
     */
    private void removeTrigger(String triggerId) {
        Log.d("Remove","Removing trigger " + triggerId);
        TriggerListener toRemove = triggerlisteners.get(triggerId);

        if(toRemove != null) {
            toRemove.unsubscribeFromTrigger();
            triggerlisteners.remove(triggerId);
        }
        //It's a location or activity trigger to be removed
        else{
            //Look in the list of Location Triggers (i.e., GeofenceListeners)
            Iterator<Map.Entry<String,GeofenceListener>> locIter = geofencelisteners.entrySet().iterator();
            while(locIter.hasNext()){
                Map.Entry<String, GeofenceListener> listener = locIter.next();
                if(listener.getValue().getTriggerId().equals(triggerId)){
                    geofencelisteners.remove(listener.getKey());
                    geofenceTriggerIds.remove(triggerId);
                    listener.getValue().removeLocationTrigger();
                    return;
                }
            }
            //Look in the list of Activity Triggers (i.e., ActivityListeners)
            Iterator<Map.Entry<String,ActivityListener>> actIter = activitylisteners.entrySet().iterator();
            while(actIter.hasNext()){
                Map.Entry<String,ActivityListener> listener = actIter.next();
                if(listener.getValue().getTriggerId().equals(triggerId)){
                    activitylisteners.remove(listener.getKey());
                    activityTriggerIds.remove(triggerId);
                    listener.getValue().removeActivityTrigger();
                    return;
                }
            }
        }
    }

}

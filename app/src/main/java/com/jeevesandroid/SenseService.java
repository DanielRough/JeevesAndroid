package com.jeevesandroid;

import android.Manifest;
import android.app.IntentService;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationResult;
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
import com.jeevesandroid.sensing.sensormanager.sensors.SensorUtils;
import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.config.GlobalState;
import com.jeevesandroid.triggers.config.TriggerConfig;
import com.jeevesandroid.triggers.triggers.TriggerUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SenseService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final HashMap<String, TriggerListener> triggerlisteners = new HashMap<>();
    public static final HashMap<Integer, SensorListener> sensorlisteners = new HashMap<>();
    private static final HashMap<String, GeofenceListener> geofencelisteners = new HashMap<>();
    private List<String> geofenceTriggerIds = new ArrayList<>();
    private List<String> activityTriggerIds = new ArrayList<>();
    private static final HashMap<String, ActivityListener> activitylisteners = new HashMap<>();
    public static final HashMap<Integer,Integer> subscribedSensors = new HashMap<>();
    private static final String ACTION_1 = "action_1";
    private static final int NOTIF_ID = 1337;
    private static final int ACTIVE = 1234;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiClient;

    private void createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(120000);
        mLocationRequest.setFastestInterval(60000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.d("LOCATION","Location is " + location.getLongitude() + "," + location.getLatitude());
                HashMap<String, Object> locData = new HashMap<>();
                String mLastUpdateTime = new Date().toString();
                locData.put("senseStartTimeMillis", mLastUpdateTime);
                locData.put("latitude", location.getLatitude());
                locData.put("longitude", location.getLongitude());
                DatabaseReference patientRef = FirebaseUtils.PATIENT_REF.child("sensordata").child("Location").push();
                patientRef.setValue(locData);
            }

            }


        };
        startLocationUpdates(mLocationRequest);

    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void startLocationUpdates(LocationRequest mLocationRequest) {
        //START THE (NOW SEPARATE) LOCATION SERVICE
        mFusedLocationClient = LocationServices
            .getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }
    /*
    public void onLocationChanged(Location location) {
        location.getLatitude();
        HashMap<String,Object> locData = new HashMap<>();
        String mLastUpdateTime = new Date().toString();
        locData.put("senseStartTimeMillis",mLastUpdateTime);
        locData.put("latitude",location.getLatitude());
        locData.put("longitude",location.getLongitude());
        DatabaseReference patientRef = FirebaseUtils.PATIENT_REF.child("sensordata").child("Location").push();
        patientRef.setValue(locData);
    }
*/
    private void startActivitySensing(){
        mGoogleApiClient = new GoogleApiClient.Builder(ApplicationContext.getContext())
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    private void stopActivitySensing(){
        mGoogleApiClient.disconnect();
    }
    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private final BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ApplicationContext.STARTACTIVITY)) {
                startActivitySensing();
                Log.d("STARTACT","Starting activity sensing");
            }
            else {
                stopActivitySensing();
                Log.d("STOPACT","Stopping activity sensing");
            }
        }
    };
    private final BroadcastReceiver sensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int sensorType = intent.getIntExtra("sensortype",0);
                int subid = intent.getIntExtra("subid",0);
                ESSensorManager.getSensorManager(ApplicationContext.getContext())
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
            if (intent.getAction().equals(ApplicationContext.STARTLOC)) {
                createLocationRequest();
                Log.d("LOCATION","Starting location updates");
            }
            else{
                stopLocationUpdates();
                Log.d("LOCATIONSTOP","Stopping location updates");
            }
        }
    };

    /*
    static {
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_MICROPHONE,
            new SensorListener(SensorUtils.SENSOR_TYPE_MICROPHONE));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_STEP_COUNTER,
            new SensorListener(SensorUtils.SENSOR_TYPE_STEP_COUNTER));
    }
    */

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

    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(SenseService.NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Context app = ApplicationContext.getContext();
            String action = intent.getAction();
            if (ACTION_1.equals(action)) {
                //Followed by an intent to actually start our survey!
                NotificationManager manager = (NotificationManager) app
                    .getSystemService(NOTIFICATION_SERVICE);
                manager.cancel(NOTIF_ID);
                //Set that we no longer have the notification actives
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

    /*
    private SenseService getInstance(){
        return this;
    }
    */
    private static final String NOTIFICATION_Service_CHANNEL_ID = "service_channel";
    @Override
    public void onCreate() {
        Notification n = buildForegroundNotification();
        //New code to cope with specifying the channel ID
        if(Build.VERSION.SDK_INT>=26) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_Service_CHANNEL_ID, "Sync Service", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Service Name");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
            .getDefaultSharedPreferences(ApplicationContext.getContext());
        String studyname = varPrefs.getString(ApplicationContext.STUDY_NAME, "");
        DatabaseReference projectRef = database
            .getReference(FirebaseUtils.PUBLIC_KEY)
            .child(FirebaseUtils.PROJECTS_KEY)
            .child(studyname);
        String uid = varPrefs.getString(ApplicationContext.UID, "");
        IntentFilter mIntentFilter = new IntentFilter(ApplicationContext.STARTLOC);
        mIntentFilter.addAction(ApplicationContext.STOPLOC);
        IntentFilter sensIntentFilter = new IntentFilter(ApplicationContext.STOPSENSOR);
        IntentFilter activityIntentFilter = new IntentFilter(ApplicationContext.STARTACTIVITY);
        activityIntentFilter.addAction(ApplicationContext.STOPACTIVITY);
        //Activity and location detection are now done differently
        ApplicationContext.getContext().registerReceiver(sensorReceiver, sensIntentFilter);
        ApplicationContext.getContext().registerReceiver(locationReceiver, mIntentFilter);
        ApplicationContext.getContext().registerReceiver(activityReceiver, activityIntentFilter);

        //We also listen on the project ref in here (this is a constant listener as opposed to
        //the one in the MainActivity
        projectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FirebaseProject post = snapshot.getValue(FirebaseProject.class);
                ApplicationContext.setCurrentproject(post);
                if (post == null) {
                    return;
                }
                updateConfig(post);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
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

    //This is the main method in which the required project is pulled from Firebase and interpreted
    private void updateConfig(FirebaseProject app) {

        final List<FirebaseTrigger> triggers = app.gettriggers();
        final List<FirebaseVariable> variables = app.getvariables();
        final List<String> sensors = app.getsensors();

        SharedPreferences varPrefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        SharedPreferences.Editor prefseditor = varPrefs.edit();

        prefseditor.apply();

        SharedPreferences.OnSharedPreferenceChangeListener mListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
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
                                    Log.d("RELAUNCH", "Relaunching trigger " + trig.gettriggerId());
                                    launchTrigger(trig);
                                }
                            }
                            break;

                    }
                }
            }

            }
        };
        varPrefs.registerOnSharedPreferenceChangeListener(mListener);


        //This reloads any existing set of triggerids we have
        Set<String> newset = (varPrefs.getStringSet("triggerids",new HashSet()));
        HashSet<String> mynewset = new HashSet<>(newset);
        ArrayList<String> triggerids = new ArrayList<>(mynewset);
        for(FirebaseVariable var : variables){
            String type = var.getvartype();
            Log.d("FOUNDVAR","Found a var called " + var.getname() + " with type " + var.getvartype());
            //if(varPrefs.contains(var.getname()))continue;
            switch(type){
                case FirebaseUtils.TIME:
                case FirebaseUtils.DATE:
                case FirebaseUtils.NUMERIC:
                    if(var.getisRandom()){
                        Log.d("NAME","var name is " + var.getname());
                        Log.d("VARS","random vars are "+ var.getrandomOptions());
                        List<String> randomVals = var.getrandomOptions();
                        double lowest = Double.parseDouble(randomVals.get(0));
                        double highest = Double.parseDouble(randomVals.get(1));
                        double range = (highest-lowest)+1;
                        String answer = Long.toString((long)(Math.random()*range + lowest));
                        prefseditor.putString(var.getname(),answer);
                        Log.d("PUT VAR","Put var " + var.getname() + " with value " + answer);
                        break;
                    }
                   // if(varPrefs.getLong(var.getname(),0) != 0)break;
                case FirebaseUtils.LOCATION:
                    //We probably want to get their location in here
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
        Toast.makeText(ApplicationContext.getContext(),"Updated app configuration",Toast.LENGTH_SHORT).show();
        ArrayList<String> newIds = new ArrayList<>();
        for (int i = 0; i < triggers.size(); i++) {
            FirebaseTrigger triggerconfig = triggers.get(i);
            String triggerId = triggerconfig.gettriggerId();
            //A compromise that will refresh a trigger if its listener is null
            newIds.add(triggerId);
            if (!triggerids.contains(triggerId)) { //Don't relaunch an already-existing trigger
                launchTrigger(triggerconfig);
            }
            else if(triggerids.contains(triggerId)
                && (triggerlisteners.get(triggerId) == null)
//                && (sensorlisteners.get(triggerId) == null))
                && !geofenceTriggerIds.contains(triggerId)
                && !activityTriggerIds.contains(triggerId))
            {
                launchTrigger(triggerconfig);
            }
        }
        //Find all the old Trigger IDs that are not in the 'new' Trigger IDs, and get rid of them
        for (String toRemove : triggerids) {
            if(!newIds.contains(toRemove))
                removeTrigger(toRemove);
        }
        triggerids = newIds;
        //This saves our current set of triggerids
        Set<String> triggerset = new HashSet<>(triggerids);
        prefseditor.putStringSet("triggerids",triggerset);
        prefseditor.commit();
        try {
            GlobalState triggerState = GlobalState.getGlobalState(this);
            triggerState.setNotificationCap(199);
        } catch (TriggerException e) {
            e.printStackTrace();
        }
    }

    private void launchTrigger(FirebaseTrigger trigger) {
        String triggerType = trigger.getname();
        String triggerId = trigger.gettriggerId();
        Log.d("LAUNTRIG","Launching trigger " + trigger.getname() + ", " + triggerId);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());

        //Awkward bit of <code></code>
        //IF we already completed this starting survey, there's no need to relaunch it again
        try {
            Log.d("AMFIN?"," " + prefs.getBoolean(ApplicationContext.FINISHED_INTRODUCTION,false));
            if(prefs.getBoolean(ApplicationContext.FINISHED_INTRODUCTION,false)
                && TriggerUtils.getTriggerType(triggerType) == TriggerUtils.TYPE_CLOCK_TRIGGER_BEGIN)
                return;
        } catch (TriggerException e) {
            e.printStackTrace();
        }
        Map<String, Object> params = trigger.getparams();
        List<FirebaseAction> actions = new ArrayList<>();
        if (trigger.getactions() != null)
            actions = trigger.getactions();

        //Then this is a special Location Trigger and we handle things a bit differently
        try {
            if (TriggerUtils.getTriggerType(triggerType) == TriggerUtils.TYPE_SENSOR_TRIGGER_LOCATION) {
                ArrayList<FirebaseAction> toExecute = new ArrayList<>(actions);
                GeofenceListener newListener;
                String changes = params.get("change").toString();
                FirebaseExpression locexpr = trigger.getlocation();
                String locationName = "";
                if (locexpr != null) {
                    locationName = locexpr.getname();
                    newListener = new GeofenceListener(this, locationName, triggerId, changes, toExecute);
                    Log.d("NEWLISTEN", "New location added to " + locexpr.getname());
                    Log.d("THIS IS", "in the place " + locexpr.getparams());
                    geofencelisteners.put(locationName, newListener);
                    geofenceTriggerIds.add(triggerId);
                    newListener.addLocationTrigger();
                }
                return;
            }

            //Need to make sure it's not an Activity trigger, whih also works differently
            if (TriggerUtils.getTriggerType(triggerType) == TriggerUtils.TYPE_SENSOR_TRIGGER_IMMEDIATE
                && params.get("selectedSensor").equals("Activity")) {
                ActivityListener newListener;
                ArrayList<FirebaseAction> toExecute = new ArrayList<>(actions);
                String activityType = params.get("result").toString();
                newListener = new ActivityListener(this,activityType,triggerId,toExecute);
                Log.d("New activity","Added a new activity trigger");
                activitylisteners.put(activityType,newListener);
                activityTriggerIds.add(triggerId);
                newListener.addActivityTrigger();
                return;
            }

        }
        catch (TriggerException e) {
            e.printStackTrace();
        }
        //Otherwise carry on as normal
        TriggerListener newListener = null;
        try {
            newListener = new TriggerListener(TriggerUtils.getTriggerType(triggerType), this);
        } catch (TriggerException e) {
            e.printStackTrace();
        }
        triggerlisteners.put(triggerId, newListener);

        //The 'TriggerConfig' has all the necessary parameters to determine under what conditions we fire off this trigger
        TriggerConfig config = new TriggerConfig();
        SharedPreferences varPrefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());

        ArrayList<Long> times = new ArrayList<>();
        if(trigger.gettimes() != null){
            for(FirebaseExpression time : trigger.gettimes()){
                if(time.getisValue()){
                    times.add(Long.parseLong(time.getvalue()));
                }
                else{
                    times.add(Long.parseLong(varPrefs.getString(time.getname(),"0")));
                }
            }
            config.addParameter("times",times);
        }
        if(trigger.getparams() != null) {
            for (String param : params.keySet()) {
                Object value = params.get(param);
                config.addParameter(param, value);
            }
        }
        //Now try and find what variables we have
        //If we have the 'dateFrom' variable for example, this overrides the 'dateFrom' value in the parameters
        if(trigger.getdateFrom() != null){
            String name = trigger.getdateFrom().getname();
            config.addParameter(TriggerConfig.FROM_DATE,varPrefs.getString(name,"0"));
        }
        if(trigger.gettimeFrom() != null){
            String name = trigger.gettimeFrom().getname();
           config.addParameter(TriggerConfig.LIMIT_BEFORE_HOUR,varPrefs.getString(name,"0"));
        }
        if(trigger.getdateTo() != null){
            String name = trigger.getdateTo().getname();
           config.addParameter(TriggerConfig.TO_DATE,varPrefs.getString(name,"0"));
        }
        if(trigger.gettimeTo() != null){
            String name = trigger.gettimeTo().getname();
           config.addParameter(TriggerConfig.LIMIT_AFTER_HOUR,varPrefs.getString(name,"0"));
        }

        //Here we make the list of actions that this trigger executes when it's activated
        ArrayList<FirebaseAction> toExecute = new ArrayList<>();
        for (int i = 0; i < actions.size(); i++) {
            toExecute.add( actions.get(i));

        }
        newListener.subscribeToTrigger(config, toExecute);
    }

    //TODO: IDs aren't stored in 'geofencelisteners' or 'activitylisteners so idk if they get deleted properly
    private void removeTrigger(String triggerId) {
        Log.d("TRIGREMOVE","Removing trigger " + triggerId);
        TriggerListener toRemove = triggerlisteners.get(triggerId);

        if(toRemove != null) {
            toRemove.unsubscribeFromTrigger();
            triggerlisteners.remove(triggerId);
        }
        //It's a location or activity trigger to be removed
        else{
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

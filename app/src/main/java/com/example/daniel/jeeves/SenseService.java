package com.example.daniel.jeeves;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.daniel.jeeves.actions.FirebaseAction;
import com.example.daniel.jeeves.firebase.FirebaseExpression;
import com.example.daniel.jeeves.firebase.FirebaseProject;
import com.example.daniel.jeeves.firebase.FirebaseTrigger;
import com.example.daniel.jeeves.firebase.FirebaseUtils;
import com.example.daniel.jeeves.firebase.UserVariable;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.sensors.SensorUtils;
import com.ubhave.triggermanager.TriggerException;
import com.ubhave.triggermanager.config.GlobalState;
import com.ubhave.triggermanager.config.TriggerConfig;
import com.ubhave.triggermanager.triggers.TriggerUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.daniel.jeeves.ApplicationContext.FINISHED_INTRODUCTION;
import static com.example.daniel.jeeves.ApplicationContext.STUDY_NAME;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.BOOLEAN;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.DATE;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.LOCATION;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.NUMERIC;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.PROJECTS_KEY;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.PUBLIC_KEY;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.TEXT;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.TIME;

public class SenseService extends Service{

    public ArrayList<String> triggerids = new ArrayList<String>();
    public ArrayList<String> sensorids = new ArrayList<String>();
    public static HashMap<String, TriggerListener> triggerlisteners = new HashMap<String, TriggerListener>();
    public static HashMap<Integer, SensorListener> sensorlisteners = new HashMap<Integer, SensorListener>();
    public static HashMap<String, GeofenceListener> geofencelisteners = new HashMap<>();
    public static HashMap<Integer,Integer> subscribedSensors = new HashMap<Integer,Integer>();
    public static final String ACTION_1 = "action_1";
    public static final String ACTION_2 = "action_2";
    public static final int NOTIF_ID = 1337;
    private final IBinder mBinder = new LocalBinder();
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private BroadcastReceiver mReceiver;
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        SenseService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SenseService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    public void startLocationUpdates(LocationRequest mLocationRequest) {
        //START THE (NOW SEPARATE) LOCATION SERVICE

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }
    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
    static {
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_ACCELEROMETER, new SensorListener(SensorUtils.SENSOR_TYPE_ACCELEROMETER));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_BLUETOOTH, new SensorListener(SensorUtils.SENSOR_TYPE_BLUETOOTH));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_MICROPHONE, new SensorListener(SensorUtils.SENSOR_TYPE_MICROPHONE));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_WIFI, new SensorListener(SensorUtils.SENSOR_TYPE_WIFI));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_BATTERY, new SensorListener(SensorUtils.SENSOR_TYPE_BATTERY));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_CONNECTION_STATE, new SensorListener(SensorUtils.SENSOR_TYPE_CONNECTION_STATE));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_PHONE_STATE, new SensorListener(SensorUtils.SENSOR_TYPE_PHONE_STATE));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_SCREEN, new SensorListener(SensorUtils.SENSOR_TYPE_SCREEN));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_SMS, new SensorListener(SensorUtils.SENSOR_TYPE_SMS));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_INTERACTION, new SensorListener(SensorUtils.SENSOR_TYPE_INTERACTION));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_STEP_COUNTER, new SensorListener(SensorUtils.SENSOR_TYPE_STEP_COUNTER));
    }
    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(SenseService.NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Context app = ApplicationContext.getContext();
            String action = intent.getAction();
            Log.d("HERE,","AM I HERE");
            if (ACTION_1.equals(action)) {
                //Followed by an intent to actually start our survey!
                NotificationManager manager = (NotificationManager) app.getSystemService(app.NOTIFICATION_SERVICE);
                manager.cancel(NOTIF_ID);
                //Set that we no longer have the notification actives
                SharedPreferences varPrefs = PreferenceManager.getDefaultSharedPreferences(app);
                SharedPreferences.Editor editor = varPrefs.edit();
                editor.putBoolean("active",false);
                editor.commit();
                //
                Intent resultIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                this.startActivity(resultIntent);

            }

        }
    }
    SharedPreferences.OnSharedPreferenceChangeListener mListener;

    public SenseService getInstance(){
        return this;
    }

    @Override
    public void onCreate(){
        final FirebaseDatabase database = FirebaseUtils.getDatabase();
        SharedPreferences varPrefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        String studyname = varPrefs.getString(STUDY_NAME, "");
        DatabaseReference projectRef = database.getReference(PUBLIC_KEY).child(PROJECTS_KEY).child(studyname);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
         //           Toast.makeText(getInstance(),"LOCATION UPDATE" + location.getLatitude() +"," + location.getLongitude(),Toast.LENGTH_SHORT).show();

                }
            };
        };
        //We also listen on the project ref in here (this is a constant listener as opposed to
        //the one in the MainActivity
        projectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                FirebaseProject post = snapshot.getValue(FirebaseProject.class);
                ApplicationContext.setCurrentproject(post);
                if(post == null){
                    Toast.makeText(getInstance(),"OH NO IT WAS NULL",Toast.LENGTH_SHORT).show();
                    Log.d("OH NO","IT WAS NULL");
                    return;
                }
                try{
                    updateConfig(post);
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });



        //Amazingly we can have a shared preferences listener!!!



        //This is a broadcast receiver that is notified whenever the user's variables have changed in response to a survey
        //Because some triggers' configuration is dependent on these variables, we need to reset them when, for example,
        //the start or end date has changed
//         mReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                if(action.equals(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER)){
//                    ArrayList<String> changedVariables = intent.getStringArrayListExtra("changedVariables");
//                    //Of course, it might be null if we didn't actually finish the survey on time
//                    if(changedVariables == null)return;
//                    Log.d("CHANGEDVARS","Changed variables are " + changedVariables);
//                    //action for sms received
//                    List<FirebaseTrigger> triggers = ApplicationContext.getProject().gettriggers();
//                    for (int i = 0; i < triggers.size(); i++) {
//                        FirebaseTrigger triggerconfig = triggers.get(i);
//                        List<String> configVariables = triggerconfig.getvariables();
//                        Log.d("CONFIGVARS","Config variables are " + configVariables);
//                        if(configVariables == null)continue;
//                        for(String var : configVariables){
//                            if(changedVariables.contains(var)){
//                                Log.d("UPDATEDVAR","This trigger has a variable that needs updating!");
//                                //Get rid of and relaunch!
//                                removeTrigger(triggerconfig.gettriggerId());
//                                launchTrigger(triggerconfig);
//                            }
//                        }
//
//                    }
//                }
//            }
//        };
    //    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
     //   filter.addAction(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER);
      //  this.registerReceiver(mReceiver, filter);
    }
//
//    @Override
//    public void onDestroy(){
//        unregisterReceiver(mReceiver);
//    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    //This is the main method in which the required project is pulled from Firebase and interpreted
    public void updateConfig(FirebaseProject app) throws JSONException {

      //  ApplicationContext.setCurrentproject(app);
        final List<FirebaseTrigger> triggers = app.gettriggers();
        final List<UserVariable> variables = app.getvariables();
        final List<String> sensors = app.getsensors();
        SharedPreferences varPrefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        SharedPreferences.Editor prefseditor = varPrefs.edit();

        sensorids = new ArrayList<>(varPrefs.getStringSet("sensorids",new HashSet()));

        ArrayList<String> newSensors = new ArrayList<String>();
        for(String sensor : sensors){
            try {
                newSensors.add(sensor);
                int sensorType= SensorUtils.getSensorType(sensor);
                SensorListener listener = sensorlisteners.get(sensorType);
                if (!sensorids.contains(sensor)) {
                    Log.d("Sensor", "Away to try and subscribe to " + sensorType);
                    int subscriptionId = ESSensorManager.getSensorManager(this).subscribeToSensorData(sensorType, listener);
                    subscribedSensors.put(sensorType, subscriptionId);
                    Log.d("SUCCESS", "Successfully subscribed to " + sensorType);
                    sensorids.add(sensor);
                }
                } catch (ESException e) {
                e.printStackTrace();
            }
        }
        for(String toRemove : sensorids){
            if(!newSensors.contains(toRemove)){
                int sensorType = 0;
                try {
                    sensorType = SensorUtils.getSensorType(toRemove);
                    ESSensorManager.getSensorManager(this).unsubscribeFromSensorData(subscribedSensors.get(sensorType));
                    sensorlisteners.remove(sensorType);
                } catch (ESException e) {
                    e.printStackTrace();
                }

            }
        }
        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                for(UserVariable var : variables){
                    if(var.getname().equals(key)){
                        Log.d("ONE","One");
                        switch(var.getvartype()){ //Okay we've updated a variable, now what exactly do we want to do about that?
                            //If it's a location variable, we also want to add a geofence
                            //If it's a time or a date variable, we want to re-evaluate the time triggers.
                            //Any other variable gets evaluated on the fly when the trigger is executed
                            case LOCATION:
                                if(geofencelisteners.containsKey(var.getname())){
                                    GeofenceListener locListener = geofencelisteners.get(var.getname());
                                    locListener.updateLocation();
                                }
                                else {
                                    GeofenceListener newListener = new GeofenceListener(getInstance(), var.getname(), new ArrayList<FirebaseAction>());
                                    geofencelisteners.put(var.getname(), newListener);
                                    newListener.addLocationTrigger();
                                }
                                break;
                            case TIME:
                            case DATE:
                                for(FirebaseTrigger trig : triggers){
                                    if(trig.getvariables() != null && trig.getvariables().contains(key)){
                                        removeTrigger(trig.gettriggerId());
                                        Log.d("RELAUNCH","Relaunching trigger " + trig.gettriggerId());
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
        triggerids = new ArrayList<>(varPrefs.getStringSet("triggerids",new HashSet()));
        for(UserVariable var : variables){
            String type = var.getvartype();
            if(varPrefs.contains(var.getname()))continue; //Don't reset any variables that already have values
            switch(type){
                case TIME:
                case DATE:
                case NUMERIC:
                   // if(varPrefs.getLong(var.getname(),0) != 0)break;
                    prefseditor.putString(var.getname(),"0"); break;
                case LOCATION:
                    //We probably want to get their location in here
                case TEXT:
                    prefseditor.putString(var.getname(),""); break;
                case BOOLEAN :
                    prefseditor.putBoolean(var.getname(),false); break;
            }
        }
        Log.d("UPDATING","Updating the config");
        Toast.makeText(ApplicationContext.getContext(),"Updated app configuration",Toast.LENGTH_SHORT).show();
        ArrayList<String> newIds = new ArrayList<>();

        for (int i = 0; i < triggers.size(); i++) {
            FirebaseTrigger triggerconfig = triggers.get(i);
            String triggerId = triggerconfig.gettriggerId();
            newIds.add(triggerId);
            if (!triggerids.contains(triggerId)) { //Don't relaunch an already-existing trigger
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
        Set triggerset = new HashSet(triggerids);
        prefseditor.putStringSet("triggerids",triggerset);
        prefseditor.commit();
        try {
            GlobalState triggerState = GlobalState.getGlobalState(this);
            triggerState.setNotificationCap(199); //TODO: Figure out why it's 199, should it be? Should the user specify this?
        } catch (TriggerException e) {
            e.printStackTrace();
        }
    }

    //Here we actually 'launch' the trigger, i.e. activate it so that it performs its actions when the necessary conditions are met
    private void launchTrigger(FirebaseTrigger trigger) {
        Log.d("TRIGLAUNCH","Launching trigger " + trigger.getname() + trigger.gettriggerId());
        String triggerType = trigger.getname();
        String triggerId = trigger.gettriggerId();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());

        //Awkward bit of <code></code>
        //IF we already completed this starting survey, there's no need to relaunch it again!
        try {
            if(prefs.getBoolean(FINISHED_INTRODUCTION,false) && TriggerUtils.getTriggerType(triggerType) == TriggerUtils.TYPE_CLOCK_TRIGGER_BEGIN)
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
            if(TriggerUtils.getTriggerType(triggerType) == TriggerUtils.TYPE_SENSOR_TRIGGER_LOCATION){
                GeofenceListener newListener = null;
                String locationName = params.get("result").toString();
                if(geofencelisteners.containsKey(locationName)){
                    newListener = geofencelisteners.get(locationName);
                    newListener.updateActions(actions);
                }
                else{
                    newListener = new GeofenceListener(this,locationName,actions);
                    geofencelisteners.put(locationName, newListener);
                    newListener.addLocationTrigger();
                }
                return;
            }
        } catch (TriggerException e) {
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
               //     Log.d("IT IS",Long.parseLong(varPrefs.getString(time.getname(),"0"));
                    times.add(Long.parseLong(varPrefs.getString(time.getname(),"0")));
                }
            }
            config.addParameter("times",times);
        }
        if(trigger.getparams() != null) {
            Iterator<String> keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String param = keys.next();
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
        newListener.subscribeToTrigger(config, toExecute, triggerId);
    }

    //Here we do the opposite. We deactivate the trigger and remove its functionality
    private void removeTrigger(String triggerId) {
        Log.d("TRIGREMOVE","Removing trigger " + triggerId);
        TriggerListener toRemove = triggerlisteners.get(triggerId);
        if(toRemove != null) {
            toRemove.unsubscribeFromTrigger("this");
            triggerlisteners.remove(triggerId);
        }
    }

}

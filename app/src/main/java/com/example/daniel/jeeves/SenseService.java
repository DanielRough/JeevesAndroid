package com.example.daniel.jeeves;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.daniel.jeeves.actions.FirebaseAction;
import com.example.daniel.jeeves.firebase.FirebaseExpression;
import com.example.daniel.jeeves.firebase.FirebaseProject;
import com.example.daniel.jeeves.firebase.FirebaseTrigger;
import com.example.daniel.jeeves.firebase.FirebaseUtils;
import com.example.daniel.jeeves.firebase.UserVariable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ubhave.sensormanager.sensors.SensorUtils;
import com.ubhave.triggermanager.TriggerException;
import com.ubhave.triggermanager.config.GlobalState;
import com.ubhave.triggermanager.config.TriggerConfig;
import com.ubhave.triggermanager.config.TriggerManagerConstants;
import com.ubhave.triggermanager.triggers.TriggerUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    public static HashMap<String, TriggerListener> triggerlisteners = new HashMap<String, TriggerListener>();
    public static HashMap<Integer, SensorListener> sensorlisteners = new HashMap<Integer, SensorListener>();

    static {
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_BLUETOOTH, new SensorListener(SensorUtils.SENSOR_TYPE_BLUETOOTH));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_MICROPHONE, new SensorListener(SensorUtils.SENSOR_TYPE_MICROPHONE));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_WIFI, new SensorListener(SensorUtils.SENSOR_TYPE_WIFI));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_BATTERY, new SensorListener(SensorUtils.SENSOR_TYPE_BATTERY));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_CONNECTION_STATE, new SensorListener(SensorUtils.SENSOR_TYPE_CONNECTION_STATE));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_PHONE_STATE, new SensorListener(SensorUtils.SENSOR_TYPE_PHONE_STATE));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_PROXIMITY, new SensorListener(SensorUtils.SENSOR_TYPE_PROXIMITY));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_SCREEN, new SensorListener(SensorUtils.SENSOR_TYPE_SCREEN));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_SMS, new SensorListener(SensorUtils.SENSOR_TYPE_SMS));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_LIGHT, new SensorListener(SensorUtils.SENSOR_TYPE_LIGHT));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_INTERACTION, new SensorListener(SensorUtils.SENSOR_TYPE_INTERACTION));
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_STEP_COUNTER, new SensorListener(SensorUtils.SENSOR_TYPE_STEP_COUNTER));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       final FirebaseDatabase database = FirebaseDatabase.getInstance();
        SharedPreferences varPrefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        SharedPreferences.Editor prefseditor = varPrefs.edit();

        String studyname;
        if(intent != null) { //intent will be null if this service gets restarted
            studyname = intent.getStringExtra("studyname");
            prefseditor.putString("studyname",studyname); //add our current study name to the shared preferences
        }
        else {
            studyname = varPrefs.getString("studyname", "");
        }
        //Find the project config in the 'public' section of the database
        DatabaseReference projectRef = database.getReference(PUBLIC_KEY).child(PROJECTS_KEY).child(studyname);

        projectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                FirebaseProject post = snapshot.getValue(FirebaseProject.class);
                ApplicationContext.setCurrentproject(post);
                String developerid = post.getresearcherno();
                FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                FirebaseUtils.PATIENT_REF = database.getReference(FirebaseUtils.PRIVATE_KEY).child(developerid).child(FirebaseUtils.PATIENTS_KEY).child(mFirebaseUser.getUid());
                //I don't like it but it should hopefully stop any errors
                try {
                    updateConfig(post);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        //This is a broadcast receiver that is notified whenever the user's variables have changed in response to a survey
        //Because some triggers' configuration is dependent on these variables, we need to reset them when, for example,
        //the start or end date has changed
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER)){
                    ArrayList<String> changedVariables = intent.getStringArrayListExtra("changedVariables");
                    Log.d("CHANGEDVARS","Changed variables are " + changedVariables);
                    //action for sms received
                    List<FirebaseTrigger> triggers = ApplicationContext.getProject().gettriggers();
                    for (int i = 0; i < triggers.size(); i++) {
                        FirebaseTrigger triggerconfig = triggers.get(i);
                        List<String> configVariables = triggerconfig.getvariables();
                        Log.d("CONFIGVARS","Config variables are " + configVariables);
                        if(configVariables == null)continue;
                        for(String var : configVariables){
                            if(changedVariables.contains(var)){
                                Log.d("UPDATEDVAR","This trigger has a variable that needs updating!");
                                //Get rid of and relaunch!
                                removeTrigger(triggerconfig.gettriggerId());
                                launchTrigger(triggerconfig);
                            }
                        }

                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER);
        this.registerReceiver(receiver, filter);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //This is the main method in which the required project is pulled from Firebase and interpreted
    public void updateConfig(FirebaseProject app) throws JSONException {
        Log.d("Isnull?","Post is null?" + (app == null));

        ApplicationContext.setCurrentproject(app);
        List<FirebaseTrigger> triggers = app.gettriggers();
        List<UserVariable> variables = app.getvariables();
        SharedPreferences varPrefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        SharedPreferences.Editor prefseditor = varPrefs.edit();

        for(UserVariable var : variables){
            String type = var.getvartype();
            switch(type){
                case TIME:
                case DATE:
                case NUMERIC:
                    prefseditor.putLong(var.getname(),0); break;
                case LOCATION:
                case TEXT:
                    prefseditor.putString(var.getname(),""); break;
                case BOOLEAN : prefseditor.putBoolean(var.getname(),false); break;
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
        Map<String, Object> params = trigger.getparams();
        List<FirebaseAction> actions = new ArrayList<>();
        if (trigger.getactions() != null)
            actions = trigger.getactions();

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

        ArrayList<Integer> times = new ArrayList<>();
        if(trigger.gettimes() != null){
            for(FirebaseExpression time : trigger.gettimes()){
                if(time.getisValue()){
                    times.add(Integer.parseInt(time.getvalue()));
                }
                else{
                    times.add(Integer.parseInt(varPrefs.getString(time.getname(),"0")));
                }
            }
            config.addParameter("times",times);
        }
        if(trigger.getparams() != null) {
            Iterator<String> keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String param = keys.next();
                Object value = params.get(param);
                if (param.equals("result")) { //This is a sensor trigger
                    if (value instanceof Map) { //Then the result value is a map, meaning it's a user variable

                    }
                }
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

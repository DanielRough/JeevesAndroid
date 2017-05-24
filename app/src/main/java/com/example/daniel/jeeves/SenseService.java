package com.example.daniel.jeeves;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.daniel.jeeves.actions.FirebaseAction;
import com.example.daniel.jeeves.firebase.FirebaseProject;
import com.example.daniel.jeeves.firebase.FirebaseTrigger;
import com.example.daniel.jeeves.firebase.UserVariable;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ubhave.sensormanager.sensors.SensorUtils;
import com.ubhave.triggermanager.TriggerException;
import com.ubhave.triggermanager.config.GlobalState;
import com.ubhave.triggermanager.config.TriggerConfig;
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
import static com.example.daniel.jeeves.firebase.FirebaseUtils.MAIN_KEY;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.NUMERIC;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.PROJECTS_KEY;
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
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(MAIN_KEY);
        String studyname = intent.getStringExtra("studyname");

        DatabaseReference projectRef = myRef.child(PROJECTS_KEY).child(studyname);

        projectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                FirebaseProject post = snapshot.getValue(FirebaseProject.class);
                ApplicationContext.setCurrentproject(post);
                try {
                    updateConfig(post);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //This is the main method in which the required project is pulled from Firebase and interpreted
    public void updateConfig(FirebaseProject app) throws JSONException {
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

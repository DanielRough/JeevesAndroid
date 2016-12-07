package com.example.daniel.jeeves;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.daniel.jeeves.actions.FirebaseAction;
import com.example.daniel.jeeves.firebase.FirebasePatient;
import com.example.daniel.jeeves.firebase.FirebaseProject;
import com.example.daniel.jeeves.firebase.FirebaseSurvey;
import com.example.daniel.jeeves.firebase.FirebaseTrigger;
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
import com.ubhave.triggermanager.triggers.TriggerUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This activity is begun when the 'Sense Data' button is pressed on the Launch screen.
 */
public class SenseActivity extends Activity {
    private static SenseActivity instance;
    FirebaseAuth mFirebaseAuth;
    FirebaseProject currentConfig = new FirebaseProject();
    TextView txtWelcome;

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
    protected void onPause(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
     //   preferences.edit().
        SharedPreferences.Editor editor = preferences.edit();
        Set set = new HashSet(triggerids);
        editor.putStringSet("triggerIds",set);
        editor.commit();
        super.onPause();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;

        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("JeevesData");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sense);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.contains("triggerIds")) {
            Set<String> trigs = prefs.getStringSet("triggerIds", null);
            triggerids = new ArrayList<>(trigs);
        }
        String studyname = getIntent().getStringExtra("studyname");
        txtWelcome = (TextView)findViewById(R.id.txtWelcome);

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        String id = user.getUid();
        final DatabaseReference patientRef = myRef.child("patients").child(id);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebasePatient post = dataSnapshot.getValue(FirebasePatient.class);
                txtWelcome.setText("Welcome, " + post.getname() + "!");
                patientRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        patientRef.addValueEventListener(postListener);

        Button btnContact = (Button) findViewById(R.id.btnContact);
        Button btnSurveys = (Button) findViewById(R.id.btnSurvey);
        Button btnMonitor = (Button) findViewById(R.id.btnMonitor);
        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance,ContactActivity.class);
                startActivity(intent);
            }
        });

        btnSurveys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance,MissedSurveyActivity.class);
                startActivity(intent);
            }
        });

        btnMonitor.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance,MonitorActivity.class);
                startActivity(intent);
            }
        });
        DatabaseReference projectRef = myRef.child("projects").child(studyname);

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

    }

    public void updateConfig(FirebaseProject app) throws JSONException {
        ApplicationContext.setCurrentproject(app);
        List<FirebaseTrigger> triggers = app.gettriggers();
        List<UserVariable> variables = app.getvariables();
        for(UserVariable var : variables){
            String type = var.getvartype();
//            switch(type){
//                case "Time" : editor.putString(var.getname(),var.getvalue()); break;
//                case "Boolean" : editor.putBoolean(var.getname(), Boolean.parseBoolean(var.getvalue())); break;
//                case "Text" : editor.putString(var.getname(),var.getvalue()); break;
//                case "Numeric" : editor.putLong(var.getname(), Long.parseLong(var.getvalue()));break;
//            }
        }
        ArrayList<String> newIds = new ArrayList<>();
        for (int i = 0; i < triggers.size(); i++) {
            FirebaseTrigger triggerconfig = triggers.get(i);
            String triggerId = triggerconfig.gettriggerId();
            newIds.add(triggerId);
            if (!triggerids.contains(triggerId)) { //Don't relaunch an already-existing trigger
                launchTrigger(triggerconfig);
            }
        }
        for (String toRemove : triggerids) {
            if(!newIds.contains(toRemove))
            removeTrigger(toRemove);
        }
        triggerids = newIds;
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
        ArrayList<FirebaseAction> toExecute = new ArrayList<>();
        for (int i = 0; i < actions.size(); i++) {
            toExecute.add( actions.get(i));
        }
        newListener.subscribeToTrigger(config, toExecute, triggerId);
    }

    private void removeTrigger(String triggerId) {
        TriggerListener toRemove = triggerlisteners.get(triggerId);
        if(toRemove != null) {
            toRemove.unsubscribeFromTrigger("this");
            triggerlisteners.remove(triggerId);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


}

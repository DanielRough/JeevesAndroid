package com.example.daniel.jeeves;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.daniel.jeeves.firebase.FirebasePatient;
import com.example.daniel.jeeves.firebase.FirebaseProject;
import com.example.daniel.jeeves.firebase.FirebaseSurvey;
import com.example.daniel.jeeves.firebase.FirebaseTrigger;
import com.example.daniel.jeeves.firebase.UserVariable;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
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

import com.example.daniel.jeeves.actions.FirebaseAction;


/**
 * This activity is begun when the 'Sense Data' button is pressed on the Launch screen.
 */
public class SenseActivity extends Activity {
    private static SenseActivity instance;
    Firebase myFirebaseRef;
    Firebase firebaseUserInfo;
    FirebaseProject currentConfig = new FirebaseProject();
    TextView txtWelcome;
    public static boolean hasSensorBegun, hasTriggerBegun;

    public String userid;
    public ArrayList<Long> triggerids = new ArrayList<Long>();
    public static HashMap<Long, TriggerListener> triggerlisteners = new HashMap<Long, TriggerListener>();
    public static HashMap<Integer, SensorListener> sensorlisteners = new HashMap<Integer, SensorListener>();

    static {
        sensorlisteners.put(SensorUtils.SENSOR_TYPE_BLUETOOTH, new SensorListener(SensorUtils.SENSOR_TYPE_BLUETOOTH));
        //  sensorlisteners.put(SensorUtils.SENSOR_TYPE_LOCATION, new SensorListener(SensorUtils.SENSOR_TYPE_LOCATION));
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

    //public static SenseActivity getMainActivity(){
//        return instance;
//    }
    private final long serviceInterval = 6000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;



        super.onCreate(savedInstanceState);
        //    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.configurations), Context.MODE_PRIVATE);
        setContentView(R.layout.activity_sense);

        txtWelcome = (TextView)findViewById(R.id.txtWelcome);
        userid = getIntent().getStringExtra("userid");
        Log.d("USERID", "User id is " + userid);
        firebaseUserInfo = new Firebase("https://incandescent-torch-8695.firebaseio.com/patients/"+userid);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("userprefs",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userid",userid);
        editor.commit(); //Save the current user for future reference
        //Get the user's additional info
        firebaseUserInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d("Snappyshot123", snapshot.getValue().toString());

                firebaseUserInfo.removeEventListener(this);
                FirebasePatient user = snapshot.getValue(FirebasePatient.class);
                txtWelcome.setText("Welcome, " + user.getfirstName());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
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
        myFirebaseRef = new Firebase("https://incandescent-torch-8695.firebaseio.com/JeevesData/projects/SimpleTest");
        Log.d("HEREWEGO", "Updating le config");
        myFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d("Snappyshot", snapshot.getValue().toString());
                FirebaseProject post = snapshot.getValue(FirebaseProject.class);
                try {
                    Log.d("UPDATING", "Updating le config");
                    updateConfig(post);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

    }

    public void updateConfig(FirebaseProject app) throws JSONException {
        ApplicationContext.setCurrentproject(app);
        List<FirebaseSurvey> surveys = app.getsurveys();
        List<FirebaseTrigger> currentTriggers = currentConfig.gettriggers();
        List<FirebaseTrigger> triggers = app.gettriggers();
        List<UserVariable> variables = app.getvariables();
        String researcherno = app.getresearcherno();
        SharedPreferences prefs = this.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("researcherno",researcherno);
        for(UserVariable var : variables){
            String type = var.getvartype();
            switch(type){
                case "Time" : editor.putString(var.getname(),var.getvalue()); break;
                case "Boolean" : editor.putBoolean(var.getname(), Boolean.parseBoolean(var.getvalue())); break;
                case "Text" : editor.putString(var.getname(),var.getvalue()); break;
                case "Numeric" : editor.putLong(var.getname(), Long.parseLong(var.getvalue()));break;
            }
        }
        editor.commit();
        ArrayList<Long> newIds = new ArrayList<Long>();
        for (int i = 0; i < triggers.size(); i++) {
            FirebaseTrigger triggerconfig = triggers.get(i);
            String triggertype = triggerconfig.gettype();
            long triggerId = triggerconfig.getid();
            newIds.add(triggerId);
            if (triggerids.contains(triggerId)) { //Don't relaunch an already-existing trigger
                triggerids.remove(triggerId);
                continue;
            }
            launchTrigger(triggerconfig);
        }
        for (long toRemove : triggerids) {
            removeTrigger(toRemove);
        }
        triggerids = newIds;
        //This should hopefully handle new, updated and deleted triggers
        try {
            GlobalState triggerState = GlobalState.getGlobalState(this);
            triggerState.setNotificationCap((int) app.getmaxNotifications());
            Log.d("Notifications", "Notification number increased to " + app.getmaxNotifications());
        } catch (TriggerException e) {
            e.printStackTrace();
        }
    }

    private void launchTrigger(FirebaseTrigger trigger) {
        Log.d("ADDING", "ADDING A TRIGGER");

        String triggerType = trigger.getname();
        long triggerId = trigger.getid();
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
        Iterator<String> keys = params.keySet().iterator();
        while (keys.hasNext()) {
            String param = keys.next();
            Log.d("WEHASAPARAM", param);
            Object value = null;
            value = params.get(param);
            if(param.equals("result")){ //This is a sensor trigger
                if(value instanceof Map){ //Then the result value is a map, meaning it's a user variable

                }
            }
            config.addParameter(param, value);
        }
        ArrayList<FirebaseAction> toExecute = new ArrayList<FirebaseAction>();
        for (int i = 0; i < actions.size(); i++) {
            toExecute.add((FirebaseAction) actions.get(i));
        }
        newListener.subscribeToTrigger(config, toExecute, triggerId);
    }

    private void removeTrigger(long triggerId) {
        Log.d("REMOVING", "REMOVIGN A TRIGGER " + triggerId);
        TriggerListener toRemove = triggerlisteners.get(triggerId);
        if(toRemove != null) {
            toRemove.unsubscribeFromTrigger("this");
            triggerlisteners.remove(triggerId);
        }
    }
}

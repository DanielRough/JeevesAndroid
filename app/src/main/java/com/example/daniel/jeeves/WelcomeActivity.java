package com.example.daniel.jeeves;

import android.app.Activity;
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
public class WelcomeActivity extends Activity {
    private static WelcomeActivity instance;
    FirebaseAuth mFirebaseAuth;
   // FirebaseProject currentConfig = new FirebaseProject();
    TextView txtWelcome;


//    @Override
//    protected void onPause(){
//        Log.d("PAUSED","Onpause");
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//     //   preferences.edit().
//        SharedPreferences.Editor editor = preferences.edit();
//        Set set = new HashSet(triggerids);
//        editor.putStringSet("triggerIds",set);
//        editor.commit();
//        super.onPause();
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        Log.d("RECREATED","Recreated the thing");
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("JeevesData");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sense);

//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        if(prefs.contains("triggerIds")) {
//            Set<String> trigs = prefs.getStringSet("triggerIds", null);
//            triggerids = new ArrayList<>(trigs);
//        }

        //--------------------------------
        //START THE SENSING SERVICE
        String studyname = getIntent().getStringExtra("studyname");
        Intent intent = new Intent(this, SenseService.class);
        intent.putExtra("studyname",studyname);
        startService(intent);
        //START THE SENSING SERVICE
        //--------------------------------

        txtWelcome = (TextView)findViewById(R.id.txtWelcome);
        txtWelcome.setText("Welcome, " + getIntent().getStringExtra("username"));


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

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


}

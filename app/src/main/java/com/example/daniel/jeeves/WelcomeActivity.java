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

import com.example.daniel.jeeves.firebase.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.daniel.jeeves.ApplicationContext.DEVELOPER_ID;
import static com.example.daniel.jeeves.ApplicationContext.UID;
import static com.example.daniel.jeeves.ApplicationContext.USERNAME;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.SURVEYS_KEY;

public class WelcomeActivity extends Activity {
    private static WelcomeActivity instance;
    FirebaseAuth mFirebaseAuth;
    TextView txtWelcome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;

        mFirebaseAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sense);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());

        //We MAY need to reset these
        if(FirebaseUtils.PATIENT_REF == null){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            FirebaseUtils.SURVEY_REF = database.getReference(FirebaseUtils.PRIVATE_KEY).child(prefs.getString(DEVELOPER_ID,"")).child(SURVEYS_KEY);
            FirebaseUtils.PATIENT_REF = database.getReference(FirebaseUtils.PRIVATE_KEY).child(prefs.getString(DEVELOPER_ID,"")).child(FirebaseUtils.PATIENTS_KEY).child(prefs.getString(UID,""));
        }
        //START THE SENSING SERVICE
        Intent intent = new Intent(this, SenseService.class);
        startService(intent);

        txtWelcome = (TextView)findViewById(R.id.txtWelcome);
        txtWelcome.setText("Welcome, " + prefs.getString(USERNAME,""));


        Button btnContact = (Button) findViewById(R.id.btnContact);
        Button btnSurveys = (Button) findViewById(R.id.btnSurvey);
        Button btnMonitor = (Button) findViewById(R.id.btnMonitor);
        Button btnLogout = (Button) findViewById(R.id.buttonLogout);
        Button btnVars = (Button) findViewById(R.id.buttonVars);
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
        btnLogout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                mFirebaseAuth = FirebaseAuth.getInstance();
                mFirebaseAuth.signOut();
                finish();
            }
        });
        btnVars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance,CheckVariablesActivity.class);
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

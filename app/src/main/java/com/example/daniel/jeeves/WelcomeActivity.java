package com.example.daniel.jeeves;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.daniel.jeeves.firebase.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


/**
 * This activity is begun when the 'Sense Data' button is pressed on the Launch screen.
 */
public class WelcomeActivity extends Activity {
    private static WelcomeActivity instance;
    FirebaseAuth mFirebaseAuth;
   // FirebaseProject currentConfig = new FirebaseProject();
    TextView txtWelcome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        Log.d("STUDYNAME","study name is " + getIntent().getStringExtra("studyname"));

        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
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

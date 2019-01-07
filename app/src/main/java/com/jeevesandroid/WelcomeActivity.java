package com.jeevesandroid;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jeevesandroid.firebase.FirebaseProject;
import com.jeevesandroid.firebase.FirebaseUtils;
import com.google.firebase.database.FirebaseDatabase;
import com.jwetherell.heart_rate_monitor.HeartRateMonitor;

import java.util.ArrayList;
import java.util.List;

import static com.jeevesandroid.ApplicationContext.DEVELOPER_ID;
import static com.jeevesandroid.ApplicationContext.STUDY_NAME;
import static com.jeevesandroid.ApplicationContext.UID;
import static com.jeevesandroid.ApplicationContext.USERNAME;

//03/08 I'm now doing location stuff in here as it's easier than sticking it in the Sensor Manager module.
public class WelcomeActivity extends Activity {
    private WelcomeActivity getInstance(){
        return this;
    }


    protected void permissionThings(){
        FirebaseProject currentProj = ApplicationContext.getProject();
        List<String> sensors = currentProj.getsensors();
        //Just check it always for now
        //checkPermission(Manifest.permission.CAMERA,REQUEST_CAMERA);
        List<String> requestList = new ArrayList<>();
        checkPermission(Manifest.permission.CAMERA,requestList);
        for(String sensor : sensors){                Log.d("SENSOR","Sensor is " + sensor);

            switch(sensor){
                case "Location":
                    checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,requestList);break;
                case "Microphone":
                    checkPermission(Manifest.permission.RECORD_AUDIO,requestList);break;
                case "SMS":
                    checkPermission(Manifest.permission.BROADCAST_SMS,requestList);break;
                case "Activity": break;
                case "Heart": break;
            }
        }
        String[] permissionArray = new String[requestList.size()];
        requestList.toArray(permissionArray);
        ActivityCompat.requestPermissions(this,
            permissionArray,
            1234);
    }
    @Override
    protected void onResume() {

        super.onResume();
        permissionThings();
        Log.d("RESUMPTION","I hath resumed");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d("CREATION","I hath CREATE");

        setContentView(R.layout.activity_sense);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        // Permission has already been granted
        //We MAY need to reset these
        if (FirebaseUtils.PATIENT_REF == null) {
            FirebaseDatabase database = FirebaseUtils.getDatabase();
            FirebaseUtils.SURVEY_REF = database.getReference(FirebaseUtils.PRIVATE_KEY).child(prefs.getString(DEVELOPER_ID, "")).child(FirebaseUtils.PROJECTS_KEY).child(prefs.getString(STUDY_NAME, "")).child(FirebaseUtils.SURVEYDATA_KEY);
            FirebaseUtils.PATIENT_REF = database.getReference(FirebaseUtils.PRIVATE_KEY).child(prefs.getString(DEVELOPER_ID, "")).child(FirebaseUtils.PATIENTS_KEY).child(prefs.getString(UID, ""));
        }
        //Before we start the service, want to check the required study permissions
        //permissionThings();

       // Intent intent = new Intent(this, SenseService.class);
       // startService(intent);

        TextView txtWelcome = findViewById(R.id.txtWelcome);
        txtWelcome.setText(String.format(getResources().getString(R.string.welcome),prefs.getString(USERNAME,"")));

        Button btnContact = findViewById(R.id.btnContact);
        Button btnSurveys = findViewById(R.id.btnSurvey);
        Button btnMonitor = findViewById(R.id.btnMonitor);
        Button btnViewData = findViewById(R.id.btnViewData);
        Button btnHeart = findViewById(R.id.btnHeart);
        btnViewData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getInstance(), PrivacyPolicy.class);
                startActivity(intent);
            }
        });
        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(), ContactActivity.class);
                startActivity(intent);
            }
        });

        btnSurveys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(), MissedSurveyActivity.class);
                startActivity(intent);
            }
        });
        btnHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(), HeartRateMonitor.class);
                startActivity(intent);
            }
        });

        btnMonitor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(), MonitorActivity.class);
                startActivity(intent);
            }
        });
    }
    public void checkPermission(String permission, List<String> requestList){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED) {
            requestList.add(permission);
        }
        else {
            // Permission has already been granted
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("GRANTED","Hurray granted!");
                    Intent intent = new Intent(this, SenseService.class);
                    startService(intent);
                } else {
                    Log.d("REFUSED","Oh no they refused");
                }
                return;

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
}

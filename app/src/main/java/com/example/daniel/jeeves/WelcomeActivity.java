package com.example.daniel.jeeves;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


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

        mFirebaseAuth = FirebaseAuth.getInstance();

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

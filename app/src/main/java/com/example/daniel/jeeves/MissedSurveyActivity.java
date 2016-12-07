package com.example.daniel.jeeves;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.daniel.jeeves.firebase.FirebasePatient;
import com.example.daniel.jeeves.firebase.FirebaseSurvey;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class MissedSurveyActivity extends AppCompatActivity {
    ListView list;
    Context app = ApplicationContext.getContext();
    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_missed_survey);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        final ArrayList<String> array = new ArrayList<String>();
        mFirebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        String userid = user.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference firebaseSurvey = database.getReference("JeevesData").child("patients").child(userid);

        firebaseSurvey.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                FirebasePatient patient = snapshot.getValue(FirebasePatient.class); //THIS IS HOW YOU DO IT TO AVOID MAKING IT A HASHMAP
                Map<String, Map<String, FirebaseSurvey>> missedSurveys = patient.getincomplete();
                if (missedSurveys == null) return;
                final ArrayList<FirebaseSurvey> surveynames = new ArrayList<FirebaseSurvey>();
                Iterator<String> iter = missedSurveys.keySet().iterator();
                while (iter.hasNext()) {
                    String key = iter.next();
                    Map<String, FirebaseSurvey> surveys = missedSurveys.get(key);
                    Iterator<String> surveyiter = surveys.keySet().iterator();
                    while (surveyiter.hasNext()) {
                        String surveykey = surveyiter.next();
                        FirebaseSurvey survey = surveys.get(surveykey);
                        long timeToGo = survey.getexpiryTime() - System.currentTimeMillis();
                        int minutes = (int) (timeToGo / 60000);
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        final long timeAlive = survey.gettimeAlive();
                        String dateString = formatter.format(new Date(survey.gettimeSent()));
                        if (survey.getexpiryTime() > System.currentTimeMillis() || survey.getexpiryTime() == 0) {
                            survey.setkey(surveykey);
                            surveynames.add(survey);
                            if (timeAlive > 0)
                                array.add(survey.getname() + "\nSent at " + dateString + "\nExpiring in " + (minutes + 1) + " minutes");
                            else
                                array.add(survey.getname() + "\nSent at " + dateString);
                        }
                    }
                }
                list = (ListView) findViewById(android.R.id.list);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                        Intent resultIntent = new Intent(MissedSurveyActivity.this, SurveyActivity.class);
                        FirebaseSurvey clickedSurvey = surveynames.get(position);
                        resultIntent.putExtra("surveyid", clickedSurvey.getkey());
                        resultIntent.putExtra("name", clickedSurvey.getname());
                        resultIntent.putExtra("timeSent", clickedSurvey.gettimeSent());
                        clickedSurvey.setbegun(); //Confirm that this survey has been started
                        startActivity(resultIntent);
                    }
                });
                MissedSurveyItem adapter = new MissedSurveyItem(MissedSurveyActivity.this, surveynames);
                list.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }
}

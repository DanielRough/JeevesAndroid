package com.example.daniel.jeeves;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ListView;

import com.example.daniel.jeeves.firebase.FirebasePatient;
import com.example.daniel.jeeves.firebase.FirebaseProject;
import com.example.daniel.jeeves.firebase.FirebaseSurvey;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MissedSurveyActivity extends AppCompatActivity {
    static final int PICK_CONTACT_REQUEST = 1;  // The request code
    ListView surveyList;
    Context app = ApplicationContext.getContext();
    SharedPreferences prefs = app.getSharedPreferences("userprefs", Context.MODE_PRIVATE);
    final String userid = prefs.getString("userid", "null");

    @Override
    protected void onResume(){
        super.onResume();

        final Firebase firebaseSurvey = new Firebase("https://incandescent-torch-8695.firebaseio.com/JeevesData/patients/" + userid);
        firebaseSurvey.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
               // firebaseSurvey.removeEventListener(this); //Get rid of it again or it causes problems
                Log.d("OOGABOOGAGFJGFJKH", "zobhkhuhkhjhjkhjkhjkhjhjhjkhjkhj");

                Log.d("Snappyshotooooo", snapshot.getValue().toString());
                //FirebasePatient patient = (FirebasePatient)snapshot.getValue();
                FirebasePatient patient = (FirebasePatient) snapshot.getValue(FirebasePatient.class); //THIS IS HOW YOU DO IT TO AVOID MAKING IT A HASHMAP
                Map<String, Map<String,FirebaseSurvey>> missedSurveys = patient.getincomplete(); //Oh wow this is horrific. Absolutely horrific.
                if (missedSurveys == null) return;
                ArrayList<FirebaseSurvey> surveynames = new ArrayList<FirebaseSurvey>();
                Iterator<String> iter = missedSurveys.keySet().iterator();
                    while (iter.hasNext()) {
                        String key = iter.next();
                       // Log.d("INCOMPLETE SURVEY", key);
                        Map<String,FirebaseSurvey> surveys = missedSurveys.get(key);
                        Iterator<String> surveyiter = surveys.keySet().iterator();
                        while (surveyiter.hasNext()) {
                            String surveykey = surveyiter.next();
                         //   Log.d("INCOMPLETE SURVEY", surveykey);
                            FirebaseSurvey survey = surveys.get(surveykey);
                            if (survey.getexpiryTime() > System.currentTimeMillis()) {
                                survey.setkey(surveykey);
                                surveynames.add(survey);
                            }
                        }
                }
                Log.d("SURVEY NAMES: ", surveynames.toString());
                CustomAdapter adapter = new CustomAdapter(getInstance(), surveynames);
                surveyList.setAdapter(adapter);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_missed_survey);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
         surveyList = (ListView)findViewById(R.id.lstMissed);
//
       Log.d("OOGABOOGAGFJGFJKH", "jhgjyghfydfty");
//


        final Firebase firebaseSurvey = new Firebase("https://incandescent-torch-8695.firebaseio.com/JeevesData/patients/" + userid);
        firebaseSurvey.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
           //     firebaseSurvey.removeEventListener(this); //Get rid of it again or it causes problems

                Log.d("Snappyshot99999999", snapshot.getValue().toString());
                //FirebasePatient patient = (FirebasePatient)snapshot.getValue();
                FirebasePatient patient = (FirebasePatient)snapshot.getValue(FirebasePatient.class); //THIS IS HOW YOU DO IT TO AVOID MAKING IT A HASHMAP
                Map<String, Map<String,FirebaseSurvey>> missedSurveys = patient.getincomplete(); //Oh wow this is horrific. Absolutely horrific.
                if (missedSurveys == null) return;
                ArrayList<FirebaseSurvey> surveynames = new ArrayList<FirebaseSurvey>();
                Iterator<String> iter = missedSurveys.keySet().iterator();
                while (iter.hasNext()) {
                    String key = iter.next();
             //       Log.d("INCOMPLETE SURVEY", key);
                    Map<String,FirebaseSurvey> surveys = missedSurveys.get(key);
                    Iterator<String> surveyiter = surveys.keySet().iterator();
                    while (surveyiter.hasNext()) {
                        String surveykey = surveyiter.next();
               //         Log.d("INCOMPLETE SURVEY", surveykey);
                        FirebaseSurvey survey = surveys.get(surveykey);
                        if (survey.getexpiryTime() > System.currentTimeMillis()) {
                            survey.setkey(surveykey);
                            surveynames.add(survey);
                        }
                    }
                }
                //Log.d("SURVEY NAMES: ", surveynames.toString());
                CustomAdapter adapter = new CustomAdapter(getInstance(), surveynames);
                surveyList.setAdapter(adapter);

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }


        });
    }
    public void updateList(){

    }
    public Activity getInstance(){
        return this;
    }

}

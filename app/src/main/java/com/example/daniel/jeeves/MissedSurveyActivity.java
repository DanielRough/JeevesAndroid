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

import com.example.daniel.jeeves.firebase.FirebaseSurvey;
import com.example.daniel.jeeves.firebase.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

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
        mFirebaseAuth = FirebaseAuth.getInstance();
        Query myTopPostsQuery = FirebaseUtils.PATIENT_REF.child("incomplete").orderByChild("timeSent").limitToLast(10);
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                final ArrayList<FirebaseSurvey> surveynames = new ArrayList<FirebaseSurvey>();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    FirebaseSurvey survey = postSnapshot.getValue(FirebaseSurvey.class);
                    String id = postSnapshot.getKey();
                    survey.setkey(id);
                    long expiryTime = survey.getexpiryTime();
                    long expiryMillis = expiryTime*60*1000;
                    long deadline = survey.gettimeSent() + expiryMillis;
                    if (deadline > System.currentTimeMillis() || survey.getexpiryTime() == 0) {
                        surveynames.add(survey);
                    }

                }
                list = (ListView) findViewById(android.R.id.list);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                        Intent resultIntent = new Intent(MissedSurveyActivity.this, SurveyActivity.class);
                        FirebaseSurvey clickedSurvey = surveynames.get(position);
                        resultIntent.putExtra("surveyid", clickedSurvey.getkey());
                        resultIntent.putExtra("name", clickedSurvey.gettitle());
                        resultIntent.putExtra("timeSent", clickedSurvey.gettimeSent());
                        clickedSurvey.setbegun(); //Confirm that this survey has been started
                        startActivity(resultIntent);
                    }
                });
                MissedSurveyItem adapter = new MissedSurveyItem(MissedSurveyActivity.this, surveynames);
                list.setAdapter(adapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });
    }
}

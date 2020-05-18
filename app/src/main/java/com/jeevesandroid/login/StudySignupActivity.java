package com.jeevesandroid.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jeevesandroid.AppContext;
import com.jeevesandroid.R;
import com.jeevesandroid.mainscreens.WelcomeActivity;
import com.jeevesandroid.firebase.FirebaseProject;
import com.jeevesandroid.firebase.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class StudySignupActivity extends AppCompatActivity {

    private FirebaseUser mFirebaseUser;
    private Map<String,FirebaseProject> projectMap;
    private String selectedStudy;
    private FirebaseDatabase database;
    private Activity getInstance(){
        return this;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        projectMap = new HashMap<>();
        database = FirebaseUtils.getDatabase();
        setContentView(R.layout.activity_study_signup);

        final Button beginStudy = findViewById(R.id.btnSignup);
        beginStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        beginStudy();
                }
        });
        TextView txtStudyTitle = findViewById(R.id.txtStudyName);
        TextView txtStudyDescription = findViewById(R.id.txtStudyDescription);
        TextView txtStudyResearcher = findViewById(R.id.txtResearcher);
        String study_url;
        JSONArray study_configs;

        //If we are getting here from an AWARE study link
        String scheme = getIntent().getScheme();
        if (scheme != null) {
            study_url = getIntent().getDataString();
            Uri url = Uri.parse(study_url);
        }
        Cursor qry = Aware.getStudy(this, study_url);
        if (qry == null || !qry.moveToFirst()) {
            new PopulateStudy().execute(study_url);
        } else {
            try {
                study_configs = new JSONArray(qry.getString(qry.getColumnIndex(Aware_Provider.Aware_Studies.STUDY_CONFIG)));
                txtStudyTitle.setText(qry.getString(qry.getColumnIndex(Aware_Provider.Aware_Studies.STUDY_TITLE)));
                txtStudyDescription.setText(Html.fromHtml(qry.getString(qry.getColumnIndex(Aware_Provider.Aware_Studies.STUDY_DESCRIPTION)), null, null));
                txtStudyResearcher.setText(qry.getString(qry.getColumnIndex(Aware_Provider.Aware_Studies.STUDY_PI)));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!qry.isClosed()) qry.close();

            if (study_configs != null) {
                populateStudyInfo(study_configs);
            }

//            btnAction.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    btnAction.setEnabled(false);
//                    btnAction.setAlpha(0.5f);
//
//                    Cursor study = Aware.getStudy(getApplicationContext(), study_url);
//                    if (study != null && study.moveToFirst()) {
//                        ContentValues studyData = new ContentValues();
//                        studyData.put(Aware_Provider.Aware_Studies.STUDY_JOINED, System.currentTimeMillis());
//                        studyData.put(Aware_Provider.Aware_Studies.STUDY_EXIT, 0);
//                        getContentResolver().update(Aware_Provider.Aware_Studies.CONTENT_URI, studyData, Aware_Provider.Aware_Studies.STUDY_URL + " LIKE '" + study_url + "'", null);
//                    }
//                    if (study != null && !study.isClosed()) study.close();
//
//                    new JoinStudyAsync().execute();
//                }
//            });
        DatabaseReference projectsRef = database
                .getReference(FirebaseUtils.PROJECTS_KEY);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Iterable<DataSnapshot> post = dataSnapshot.getChildren();
                for (DataSnapshot aPost : post) {
                        FirebaseProject proj = aPost.getValue(FirebaseProject.class);
                        String name = proj.getname();
                        projectMap.put(name, proj);
                        Log.d("PUTPROJ","Put project " + name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        projectsRef.addValueEventListener(postListener);
    }

    private void beginStudy(){
        FirebaseProject selectedProject = projectMap.get(selectedStudy);
        AppContext.setCurrentproject(selectedProject);

        String developerid = selectedProject.getresearcherno();
        FirebaseUtils.SURVEY_REF = database
                .getReference(FirebaseUtils.PROJECTS_KEY)
                .child(selectedStudy)
                .child(FirebaseUtils.SURVEYDATA_KEY);
        FirebaseUtils.PATIENT_REF = database
                .getReference(FirebaseUtils.PATIENTS_KEY)
                .child(mFirebaseUser.getUid());


        final Intent intent = new Intent(getInstance(),WelcomeActivity.class);
        SharedPreferences varPrefs = PreferenceManager
                .getDefaultSharedPreferences(AppContext.getContext());
        //Add the user's selected study to SharedPreferences
        SharedPreferences.Editor prefsEditor = varPrefs.edit();
        prefsEditor.putString(AppContext.STUDY_NAME,selectedStudy);
        //prefsEditor.putString(AppContext.DEVELOPER_ID,developerid);
        prefsEditor.apply();

        //Add in the initial values
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
        String username = prefs.getString(AppContext.USERNAME,"");
        String email = prefs.getString(AppContext.EMAIL,"");
        final String sensitiveData = username+";"+email;

        Map<String,Object> childMap = new HashMap<>();
        childMap.put("userinfo",FirebaseUtils.encodeKey(sensitiveData));
        childMap.put("name",mFirebaseUser.getUid());
        childMap.put("currentStudy",selectedStudy);
        childMap.put("completed",0);
        childMap.put("missed",0);
        FirebaseUtils.PATIENT_REF.setValue(childMap);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }
}

package com.jeevesandroid.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
        final EditText txtStudyId = findViewById(R.id.textStudyId);
        TextView txtWelcome = findViewById(R.id.txtWelcome);
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(AppContext.getContext());
        String username = prefs.getString(AppContext.USERNAME, "");
        txtWelcome.setText(String.format(getResources().getString(R.string.welcome), username));



        final Button beginStudy = findViewById(R.id.btnBeginStudy);
        beginStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String studyId = txtStudyId.getText().toString();
                boolean found = false;
                for (FirebaseProject firebaseProject : projectMap.values()) {
                    if(firebaseProject.getid().equals(studyId)){
                        found = true;
                        selectedStudy = firebaseProject.getname();
                        beginStudy();
                    }
                }
                if(!found)
                    Toast.makeText(getInstance(),"No study found with this ID",Toast.LENGTH_SHORT).show();
            }
        });
        //  final ListView lstStudies = (ListView)findViewById(R.id.lstStudies);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Start study");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                beginStudy();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        DatabaseReference projectsRef = database
                .getReference(FirebaseUtils.PUBLIC_KEY)
                .child(FirebaseUtils.PROJECTS_KEY);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Iterable<DataSnapshot> post = dataSnapshot.getChildren();
                for (DataSnapshot aPost : post) {
                    FirebaseProject proj = aPost.getValue(FirebaseProject.class);
                    String name = proj.getname();
                    projectMap.put(name, proj);
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
                .getReference(FirebaseUtils.PRIVATE_KEY)
                .child(developerid)
                .child(FirebaseUtils.PROJECTS_KEY)
                .child(selectedStudy)
                .child(FirebaseUtils.SURVEYDATA_KEY);
        FirebaseUtils.PATIENT_REF = database
                .getReference(FirebaseUtils.PRIVATE_KEY)
                .child(developerid)
                .child(FirebaseUtils.PATIENTS_KEY)
                .child(mFirebaseUser.getUid());


        final Intent intent = new Intent(getInstance(),WelcomeActivity.class);
        SharedPreferences varPrefs = PreferenceManager
                .getDefaultSharedPreferences(AppContext.getContext());
        //Add the user's selected study to SharedPreferences
        SharedPreferences.Editor prefsEditor = varPrefs.edit();
        prefsEditor.putString(AppContext.STUDY_NAME,selectedStudy);
        prefsEditor.putString(AppContext.DEVELOPER_ID,developerid);
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

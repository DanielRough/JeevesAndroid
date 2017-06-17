package com.example.daniel.jeeves.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.R;
import com.example.daniel.jeeves.WelcomeActivity;
import com.example.daniel.jeeves.firebase.FirebaseProject;
import com.example.daniel.jeeves.firebase.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.example.daniel.jeeves.ApplicationContext.DEVELOPER_ID;
import static com.example.daniel.jeeves.ApplicationContext.EMAIL;
import static com.example.daniel.jeeves.ApplicationContext.PHONE;
import static com.example.daniel.jeeves.ApplicationContext.STUDY_NAME;
import static com.example.daniel.jeeves.ApplicationContext.USERNAME;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.SURVEYS_KEY;

public class StudySignupActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    String username = "";
    Map<String,FirebaseProject> projectMap;
    ArrayAdapter<String> adapter;
    ArrayList<String> listItems=new ArrayList<String>();
    String selectedStudy;
    FirebaseDatabase database;
    public Activity getInstance(){
        return this;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        projectMap = new HashMap<String,FirebaseProject>();
        database = FirebaseUtils.getDatabase();
        setContentView(R.layout.activity_study_signup);
        final EditText txtStudyId = (EditText) findViewById(R.id.textStudyId);
        TextView txtWelcome = (TextView) findViewById(R.id.txtWelcome);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        username = prefs.getString(USERNAME,"");
        txtWelcome.setText("Welcome, " + username);

        final Button beginStudy = (Button) findViewById(R.id.btnBeginStudy);
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
                if(found == false)
                    Toast.makeText(getInstance(),"No study found with this ID",Toast.LENGTH_SHORT).show();
            }
        });
        final ListView lstStudies = (ListView)findViewById(R.id.lstStudies);
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

        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        lstStudies.setAdapter(adapter);
        lstStudies.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                selectedStudy= lstStudies.getItemAtPosition(position).toString();
                builder.setMessage("Are you sure you want to start study " + selectedStudy + "?");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        DatabaseReference projectsRef = database.getReference(FirebaseUtils.PUBLIC_KEY).child(FirebaseUtils.PROJECTS_KEY);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                listItems.clear();
                Iterable<DataSnapshot> post = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iter = post.iterator();
                while(iter.hasNext()){
                    FirebaseProject proj = iter.next().getValue(FirebaseProject.class);
                    String name = proj.getname();
                    projectMap.put(name, proj);
                    //Don't let them select it from the list if it's not public
                    if(proj.getisPublic()) {
                        listItems.add(name);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        projectsRef.addValueEventListener(postListener);
    }

    public void beginStudy(){
        FirebaseProject selectedProject = projectMap.get(selectedStudy);
        ApplicationContext.setCurrentproject(selectedProject);

        String developerid = selectedProject.getresearcherno();

        //Set the reference we need to push our survey results to
        FirebaseUtils.SURVEY_REF = database.getReference(FirebaseUtils.PRIVATE_KEY).child(developerid).child(SURVEYS_KEY);
        FirebaseUtils.PATIENT_REF = database.getReference(FirebaseUtils.PRIVATE_KEY).child(developerid).child(FirebaseUtils.PATIENTS_KEY).child(mFirebaseUser.getUid());


        Intent intent = new Intent(getInstance(),WelcomeActivity.class);
        SharedPreferences varPrefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        //Add the user's selected study to SharedPreferences
        SharedPreferences.Editor prefsEditor = varPrefs.edit();
        prefsEditor.putString(STUDY_NAME,selectedStudy);
        prefsEditor.putString(DEVELOPER_ID,developerid);
        prefsEditor.commit();

        //Add in the initial values
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        String username = prefs.getString(USERNAME,"");
        String email = prefs.getString(EMAIL,"");
        String phoneno = prefs.getString(PHONE,"");
        String sensitiveData = username+";"+email+";"+phoneno;
        FirebaseUtils.PATIENT_REF.child("userinfo").setValue(FirebaseUtils.encodeAnswers(sensitiveData));
        FirebaseUtils.PATIENT_REF.child("name").setValue(mFirebaseUser.getUid());
        FirebaseUtils.PATIENT_REF.child("currentStudy").setValue(selectedStudy);
        FirebaseUtils.PATIENT_REF.child("completed").setValue(0);
        FirebaseUtils.PATIENT_REF.child("missed").setValue(0);
        startActivity(intent);
    }
}

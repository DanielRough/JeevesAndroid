package com.example.daniel.jeeves.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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

import static com.example.daniel.jeeves.ApplicationContext.STUDY_NAME;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.PROJECTS_KEY;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.PUBLIC_KEY;


public class MainActivity extends Activity{
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;


    public Activity getInstance() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        //If we're not signed in, launch the sign-in activity
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
            //Maybe we can get the project HERE, so that when the app is destroyed and recreated, we're only allowed to carry
            //on once our project has been loaded.
            //This would even work if we restart while offline, because I think persistence is now enabled...?

            if (preferences.contains(STUDY_NAME)) {
                final FirebaseDatabase database = FirebaseUtils.getDatabase();
                SharedPreferences varPrefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
                String studyname = varPrefs.getString(STUDY_NAME, "");
                //If we've restarted the app we also want to reset the triggers NO WE BLOODY WELL DO NOT, this happens in SenseService!
//                SharedPreferences.Editor prefseditor = varPrefs.edit();
//                prefseditor.putStringSet("triggerids",new HashSet<String>());
//                prefseditor.commit();

                DatabaseReference projectRef = database.getReference(PUBLIC_KEY).child(PROJECTS_KEY).child(studyname);

                projectRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        FirebaseProject post = snapshot.getValue(FirebaseProject.class);
                        ApplicationContext.setCurrentproject(post);
                        if(post == null){
                            Toast.makeText(getInstance(),"OH NO IT WAS NULL",Toast.LENGTH_SHORT).show();
                            Log.d("OH NO","IT WAS NULL");
                            return;
                        }
                        //Okay, NOW we're safe to start the welcome activity, maybe...
                        Intent intent = new Intent(getInstance(), WelcomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

            } else
                startStudySignUp();
        }
    }
    private void startStudySignUp() {
        Intent intent = new Intent(this, StudySignupActivity.class);
        startActivity(intent);
        finish();
    }

}

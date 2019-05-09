package com.jeevesandroid.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.jeevesandroid.ApplicationContext;
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

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity{
    private static final int REQUEST_SIGNUP = 0;

    FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private Activity getInstance() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        ProgressBar mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();

        //If we're not signed in, launch the sign-in activity
        if (mFirebaseUser == null) {
            startActivityForResult(new Intent(this, SignUpActivity.class),REQUEST_SIGNUP);
        } else {
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(ApplicationContext.getContext());
            if (preferences.contains(ApplicationContext.STUDY_NAME)) {
                final FirebaseDatabase database = FirebaseUtils.getDatabase();
                SharedPreferences varPrefs = PreferenceManager
                        .getDefaultSharedPreferences(ApplicationContext.getContext());
                String studyname = varPrefs.getString(ApplicationContext.STUDY_NAME, "");
                DatabaseReference projectRef = database
                        .getReference(FirebaseUtils.PUBLIC_KEY)
                        .child(FirebaseUtils.PROJECTS_KEY)
                        .child(studyname);

                projectRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        FirebaseProject post = snapshot.getValue(FirebaseProject.class);
                        ApplicationContext.setCurrentproject(post);
                        if(post == null){
                            return;
                        }
                        Intent intent = new Intent(getInstance(), WelcomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });

            }
            else
                startStudySignUp();
        }
    }
    private void startStudySignUp() {
        Intent intent = new Intent(this, StudySignupActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                final String name = data.getStringExtra("name");
                mFirebaseAuth = FirebaseAuth.getInstance();
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
                mFirebaseAuth.getCurrentUser().updateProfile(profileUpdates) .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startStudySignUp();
                            finish();
                        }
                    }
                });
            }
        }
    }
}

package com.example.daniel.jeeves.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ProgressBar;

import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.R;
import com.example.daniel.jeeves.WelcomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


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
            String uid = mFirebaseUser.getUid();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
            if (preferences.contains(uid+"_STUDY")) {
                String studyname = preferences.getString(uid+"_STUDY","");
                String username = preferences.getString(uid+"_NAME","");
                Intent intent = new Intent(getInstance(), WelcomeActivity.class);
                intent.putExtra("studyname", studyname);
                intent.putExtra("username",username);

                startActivity(intent);
                finish();
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

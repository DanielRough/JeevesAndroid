package com.jeevesandroid.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.UserProfileChangeRequest;
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

public class MainActivity extends Activity{
    private static final int REQUEST_SIGNUP = 0;
    private static final int REQUEST_CONFIG = 1;
    FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private Activity getInstance() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().hasExtra("uncaughtException")) {
            Toast.makeText(this,"Jeeves restarted after a crash. This crash has been reported to the developers. Sorry about that!",Toast.LENGTH_LONG).show();
        }

        ProgressBar mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);


        //I think we always need to do the config thing.
        //I guess we could do it as a 'STartActivityForResult'
//        if (!preferences.contains(AppContext.CONFIG)) {
//            Intent i = new Intent(getInstance(),ConfigActivity.class);
//            startActivity(i);
//            Log.d("CONFIG","starting config stuff");
//            finish();
//        }
        startActivityForResult(new Intent(this, ConfigActivity.class), REQUEST_CONFIG);

     //   else {

     //   }
    }
    private void startStudySignUp() {
        Intent intent = new Intent(this, StudySignupActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CONFIG){
            Log.d("HEY HO","Hey ho a daddy o");
            FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
            SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(AppContext.getContext());
            //If we're not signed in, launch the sign-in activity
            if (mFirebaseUser == null) {
                Log.d("LOGIN", "Starting login stuff");
                startActivityForResult(new Intent(this, SignUpActivity.class), REQUEST_SIGNUP);
            } else {
                if (preferences.contains(AppContext.STUDY_NAME)) {
                    final FirebaseDatabase database = FirebaseUtils.getDatabase();
                    SharedPreferences varPrefs = PreferenceManager
                        .getDefaultSharedPreferences(AppContext.getContext());
                    String studyname = preferences.getString(AppContext.STUDY_NAME, "");
                    Log.d("STUDYNAME","Study name is " + studyname);
                    DatabaseReference projectRef = database
                        .getReference(FirebaseUtils.PROJECTS_KEY)
                        .child(studyname);

                    projectRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            FirebaseProject post = snapshot.getValue(FirebaseProject.class);
                            AppContext.setCurrentproject(post);
                            if (post == null) {
                                Log.d("NULL","Oh deary me it's null");
                                startStudySignUp();
                                finish();
                            }
                            Intent intent = new Intent(getInstance(), WelcomeActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                } else
                    startStudySignUp();
            }
        }
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

package com.jeevesx.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.jeevesx.AppContext;
import com.jeevesx.R;
import com.jeevesx.mainscreens.WelcomeActivity;
import com.jeevesx.firebase.FirebaseProject;
import com.jeevesx.firebase.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends Activity{

    private Activity getInstance() {
        return this;
    }
    private static final int REQUEST_CONFIG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MAIN","Main activity again");
        setContentView(R.layout.activity_main);

        if (getIntent().hasExtra("uncaughtException")) {
            Toast.makeText(this,"Jeeves restarted after a crash.",Toast.LENGTH_LONG).show();
        }

        ProgressBar mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        startActivityForResult(new Intent(this, StudySignupActivity.class), REQUEST_CONFIG);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(AppContext.getContext());
            //If we're not signed in, launch the sign-in activity
            if (preferences.contains(AppContext.STUDY_NAME)) {
                Log.d("HOODABOGA","jaimerais le jambon");
                final FirebaseDatabase database = FirebaseUtils.getDatabase();
                String studyname = preferences.getString(AppContext.STUDY_NAME, "");
                DatabaseReference projectRef = database
                        .getReference(FirebaseUtils.PROJECTS_KEY)
                        .child(studyname);

                projectRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        FirebaseProject post = snapshot.getValue(FirebaseProject.class);
                        AppContext.setCurrentproject(post);
                        Intent intent = new Intent(getInstance(), WelcomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

            } else {
                Intent intent = new Intent(getInstance(), SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        }
        else{
            Intent intent = new Intent(getInstance(), SignUpActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

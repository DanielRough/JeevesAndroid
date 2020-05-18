package com.jeevesandroid.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
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
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class StudySignupActivity extends AppCompatActivity {

    //private FirebaseUser mFirebaseUser;
    private Map<String,FirebaseProject> projectMap;
    private String selectedStudy;
    private FirebaseDatabase database;
    TextView txtStudyTitle;
    TextView txtStudyDescription;
    TextView txtStudyResearcher;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Activity getInstance(){
        return this;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        // mFirebaseUser = mFirebaseAuth.getCurrentUser();
        projectMap = new HashMap<>();
        database = FirebaseUtils.getDatabase();
        setContentView(R.layout.activity_study_signup);

        final Button beginStudy = findViewById(R.id.btnSignup);
        beginStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });
        txtStudyTitle = findViewById(R.id.txtStudyName);
        txtStudyDescription = findViewById(R.id.txtStudyDescription);
        txtStudyResearcher = findViewById(R.id.txtResearcher);
        String study_url;

        String scheme = getIntent().getScheme();
        if (scheme != null) {
            study_url = getIntent().getDataString();
            Uri url = Uri.parse(study_url);
            String jsonConfig = null;
            try {
                jsonConfig = readTextFromUri(url);
                initialiseConfig(jsonConfig);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

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
                    Log.d("PUTPROJ", "Put project " + name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        projectsRef.addValueEventListener(postListener);
    }

    public void initialiseConfig(String jsonConfig) throws JSONException {
        JSONObject reader = new JSONObject(jsonConfig);
        JSONObject projinfo = reader.getJSONObject("project_info");
        JSONObject studyinfo = reader.getJSONObject("study_info");
        JSONObject client = reader.getJSONArray("client").getJSONObject(0);
        String firebase_database_url = projinfo.getString("firebase_url");
        String gcm_defaultSenderId = projinfo.getString("project_number");
        String google_api_key = client.getJSONArray("api_key").getJSONObject(0).getString("current_key");
        String google_app_id = client.getJSONObject("client_info").getString("mobilesdk_app_id");
        String google_storage_bucket = projinfo.getString("storage_bucket");
        String project_id = projinfo.getString("project_id");
        FirebaseOptions options = new FirebaseOptions.Builder()
            .setApplicationId(google_app_id) // Required for Analytics.
            .setApiKey(google_api_key) // Required for Auth.
            .setDatabaseUrl(firebase_database_url)
            .setStorageBucket(google_storage_bucket)
            .setGcmSenderId(gcm_defaultSenderId)
            .setProjectId(project_id)
            .build();
        for(FirebaseApp app : FirebaseApp.getApps(getInstance())){
            if(!app.getOptions().getApiKey().equals(google_api_key)) {
                app.delete();
                FirebaseApp.initializeApp(getApplicationContext(), options);
                break;
            }
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstance());
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(AppContext.CONFIG,jsonConfig);
        prefsEditor.apply();

        txtStudyTitle.setText(studyinfo.getString("title"));
        txtStudyDescription.setText(studyinfo.getString("description"));
        txtStudyResearcher.setText(studyinfo.getString("researcher"));
        selectedStudy = studyinfo.getString("ID");
    }

    private String readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();
        return stringBuilder.toString();
    }

    private void signup(){
        final ProgressDialog progressDialog = new ProgressDialog(this,
            R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();
        EditText emailText = findViewById(R.id.txtEmail);
        final String email = emailText.getText().toString();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstance());
                if(prefs.contains(AppContext.UID)){
                    final String uid = prefs.getString(AppContext.UID,"");
                    final String email = prefs.getString(AppContext.EMAIL,"");
                    firebaseAuth.signInWithEmailAndPassword(email,"password").addOnCompleteListener(getInstance(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                onSignupSuccess(uid,email);

                            } else {
                                Log.d("Error", "signInWithEmail:failure", task.getException());
                            }
                        }
                    });
                }
            }
        };
        mFirebaseAuth.addAuthStateListener(mAuthListener);
        mFirebaseAuth.createUserWithEmailAndPassword(email,"password")
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        progressDialog.dismiss();
                        //  onSignupFailed();
                        Toast.makeText(getInstance(),task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                    }
                    else {
                        progressDialog.dismiss();
                    }
                }

            });
    }
    private void onSignupSuccess(String userId, String email) {
        SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(AppContext.UID,userId);
        prefsEditor.putString(AppContext.EMAIL,email);
        prefsEditor.putString(AppContext.STUDY_NAME,selectedStudy);
        prefsEditor.apply();

        FirebaseProject selectedProject = projectMap.get(selectedStudy);
        AppContext.setCurrentproject(selectedProject);

        FirebaseUtils.SURVEY_REF = database
            .getReference(FirebaseUtils.PROJECTS_KEY)
            .child(selectedStudy)
            .child(FirebaseUtils.SURVEYDATA_KEY);
        FirebaseUtils.PATIENT_REF = database
            .getReference(FirebaseUtils.PATIENTS_KEY)
            .child(userId);

        final Intent intent = new Intent(getInstance(),WelcomeActivity.class);

        Map<String,Object> childMap = new HashMap<>();
        childMap.put("userinfo",FirebaseUtils.encodeKey(email));
        childMap.put("name",userId);
        childMap.put("currentStudy",selectedStudy);
        childMap.put("completed",0);
        childMap.put("missed",0);
        FirebaseUtils.PATIENT_REF.setValue(childMap);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}

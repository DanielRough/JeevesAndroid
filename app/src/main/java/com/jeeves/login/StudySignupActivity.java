package com.jeeves.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.jeeves.AppContext;
import com.jeeves.R;
import com.jeeves.mainscreens.WelcomeActivity;
import com.jeeves.firebase.FirebaseProject;
import com.jeeves.firebase.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StudySignupActivity extends AppCompatActivity {

    private Map<String,FirebaseProject> projectMap;
    private FirebaseDatabase database;
    TextView txtStudyTitle;
    TextView txtStudyDescription;
    TextView txtStudyResearcher;
    private Activity getInstance(){
        return this;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectMap = new HashMap<>();
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

        //If we arrived here from a URL
        String scheme = getIntent().getScheme();
        if (scheme != null) {
            study_url = getIntent().getDataString();
            URL url = null;
            try {
                url = new URL(study_url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            new MyTask().execute(this,url);
        }
        else {
            //If we arrived here from MainActivity
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(AppContext.getContext());
            if (preferences.contains(AppContext.CONFIG)) {
                String jsonConfig = preferences.getString(AppContext.CONFIG, "");
                try {
                    Log.d("HEYCONFIG", "INitlaising config");
                    initialiseConfig(jsonConfig);
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK,resultIntent);
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                setResult(RESULT_CANCELED,new Intent());
                finish();
            }
        }
    }

    public void initialiseConfig(String jsonConfig) throws JSONException {
        JSONObject reader = new JSONObject(jsonConfig);
        Log.d("config is ",reader.toString());
        JSONObject projinfo = reader.getJSONObject("project_info");
        JSONObject studyinfo = reader.getJSONObject("studyinfo");
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
        database = FirebaseUtils.getDatabase();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstance());
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(AppContext.CONFIG,jsonConfig);
        prefsEditor.apply();

        txtStudyTitle.setText(studyinfo.getString("title"));
        txtStudyDescription.setText(studyinfo.getString("description"));
        txtStudyResearcher.setText(studyinfo.getString("researcher"));

        prefsEditor.putString(AppContext.CONFIG,jsonConfig);
        prefsEditor.apply();


    }

    private static class MyTask extends AsyncTask<Object, Void, Void> {

        StudySignupActivity activity;
        String jsonConfig = null;

        @Override
        protected Void doInBackground(Object... objects) {
            activity = (StudySignupActivity)objects[0];
            try {
                jsonConfig = activity.readTextFromUri((URL)objects[1]);
                activity.initialiseConfig(jsonConfig);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private String readTextFromUri(URL url) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            url.openStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    private void signup(){
        final ProgressDialog progressDialog = new ProgressDialog(this,
            R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Signing up...");
        progressDialog.show();
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
                progressDialog.dismiss();
                String selectedStudy = String.valueOf(txtStudyTitle.getText());
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(AppContext.getContext());
                SharedPreferences.Editor prefsEditor = prefs.edit();
                String userId = UUID.randomUUID().toString();
                prefsEditor.putString(AppContext.UID,userId);
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
                childMap.put("name",userId);
                childMap.put("currentStudy",selectedStudy);
                childMap.put("completed",0);
                childMap.put("missed",0);
                FirebaseUtils.PATIENT_REF.setValue(childMap);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        projectsRef.addValueEventListener(postListener);
    }
}

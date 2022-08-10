package com.jeevesx.login;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseAuth;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class StudySignupActivity extends AppCompatActivity {

    private Map<String, FirebaseProject> projectMap;
    private FirebaseDatabase database;
    TextView txtStudyTitle;
    //   TextView txtStudyDescription;
    TextView txtStudyResearcher;
    EditText txtEmail;

    //ActionCodeSettings actionCodeSettings;
    private Activity getInstance() {
        return this;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("STOPP","Studysignupactivity stopped");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("RESUUME","Studysignupactivity resumed!");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectMap = new HashMap<>();
        setContentView(R.layout.activity_study_signup);

        Log.d("STUDYSI","Study sign up activity");
        final Button beginStudy = findViewById(R.id.btnSignup);
        beginStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = txtEmail.getText().toString();
                //sendSignInLink(email);
                signup(email);
            }
        });
        txtStudyTitle = findViewById(R.id.txtStudyName);
        //txtStudyDescription = findViewById(R.id.txtStudyDescription);
        txtStudyResearcher = findViewById(R.id.txtResearcher);
        txtEmail = findViewById(R.id.txtEmail);
        String study_url;

        //If we arrived here from a URL
        String scheme = getIntent().getScheme();
        Log.d("SCHEMEYH", "Scheme is " + scheme);
        if (scheme != null) {
            study_url = getIntent().getDataString();
            URL url = null;
            try {
                url = new URL(study_url);
//                 actionCodeSettings =
//                        ActionCodeSettings.newBuilder()
//                                .setUrl("https://jeevesx.page.link")
//                                // This must be true
//                                .setHandleCodeInApp(true)
//                                .setAndroidPackageName(
//                                        "com.jeevesx",
//                                        false, /* installIfNotAvailable */
//                                        "0"    /* minimumVersion */)
//                                .build();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Log.d("YUP", "DAAARN TOOTIN");
            new MyTask().execute(this, url);
        } else {
            //If we arrived here from MainActivity
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(AppContext.getContext());
            if (preferences.contains(AppContext.UID)) { //If we've previously signed up
                String jsonConfig = preferences.getString(AppContext.CONFIG, "");

                try {
                    Log.d("HEYCONFIG", "INitlaising config");
                    initialiseConfig(jsonConfig);
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                setResult(RESULT_CANCELED, new Intent());
                finish();
            }
        }

        // Confirm the link is a sign-in with email link.
//        if (auth.isSignInWithEmailLink(emailLink)) {
//            // Retrieve this from wherever you stored it
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
//
//            String email = prefs.getString("UserEmail","");
//            Log.d("EMAIL", "user's email is " + email);
//
//            // The client SDK will parse the code from the link for you.
//            auth.signInWithEmailLink(email, emailLink)
//                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            if (task.isSuccessful()) {
//                                Log.d(TAG, "Successfully signed in with email link!");
//                                AuthResult result = task.getResult();
//                                String userId = result.getUser().getUid();
//                                signup(userId);
//                                // You can access the new user via result.getUser()
//                                // Additional user info profile *not* available via:
//                                // result.getAdditionalUserInfo().getProfile() == null
//                                // You can check if the user is new or existing:
//                                // result.getAdditionalUserInfo().isNewUser()
//                            } else {
//                                Log.e(TAG, "Error signing in with email link", task.getException());
//                            }
//                        }
//                    });
//        }
    }

    //    private void sendSignInLink(String email){
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        auth.sendSignInLinkToEmail(email, actionCodeSettings)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Log.d(TAG, "Email sent.");
//                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
//                            SharedPreferences.Editor editor = prefs.edit();
//                            editor.putString("UserEmail", email);
//                            editor.apply();
//                            Toast.makeText(StudySignupActivity.this,"Email sent!",Toast.LENGTH_LONG).show();
//
//                        }
//                        else {
//                            Log.e(TAG, "Error signing in with email link", task.getException());
//                        }
//                    }
//                });
//
//    }
    public void initialiseConfig(String jsonConfig) throws JSONException {
        JSONObject reader = new JSONObject(jsonConfig);
        Log.d("config is ", reader.toString());
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

        for (FirebaseApp app : FirebaseApp.getApps(getInstance())) {
            if (!app.getOptions().getApiKey().equals(google_api_key)) {
                app.delete();
                FirebaseApp.initializeApp(getApplicationContext(), options);
                Log.d("INITIALISING", "We are initialising an app hurray!");
                break;
            }
        }
        Log.d("DATABASE", "away to set the database now");
        database = FirebaseUtils.getDatabase();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstance());
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(AppContext.CONFIG, jsonConfig);
        prefsEditor.putString(AppContext.STUDY_NAME, studyinfo.getString("title"));
        prefsEditor.apply();

        txtStudyTitle.setText("Study Name: " + studyinfo.getString("title"));
        //   txtStudyDescription.setText(studyinfo.getString("description"));
        txtStudyResearcher.setText("Study Researcher: " + studyinfo.getString("researcher"));

        prefsEditor.putString(AppContext.CONFIG, jsonConfig);
        prefsEditor.apply();


    }

    private static class MyTask extends AsyncTask<Object, Void, Void> {

        StudySignupActivity activity;
        String jsonConfig = null;

        @Override
        protected Void doInBackground(Object... objects) {
            activity = (StudySignupActivity) objects[0];
            try {
                jsonConfig = activity.readTextFromUri((URL) objects[1]);
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

    private void signup(String email) {
        final ProgressDialog progressDialog = new ProgressDialog(this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Signing up...");
        progressDialog.show();

        FirebaseAuth auth = FirebaseAuth.getInstance();
//        Intent intent = getIntent();
//        if(intent.getData() == null)
//            return;
//        String emailLink = intent.getData().toString();

        auth.createUserWithEmailAndPassword(email, "password")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            AuthResult result = task.getResult();
                            String userId = result.getUser().getUid();
                            setupDatabase(userId);
                            progressDialog.dismiss();
                            //FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(StudySignupActivity.this, "User already exists",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            //updateUI(null);
                        }
                    }
                });

    }
    private void setupDatabase(String userId) {
        Log.d("DATABASE", "away to set the database now");
        database = FirebaseUtils.getDatabase();
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
                //progressDialog.dismiss();
                //String selectedStudy = String.valueOf(txtStudyTitle.getText());
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(AppContext.getContext());
                String selectedStudy = prefs.getString(AppContext.STUDY_NAME, "");
                Log.d("study name", "Study name is " + selectedStudy);
                SharedPreferences.Editor prefsEditor = prefs.edit();
                //String userId = UUID.randomUUID().toString();
                prefsEditor.putString(AppContext.UID, userId);
                //prefsEditor.putString(AppContext.STUDY_NAME,selectedStudy);
                prefsEditor.apply();

                FirebaseProject selectedProject = projectMap.get(selectedStudy);
                Log.d("PROJ", "project selected is " + selectedProject.getname() + " " + selectedProject.getisDebug());
                AppContext.setCurrentproject(selectedProject);

                FirebaseUtils.SURVEY_REF = database
                        .getReference(FirebaseUtils.PROJECTS_KEY)
                        .child(selectedStudy)
                        .child(FirebaseUtils.SURVEYDATA_KEY);
                FirebaseUtils.PATIENT_REF = database
                        .getReference(FirebaseUtils.PATIENTS_KEY)
                        .child(userId);

                final Intent intent = new Intent(getInstance(), WelcomeActivity.class);

                Map<String, Object> childMap = new HashMap<>();
                childMap.put("name", userId);
                childMap.put("currentStudy", selectedStudy);
                childMap.put("completed", 0);
                childMap.put("missed", 0);
                //String email = prefs.getString("UserEmail", "");
                String email = txtEmail.getText().toString();
                childMap.put("email", FirebaseUtils.encodeKey(email));
                FirebaseUtils.PATIENT_REF.setValue(childMap);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                projectsRef.removeEventListener(this); //BLOODY IMPORTANT
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        projectsRef.addValueEventListener(postListener);
    }

}

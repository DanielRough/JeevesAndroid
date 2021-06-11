package com.jeevesandroid.login;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.jeevesandroid.AppContext;
import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseProject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigActivity extends AppCompatActivity {
    private Activity getInstance(){
        return this;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        final Button findFile = findViewById(R.id.btnFindFile);
        SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());
        findFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performFileSearch();
            }
        });
        Log.d("HEYCONFIG111","INitlaising config");
        if (preferences.contains(AppContext.CONFIG)) {
            String jsonConfig = preferences.getString(AppContext.CONFIG,"");
            try {
                Log.d("HEYCONFIG","INitlaising config");
                initialiseConfig(jsonConfig);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Log.d("CONFIG","starting config stuff");
        }
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    public void initialiseConfig(String jsonConfig) throws JSONException {
        Log.d("Config","Config file is " + jsonConfig);
        JSONObject reader = new JSONObject(jsonConfig);
        JSONObject projinfo = reader.getJSONObject("project_info");
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
//        if (FirebaseApp.getApps(getApplicationContext()).isEmpty()) {
//            FirebaseApp.initializeApp(getApplicationContext(),options);
//         //   FirebaseAPp.initi
//        }
        for(FirebaseApp app : FirebaseApp.getApps(getInstance())){
           if(!app.getOptions().getApiKey().equals(google_api_key)) {
               app.delete();
               FirebaseApp.initializeApp(getApplicationContext(), options);
                break;
           }
     //       Log.d("APPNAME",app.getName());
       //     Log.d("KEY",app.getOptions().getApiKey());
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstance());
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(AppContext.CONFIG,jsonConfig);
        prefsEditor.apply();
        Intent resultIntent = new Intent();
        setResult(RESULT_OK,resultIntent);
       // Intent i = new Intent(getInstance(),MainActivity.class);
       // startActivity(i);
        finish();
    }
    private static final int READ_REQUEST_CODE = 42;
    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    String jsonConfig = readTextFromUri(uri);
                    initialiseConfig(jsonConfig);
                } catch (IOException | JSONException e) {
                    Toast.makeText(getInstance(), "Not a valid config file", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
}

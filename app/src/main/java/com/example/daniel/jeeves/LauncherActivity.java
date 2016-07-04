package com.example.daniel.jeeves;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.client.Firebase;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        SharedPreferences prefs = getSharedPreferences("userprefs", Context.MODE_PRIVATE);

        Firebase.setAndroidContext(this);

        if(prefs.getString("userid","").length() == 0) {
            Intent intent = new Intent(this, MainActivity.class);
            Log.d("AARGH","WHEYYYYYYY");
            startActivity(intent);
        }
        else {
            Intent intent2 = new Intent(this, SenseActivity.class);
            String id = prefs.getString("userid", "");
            intent2.putExtra("userid", id);
            startActivity(intent2);
        }
    }
}

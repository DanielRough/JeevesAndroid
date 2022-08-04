package com.jeevesx.login;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.jeevesx.R;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onStop(){
        super.onStop();
        Log.d("SIGNUP","Sign up stopped");
        finish();
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("SIGNUP", "Sign up resumed");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }


}

package com.jeevesandroid.login;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.jeevesandroid.R;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onStop(){
        super.onStop();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }


}

package com.jeevesx.login;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.jeevesx.R;

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

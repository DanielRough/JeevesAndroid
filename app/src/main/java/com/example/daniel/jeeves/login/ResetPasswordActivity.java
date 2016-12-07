package com.example.daniel.jeeves.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.jeeves.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    private TextView loginLink;
    private Button resetButton;
    private EditText mEmailField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        loginLink = (TextView)findViewById(R.id.link_backtologin);
        resetButton = (Button)findViewById(R.id.reset_password_button);
        mEmailField = (EditText)findViewById(R.id.reset_email);
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPasswordActivity();
            }
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        return valid;
    }
    private void resetPasswordActivity(){
        if(!validateForm()){
            return;
        }
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String email = mEmailField.getText().toString();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetPasswordActivity.this, R.string.password_reset,
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        else{
                            Toast.makeText(ResetPasswordActivity.this, R.string.failed_reset,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

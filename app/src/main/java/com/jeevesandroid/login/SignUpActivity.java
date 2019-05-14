package com.jeevesandroid.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jeevesandroid.AppContext;
import com.jeevesandroid.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//import static com.jeevesandroid.AppContext.PHONE;


public class SignUpActivity extends AppCompatActivity {

    private EditText nameText;
    private EditText emailText;
    private Button signUpButton;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onStop(){
        super.onStop();
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nameText = findViewById(R.id.input_name);
        emailText = findViewById(R.id.input_email);
        signUpButton = findViewById(R.id.btn_signup);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

    }


    @Override
    public void onResume(){
        super.onResume();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                final String name = nameText.getText().toString();
                final String email = emailText.getText().toString();
                onSignupSuccess(user.getUid(),name,email);
            }
            }
        };
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }
    private Activity getInstance(){
        return this;
    }

    private void signup() {

        if (!validate()) {
            onSignupFailed();
            return;
        }

        signUpButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        final String email = emailText.getText().toString();

        mFirebaseAuth.createUserWithEmailAndPassword(email,"password")
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    progressDialog.dismiss();
                    onSignupFailed();
                    Toast.makeText(getInstance(),task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    progressDialog.dismiss();
                }
                    }

            });
    }


    private void onSignupSuccess(String userId, String name, String email) {
        signUpButton.setEnabled(true);
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(AppContext.getContext());
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Intent resultIntent = new Intent();
        prefsEditor.putString(AppContext.UID,userId);
        prefsEditor.putString(AppContext.USERNAME,name);
        prefsEditor.putString(AppContext.EMAIL,email);
        prefsEditor.apply();
        setResult(RESULT_OK, resultIntent);

        finish();
    }

    private void onSignupFailed() {

        signUpButton.setEnabled(true);
    }

    private boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            nameText.setError("at least 3 characters");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }
        return valid;
    }

}

package com.example.daniel.jeeves.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.example.daniel.jeeves.ApplicationContext.EMAIL;
import static com.example.daniel.jeeves.ApplicationContext.PHONE;
import static com.example.daniel.jeeves.ApplicationContext.UID;
import static com.example.daniel.jeeves.ApplicationContext.USERNAME;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameText;
    private EditText emailText;
    private EditText passwordText;
    private EditText phoneText;
    private Button signUpButton;
    private TextView loginLink;
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

        nameText = (EditText)findViewById(R.id.input_name);
        emailText = (EditText)findViewById(R.id.input_email);
        passwordText = (EditText)findViewById(R.id.input_password);
        phoneText = (EditText)findViewById(R.id.input_phone);
        signUpButton = (Button)findViewById(R.id.btn_signup);
        loginLink = (TextView)findViewById(R.id.link_login);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
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
    public Activity getInstance(){
        return this;
    }

    public void signup() {

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
        String password = passwordText.getText().toString();

        // TODO: Implement your own signup logic here.

       // showProgressDialog();

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.d("NOPE",task.getException().getMessage());
                            progressDialog.dismiss();
                            onSignupFailed();
                            Toast.makeText(getInstance(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                        else {
                            progressDialog.dismiss();
                        }
                        }

                });
    }


    public void onSignupSuccess(String userId, String name, String email) {
        Log.d("IS THIS","HAPPENING");
        signUpButton.setEnabled(true);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        SharedPreferences.Editor prefsEditor = prefs.edit();
        String phoneNo = phoneText.getText().toString();
        Intent resultIntent = new Intent();
        //Add the user's personal/confidential information to SharedPreferences
        prefsEditor.putString(UID,userId);
        prefsEditor.putString(USERNAME,name);
        prefsEditor.putString(EMAIL,email);
        prefsEditor.putString(PHONE,phoneNo);
        prefsEditor.commit();
        setResult(RESULT_OK, resultIntent);
       // startStudySignup();
        finish();
    }

    public void onSignupFailed() {

        signUpButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String phone = phoneText.getText().toString();

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

        if (password.isEmpty() || password.length() < 6 || password.length() > 10) {
            passwordText.setError("between 6 and 10 alphanumeric characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }
        if(!android.util.Patterns.PHONE.matcher(phone).matches()){
            phoneText.setError("enter a valid phone number");
            valid = false;
        } else {
            phoneText.setError(null);
        }


        return valid;
    }

}

/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.daniel.jeeves.login;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.R;
import com.example.daniel.jeeves.WelcomeActivity;
import com.example.daniel.jeeves.firebase.FirebasePatient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int MY_PERMISSIONS = 12345;

    FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private static final int REQUEST_SIGNUP = 0;
    private static final int REQUEST_PASSWORD = 1;
    private EditText mEmailField;
    private EditText mPasswordField;
    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Views
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        // Buttons
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.link_signup).setOnClickListener(this);
        findViewById(R.id.link_forgot).setOnClickListener(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        Context context = this.getApplicationContext();

        Log.d("MAIN", "Main activity created");
        String[] allpermissions = new String[]{
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE
        };
//        List<String> permsList = new ArrayList<String>();
//        for (String perm : allpermissions)
//            permsList.add(perm);
//
//        for(String permission : allpermissions){
//        if (ContextCompat.checkSelfPermission(this,permission)
//                == PackageManager.PERMISSION_GRANTED) {
//            permsList.remove(permission);
//        }
//        }
//        //Ask for all the permissions we don't have
//        String[] permyperms = {};
//        permsList.toArray(permyperms);
//        for(String permission : permyperms){
//            Log.d("PERM",permission);
//        }
        ActivityCompat.requestPermissions(this,allpermissions,
                MY_PERMISSIONS);


    }

    public Activity getInstance(){
        return this;
    }
    private void signIn(String email, String password){
        if (!validateForm()) {
            return;
        }
        showProgressDialog();

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            mFirebaseUser = mFirebaseAuth.getCurrentUser();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            final DatabaseReference myRef = database.getReference("JeevesData").child("patients").child(mFirebaseUser.getUid());
                            myRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    FirebasePatient user = dataSnapshot.getValue(FirebasePatient.class);
                                    myRef.removeEventListener(this);
                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
                                    preferences.edit().putString("userphone",user.getphoneNo());
                                    preferences.edit().commit();
                                    if(user.getcurrentStudy() != null){
                                        Intent intent = new Intent(getInstance(),WelcomeActivity.class);
                                        intent.putExtra("studyname",user.getcurrentStudy());
                                        intent.putExtra("username",user.getname());
                                        startActivity(intent);
                                        finish();
                                    }
                                    else {
                                        goToSecondActivity();
                                        finish();

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        // ...
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

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.link_forgot:
                Intent forgotintent = new Intent(getApplicationContext(), ResetPasswordActivity.class);
                startActivityForResult(forgotintent, REQUEST_PASSWORD);
                break;
            case R.id.link_signup:
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                //   createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
                break;
            case R.id.email_sign_in_button:
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                final String name = data.getStringExtra("name");
                mFirebaseAuth = FirebaseAuth.getInstance();
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build();
                mFirebaseAuth.getCurrentUser().updateProfile(profileUpdates) .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            goToSecondActivity();
                            finish();
                        }
                    }
                });
                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
            }
        }
    }

    private void goToSecondActivity(){
        Intent intent = new Intent(this,StudySignupActivity.class);
        startActivity(intent);
        finish();

        //   finish(); //Stops them going back to the login screen EVER
    }

}

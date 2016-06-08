package com.example.daniel.jeeves;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.ubhave.datahandler.loggertypes.AbstractDataLogger;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;

public class MainActivity extends AppCompatActivity {
    EditText txtusername;
    EditText txtpassword;
    TextView txtUpdate;
    String username,password;
    Button login;
    Button test;
    Firebase myFirebaseRef;
    Context context;
    private AbstractDataLogger logger;
    private static final int MY_PERMISSIONS = 12345;

    private ProgressBar spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this,
                new String[]{
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
                        Manifest.permission.READ_PHONE_STATE},
                MY_PERMISSIONS);
        Firebase.setAndroidContext(this);

        context = this.getApplicationContext();
        setContentView(R.layout.activity_main);
        myFirebaseRef = new Firebase("https://incandescent-torch-8695.firebaseio.com/");
        spinner = (ProgressBar)findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        login = (Button)findViewById(R.id.btnLogin);
        test = (Button)findViewById(R.id.btnTest);
        txtusername = (EditText)findViewById(R.id.txtUser);
        txtpassword = (EditText)findViewById(R.id.txtPass);
        txtUpdate = (TextView)findViewById(R.id.txtUpdate);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToTestActivity();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                txtUpdate.setText("Registering device");
                username = txtusername.getText().toString();
                password = txtpassword.getText().toString();
                myFirebaseRef.authWithPassword(username, password, new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        spinner.setVisibility(View.GONE);
                        txtUpdate.setText("");

                        Log.d("SUCCSS", "Great success!");
                        Toast.makeText(getApplicationContext(),"successfully logged in!",Toast.LENGTH_LONG).show();
                        goToSecondActivity();
                    }
                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        spinner.setVisibility(View.GONE);
                        txtUpdate.setText("");

                        Log.d("FAILED","Oh dear, you've failed to login");
                        Log.d("BECUSE",firebaseError.getMessage());
                        Toast.makeText(getApplicationContext(),firebaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    private void goToSecondActivity(){
        Intent intent = new Intent(this,SenseActivity.class);
        startActivity(intent);
    }
    private void goToTestActivity(){
        try {
            ESSensorManager sm = ESSensorManager.getSensorManager(context);
            Intent intent = new Intent(this,SenseActivity.class);
            startActivity(intent);
        } catch (ESException e) {
            e.printStackTrace();
        }

//        Intent intent = new Intent(this,TestActivity.class);
//        startActivity(intent);
    }

}

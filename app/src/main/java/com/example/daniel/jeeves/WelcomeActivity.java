package com.example.daniel.jeeves;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.daniel.jeeves.firebase.FirebaseUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.ContentValues.TAG;
import static com.example.daniel.jeeves.ApplicationContext.DEVELOPER_ID;
import static com.example.daniel.jeeves.ApplicationContext.STUDY_NAME;
import static com.example.daniel.jeeves.ApplicationContext.UID;
import static com.example.daniel.jeeves.ApplicationContext.USERNAME;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.PROJECTS_KEY;
import static com.example.daniel.jeeves.firebase.FirebaseUtils.SURVEYDATA_KEY;

//03/08 I'm now doing location stuff in here as it's easier than sticking it in the Sensor Manager module.
public class WelcomeActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static WelcomeActivity instance;
    FirebaseAuth mFirebaseAuth;
    TextView txtWelcome;
    private LocationRequest mLocationRequest;
    SenseService mService;
    boolean mBound = false;
    GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_CHECK_SETTINGS = 101;
    private boolean mRequestingLocationUpdates = true;
    @Override
    protected void onResume() {
        super.onResume();
        instance = this;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
//        //Rebind ourselves to the service
//        Intent intent = new Intent(this, SenseService.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
//        if (mBound) {
//            unbindService(mConnection);
//            mBound = false;
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        mFirebaseAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sense);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());

        //We MAY need to reset these
        if (FirebaseUtils.PATIENT_REF == null) {
            FirebaseDatabase database = FirebaseUtils.getDatabase();
            FirebaseUtils.SURVEY_REF = database.getReference(FirebaseUtils.PRIVATE_KEY).child(prefs.getString(DEVELOPER_ID, "")).child(PROJECTS_KEY).child(prefs.getString(STUDY_NAME, "")).child(SURVEYDATA_KEY);
            FirebaseUtils.PATIENT_REF = database.getReference(FirebaseUtils.PRIVATE_KEY).child(prefs.getString(DEVELOPER_ID, "")).child(FirebaseUtils.PATIENTS_KEY).child(prefs.getString(UID, ""));
        }
        //START THE SENSING SERVICE
        //BIND TO IT
        Intent intent = new Intent(this, SenseService.class);
        startService(intent);
   //     bindService(intent, mConnection, Context.BIND_AUTO_CREATE);



        txtWelcome = (TextView)findViewById(R.id.txtWelcome);
        txtWelcome.setText("Welcome, " + prefs.getString(USERNAME,""));


        Button btnContact = (Button) findViewById(R.id.btnContact);
        Button btnSurveys = (Button) findViewById(R.id.btnSurvey);
        Button btnMonitor = (Button) findViewById(R.id.btnMonitor);
        Button btnLogout = (Button) findViewById(R.id.buttonLogout);
        Button btnVars = (Button) findViewById(R.id.buttonVars);
        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance,ContactActivity.class);
                startActivity(intent);
            }
        });

        btnSurveys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance,MissedSurveyActivity.class);
                startActivity(intent);
            }
        });


        btnMonitor.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance,MonitorActivity.class);
                startActivity(intent);
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance,CheckScheduleActivity.class);
                startActivity(intent);
                //       mFirebaseAuth = FirebaseAuth.getInstance();
          //      mFirebaseAuth.signOut();
          //      finish();
            }
        });
        btnVars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance,CheckVariablesActivity.class);
                startActivity(intent);
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,builder.build());
        Log.d("HEREWEGO","here we go away to try the task!");
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d("SUCCEEEEEEEEES","here we go away to try the task!");

                        Intent intent = new Intent(WelcomeActivity.this, SenseService.class);
                        intent.putExtra("locationRequest", mLocationRequest);
                        Log.d("STARTING","Away to start service with a location request!");
                        startService(intent);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        Log.d("FAIL","Failed but we can fix this!");

                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(WelcomeActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d("SUPERFAIL","Failed and there's no going back");

                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
//        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
//            @Override
//            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//
//            }
//        });
//        task.addOnFailureListener(this, new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                int statusCode = ((ApiException) e).getStatusCode();
//                Log.d("FAIL","All aboard the failboat" + statusCode);
//                switch (statusCode) {
//                    case CommonStatusCodes.RESOLUTION_REQUIRED:
//
//                        break;
//                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//
//                        break;
//                }
//            }
//        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() called with: " + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(WelcomeActivity.this, SenseService.class);
                    intent.putExtra("locationRequest", mLocationRequest);
                    Log.d("STARTING","Away to start service with a location request!");
                    startService(intent);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    /** Defines callbacks for service binding, passed to bindService() */
//    private ServiceConnection mConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className,
//                                       IBinder service) {
//            // We've bound to LocalService, cast the IBinder and get LocalService instance
//            SenseService.LocalBinder binder = (SenseService.LocalBinder) service;
//            mService = binder.getService();
//            mBound = true;
//            createLocationRequest();
//
//            if (mRequestingLocationUpdates) {
//                mService.startLocationUpdates(mLocationRequest);
//            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            mBound = false;
//        }
//    };
}

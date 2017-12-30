package com.jeeves;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jeeves.R;
import com.jeeves.firebase.FirebaseUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;

import static android.content.ContentValues.TAG;
import static com.jeeves.ApplicationContext.DEVELOPER_ID;
import static com.jeeves.ApplicationContext.STUDY_NAME;
import static com.jeeves.ApplicationContext.UID;
import static com.jeeves.ApplicationContext.USERNAME;

//03/08 I'm now doing location stuff in here as it's easier than sticking it in the Sensor Manager module.
public class WelcomeActivity extends Activity {
    private static WelcomeActivity instance;
   // private LocationCallback mLocationCallback;

    FirebaseAuth mFirebaseAuth;
    TextView txtWelcome;
  //  private LocationRequest mLocationRequest;
    SenseService mService;
   // boolean mBound = false;
    GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_CHECK_SETTINGS = 101;
    private boolean mRequestingLocationUpdates = true;
    private FusedLocationProviderClient mFusedLocationClient;
  //  private ActivityRecognitionClient activityRecognitionClient;
    private SettingsClient mSettingsClient;
  //  private LocationSettingsRequest mLocationSettingsRequest;
    @Override
    protected void onResume() {
        super.onResume();
        instance = this;

        //  buildLocationSettingsRequest();
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(LocationServices.API)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .build();
//        mGoogleApiClient.connect();
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
      //  mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
       // mSettingsClient = LocationServices.getSettingsClient(this);
       // createLocationCallback();

        //createLocationRequest();

//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(ActivityRecognition.API)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .build();
//        mGoogleApiClient.connect();
        //activityRecognition = new ActivityRecognitionClient(this, this, this);
       // activityRecognition.connect();
//        activityRecognitionClient = ActivityRecognition.getClient(this);
//        Intent serviceIntent = new Intent(this, ActivityService.class);
//        PendingIntent pendingIntent = PendingIntent.getService(this,0,serviceIntent,PendingIntent.FLAG_UPDATE_CURRENT);
//        Task<Void> task = activityRecognitionClient.requestActivityUpdates(20000, pendingIntent);
//
//        task.addOnSuccessListener(new OnSuccessListener<Void>() {
//
//            @Override
//
//            public void onSuccess(Void result) {
//
//                Toast.makeText(ApplicationContext.getContext(),
//
//                        "Activity updates ARE enabled",
//
//                        Toast.LENGTH_SHORT)
//
//                        .show();
//
//                //setUpdatesRequestedState(true);
//
//               // updateDetectedActivitiesList();
//
//            }
//
//        });
//
//
//
//        task.addOnFailureListener(new OnFailureListener() {
//
//            @Override
//
//            public void onFailure(@NonNull Exception e) {
//
//                Log.w(TAG, "Activity updates not enabled");
//
//                Toast.makeText(ApplicationContext.getContext(),
//
//                    "Activity updates not enabled",
//                        Toast.LENGTH_SHORT)
//
//                        .show();
//
//               // setUpdatesRequestedState(false);
//
//            }
//
//        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sense);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());

        //We MAY need to reset these
        if (FirebaseUtils.PATIENT_REF == null) {
            FirebaseDatabase database = FirebaseUtils.getDatabase();
            FirebaseUtils.SURVEY_REF = database.getReference(FirebaseUtils.PRIVATE_KEY).child(prefs.getString(DEVELOPER_ID, "")).child(FirebaseUtils.PROJECTS_KEY).child(prefs.getString(STUDY_NAME, "")).child(FirebaseUtils.SURVEYDATA_KEY);
            FirebaseUtils.PATIENT_REF = database.getReference(FirebaseUtils.PRIVATE_KEY).child(prefs.getString(DEVELOPER_ID, "")).child(FirebaseUtils.PATIENTS_KEY).child(prefs.getString(UID, ""));
        }
        //START THE SENSING SERVICE
        //BIND TO IT
        Intent intent = new Intent(this, SenseService.class);
        startService(intent);
        //     bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


        txtWelcome = (TextView) findViewById(R.id.txtWelcome);
        txtWelcome.setText("Welcome, " + prefs.getString(USERNAME, ""));


        Button btnContact = (Button) findViewById(R.id.btnContact);
        Button btnSurveys = (Button) findViewById(R.id.btnSurvey);
        Button btnMonitor = (Button) findViewById(R.id.btnMonitor);
        Button btnViewData = (Button) findViewById(R.id.btnViewData);
//        Button btnLogout = (Button) findViewById(R.id.buttonLogout);
//        Button btnVars = (Button) findViewById(R.id.buttonVars);

        btnViewData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(instance, ViewDataActivity.class);
                startActivity(intent);
            }
        });
        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance, ContactActivity.class);
                startActivity(intent);
            }
        });

        btnSurveys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance, MissedSurveyActivity.class);
                startActivity(intent);
            }
        });


        btnMonitor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance, MonitorActivity.class);
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
    public void forceCrash(View view) {
        throw new RuntimeException("This is a crash");
    }
//
//    private void createLocationCallback() {
//
//        mLocationCallback = new LocationCallback() {
//
//            @Override
//
//            public void onLocationResult(LocationResult locationResult) {
//
//                super.onLocationResult(locationResult);
//                HashMap<String, Object> locData = new HashMap<String, Object>();
//                String mLastUpdateTime = new Date().toString();
//                locData.put("senseStartTimeMillis", mLastUpdateTime);
//                locData.put("latitude", locationResult.getLastLocation().getLatitude());
//                locData.put("longitude", locationResult.getLastLocation().getLongitude());
//                Log.d("OOOH","Locaiton changed");
//                DatabaseReference patientRef = FirebaseUtils.PATIENT_REF.child("sensordata").child("Location").push();
//                patientRef.setValue(locData);
//            }
//
//        };
//
//    }
//
//    private void buildLocationSettingsRequest() {
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//
//        builder.addLocationRequest(mLocationRequest);
//
//        mLocationSettingsRequest = builder.build();
//
//    }

//    public void createLocationRequest() {
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(20000);
//        mLocationRequest.setFastestInterval(5000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        buildLocationSettingsRequest();
//                mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
//
//                        .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
//
//                            @Override
//
//                            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//
//                                Log.i(TAG, "All location settings are satisfied.");
//
//
//                                //noinspection MissingPermission
//
//                                if (ActivityCompat.checkSelfPermission(WelcomeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(WelcomeActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                                    return;
//                                }
//                                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
//
//                                        mLocationCallback, Looper.myLooper());
//                            }
//
//                        })
//
//                        .addOnFailureListener(this, new OnFailureListener() {
//
//                            @Override
//
//                            public void onFailure(@NonNull Exception e) {
//
//                                int statusCode = ((ApiException) e).getStatusCode();
//
//                                switch (statusCode) {
//
//                                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//
//                                        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
//
//                                                "location settings ");
//
//                                        try {
//
//                                            // Show the dialog by calling startResolutionForResult(), and check the
//
//                                            // result in onActivityResult().
//
//                                            ResolvableApiException rae = (ResolvableApiException) e;
//
//                                            rae.startResolutionForResult(WelcomeActivity.this, REQUEST_CHECK_SETTINGS);
//
//                                        } catch (IntentSender.SendIntentException sie) {
//
//                                            Log.i(TAG, "PendingIntent unable to execute request.");
//
//                                        }
//
//                                        break;
//
//                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//
//                                        String errorMessage = "Location settings are inadequate, and cannot be " +
//
//                                                "fixed here. Fix in Settings.";
//
//                                        Log.e(TAG, errorMessage);
//                                        mRequestingLocationUpdates = false;
//
//                                }
//                            }
//
//                        });
//
//    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d(TAG, "onActivityResult() called with: " + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
//        switch (requestCode) {
//            case REQUEST_CHECK_SETTINGS:
//                if (resultCode == Activity.RESULT_OK) {
//                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                        return;
//                    }
//
//                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
////                    Intent intent = new Intent(WelcomeActivity.this, SenseService.class);
////                    intent.putExtra("locationRequest", mLocationRequest);
////                    Log.d("STARTING","Away to start service with a location request!");
////                    startService(intent);
//                }
//                break;
//            default:
//                super.onActivityResult(requestCode, resultCode, data);
//                break;
//        }
//    }

//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//    Intent intent = new Intent( this, ActivityService.class );
//    Log.d("CONNECTED","I am connected");
//    PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
//    ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mGoogleApiClient, 60000, pendingIntent );
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Log.d("NOPEFAIL","I am NOT CONNECTED");
//
//    }
}

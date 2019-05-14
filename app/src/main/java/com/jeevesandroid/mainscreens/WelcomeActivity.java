package com.jeevesandroid.mainscreens;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jeevesandroid.AppContext;
import com.jeevesandroid.R;
import com.jeevesandroid.SenseService;
import com.jeevesandroid.actions.WhileLoopReceiver;
import com.jeevesandroid.firebase.FirebaseProject;
import com.jeevesandroid.firebase.FirebaseUtils;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends Activity {
    private WelcomeActivity getInstance(){
        return this;
    }


    /**
     * This checks that all necessary permissions have been given by the user.
     * If not, they are asked for sequentially.
     */
    private void permissionThings(){
        FirebaseProject currentProj = AppContext.getProject();
        List<String> sensors = currentProj.getsensors();
        List<String> requestList = new ArrayList<>();
        for(String sensor : sensors){

            switch(sensor){
                case "Location":
                    checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,requestList);
                    break;
                case "Microphone":
                    checkPermission(Manifest.permission.RECORD_AUDIO,requestList);
                    break;
                case "SMS":
                    checkPermission(Manifest.permission.BROADCAST_SMS,requestList);
                    break;
                case "Heart":
                    checkPermission(Manifest.permission.CAMERA,requestList);
                    break;
            }
        }
        String[] permissionArray = new String[requestList.size()];
        requestList.toArray(permissionArray);
        //Do the permission request (callback implemented in onRequestPermissionsResult)
        if(permissionArray.length > 0) {
            ActivityCompat.requestPermissions(this,
                permissionArray,
                1234);
        }
        else{
            Intent intent = new Intent(this, SenseService.class);
            startService(intent);
        }
    }
    /**
     * Check we have the permission. If not, add it to the list of permissions
     * we need to request
     * @param permission String representation of necessary permission
     * @param requestList List of permissions to be requested
     */
    private void checkPermission(String permission, List<String> requestList){
        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED) {
            requestList.add(permission);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        permissionThings();
        setContentView(R.layout.activity_welcome);
        SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());
        //Reset these if the app has been closed and then opened again
        if (FirebaseUtils.PATIENT_REF == null) {
            FirebaseDatabase database = FirebaseUtils.getDatabase();
            FirebaseUtils.SURVEY_REF = database.getReference(FirebaseUtils.PRIVATE_KEY)
                .child(prefs.getString(AppContext.DEVELOPER_ID, ""))
                .child(FirebaseUtils.PROJECTS_KEY)
                .child(prefs.getString(AppContext.STUDY_NAME, ""))
                .child(FirebaseUtils.SURVEYDATA_KEY);
            FirebaseUtils.PATIENT_REF = database.getReference(FirebaseUtils.PRIVATE_KEY)
                .child(prefs.getString(AppContext.DEVELOPER_ID, ""))
                .child(FirebaseUtils.PATIENTS_KEY)
                .child(prefs.getString(AppContext.UID, ""));
        }

        TextView txtWelcome = findViewById(R.id.txtWelcome);
        txtWelcome.setText(String.format(getResources().getString(R.string.welcome),
            prefs.getString(AppContext.USERNAME,"")));

        Button btnContact = findViewById(R.id.btnContact);
        Button btnSurveys = findViewById(R.id.btnSurvey);
        Button btnMonitor = findViewById(R.id.btnMonitor);
        Button btnViewData = findViewById(R.id.btnViewData);
        Button btnQuit = findViewById(R.id.btnQuit);
        btnViewData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getInstance(), PrivacyPolicy.class);
                startActivity(intent);
            }
        });
        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(), ContactActivity.class);
                startActivity(intent);
            }
        });

        btnSurveys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(), MissedSurveyActivity.class);
                startActivity(intent);
            }
        });
        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            new AlertDialog.Builder(getInstance())
                .setTitle("Exit app")
                .setMessage("If you're done with the Jeeves app for now, press 'OK' to quit.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                    Intent intent = new Intent(getInstance(), SenseService.class);
                    stopService(intent);
                    quitApp();
                    }})
                .setNegativeButton(android.R.string.no, null).show();

            }
        });

        btnMonitor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(), SelfReportActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Kill the While Loop broadcast receiver that lives on after SenseService is
     * dead, then quit the app altogether
     */
    private void quitApp(){
        AlarmManager alarmManager = (AlarmManager) AppContext.getContext()
            .getSystemService(ALARM_SERVICE);
        Intent newLoopIntent=new Intent(AppContext.getContext(), WhileLoopReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(
            AppContext.getContext(), 234, newLoopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pi);
        finish();
    }

    /**
     * Callback function that handles a permission being granted/refused
     * @param requestCode Integer request code
     * @param permissions List of permissions requested
     * @param grantResults List of results granted to the permssions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        boolean denied = false;
        String deniedpermission = "";
        for (int i = 0, len = permissions.length; i < len; i++) {
            String permission = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                denied = true;
                deniedpermission = permission;
                break;
            }
        }
        if(!denied) { //All permissions are accepted! Start our Sensing Service
            Intent intent = new Intent(this, SenseService.class);
            startService(intent);
            return;
        }

        boolean showRationale = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            showRationale = shouldShowRequestPermissionRationale(deniedpermission);
        }
        if (! showRationale) {
            new AlertDialog.Builder(this)
                .setTitle(R.string.permissions_title)
                .setMessage(R.string.need_permissions)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        } else{
            new AlertDialog.Builder(this)
            .setTitle(R.string.permissions_title)
            .setMessage(R.string.permissions_rationale)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    permissionThings();
                }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
}

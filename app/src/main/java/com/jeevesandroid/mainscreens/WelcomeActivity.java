package com.jeevesandroid.mainscreens;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jeevesandroid.AppContext;
import com.jeevesandroid.UncaughtExceptionHandler;
import com.jeevesandroid.R;
import com.jeevesandroid.SenseService;
import com.jeevesandroid.SnoozeListener;
import com.jeevesandroid.actions.WhileLoopReceiver;
import com.jeevesandroid.firebase.FirebaseProject;
import com.jeevesandroid.firebase.FirebaseUtils;
import com.google.firebase.database.FirebaseDatabase;
import com.jeevesandroid.triggers.TriggerException;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public class WelcomeActivity extends Activity {
    private WelcomeActivity getInstance(){
        return this;
    }

    private AlertDialog snoozeDialog;
    SharedPreferences.OnSharedPreferenceChangeListener mListener;
    PendingIntent wakeupSnoozepi;
    /**
     * This checks that all necessary permissions have been given by the user.
     * If not, they are asked for sequentially.
     */
    private void permissionThings(){
        FirebaseProject currentProj = AppContext.getProject();
        //It can happen...
        if(currentProj == null){
            return;
        }
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
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(AppContext.getContext()));

        permissionThings();
        setContentView(R.layout.activity_welcome);
        SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());
        //Reset these if the app has been closed and then opened again
        if (FirebaseUtils.PATIENT_REF == null) {
            FirebaseDatabase database = FirebaseUtils.getDatabase();
            FirebaseUtils.SURVEY_REF = database
                //.getReference(FirebaseUtils.PRIVATE_KEY)
                //.child(prefs.getString(AppContext.DEVELOPER_ID, ""))
                .getReference(FirebaseUtils.PROJECTS_KEY)
                .child(prefs.getString(AppContext.STUDY_NAME, ""))
                .child(FirebaseUtils.SURVEYDATA_KEY);
            FirebaseUtils.PATIENT_REF = database
                //.getReference(FirebaseUtils.PRIVATE_KEY)
                //.child(prefs.getString(AppContext.DEVELOPER_ID, ""))
                .getReference(FirebaseUtils.PATIENTS_KEY)
                .child(prefs.getString(AppContext.UID, ""));
        }

        TextView txtWelcome = findViewById(R.id.txtWelcome);
        txtWelcome.setText(String.format(getResources().getString(R.string.welcome),
            prefs.getString(AppContext.USERNAME,"")));

        Button btnContact = findViewById(R.id.btnContact);
        Button btnSurveys = findViewById(R.id.btnSurvey);
        Button btnMonitor = findViewById(R.id.btnMonitor);
        Button btnViewData = findViewById(R.id.btnViewData);
        Button btnTriggers = findViewById(R.id.btnTriggers);
        final Button btnQuit = findViewById(R.id.btnQuit);
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
        btnTriggers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getInstance(), TriggerViewActivity.class);
                startActivity(intent);
            }
        });
        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSnoozeDialog();
            }
        });
        final TextView textDesc = findViewById(R.id.textView);
        if(prefs.getBoolean(AppContext.SNOOZE,false)) {
            btnQuit.setText(R.string.unsnooze);
            textDesc.setText(R.string.unsnooze_desc);
            btnQuit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unsnooze();
                }
            });

        }
        else{
            btnQuit.setText(R.string.snooze);
            textDesc.setText(R.string.snooze_desc);
        }
        //https://stackoverflow.com/questions/2542938/
        mListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
               // Log.d("REFS","Pref change");
                if(key.equals(AppContext.SNOOZE)){
                    Log.d("PREFCHANGE","And it's snoooze");
                    if(prefs.getBoolean(AppContext.SNOOZE,false)) {
                        btnQuit.setText(R.string.unsnooze);
                        textDesc.setText(R.string.unsnooze_desc);
                        btnQuit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                unsnooze();
                            }
                        });
                    }
                    else{
                        btnQuit.setText(R.string.snooze);
                        textDesc.setText(R.string.snooze_desc);
                        btnQuit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showSnoozeDialog();
                            }
                        });
                    }
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(mListener);
        btnMonitor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(), SelfReportActivity.class);
                startActivity(intent);
            }
        });
        if(prefs.getBoolean(AppContext.SNOOZE,false)) {
            btnQuit.setText(R.string.unsnooze);
            textDesc.setText(R.string.unsnooze_desc);
            btnQuit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unsnooze();
                }
            });
        }
        else{
            btnQuit.setText(R.string.snooze);
            textDesc.setText(R.string.snooze_desc);
        }
        //https://stackoverflow.com/questions/2542938/
        mListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
               // Log.d("REFS","Pref change");
                if(key.equals(AppContext.SNOOZE)){
                    Log.d("PREFCHANGE","And it's snoooze");
                    if(prefs.getBoolean(AppContext.SNOOZE,false)) {
                        btnQuit.setText(R.string.unsnooze);
                        textDesc.setText(R.string.unsnooze_desc);
                        btnQuit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                unsnooze();
                            }
                        });
                    }
                    else{
                        btnQuit.setText(R.string.snooze);
                        textDesc.setText(R.string.snooze_desc);
                        btnQuit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showSnoozeDialog();
                            }
                        });
                    }
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(mListener);
    }
    private void unsnooze(){
        AlarmManager alarmManager = (AlarmManager) AppContext.getContext()
            .getSystemService(ALARM_SERVICE);
        alarmManager.cancel(wakeupSnoozepi);
        SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());
        Toast.makeText(this,"Snooze finished",Toast.LENGTH_LONG).show();
        SharedPreferences.Editor prefseditor = prefs.edit();
        prefseditor.putBoolean(AppContext.SNOOZE,false);
        prefseditor.apply();
    }
    private void showSnoozeDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getInstance());
        // Get the layout inflater
        LayoutInflater inflater = getInstance().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.snooze_dialog, null))
            // Add action buttons
            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    snoozeApp();
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
        snoozeDialog = builder.create();
        snoozeDialog.show();
    }
    private void snoozeApp(){
        Spinner spin = snoozeDialog.findViewById(R.id.mySpinner);
        String item = spin.getSelectedItem().toString();
        SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());
        AlarmManager alarmManager = (AlarmManager) AppContext.getContext()
            .getSystemService(ALARM_SERVICE);
        Intent intent=new Intent(AppContext.getContext(), SnoozeListener.class);
        wakeupSnoozepi = PendingIntent.getBroadcast(AppContext.getContext(), 0, intent, 0);

        SharedPreferences.Editor prefseditor = prefs.edit();
        if(item.equals(getString(R.string.fifteen))) {
            prefseditor.putBoolean(AppContext.SNOOZE,true);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + (15*60*1000), wakeupSnoozepi);
        }
        else if(item.equals(getString(R.string.thirty))){
            prefseditor.putBoolean(AppContext.SNOOZE,true);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + (30*60*1000), wakeupSnoozepi);
        }
        else if(item.equals(getString(R.string.hour))){
            prefseditor.putBoolean(AppContext.SNOOZE,true);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + (60*60*1000), wakeupSnoozepi);
        }
        else if(item.equals(getString(R.string.threehr))){
            prefseditor.putBoolean(AppContext.SNOOZE,true);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + (180*60*1000), wakeupSnoozepi);
        }
        else if(item.equals(getString(R.string.day))){
            prefseditor.putBoolean(AppContext.SNOOZE,true);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + (24*3600*1000), wakeupSnoozepi);
        }
        //Stop the app altogether
        else if(item.equals(getString(R.string.forever))){
            Intent stopIntent = new Intent(getInstance(), SenseService.class);
            stopService(stopIntent);
            quitApp();
        }
        prefseditor.apply();
        Toast.makeText(this,"Jeeves is now snoozing " + item,Toast.LENGTH_LONG).show();

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

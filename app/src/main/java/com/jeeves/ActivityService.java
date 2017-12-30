package com.jeeves;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jeeves.firebase.FirebaseUtils;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.database.DatabaseReference;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.icu.lang.UCharacter.getType;

/**
 * Created by Dan on 10/27/2017.
 */


public class ActivityService extends IntentService {

    public ActivityService(){
        super("Service");
    }
    // public ActivityService(String name) {
    //     super(name);
    // }
    int activityCode;
    int activityConfidence;
    ActivityRecognitionResult result;
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
             result = ActivityRecognitionResult.extractResult(intent);
            Log.i(TAG, getType(result.getMostProbableActivity().getType()) + "t" + result.getMostProbableActivity().getConfidence());
            Log.d("o3nWatcherLog", "ActivityRecognitionService onHandleIntent called...");

            activityConfidence = result.getMostProbableActivity().getConfidence();
            activityCode = result.getMostProbableActivity().getType();

            Log.d("o3nWatcherLog", " ACTIVITY CODE : " + activityCode + " ACTIVITY CONFIDENCE : " + activityConfidence);

            // Evaluate the avtivity recognition result
            evaluateActivityResult();


        }
        else
        {
            Log.d("NO RESULT", "DIDnt get a result");
        }
    }
    public void evaluateActivityResult() {
        String activityResult = "";
        switch (activityCode) {
            case DetectedActivity.IN_VEHICLE:
                activityResult = "Driving";
                break;
            case DetectedActivity.ON_BICYCLE:
                activityResult = "Cycling";
                break;
            case DetectedActivity.ON_FOOT:
                activityResult = "On foot";
                break;
            case DetectedActivity.RUNNING:
                activityResult = "Running";
                break;
            case DetectedActivity.STILL:
                activityResult = "Still";
                break;
            case DetectedActivity.TILTING:
                activityResult = "Tilting";
                break;
            case DetectedActivity.UNKNOWN:
                activityResult = "Unknown";
                break;
            case DetectedActivity.WALKING:
                activityResult = "Walking";
                break;

            default:
                Log.d("Oh for", "fuck sake");
                break;

        }
        HashMap<String, Object> locData = new HashMap<String, Object>();
        String mLastUpdateTime = new Date().toString();
        locData.put("senseStartTimeMillis", mLastUpdateTime);
        if (activityCode == DetectedActivity.ON_FOOT) {
            DetectedActivity betterActivity = walkingOrRunning(result.getProbableActivities());
            if (null != betterActivity)
                switch(betterActivity.getType()){
                    case DetectedActivity.WALKING: activityResult = "Walking"; break;
                    case DetectedActivity.RUNNING: activityResult = "Running"; break;}
        }
        locData.put("result", activityResult);

        Log.d("OOOH", "Locaiton changed");
        if(FirebaseUtils.PATIENT_REF == null)
            return;
        DatabaseReference patientRef = FirebaseUtils.PATIENT_REF.child("sensordata").child("Activity").push();
        patientRef.setValue(locData);

    }
        private DetectedActivity walkingOrRunning(List<DetectedActivity> probableActivities) {
            DetectedActivity myActivity = null;
            int confidence = 0;
            for (DetectedActivity activity : probableActivities) {
                if (activity.getType() != DetectedActivity.RUNNING && activity.getType() != DetectedActivity.WALKING)
                    continue;

                if (activity.getConfidence() > confidence)
                    myActivity = activity;
            }

            return myActivity;
        }
    }

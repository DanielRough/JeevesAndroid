package com.jeevesx.sensing;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.jeevesx.actions.ActionExecutorService;
import com.jeevesx.AppContext;
import com.jeevesx.actions.ActionUtils;
import com.jeevesx.actions.actiontypes.FirebaseAction;

import java.util.ArrayList;
import java.util.List;

/**
 This is the Activity equivalent of the Geofence Listener(i.e. Activity Trigger detection)
 The 'ActivityService' class is that used for continuous Activity detection
 */
public class ActivityListener {

    private final Context serviceContext;
    private final String activityType;
    private final List<FirebaseAction> actions;
    private final ArrayList<ActivityTransition> transitionList;
    private ActivityRecognitionClient mClient;
    private int transitionType;
    private final String triggerId;

    public ActivityListener(Context c,String activity, String id, List<FirebaseAction> acts) {
        this.serviceContext = c;
        this.activityType = activity;
        this.triggerId = id;
        transitionList = new ArrayList<>();
        actions = new ArrayList<>();
        for (FirebaseAction action : acts) {
            actions.add(ActionUtils.create(action));
        }
    }

    public void removeActivityTrigger(){
        mClient.removeActivityTransitionUpdates(getActionsPendingIntent());
    }

    public String getTriggerId(){
        return triggerId;
    }

    public void addActivityTrigger() {
        mClient = ActivityRecognition.getClient(serviceContext);
        switch(activityType){
            case "Walking":
                transitionType = DetectedActivity.WALKING;
                break;
            case "Running":
                transitionType = DetectedActivity.RUNNING;
                break;
            case "Still":
                transitionType = DetectedActivity.STILL;
                break;
            case "Driving":
                transitionType = DetectedActivity.IN_VEHICLE;
                break;
            default:
                Log.d("UNKNOWN","Unkown activity");
                return;
        }

        transitionList.add(new ActivityTransition.Builder()
            .setActivityType(transitionType)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build());
        ActivityTransitionRequest request = new ActivityTransitionRequest(transitionList);
        Task task = mClient
            .requestActivityTransitionUpdates(request, getActionsPendingIntent());
        task.addOnSuccessListener(
            new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    Log.d("ACTIVITY","Listening on activity updates");
                }
            });
        task.addOnFailureListener(
            new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                }
            });
    }

    private PendingIntent getActionsPendingIntent(){
        Intent actionIntent = new Intent(serviceContext, ActionExecutorService.class);
        actionIntent.putExtra(ActionUtils.ACTIONSETID, activityType);
        AppContext.getActivityActions().put(activityType,actions);
        return PendingIntent.getService(serviceContext, transitionType, actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);
    }

}

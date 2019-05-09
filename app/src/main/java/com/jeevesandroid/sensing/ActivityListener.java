package com.jeevesandroid.sensing;

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
import com.jeevesandroid.actions.ActionExecutorService;
import com.jeevesandroid.ApplicationContext;
import com.jeevesandroid.actions.ActionUtils;
import com.jeevesandroid.actions.actiontypes.FirebaseAction;

import java.util.ArrayList;
import java.util.List;

/*
This should be the Activity equivalent of the Geofence Listener
(i.e. Activity Trigger detection)
 */
public class ActivityListener {

    private Context serviceContext;
    private String activityType;
    private List<FirebaseAction> actions;
    private ArrayList<ActivityTransition> transitionList;
    private ActivityRecognitionClient mClient;
    private int transitionType;
    private final String triggerId;

    public ActivityListener(Context c,String activityType, String triggerId, List<FirebaseAction> actionsToPerform) {
        this.serviceContext = c;
        this.activityType = activityType;
        this.triggerId = triggerId;
        transitionList = new ArrayList<>();
        actions = new ArrayList<>();
        for (FirebaseAction action : actionsToPerform) {
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
                    Log.d("ACTIFAIL","Failed");
                    e.printStackTrace();
                }
            });
    }

    private PendingIntent getActionsPendingIntent(){
        Intent actionIntent = new Intent(serviceContext, ActionExecutorService.class);
        actionIntent.putExtra(ActionUtils.ACTIONSETID, activityType); //each location name corresponds to a set of actions
        //Although the ApplicationContext variable would get destroyed when the app resets, the Geofencing trigger will also reset itself...won't it?
        ApplicationContext.getActivityActions().put(activityType,actions);
        //If we use the transition type as the request code, this should distinguish the pending intents
        return PendingIntent.getService(serviceContext, transitionType, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}

package com.jeeves.sensing;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.jeeves.AppContext;
import com.jeeves.actions.ActionExecutorService;
import com.jeeves.actions.ActionUtils;
import com.jeeves.actions.actiontypes.FirebaseAction;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

/**
* Listener for Location Triggers using Google's Geofencing API
 */

public class GeofenceListener {

    private GeofencingClient mGeofencingClient;
    private ArrayList<Geofence> mGeofenceList;
    private final Context serviceContext;
    private final String locationName;
    private final String changes;
    private final List<FirebaseAction> actionsToPerform;
    private final String triggerId;


    public GeofenceListener(Context c,String locationName, String triggerId,
                            String changes, List<FirebaseAction> actions){
        this.serviceContext = c;
        this.locationName = locationName;
        this.triggerId = triggerId;
        this.changes = changes;
        actionsToPerform = new ArrayList<>();

        for (FirebaseAction action : actions) {
            actionsToPerform.add(ActionUtils.create(action));
        }
    }
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        if(changes.equals("enters"))
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        else if(changes.equals("leaves"))
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT);
        else
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    public void removeLocationTrigger(){
        mGeofencingClient.removeGeofences(getActionsPendingIntent())
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    addLocationTrigger();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
    }

    public String getTriggerId(){
        return triggerId;
    }
    public void updateLocation(){
        removeLocationTrigger();
    }

    public void addLocationTrigger() {
        mGeofencingClient = LocationServices.getGeofencingClient(serviceContext);
        //   String locationName = params.getParameter("result").toString();
        Log.d("LocationName","Location name is " + locationName);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(serviceContext);
        String latlong = prefs.getString(locationName,"");
        Log.d("LATLONG","Latlong is " + latlong);
        if(latlong.isEmpty())return;
        if(latlong.endsWith(";")){
            latlong = latlong.substring(0,latlong.length()-1);
        }

        String[] latlongarray = latlong.split(":");
        double latval = Double.parseDouble(latlongarray[0]);
        double longval = Double.parseDouble(latlongarray[1]);
        int transition;
        switch(changes){
            case "enters":transition = Geofence.GEOFENCE_TRANSITION_ENTER; break;
            case "leaves":transition = Geofence.GEOFENCE_TRANSITION_EXIT; break;
            default: transition = Geofence.GEOFENCE_TRANSITION_DWELL; break;
        }
        mGeofenceList = new ArrayList<>();
        mGeofenceList.add(new Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId(locationName)

            .setCircularRegion(
                latval,
                longval,
                100
            )
            .setTransitionTypes(transition)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setLoiteringDelay(300000)
            .build());

        if (ContextCompat.checkSelfPermission(serviceContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mGeofencingClient.addGeofences(getGeofencingRequest(), getActionsPendingIntent())
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("SUCCSS","Successfully added the intent to the geofence!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("FAIL","failure");
                    e.printStackTrace();
                }
            });
    }

    private PendingIntent getActionsPendingIntent(){
        Intent actionIntent = new Intent(serviceContext, ActionExecutorService.class);
        actionIntent.putExtra(ActionUtils.ACTIONSETID, locationName); //each location name corresponds to a set of actions
        //Although the AppContext variable would get destroyed when the app resets, the Geofencing trigger will also reset itself...won't it?
        AppContext.getLocationActions().put(locationName,actionsToPerform);
        return PendingIntent.getService(serviceContext, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

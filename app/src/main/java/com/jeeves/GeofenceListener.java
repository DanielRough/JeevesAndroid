package com.jeeves;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.jeeves.actions.ActionUtils;
import com.jeeves.actions.FirebaseAction;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import static com.jeeves.actions.ActionUtils.ACTIONSETID;

/**
 * Created by Daniel on 07/08/2017.
 */

public class GeofenceListener {

    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;
    private ArrayList<Geofence> mGeofenceList;
    private Context serviceContext;
    String locationName;
    String changes;
    List<FirebaseAction> actionsToPerform;


    public GeofenceListener(Context c,String locationName, String changes, List<FirebaseAction> actions){
        this.serviceContext = c;
        this.locationName = locationName;
        this.changes = changes;
        actionsToPerform = new ArrayList<>();

        for (FirebaseAction action : actions) {
            Log.d("IIIIIS",  action.getname());

            actionsToPerform.add(ActionUtils.create(action));
            Log.d("oooh", "its" +  actionsToPerform.get(0).getname());
        }
//        for(FirebaseAction a : actionsToPerform){
//            Log.d("AND THIS HNNNNNNG","IS" + a.toString());
//        }
    }
        private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

//        if(changes.equals("enters"))
//            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
//        else if(changes.equals("leaves"))
//            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT);
//        else
//            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
            builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    public void removeLocationTrigger(){
        mGeofencingClient.removeGeofences(getActionsPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("REMOVED","OH WOW I REMOVED SOMETHING");
                        addLocationTrigger();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                    }
                });
    }


    public void updateLocation(){
        removeLocationTrigger();
       // addLocationTrigger(); //I think we must need to add this again!
    }
//    public void updateActions(List<FirebaseAction> actions){
//        actionsToPerform = new ArrayList<>();
//
//        for (FirebaseAction action : actions) {
//
//            actionsToPerform.add(ActionUtils.create(action));
//            Log.d("fAfffffction is ", action.getname());
//        }
//     //   this.actionsToPerform = actionsToPerform;
//        mGeofencePendingIntent = null;
//        removeLocationTrigger();
//    }
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
        int transition = 0;
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

        if (ActivityCompat.checkSelfPermission(serviceContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                    }
                });
    }



    private PendingIntent getActionsPendingIntent(){
        if (mGeofencePendingIntent != null) {
       //     return mGeofencePendingIntent;
        }
        //FirebaseAction setAttrAction =
        Intent actionIntent = new Intent(serviceContext, ActionExecutorService.class);
        actionIntent.putExtra(ACTIONSETID, locationName); //each location name corresponds to a set of actions
        Log.d("I'M AWAY","To list some actions");
        for(FirebaseAction a : actionsToPerform){
            Log.d("AND THIS ACTION","IS" + a.getname());
        }
        //Although the ApplicationContext variable would get destroyed when the app resets, the Geofencing trigger will also reset itself...won't it?
        ApplicationContext.getLocationActions().put(locationName,actionsToPerform);
     //   actionIntent.putExtra(TRIG_TYPE, triggerType);
        mGeofencePendingIntent = PendingIntent.getService(serviceContext, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }
}

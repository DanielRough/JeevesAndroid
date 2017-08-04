package com.example.daniel.jeeves;/* **************************************************
 Copyright (c) 2012, University of Cambridge
 Neal Lathia, neal.lathia@cl.cam.ac.uk
This demo application was developed as part of the EPSRC Ubhave (Ubiquitous and
Social Computing for Positive Behaviour Change) Project. For more
information, please visit http://www.emotionsense.org
Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.
THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ************************************************** */

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.daniel.jeeves.actions.ActionUtils;
import com.example.daniel.jeeves.actions.FirebaseAction;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.ubhave.triggermanager.ESTriggerManager;
import com.ubhave.triggermanager.TriggerException;
import com.ubhave.triggermanager.TriggerReceiver;
import com.ubhave.triggermanager.config.TriggerConfig;
import com.ubhave.triggermanager.triggers.TriggerUtils;

import java.util.ArrayList;

import static com.example.daniel.jeeves.ApplicationContext.TRIG_TYPE;
import static com.example.daniel.jeeves.actions.ActionUtils.ACTIONS;
import static com.example.daniel.jeeves.actions.ActionUtils.ACTIONSETID;

public class TriggerListener implements TriggerReceiver {

    private GeofencingClient mGeofencingClient;

// ...

    private final ESTriggerManager triggerManager;
    private String triggerId;
    private int triggerType, triggerSubscriptionId;
    private Context serviceContext;
    private ArrayList<FirebaseAction> actionsToPerform;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private TriggerConfig params;
    Intent actionIntent;
    private static int locTriggerId = 0;

    public TriggerListener(int triggerType, Context c) throws TriggerException {
        this.triggerType = triggerType;
        this.serviceContext = c;
        this.triggerManager = ESTriggerManager.getTriggerManager(ApplicationContext.getContext());

    }


    public void subscribeToTrigger(final TriggerConfig params, ArrayList<FirebaseAction> actions, String triggerId) {
        //     this.actions = actions;
        try {
            this.triggerId = triggerId;
            this.params = params;
            actionsToPerform = new ArrayList<>();
            for (FirebaseAction action : actions) {

                actionsToPerform.add(ActionUtils.create(action));
                Log.d("Action is ", action.getname());
            }
            //I think this is where I should check whether it's a location trigger?
            if (triggerType == TriggerUtils.TYPE_SENSOR_TRIGGER_IMMEDIATE) {
                String sensorType = params.getParameter("selectedSensor").toString();
                if (sensorType.equals("Location")) {
                    addLocationTrigger();
                    return;
                }
            }
            triggerSubscriptionId = triggerManager.addTrigger(triggerType, this, params);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unsubscribeFromTrigger(String caller) {
        try {
            //Again I'd better check if this is a location trigger because these need to be removed differently
            if (triggerType == TriggerUtils.TYPE_SENSOR_TRIGGER_IMMEDIATE) {
                String sensorType = params.getParameter("selectedSensor").toString();
                if (sensorType.equals("Location")) {
                    removeLocationTrigger();
                    return;
                }
            }
            triggerManager.removeTrigger(triggerSubscriptionId);
       ///     SubscriptionIds.removeSubscription(triggerId);
        } catch (TriggerException e) {
            e.printStackTrace();
        }
    }

    //Here's the method that gets called when the conditions are fulfilled. It starts the 'ActionExecutorService' to begin going through dem actions
    @Override
    public void onNotificationTriggered(int triggerId) {
        actionIntent = new Intent(serviceContext, ActionExecutorService.class);
        actionIntent.putExtra(ACTIONS, actionsToPerform);

        actionIntent.putExtra(TRIG_TYPE, triggerType);
        serviceContext.startService(actionIntent);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getActionsPendingIntent(){
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent actionIntent = new Intent(serviceContext, ActionExecutorService.class);
        actionIntent.putExtra(ACTIONSETID, locTriggerId);
        ApplicationContext.getLocationActions().put(locTriggerId,actionsToPerform);
        actionIntent.putExtra(TRIG_TYPE, triggerType);
        mGeofencePendingIntent = PendingIntent.getService(serviceContext, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    public void removeLocationTrigger(){
        mGeofencingClient.removeGeofences(getActionsPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                      Log.d("REMOVED","OH WOW I REMOVED SOMETHING");
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
//    public class LocalService extends IntentService {
//
////        public LocalService(String name) {
////            super(name);
////        }
//        public LocalService(){
//            super("Local Service");
//        }
//        @Nullable
//        @Override
//        public IBinder onBind(Intent intent) {
//            return null;
//        }
//
//        @Override
//        public void onCreate() {
//            super.onCreate();
//        }
//
//        @Override
//        public void onDestroy() {
//            super.onDestroy();
//        }
//
//        @Override
//        protected void onHandleIntent(Intent intent) {
//            Log.d("HELLO","DO I EVEN HAPPEN");
//
//            serviceContext.startService(actionIntent);
//        }
//        }

    public void addLocationTrigger() {
        locTriggerId++;
        mGeofencingClient = LocationServices.getGeofencingClient(serviceContext);
        String locationName = params.getParameter("result").toString();
        Log.d("LocationName","Location name is " + locationName);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(serviceContext);
        String latlong = prefs.getString(locationName,"");
        Log.d("LATLONG","Latlong is " + latlong);
        if(latlong.isEmpty())return;

//        actionIntent = new Intent(serviceContext, ActionExecutorService.class);
//        actionIntent.putExtra(ACTIONS, actionsToPerform);
//        actionIntent.putExtra(TRIG_TYPE, triggerType);
//        Log.d("ADDED A THING","put " + triggerType + " as the trigger type");
//
//        //This is a merciless hack to get the actions passed to the Geofence trigger properly
//    //    Intent localIntent = new Intent(serviceContext, LocalService.class);
//        PendingIntent pintent = PendingIntent.getService(serviceContext, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String[] latlongarray = latlong.split(";");
        double latval = Double.parseDouble(latlongarray[0]);
        double longval = Double.parseDouble(latlongarray[1]);
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
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
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

}

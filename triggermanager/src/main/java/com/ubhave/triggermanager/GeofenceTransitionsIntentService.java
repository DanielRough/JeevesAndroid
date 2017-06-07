package com.ubhave.triggermanager;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static com.google.android.gms.internal.zzt.TAG;

/**
 * Created by Daniel on 04/06/2017.
 */

public class GeofenceTransitionsIntentService extends IntentService {

    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            int errorMessage = geofencingEvent.getErrorCode();
            Log.e(TAG, Integer.toString(errorMessage));
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            String[] triggerIds = new String[triggeringGeofences.size()];

            for (int i = 0; i < triggerIds.length; i++) {
                // Store the Id of each geofence
                triggerIds[i] = triggeringGeofences.get(i).getRequestId();
                Log.d("INGEOFENC", "In the geofence " + triggerIds[1]);
            }

            // Send notification and log the transition details.
//            sendNotification();

        } else {
            // Log the error.
            Log.e(TAG, "There be an error here");
        }
    }
}

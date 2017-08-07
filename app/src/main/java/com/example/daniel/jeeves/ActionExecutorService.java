package com.example.daniel.jeeves;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.daniel.jeeves.actions.ActionUtils;
import com.example.daniel.jeeves.actions.FirebaseAction;
import com.example.daniel.jeeves.actions.IfControl;
import com.example.daniel.jeeves.actions.WaitingAction;
import com.example.daniel.jeeves.firebase.FirebaseExpression;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.daniel.jeeves.ApplicationContext.TRIG_TYPE;
import static com.example.daniel.jeeves.actions.ActionUtils.ACTIONS;
import static com.example.daniel.jeeves.actions.ActionUtils.ACTIONSETID;

/**
 * This is a service which sequentially executes a series of com.example.daniel.jeeves.actions passed to it via its intent. It maybe doesn't quite work but that remains to be seen
 */
public class ActionExecutorService extends IntentService {
    //Service binder code from https://developer.android.com/guide/components/bound-services.html#Binding
    ActionExecutorService mService;
    private List<FirebaseAction> actions;
    private int triggerType;

    public ActionExecutorService() {
        super("HelloIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("EXECUTION", "gonna execute some com.example.daniel.jeeves.actions now");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (!geofencingEvent.hasError()) {

            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                // Get the geofences that were triggered. A single event can trigger multiple geofences.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                // Get the transition details as a String.
                String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
                        triggeringGeofences);
                Toast.makeText(ApplicationContext.getContext(), geofenceTransitionDetails, Toast.LENGTH_LONG).show();

//                return;
            }
        }

        // Get the transition type.

            List<FirebaseAction> remainingActions = (ArrayList<FirebaseAction>) intent.getExtras().get(ACTIONS);

        //If our actions are null, then this means it was a location trigger (which works differently using the Geofencing API)
        if(remainingActions == null) { //Then try to get it from the global location action sets
                String locationName = intent.getStringExtra(ACTIONSETID);
                //We should store this location in our last location attribute
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
            SharedPreferences.Editor prefseditor = prefs.edit();
            prefseditor.putString("LastLocation",locationName);
            prefseditor.commit();
            Log.d("STORAGE","Stored last location as " + locationName);
                Log.d("GETTING","Getting actions from locTriggerId " + locationName);
                remainingActions = ApplicationContext.locationActions.get(locationName);
            }
            triggerType = intent.getIntExtra(TRIG_TYPE, 0);
            this.actions = remainingActions;
            executeActions();

    }
    private String getGeofenceTransitionDetails(
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "TRANSITION ENTER";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "TRANSITION EXIT";
            default:
                return "UNKNOWN TRANSITION";
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void executeActions() {
        for (int i = 0; i < actions.size(); i++) {
            FirebaseAction newaction = actions.get(i);
            Log.d("AND THOSE", "ACTIONS ARE " + newaction.toString());
        }
        Log.i("EXEUTION", "AWAY TO EXEUTE SOME ACTIONS  ");
        int count;
        for (int i = 0; i < actions.size(); i++) {
            FirebaseAction newaction = actions.get(i);

            if (newaction instanceof WaitingAction) {
                String stimeToWait = newaction.getparams().get("time").toString();
                int timeToWait = Integer.parseInt(stimeToWait) * 1000;
                try {
                    Thread.sleep(timeToWait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ArrayList<FirebaseAction> controlactions;
            if (newaction instanceof IfControl) {
                count = 1;
                IfControl ifAction = (IfControl) newaction;
                ExpressionParser parser = new ExpressionParser(ApplicationContext.getContext());
                FirebaseExpression expression = null;
                expression = ifAction.getcondition();
                controlactions = (ArrayList<FirebaseAction>) ifAction.getactions();

                //Converting the actions into their correct types
                ArrayList<FirebaseAction> actionsToPerform = new ArrayList<>();
                if (controlactions == null)
                    continue; //It might be null if we've got nothing inseide
                if (expression != null && parser.evaluate(expression).equals("false")) //expressionw will be null if we don't have an expression in the first place
                    continue; //If this IfControl has a dodgy expression, just skip it
                    //otherwise what we do is we add all the internal actions to this iterator, and carry on as normal

                else {
                    for (FirebaseAction action : controlactions) {
                        //   actionsToPerform.add(ActionUtils.create(action)); //Oh good lord really!?
                        actions.add(i + count, ActionUtils.create(action));
                        Log.d("Added", "added " + action.getname() + " to index " + (i + count));
                        count++;

                    }
                    continue;
                }

            }
            //Some actions might not have parameters
            if (newaction.getparams() == null)
                newaction.setparams(new HashMap<String, Object>());
            newaction.getparams().put(TRIG_TYPE, triggerType);
            newaction.execute(); //Will this block?
        }

    }

}

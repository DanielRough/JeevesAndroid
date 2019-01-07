package com.jeevesandroid;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.jeevesandroid.actions.ActionUtils;
import com.jeevesandroid.actions.FirebaseAction;
import com.jeevesandroid.actions.IfControl;
import com.jeevesandroid.actions.WaitingAction;
import com.jeevesandroid.actions.WhileControl;
import com.jeevesandroid.firebase.FirebaseExpression;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.jeevesandroid.ApplicationContext.TRIG_TYPE;
import static com.jeevesandroid.actions.ActionUtils.ACTIONS;
import static com.jeevesandroid.actions.ActionUtils.ACTIONSETID;

/**
 * This is a service which sequentially executes a series of com.jeeves.actions passed to it via its intent. It maybe doesn't quite work but that remains to be seen
 */
public class ActionExecutorService extends IntentService {
    private List<FirebaseAction> actions;
    private int triggerType;

    public ActionExecutorService() {
        super("HelloIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (!geofencingEvent.hasError()) {
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
        }
            List<FirebaseAction> remainingActions = (ArrayList<FirebaseAction>) intent.getExtras().get(ACTIONS);
        Log.d("WASSUP","We are handling things");
        if (ActivityTransitionResult.hasResult(intent)) {
            Log.d("OOOH","This is interesting");
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                Log.d("HURRAY","we found a result");
                Log.d("IT IS",event.getActivityType() + " , " + event.getTransitionType());
                Toast.makeText(ApplicationContext.getContext(),"IT IS " + event.getActivityType(),Toast.LENGTH_LONG);
            }
            remainingActions = ApplicationContext.getActivityActions().get(intent.getStringExtra(ACTIONSETID));
            Log.d("IT IS",intent.getStringExtra(ACTIONSETID));
            Log.d("ACTIONS","actions are " + ApplicationContext.getActivityActions().get(intent.getStringExtra(ACTIONSETID)));
            this.actions = remainingActions;
            executeActions();
            return;
        }
        //If our actions are null, then this means it was a location trigger (which works differently using the Geofencing API)
        if(remainingActions == null) { //Then try to get it from the global location action sets
                String locationName = intent.getStringExtra(ACTIONSETID);
                //We should store this location in our last location attribute
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
            SharedPreferences.Editor prefseditor = prefs.edit();
            prefseditor.putString("LastLocation",locationName);
            prefseditor.apply();
            Log.d("STORAGE","Stored last location as " + locationName);
                Log.d("GETTING","Getting actions from locTriggerId " + locationName);
                remainingActions = ApplicationContext.getLocationActions().get(locationName);
                Log.d("ACTIONS SIZE","Size is " + remainingActions.size());
            }
            triggerType = intent.getIntExtra(TRIG_TYPE, 0);
            this.actions = remainingActions;
            executeActions();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void executeActions() {
        for (int i = 0; i < actions.size(); i++) {
            FirebaseAction newaction = actions.get(i);
        }
        int count;
        for (int i = 0; i < actions.size(); i++) {
            FirebaseAction newaction = actions.get(i);
            if (newaction instanceof WaitingAction && newaction.getparams() != null && newaction.getparams().containsKey("time")) {
                String stimeToWait = newaction.getparams().get("time").toString();
                int timeToWait = Integer.parseInt(stimeToWait) * 1000 * 60;
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
                ExpressionParser parser = new ExpressionParser();
                FirebaseExpression expression;
                expression = ifAction.getcondition();
                controlactions = (ArrayList<FirebaseAction>) ifAction.getactions();

                //Converting the actions into their correct types
                //ArrayList<FirebaseAction> actionsToPerform = new ArrayList<>();
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
            else if(newaction instanceof WhileControl){
                count = 1;
                WhileControl whileAction = (WhileControl) newaction;
                ExpressionParser parser = new ExpressionParser();
                FirebaseExpression expression;
                expression = whileAction.getcondition();
                controlactions = (ArrayList<FirebaseAction>) whileAction.getactions();

                //Converting the actions into their correct types
                if (controlactions == null)
                    continue; //It might be null if we've got nothing inseide
                if (expression != null && parser.evaluate(expression).equals("false")) //expressionw will be null if we don't have an expression in the first place
                    continue; //If this IfControl has a dodgy expression, just skip it
                    //otherwise what we do is we add all the internal actions to this iterator, and carry on as normal

                else {
                    for (FirebaseAction action : controlactions) {
                        actions.add(i + count, ActionUtils.create(action));
                        count++;
                    }
                    actions.add(i+count,ActionUtils.create(newaction));

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

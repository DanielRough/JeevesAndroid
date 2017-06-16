package com.example.daniel.jeeves;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.daniel.jeeves.actions.ActionUtils;
import com.example.daniel.jeeves.actions.FirebaseAction;
import com.example.daniel.jeeves.actions.IfControl;
import com.example.daniel.jeeves.actions.WaitingAction;
import com.example.daniel.jeeves.firebase.FirebaseExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static android.content.ContentValues.TAG;
import static com.example.daniel.jeeves.ApplicationContext.TRIG_TYPE;
import static com.example.daniel.jeeves.actions.ActionUtils.ACTIONS;

/**
 * This is a service which sequentially executes a series of com.example.daniel.jeeves.actions passed to it via its intent. It maybe doesn't quite work but that remains to be seen
 */
public class ActionExecutorService extends IntentService {
    //Service binder code from https://developer.android.com/guide/components/bound-services.html#Binding
    ActionExecutorService mService;
    private ArrayList<FirebaseAction> actions;
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
        ArrayList<FirebaseAction> remainingActions = (ArrayList<FirebaseAction>) intent.getExtras().get(ACTIONS);
        triggerType = intent.getIntExtra(TRIG_TYPE, 0);
        this.actions = remainingActions;
        executeActions();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void executeActions() {
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

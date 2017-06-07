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
import java.util.Iterator;

import static android.content.ContentValues.TAG;

/**
 * This is a service which sequentially executes a series of com.example.daniel.jeeves.actions passed to it via its intent. It maybe doesn't quite work but that remains to be seen
 */
public class ActionExecutorService extends IntentService{
    private Context serviceContext;
    private ArrayList<FirebaseAction> actions;
    private FirebaseExpression expr;
    private String controlType;
    //Default constructor
    public ActionExecutorService(){
        super("HelloIntentService");

    }
    public boolean manual = false; //Were these actions triggered by the user?
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ActionExecutorService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        List<FirebaseAction> toexecute = (List<FirebaseAction>)intent.getExtras().get("com.example.daniel.jeeves.actions");
//        executeActions(toexecute);
//        return START_STICKY;
//    }

    /**
     * Actually, what we could do in here is check the evaluation condition, and decide whether to call execute com.example.daniel.jeeves.actions again, or just destroy everything
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("EXECUTION","gonna execute some com.example.daniel.jeeves.actions now");
        //  final Handler h = new Handler(); //In case some of our com.example.daniel.jeeves.actions have delayed execution
        ArrayList<FirebaseAction> remainingActions = (ArrayList<FirebaseAction>)intent.getExtras().get("com/example/daniel/jeeves/actions");
        manual = intent.getBooleanExtra("manual",false);
        Log.i("MANUAL","Manual in intent is " + manual);
        FirebaseExpression expression = (FirebaseExpression)intent.getExtras().get("expression");
        controlType = (String)intent.getExtras().get("controltype");
        this.actions = remainingActions;
        this.expr = expression;
//        if(expr != null)
//            checkCondition();
//        else
        executeActions();
        //Made this so that the executor service sends out stuff when it's finished with all its actions
//        Intent localIntent = new Intent("BROADCAST");
//        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }

    public class LocalBinder extends Binder {
        public ActionExecutorService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ActionExecutorService.this;
        }
    }
    boolean mBound = false;

    ActionExecutorService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            ActionExecutorService.LocalBinder binder = (ActionExecutorService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "onServiceDisconnected");
            mBound = false;
        }
    };
    //Service binder code from https://developer.android.com/guide/components/bound-services.html#Binding
    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */

    public void executeActions(){

        Log.i("EXEUTION","AWAY TO EXEUTE SOME ACTIONS  ");
        int count;

        //while(actionIterator.hasNext()){
        for(int i = 0; i < actions.size(); i++){
            FirebaseAction newaction = actions.get(i);

            if(newaction instanceof WaitingAction) {
                String stimeToWait = newaction.getparams().get("time").toString();
                int timeToWait = Integer.parseInt(stimeToWait) *1000;
                try {
                    Thread.sleep(timeToWait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ArrayList<FirebaseAction> controlactions;
            if(newaction instanceof IfControl) {
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
                        actions.add(i+count, ActionUtils.create(action));
                        Log.d("Added", "added " + action.getname() + " to index " + (i+count));
                        count++;

                    }
                    continue;
                }

            }
            newaction.setManual(manual);
            newaction.execute(); //Will this block?
        }

    }
//    /**
//     * This method will check that our Boolean expression that we passed in evaluates to true, in which case we stop executing com.example.daniel.jeeves.actions
//     */
//    public void checkCondition(){
//        ExpressionParser parser = new ExpressionParser(ApplicationContext.getContext());
//        Log.d("ExPRNAME", expr.getname());
//        if(parser.evaluate(expr).equals("false")) //expressionw will be null if we don't have an expression in the first place
//            return; //Our expression is false, don't execute
//        else
//            executeActions(); //Let's execute our actions!
//    }

    ActionExecutorService getService(){
        return ActionExecutorService.this;
    }

}

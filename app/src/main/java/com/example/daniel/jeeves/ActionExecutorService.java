package com.example.daniel.jeeves;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.daniel.jeeves.actions.FirebaseAction;
import com.example.daniel.jeeves.actions.WaitingAction;
import com.example.daniel.jeeves.firebase.FirebaseExpression;

import java.util.ArrayList;
import java.util.Iterator;

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
        Log.i("DESTROOYY","I have been destroyed");
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
        if(expr != null)
            checkCondition();
        else
            executeActions();

    }

    public void executeActions(){

        Log.i("EXEUTION","AWAY TO EXEUTE SOME ACTIONS  ");
        Iterator<FirebaseAction> actionIterator = actions.iterator();
        while(actionIterator.hasNext()){
            Log.i("Actionnnaaay","Here's an action!");
            FirebaseAction newaction = actionIterator.next();
        //    FirebaseAction actualAction = ActionFactory.createAction(newaction);
            Log.i("ACTION IS","Action is " + newaction.toString());
            if(newaction instanceof WaitingAction) {
                String stimeToWait = newaction.getparams().get("time").toString();
                int timeToWait = Integer.parseInt(stimeToWait) *1000;
                try {
                    Thread.sleep(timeToWait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            newaction.setManual(manual);
            newaction.execute();
        }

    }
    /**
     * This method will check that our Boolean expression that we passed in evaluates to true, in which case we stop executing com.example.daniel.jeeves.actions
     */
    public void checkCondition(){
        ExpressionParser parser = new ExpressionParser(ApplicationContext.getContext());
        switch(controlType) {
//            case "for":
//                    long numberOfTimes = (long)parser.evaluate(expr);
//                for(long i = 0; i < numberOfTimes; i++)
//                    executeActions();
//                break;
//            case "do":
//                if((boolean) parser.evaluate(expr) == true)
//                    return; //Our expression is true, STOP EXECUTING
//                else {
//                    executeActions(); //Let's go round again!
//                    checkCondition();
//                }
//                    break;
            case "if":
                if((boolean) parser.evaluate(expr) == false) //expressionw will be null if we don't have an expression in the first place
                    return; //Our expression is true, STOP EXECUTING
                else
                    executeActions(); //Let's go round again!
                break;
        }

    }
    ActionExecutorService getService(){
        return ActionExecutorService.this;
    }

}

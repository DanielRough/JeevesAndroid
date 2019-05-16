package com.jeevesandroid.actions;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.jeevesandroid.AppContext;
import com.jeevesandroid.ExpressionParser;
import com.jeevesandroid.R;
import com.jeevesandroid.actions.actiontypes.FirebaseAction;
import com.jeevesandroid.actions.actiontypes.IfControl;
import com.jeevesandroid.actions.actiontypes.WaitingAction;
import com.jeevesandroid.actions.actiontypes.WhileControl;
import com.jeevesandroid.firebase.FirebaseExpression;
import com.google.android.gms.location.GeofencingEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a service which sequentially executes a series of actions passed to it via
 * its intent. Also handles control flow conditions such as Ifs and Whiles
 */
public class ActionExecutorService extends Service {

    private List<FirebaseAction> actions;
    private int triggerType;
    private ActionExecutorReceiver receiver;
    public ActionExecutorService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("customreceiveraction");
//        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
//        if (!geofencingEvent.hasError()) {
//            int geofenceTransition = geofencingEvent.getGeofenceTransition();
//        }
        List<FirebaseAction> remainingActions = (ArrayList<FirebaseAction>) intent
            .getExtras().get(ActionUtils.ACTIONS);

        //This is an Activity trigger
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            remainingActions = AppContext.getActivityActions()
                .get(intent.getStringExtra(ActionUtils.ACTIONSETID));
//            this.actions = remainingActions;
//            executeActions();
        }
        //This is a Location trigger
        if(remainingActions == null) { //Then try to get it from the global location action sets
            String locationName = intent.getStringExtra(ActionUtils.ACTIONSETID);
            //We should store this location in our last location attribute
            SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(AppContext.getContext());
            SharedPreferences.Editor prefseditor = prefs.edit();
            prefseditor.putString("LastLocation",locationName);
            prefseditor.apply();
            remainingActions = AppContext.getLocationActions().get(locationName);
        }
        triggerType = intent.getIntExtra(AppContext.TRIG_TYPE, 0);
        this.actions = remainingActions;
        executeActions();
        return START_NOT_STICKY;
    }
    private int loopcount = 0;
    private HashMap<String,Object> snoozevar;
    private String snoozegranularity;
    /**
     * Helper method to get the time to wait from a Snooze action
     * @param newaction JSON snooze action
     * @return Time in ms to snooze for
     */
    private int getTimeToWait(FirebaseAction newaction){
        String stimeToWait = "";
        SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());
        if(newaction.getparams().get("time") instanceof Map){
            String varname = ((Map<String,Object>)newaction
                .getparams().get("time")).get("name").toString();
            Map<String,Object> var = ((Map<String,Object>)newaction.getparams().get("time"));
            snoozevar = (HashMap<String, Object>) var;
            if((boolean) var.get("isRandom")){
                List<String> randomVals = ((List<String>)var.get("randomOptions"));
                double lowest = Double.parseDouble(randomVals.get(0));
                double highest = Double.parseDouble(randomVals.get(1));
                double range = (highest-lowest)+1;
                stimeToWait = Long.toString((long)(Math.random()*range + lowest));
            }
            else {
                stimeToWait = preferences.getString(varname, "");
            }
        }
        else if(newaction.getparams().get("time") != null)
            stimeToWait = (newaction.getparams().get("time").toString());
        else
            return 0;
        String granularity = (String)newaction.getparams().get("granularity");
        snoozegranularity = granularity;
        int timeToWait = Integer.parseInt(stimeToWait) * 1000;
        if(granularity.equals("minutes")){
            timeToWait *= 60;
        }
        if(granularity.equals("hours")){
            timeToWait *= 3600;
        }
        return timeToWait;
    }


    /**
     * Pauses execution for a specified time, then resumes with leftover actions
     * @param newaction JSON Snooze action
     */
    private void snooze(FirebaseAction newaction){
        int timeToWait = getTimeToWait(newaction);
        final String SOME_ACTION =
            "com.jeevesandroid.actions.ActionExecutorService.ActionExecutorReceiver";
        IntentFilter filter = new IntentFilter(SOME_ACTION);
        receiver = new ActionExecutorReceiver();
        AppContext.getContext().registerReceiver(receiver,filter);
        AlarmManager alarmManager = (AlarmManager) AppContext.getContext()
            .getSystemService(ALARM_SERVICE);
        Intent intent=new Intent(SOME_ACTION);
        PendingIntent pi=PendingIntent.getBroadcast(AppContext.getContext(), 0, intent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + timeToWait, pi);
        }
        else{
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + timeToWait, pi);
        }
    }
    private void executeActions() {
        int count;
        for (int i = loopcount; i < actions.size(); i++) {
            FirebaseAction newaction = actions.get(i);
            Log.d("ACTION","this action is " + newaction.getname());
            //Suspend execution
            if (newaction instanceof WaitingAction && newaction.getparams() != null &&
                newaction.getparams().containsKey("time")) {
                loopcount = i+1;
                snooze(newaction);
                return;
            }
            ArrayList<FirebaseAction> controlactions;
            if (newaction instanceof IfControl) {
                count = 1;
                IfControl ifAction = (IfControl) newaction;
                ExpressionParser parser = new ExpressionParser();
                FirebaseExpression expression;
                expression = ifAction.getcondition();
                controlactions = (ArrayList<FirebaseAction>) ifAction.getactions();
                if (controlactions == null) {
                    continue; //It might be null if we've got nothing inside
                }
                //Evaluate the expression here
                if (expression != null && parser.evaluate(expression).equals("false")) {
                    continue;
                }
                //otherwise add all the internal actions to the iterator, and carry on as normal
                else {
                    for (FirebaseAction action : controlactions) {
                        actions.add(i + count, ActionUtils.create(action));
                        count++;
                    }
                    continue;
                }
            }

            //While loop functionality
            else if(newaction instanceof WhileControl){
                WhileControl whileAction = (WhileControl) newaction;
                controlactions = (ArrayList<FirebaseAction>) whileAction.getactions();
                int totalTime = 0;
                List<FirebaseAction> snoozeactions = new ArrayList<>();
                for(FirebaseAction a : controlactions){
                    int time = 0;
                    Log.d("INSIDE","Inside is " + a.getname());
                    //Are there any snooze actions?
                    if(a.getname().equals(ActionUtils.NAME_WAIT_ACTION)){
                        snoozeactions.add(a);
                        time = getTimeToWait(a);
                    }
                    totalTime += time;
                }

                //If there are snooze actions in the loop, snooze for cumulative time
                //otherwise just snooze for 1 minute
                //TODO: Ensure researcher specifies snooze time
                if(totalTime == 0){
                    totalTime = 60000;
                }
                //Remove the snooze actions, the alarm manager takes care of these
                whileAction.getactions().removeAll(snoozeactions);

                //Store the actions to execute after this While loop terminates
                ArrayList<FirebaseAction> afterActions =
                    new ArrayList<>(actions.subList(actions.indexOf(whileAction)+1,actions.size()));
                AlarmManager alarmManager = (AlarmManager) AppContext.getContext()
                    .getSystemService(ALARM_SERVICE);
                Intent intent=new Intent(AppContext.getContext(),WhileLoopReceiver.class);

                //snoozyvar gets updated in the 'getTimeToWait' method.
                if(snoozevar != null){
                    intent.putExtra(String.valueOf(R.string.snoozevar),snoozevar);
                    intent.putExtra("granularity",snoozegranularity);
                }
                intent.putExtra("looptime",totalTime);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                ObjectOutputStream out = null;
                ObjectOutputStream out2 = null;
                try {
                    out = new ObjectOutputStream(bos);
                    out.writeObject(whileAction);
                    out.flush();
                    byte[] whileLoopBytes = bos.toByteArray();
                   // intent.putExtra("loop", data);
                    out2 = new ObjectOutputStream(bos2);
                    out2.writeObject(afterActions);
                    out2.flush();
                    byte[] afterActionBytes = bos2.toByteArray();
                    intent.putExtra("loop", whileLoopBytes);
                    intent.putExtra("afteractions", afterActionBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        bos.close();
                        bos2.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                PendingIntent pi=PendingIntent.getBroadcast
                    (AppContext.getContext(),234, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + totalTime, pi);
                }
                else{
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + totalTime, pi);
                }
                return; //We're done here, leave it to the receiver
            }


            //Execute next action as normal
            if (newaction.getparams() == null)
                newaction.setparams(new HashMap<String, Object>());
            newaction.getparams().put(AppContext.TRIG_TYPE, triggerType);
            newaction.execute();
        }
    stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Receiver for picking up again from a snooze action
     */
    public class ActionExecutorReceiver extends BroadcastReceiver{

        public ActionExecutorReceiver(){
            super();
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            executeActions();

        }
    }

    @Override
    public void onDestroy(){
        try {
            AppContext.getContext().unregisterReceiver(receiver);
        }
        catch(IllegalArgumentException e){
            Log.e("EXC",e.getMessage());
        }
    }
}

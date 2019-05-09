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
import com.jeevesandroid.ApplicationContext;
import com.jeevesandroid.ExpressionParser;
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
 * This is a service which sequentially executes a series of com.jeeves.actions passed to it via its intent. It maybe doesn't quite work but that remains to be seen
 */
public class ActionExecutorService extends Service {

    //private Intent intent;
    private List<FirebaseAction> actions;
    private int triggerType;
    private ActionExecutorReceiver receiver;
    //private WhileLoopReceiver whileLoopReceiver;
    public ActionExecutorService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("customreceiveraction");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (!geofencingEvent.hasError()) {
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
        }
        List<FirebaseAction> remainingActions = (ArrayList<FirebaseAction>) intent.getExtras().get(ActionUtils.ACTIONS);
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                Toast.makeText(ApplicationContext.getContext(),"IT IS " + event.getActivityType(),Toast.LENGTH_LONG);
            }
            remainingActions = ApplicationContext.getActivityActions().get(intent.getStringExtra(ActionUtils.ACTIONSETID));
            this.actions = remainingActions;
            executeActions();
        }
        //If our actions are null, then this means it was a location trigger (which works differently using the Geofencing API)
        if(remainingActions == null) { //Then try to get it from the global location action sets
            String locationName = intent.getStringExtra(ActionUtils.ACTIONSETID);
            //We should store this location in our last location attribute
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
            SharedPreferences.Editor prefseditor = prefs.edit();
            prefseditor.putString("LastLocation",locationName);
            prefseditor.apply();
            remainingActions = ApplicationContext.getLocationActions().get(locationName);
        }
        triggerType = intent.getIntExtra(ApplicationContext.TRIG_TYPE, 0);
        this.actions = remainingActions;
        executeActions();
        return START_NOT_STICKY;
    }
    private int loopcount = 0; //Should be safe enough
    private void executeActions() {
        int count;
        for (int i = loopcount; i < actions.size(); i++) {

            FirebaseAction newaction = actions.get(i);
            Log.d("ACTION","this action is " + newaction.getname());
            String stimeToWait = "";
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());

            if (newaction instanceof WaitingAction && newaction.getparams() != null && newaction.getparams().containsKey("time")) {
                if(newaction.getparams().get("time") instanceof Map){
                    String varname = ((Map<String,Object>)newaction.getparams().get("time")).get("name").toString();
                    Log.d("VARAME","varname is " + varname);
                    //Bit of a temporary hack here
                    Map<String,Object> var = ((Map<String,Object>)newaction.getparams().get("time"));
//                    FirebaseVariable var = (FirebaseVariable)expr;
                    String randomanswer = "";
                    if((boolean)var.get("isRandom") == true){
                        Log.d("NAME","RANDOM var name is " + var.get("name").toString());
                        //Log.d("VARS","random vars are "+ var.getrandomOptions());
                        List<String> randomVals = ((List<String>)var.get("randomOptions"));
                        double lowest = Double.parseDouble(randomVals.get(0));
                        double highest = Double.parseDouble(randomVals.get(1));
                        double range = (highest-lowest)+1;
                        randomanswer = Long.toString((long)(Math.random()*range + lowest));
                        Log.d("PUT VAR","Put var " + var.get("name") + " with value " + randomanswer);
                        stimeToWait = randomanswer;
                    }
                    else {
                        stimeToWait = preferences.getString(varname, "");
                    }
                }
                else if(newaction.getparams().get("time") != null)
                    stimeToWait = (newaction.getparams().get("time").toString());
                else
                    return;
                String granularity = (String)newaction.getparams().get("granularity");

                int timeToWait = Integer.parseInt(stimeToWait) * 1000;
                if(granularity.equals("minutes")){
                    timeToWait *= 60;
                }
                if(granularity.equals("hours")){
                    timeToWait *= 3600;
                }
                final String SOME_ACTION = "com.jeevesandroid.actions.ActionExecutorService.ActionExecutorReceiver";

                loopcount = i+1;
                IntentFilter filter = new IntentFilter(SOME_ACTION);
                Log.d("WAITING","Gonna wait for " + timeToWait + "seconds");
                receiver = new ActionExecutorReceiver();
                ApplicationContext.getContext().registerReceiver(receiver,filter);
                AlarmManager alarmManager = (AlarmManager) ApplicationContext.getContext()
                    .getSystemService(ALARM_SERVICE);
                Intent intent=new Intent(SOME_ACTION);
                PendingIntent pi=PendingIntent.getBroadcast(ApplicationContext.getContext(), 0, intent, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeToWait, pi);
                }
                else{
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeToWait, pi);

                }
                return;

            }
            ArrayList<FirebaseAction> controlactions;
            if (newaction instanceof IfControl) {
                Log.d("SURE IS","Sure is an if control!");
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
            //Here I'm going to need some sort of alarm manager that periodically executes the actions in the while loop
            //until those actions are false, and after that executes the remaining actions in the trigger.
            else if(newaction instanceof WhileControl){
                Log.d("YUPWHILE","It's a while condition");
                WhileControl whileAction = (WhileControl) newaction;
                controlactions = (ArrayList<FirebaseAction>) whileAction.getactions();
                int totalTime = 0;
                HashMap<String,Object> snoozyvar = null;
                String snoozytime = "";
                List<FirebaseAction> snoozies = new ArrayList<>();
                for(FirebaseAction a : controlactions){
                    int time = 0;
                    Log.d("INSIDE","Inside is " + a.getname());
                    //Are there any snooze actions?
                    if(a.getname().equals(ActionUtils.NAME_WAIT_ACTION)){
                        snoozies.add(a);
                        //Brace yourself I've just copied and pasted this monolithic code block
                        if(a.getparams().get("time") instanceof Map){
                            String varname = ((Map<String,Object>)a.getparams().get("time")).get("name").toString();
                            snoozyvar = (HashMap<String,Object>)a.getparams().get("time");
                            Map<String,Object> var = ((Map<String,Object>)a.getparams().get("time"));
                            String randomanswer = "";
                            if((boolean)var.get("isRandom") == true){
                                List<String> randomVals = ((List<String>)var.get("randomOptions"));
                                double lowest = Double.parseDouble(randomVals.get(0));
                                double highest = Double.parseDouble(randomVals.get(1));
                                double range = (highest-lowest)+1;
                                randomanswer = Long.toString((long)(Math.random()*range + lowest));
                                stimeToWait = randomanswer;
                            }
                            else {
                                stimeToWait = preferences.getString(varname, "");
                            }
                        }
                        else if(a.getparams().get("time") != null)
                            stimeToWait = (a.getparams().get("time").toString());
                        else
                            return;
                        Log.d("SNOOZY","our snoozy time is " + stimeToWait);
                        String granularity = (String)a.getparams().get("granularity");
                        snoozytime = granularity;
                        time = Integer.parseInt(stimeToWait) * 1000;
                        if(granularity.equals("minutes")){
                            time *= 60;
                        }
                        if(granularity.equals("hours")){
                            time *= 3600;
                        }
                    }
                    totalTime += time;
                }
                //Okay so now if there WERE snooze actions, we snooze for that cumulative length of time
                //OTHERWISE we just snooze for 1 minute because that's reasonable right?
                if(totalTime == 0){
                    totalTime = 60000;
                }
                //Remove the snoozy actions, the alarm manager takes care of these
                whileAction.getactions().removeAll(snoozies);
                ArrayList<FirebaseAction> afterActions = new ArrayList<FirebaseAction>(actions.subList(actions.indexOf(whileAction)+1,actions.size()));
                AlarmManager alarmManager = (AlarmManager) ApplicationContext.getContext()
                    .getSystemService(ALARM_SERVICE);
                Intent intent=new Intent(ApplicationContext.getContext(),WhileLoopReceiver.class);

                if(snoozyvar != null){
                    intent.putExtra("snoozyvar",snoozyvar);
                    intent.putExtra("snoozytime",snoozytime);
                }
                intent.putExtra("loopytime",totalTime);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                ObjectOutputStream out = null;
                ObjectOutputStream out2 = null;
                try {
                    out = new ObjectOutputStream(bos);
                    out.writeObject(whileAction);
                    out.flush();
                    byte[] data = bos.toByteArray();
                    intent.putExtra("loop", data);
                    out2 = new ObjectOutputStream(bos2);
                    out2.writeObject(afterActions);
                    out2.flush();
                    byte[] data2 = bos2.toByteArray();
                    intent.putExtra("loop", data);
                    intent.putExtra("afteractions", data2);
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
                PendingIntent pi=PendingIntent.getBroadcast(ApplicationContext.getContext(), 234, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + totalTime, pi);
                }
                else{
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + totalTime, pi);
                }
                Log.d("WAKEUP","Will hopefully wake up in " + totalTime + "milliseconds");
                return; //We're done here, leave it to the receiver
            }

            //Some actions might not have parameters
            if (newaction.getparams() == null)
                newaction.setparams(new HashMap<String, Object>());
            newaction.getparams().put(ApplicationContext.TRIG_TYPE, triggerType);

            newaction.execute(); //Will this block?
        }
    stopSelf();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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
            ApplicationContext.getContext().unregisterReceiver(receiver);
        }
        catch(IllegalArgumentException e){
            Log.e("EXC",e.getMessage());
        }
    }
}

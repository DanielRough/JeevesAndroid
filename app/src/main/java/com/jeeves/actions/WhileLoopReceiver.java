package com.jeeves.actions;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.jeeves.AppContext;
import com.jeeves.ExpressionParser;
import com.jeeves.actions.actiontypes.FirebaseAction;
import com.jeeves.actions.actiontypes.WhileControl;
import com.jeeves.firebase.FirebaseExpression;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class that receives the update from a While Loop to re-execute the actions inside,
 * or finish off the actions to be executed afterwards
 */
public class WhileLoopReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ArrayList<FirebaseAction> controlactions;
        ByteArrayInputStream bis =
            new ByteArrayInputStream(intent.getByteArrayExtra("loop"));
        ByteArrayInputStream bis2 =
            new ByteArrayInputStream(intent.getByteArrayExtra("afteractions"));
        ObjectInput in = null;
        ObjectInput in2 = null;
        WhileControl myloop = null;
        ArrayList<FirebaseAction> afterActions = null;
        try {
            in = new ObjectInputStream(bis);
            in2 = new ObjectInputStream(bis2);
            myloop = (WhileControl)in.readObject();
            afterActions = (ArrayList<FirebaseAction>)in2.readObject();
        }
        catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
                if(in2 != null){
                    in2.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        ExpressionParser parser = new ExpressionParser();
        FirebaseExpression expression;
        HashMap<String,Object> snoozyvar = null;
        String granularity = null;
        expression = myloop.getcondition();
        controlactions = (ArrayList<FirebaseAction>) myloop.getactions();
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());

        if (controlactions == null) {
            executeRemainingActions(afterActions);
        }
        if (expression != null && parser.evaluate(expression).equals("false")){
            executeRemainingActions(afterActions);
        }

        else {
            //FirebaseActions lose their specific type, so this needs to be re-added
            ArrayList<FirebaseAction> newControlActions = new ArrayList<>();
            for(FirebaseAction a : controlactions){
                newControlActions.add(ActionUtils.create(a));
            }
            int totalTime = 0;
            String stimeToWait = "";

            //We pass the snooze variable if it exists
            //This way the loop's snoozing time can be updated if necessary (or randomised)
            if(intent.hasExtra("snoozevar")){
                snoozyvar = (HashMap<String, Object>) intent.getSerializableExtra("snoozevar");
                String varname = snoozyvar.get("name").toString();
                String randomanswer = "";
                if((boolean)snoozyvar.get("isRandom") == true){
                    List<String> randomVals = ((List<String>)snoozyvar.get("randomOptions"));
                    double lowest = Double.parseDouble(randomVals.get(0));
                    double highest = Double.parseDouble(randomVals.get(1));
                    double range = (highest-lowest)+1;
                    randomanswer = Long.toString((long)(Math.random()*range + lowest));
                    stimeToWait = randomanswer;
                }
                else {
                    stimeToWait = preferences.getString(varname, "");
                }
                granularity = intent.getStringExtra("snoozegranularity");

                totalTime = Integer.parseInt(stimeToWait) * 1000;

                if(granularity.equals("minutes")){
                    totalTime *= 60;
                }
                if(granularity.equals("hours")){
                    totalTime *= 3600;
                }
            }
            else {
                totalTime = intent.getIntExtra("looptime", 30000);
            }

            //Start the ActionExecutorService to execute in-loop actions
            Intent actionIntent = new Intent(AppContext.getContext(), ActionExecutorService.class);
            actionIntent.putExtra(ActionUtils.ACTIONS, newControlActions);
            AppContext.getContext().startService(actionIntent);

            //Begin the whole process again
            AlarmManager alarmManager = (AlarmManager) AppContext.getContext()
                .getSystemService(Context.ALARM_SERVICE);
            Intent newLoopIntent=new Intent(AppContext.getContext(),WhileLoopReceiver.class);

            newLoopIntent.putExtra("looptime",totalTime);
            if(snoozyvar != null) {
                newLoopIntent.putExtra("snoozevar", snoozyvar);
                newLoopIntent.putExtra("snoozegranularity", granularity);

            }
            newLoopIntent.putExtra("loop", intent.getByteArrayExtra("loop"));
            newLoopIntent.putExtra("afteractions", intent.getByteArrayExtra("afteractions"));
            PendingIntent pi=PendingIntent.getBroadcast(AppContext.getContext(),
                234, newLoopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + totalTime, pi);
            }
            else{
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + totalTime, pi);
            }
        }

    }

    /**
     * Starts a new ActionExecutorService to finish off executing post-loop actions
     * @param remainingActions List of JSON actions that take place after the loop
     */
    public void executeRemainingActions(List<FirebaseAction> remainingActions){
        ArrayList<FirebaseAction> newRemainingActions = new ArrayList<>();
        for(FirebaseAction a : remainingActions){
            newRemainingActions.add(a);
        }
        Intent actionIntent = new Intent(AppContext.getContext(), ActionExecutorService.class);
        actionIntent.putExtra(ActionUtils.ACTIONS, newRemainingActions);
        AppContext.getContext().startService(actionIntent);
    }
}

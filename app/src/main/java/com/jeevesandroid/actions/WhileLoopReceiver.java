package com.jeevesandroid.actions;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jeevesandroid.ApplicationContext;
import com.jeevesandroid.ExpressionParser;
import com.jeevesandroid.actions.actiontypes.FirebaseAction;
import com.jeevesandroid.actions.actiontypes.WhileControl;
import com.jeevesandroid.firebase.FirebaseExpression;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WhileLoopReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Get condition
        ArrayList<FirebaseAction> controlactions;
        ByteArrayInputStream bis = new ByteArrayInputStream(intent.getByteArrayExtra("loop"));
        ByteArrayInputStream bis2 = new ByteArrayInputStream(intent.getByteArrayExtra("afteractions"));
        ObjectInput in = null;
        ObjectInput in2 = null;
        WhileControl myloop = null;
        ArrayList<FirebaseAction> afterActions = null;
        Log.d("RECEIVED","I hate receiveth the receiver");
        try {
            in = new ObjectInputStream(bis);
            in2 = new ObjectInputStream(bis2);
            myloop = (WhileControl)in.readObject();
            afterActions = (ArrayList<FirebaseAction>)in2.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());

        //Converting the actions into their correct types
        if (controlactions == null) {
            Log.d("NULL","Control actions are null!");
            executeRemainingStuff(afterActions);
        }
        if (expression != null && parser.evaluate(expression).equals("false")){ //expressionw will be null if we don't have an expression in the first place
            Log.d("FALSE","Expression is false!");
            executeRemainingStuff(afterActions);

        }   //otherwise what we do is we add all the internal actions to this iterator, and carry on as normal

        else {
            //Oddly the FirebaseActions lose their specific type, so this needs to be added for 'em all again
            ArrayList<FirebaseAction> newControlActions = new ArrayList<>();
            for(FirebaseAction a : controlactions){
                newControlActions.add(ActionUtils.create(a));
                Log.d("CONTROL","adding " + a.getname());
            }
            int totalTime = 0;
            String stimeToWait = "";
            //If we've got the snoozy var, use that to calculate the time instead kay?
            if(intent.hasExtra("snoozyvar")){
                snoozyvar = (HashMap<String, Object>) intent.getSerializableExtra("snoozyvar");
                String varname = snoozyvar.get("name").toString();
                Log.d("VARAME","varname is " + varname);
                //Bit of a temporary hack here
//                    FirebaseVariable var = (FirebaseVariable)expr;
                String randomanswer = "";
                if((boolean)snoozyvar.get("isRandom") == true){
                    Log.d("NAME","RANDOM var name is " + snoozyvar.get("name").toString());
                    //Log.d("VARS","random vars are "+ var.getrandomOptions());
                    List<String> randomVals = ((List<String>)snoozyvar.get("randomOptions"));
                    double lowest = Double.parseDouble(randomVals.get(0));
                    double highest = Double.parseDouble(randomVals.get(1));
                    double range = (highest-lowest)+1;
                    randomanswer = Long.toString((long)(Math.random()*range + lowest));
                    Log.d("PUT VAR","Put var " + snoozyvar.get("name") + " with value " + randomanswer);
                    stimeToWait = randomanswer;
                }
                else {
                    stimeToWait = preferences.getString(varname, "");
                }
                granularity = intent.getStringExtra("snoozytime");

                totalTime = Integer.parseInt(stimeToWait) * 1000;
                Log.d("FOUNDSNOOZE","Time was " + totalTime);

                if(granularity.equals("minutes")){
                    totalTime *= 60;
                }
                if(granularity.equals("hours")){
                    totalTime *= 3600;
                }
            }
            else {
                totalTime = intent.getIntExtra("loopytime", 30000);
            }
            Intent actionIntent = new Intent(ApplicationContext.getContext(), ActionExecutorService.class);
            actionIntent.putExtra(ActionUtils.ACTIONS, newControlActions);
            Log.d("SHOULDBE","Should be away to execute these things");
            ApplicationContext.getContext().startService(actionIntent);

            AlarmManager alarmManager = (AlarmManager) ApplicationContext.getContext()
                .getSystemService(Context.ALARM_SERVICE);
            Intent newLoopIntent=new Intent(ApplicationContext.getContext(),WhileLoopReceiver.class);


            newLoopIntent.putExtra("loopytime",totalTime);
            if(snoozyvar != null) {
                newLoopIntent.putExtra("snoozyvar", snoozyvar);
                newLoopIntent.putExtra("snoozytime", granularity);

            }
            newLoopIntent.putExtra("loop", intent.getByteArrayExtra("loop"));
            newLoopIntent.putExtra("afteractions", intent.getByteArrayExtra("afteractions"));
            PendingIntent pi=PendingIntent.getBroadcast(ApplicationContext.getContext(), 234, newLoopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + totalTime, pi);
            }
            else{
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + totalTime, pi);
            }
            Log.d("OKAY AGIN","It's back in the hands of God now");
        }

    }
    public void executeRemainingStuff(List<FirebaseAction> remainingActions){
        ArrayList<FirebaseAction> newRemainingActions = new ArrayList<>();
        for(FirebaseAction a : remainingActions){
         //   Log.d("ACTION","this action is " + a.toString());
          //  Log.d("ACTION","this action is " + a.getname());
            newRemainingActions.add(a);
        }
        Intent actionIntent = new Intent(ApplicationContext.getContext(), ActionExecutorService.class);
        actionIntent.putExtra(ActionUtils.ACTIONS, newRemainingActions);
        ApplicationContext.getContext().startService(actionIntent);
        //Great now cancel the alarm
    }
}

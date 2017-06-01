package com.example.daniel.jeeves.actions;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.daniel.jeeves.ActionExecutorService;
import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.ExpressionParser;
import com.example.daniel.jeeves.firebase.FirebaseExpression;
import com.example.daniel.jeeves.firebase.FirebaseProject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by Daniel on 08/06/15.
 */
public class IfControl extends FirebaseAction {
    public ArrayList<FirebaseAction> controlactions = new ArrayList<FirebaseAction>();
    ActionExecutorService mService;
    boolean mBound = false;

    public IfControl(Map<String,Object> params, FirebaseExpression condition, List<FirebaseAction> actions){
        setparams(params);
        this.actions = actions;
        this.condition = condition;
    }
//    @Override
//    public boolean execute() {
//
////        Log.d("CONTROLIF", "IF CONTROL");
////      //  FirebaseExpression expression = getcondition();
////        Context app = ApplicationContext.getContext();
////        ExpressionParser parser = new ExpressionParser(ApplicationContext.getContext());
////        FirebaseExpression expression = null;
////        expression = getcondition();
////        controlactions = (ArrayList<FirebaseAction>) getactions();
////        //Converting the actions into their correct types
////        ArrayList<FirebaseAction> actionsToPerform = new ArrayList<>();
////        if(controlactions == null)return true; //It might be null if we've got nothing inseide
////        for(FirebaseAction action : controlactions){
////            actionsToPerform.add(ActionUtils.create(action)); //Oh good lord really!?
////        }
////        Intent actionIntent = new Intent(app,ActionExecutorService.class);
////
////        if(this.getmanual())
////            actionIntent.putExtra("manual",true);
////        actionIntent.putExtra("com/example/daniel/jeeves/actions",actionsToPerform);
////        actionIntent.putExtra("expression",expression);
////        actionIntent.putExtra("controltype","if");
////        app.startService(actionIntent);
//        return true;
//    }


 //   @Override
    public ArrayList<FirebaseAction> getControlActions() {
        return controlactions;
   }
    // Broadcast receiver for receiving status updates from the IntentService
    private class MyResponseReceiver extends BroadcastReceiver {
        // Called when the BroadcastReceiver gets an Intent it's registered to receive

        public void onReceive(Context context, Intent intent) {

        /*
         * You get notified here when your IntentService is done
         * obtaining data form the server!
         */

        }
    }
}

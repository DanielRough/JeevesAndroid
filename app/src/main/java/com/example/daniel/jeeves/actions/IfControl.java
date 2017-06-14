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

 //   @Override
    public ArrayList<FirebaseAction> getControlActions() {
        return controlactions;
   }
    // Broadcast receiver for receiving status updates from the IntentServices
}

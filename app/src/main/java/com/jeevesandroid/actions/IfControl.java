package com.jeevesandroid.actions;

import com.jeevesandroid.ActionExecutorService;
import com.jeevesandroid.firebase.FirebaseExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

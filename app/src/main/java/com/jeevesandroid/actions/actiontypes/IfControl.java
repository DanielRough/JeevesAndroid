package com.jeevesandroid.actions.actiontypes;

import com.jeevesandroid.actions.ActionExecutorService;
import com.jeevesandroid.firebase.FirebaseExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 08/06/15.
 */
public class IfControl extends FirebaseAction {
    private final ArrayList<FirebaseAction> controlactions = new ArrayList<>();
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
// --Commented out by Inspection STOP (5/8/2019 4:26 PM)
    // Broadcast receiver for receiving status updates from the IntentServices
}

package com.jeevesandroid.actions;

import com.jeevesandroid.firebase.FirebaseExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 08/06/15.
 */
public class WhileControl extends FirebaseAction {
    private final ArrayList<FirebaseAction> controlactions = new ArrayList<>();

    public WhileControl(Map<String,Object> params, FirebaseExpression condition, List<FirebaseAction> actions,String name){
        setparams(params);
        this.actions = actions;
        this.condition = condition;
        //A weird hack we have to do for the While Control because whenever it
        //goes back around twice it loses its name.
        this.setname(name);
    }

    //   @Override
    public ArrayList<FirebaseAction> getControlActions() {
        return controlactions;
    }
    // Broadcast receiver for receiving status updates from the IntentServices
}

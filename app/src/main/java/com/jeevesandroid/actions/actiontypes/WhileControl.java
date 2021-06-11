package com.jeevesandroid.actions.actiontypes;

import com.jeevesandroid.firebase.FirebaseExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class representing the 'While' loop condition block. It is not a 'true' action
 * (instead determining whether to execute other actions) and so its functionality is
 * implemented in the ActionExecutorService class.
 */
public class WhileControl extends FirebaseAction {
    private final ArrayList<FirebaseAction> controlactions = new ArrayList<>();

    public WhileControl(Map<String,Object> params, FirebaseExpression condition,
                        List<FirebaseAction> actions, String name){
        setparams(params);
        this.actions = actions;
        this.condition = condition;
        this.setname(name);
    }

}

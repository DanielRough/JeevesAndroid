package com.jeevesandroid.actions.actiontypes;

import com.jeevesandroid.firebase.FirebaseExpression;
import java.util.List;
import java.util.Map;

/**
 * Class representing the 'If' condition block. As it is not a 'true'
 * action (it instead determines whether other actions should be executed)
 * its functionality is implemented in the ActionExecutorService class
 */
public class IfControl extends FirebaseAction {

    public IfControl(Map<String,Object> params, FirebaseExpression condition, List<FirebaseAction> actions){
        setparams(params);
        this.actions = actions;
        this.condition = condition;
    }

}

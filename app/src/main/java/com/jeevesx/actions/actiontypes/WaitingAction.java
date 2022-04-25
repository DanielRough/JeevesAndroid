package com.jeevesx.actions.actiontypes;

import java.util.Map;

/**
 * Class representing the 'snooze app' action block.
 * Again, this is not a 'true' action and its functionality is implemented
 * in the ActionExecutorService class
 */
public class WaitingAction extends FirebaseAction {

    public WaitingAction(Map<String,Object> params){
        setparams(params);

    }
}

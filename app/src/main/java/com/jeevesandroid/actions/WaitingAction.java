package com.jeevesandroid.actions;
import java.util.Map;

/**
 * Created by Daniel on 27/05/15.
 */
public class WaitingAction extends FirebaseAction {

    public WaitingAction(Map<String,Object> params){
        setparams(params);

    }
    @Override
    public boolean execute() {
        return true;
    }
}

package com.example.daniel.jeeves.actions;

import android.content.Context;
import android.util.Log;

import com.example.daniel.jeeves.ApplicationContext;

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
        Log.d("ACTIONWAIT", "SOMEHOW GONNA WAIT BEFORE EXECUTING");
        Context app = ApplicationContext.getContext();
        return true;
    }
}

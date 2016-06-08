package com.example.daniel.jeeves.actions;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.daniel.jeeves.ActionExecutorService;
import com.example.daniel.jeeves.firebase.FirebaseExpression;
import com.example.daniel.jeeves.ApplicationContext;

import java.util.ArrayList;

/**
 * Created by Daniel on 08/06/15.
 */
public class ForControl extends FirebaseControl implements IControl {
    ActionExecutorService mService;
    boolean mBound = false;
    public ArrayList<FirebaseAction> controlactions = new ArrayList<FirebaseAction>();

    @Override
    public void execute() {

        Log.d("CONTROLFOR", "FOR CONTROL");
        FirebaseExpression expression = getcondition();

        Context app = ApplicationContext.getContext();
        controlactions = (ArrayList<FirebaseAction>) params.get("actions");
        Intent actionIntent = new Intent(app,ActionExecutorService.class);
        actionIntent.putExtra("com/example/daniel/jeeves/actions",controlactions);
      //  actionIntent.putExtra("expression",expression);
        app.startService(actionIntent);
    }
    @Override
    public ArrayList<FirebaseAction> getControlActions() {
        return controlactions;
    }
}

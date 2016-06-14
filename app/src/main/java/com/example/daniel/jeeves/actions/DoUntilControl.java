package com.example.daniel.jeeves.actions;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.daniel.jeeves.ActionExecutorService;
import com.example.daniel.jeeves.ExpressionParser;
import com.example.daniel.jeeves.firebase.FirebaseExpression;
import com.example.daniel.jeeves.ApplicationContext;

import java.util.ArrayList;

/**
 * Created by Daniel on 08/06/15.
 */
public class DoUntilControl extends FirebaseControl{
    public ArrayList<FirebaseAction> controlactions = new ArrayList<FirebaseAction>();
   private Object var;
    private Object val;
    private String op;
 //   ActionExecutorService mService;
    boolean mBound = false;
    public void execute() {
        Log.d("CONTROLDO", "DO CONTROL");
       // FirebaseExpression expression = getcondition();
        Context app = ApplicationContext.getContext();
        ArrayList<FirebaseAction> toExecute = new ArrayList<FirebaseAction>();
        toExecute = (ArrayList<FirebaseAction>) getactions();
        FirebaseExpression expression = getcondition();
        Intent actionIntent = new Intent(app,ActionExecutorService.class);
        actionIntent.putExtra("com/example/daniel/jeeves/actions",toExecute);
        actionIntent.putExtra("expression",expression);
        actionIntent.putExtra("controltype","do");

        app.startService(actionIntent);
    }

    @Override
    public ArrayList<FirebaseAction> getControlActions() {
        return controlactions;
    }
}

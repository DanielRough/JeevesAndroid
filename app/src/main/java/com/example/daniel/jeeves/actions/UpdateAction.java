package com.example.daniel.jeeves.actions;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.ExpressionParser;
import com.example.daniel.jeeves.firebase.FirebaseExpression;
import com.example.daniel.jeeves.firebase.UserVariable;

import java.util.Map;

/**
 * Created by Daniel on 27/05/15.
 */
public class UpdateAction extends FirebaseAction {

    private FirebaseExpression value;
    public FirebaseExpression getvalue(){
        return value;
    }
    @Override
    public void execute() {
        Log.d("ACTIONUPDATEUSER", "UPDATED USER VAR");
        Context app = ApplicationContext.getContext();
        SharedPreferences pref = app.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        Map<String,Object> variable = (Map<String,Object>)getparams().get("variable");
            String varName = variable.get("name").toString();
            String varType = getparams().get("vartype").toString();
            SharedPreferences.Editor editor = pref.edit();
            Object valueresult = null;
            ExpressionParser parser = new ExpressionParser(ApplicationContext.getContext());
            FirebaseExpression expr = null;
            expr = (FirebaseExpression)(getparams().get("value"));
                valueresult = parser.evaluate(getvalue());

            if (varType.equals("Text")) {
                editor.putString(varName, (String)valueresult);
            } else if (varType.equals("Numeric")) {
                editor.putLong(varName, (Long)valueresult);
            } else if (varType.equals("Boolean")) {
                editor.putBoolean(varName, (boolean)valueresult);
            } else if (varType.equals("Time")) {
                editor.putString(varName, (String)valueresult);
            }
            editor.commit();
    }
}

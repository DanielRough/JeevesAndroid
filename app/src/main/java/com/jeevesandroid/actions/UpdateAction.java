package com.jeevesandroid.actions;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jeevesandroid.ApplicationContext;
import com.jeevesandroid.ExpressionParser;
import com.jeevesandroid.firebase.FirebaseExpression;

import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 27/05/15.
 */
public class UpdateAction extends FirebaseAction {

    private FirebaseExpression value;
    public FirebaseExpression getvalue(){
        return value;
    }

    public UpdateAction(Map<String,Object> params, List<FirebaseExpression> vars){
        setparams(params);
        setvars(vars);
    }
    @Override
    public void execute() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        if(getvars()==null){
            return;
        } //got an empty action!!
        FirebaseExpression variable = getvars().get(0);
            String varName = variable.getname();
            String varType = variable.getvartype();
            SharedPreferences.Editor editor = preferences.edit();
            Object valueresult;
            ExpressionParser parser = new ExpressionParser();
            FirebaseExpression expr = getvars().get(1);
            valueresult = parser.evaluate(expr);

            Log.d("PUTTING","Put " + varName + " as value " + valueresult.toString());
        switch (varType) {
            case "Text":
                editor.putString(varName, (String) valueresult);
                break;
            case "Numeric":
                editor.putString(varName, (String) valueresult);
                break;
            case "Boolean":
                editor.putBoolean(varName, (Boolean.parseBoolean(valueresult.toString())));
                break;
            default:
                editor.putString(varName, (String) valueresult);
                break;
        }
            editor.apply();
    }
}

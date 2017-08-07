package com.example.daniel.jeeves.actions;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.ExpressionParser;
import com.example.daniel.jeeves.firebase.FirebaseExpression;

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
    public boolean execute() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        if(getvars()==null){
            return true;
        } //got an empty action!!
        FirebaseExpression variable = getvars().get(0);
            String varName = variable.getname();
            String varType = variable.getvartype();
            SharedPreferences.Editor editor = preferences.edit();
            Object valueresult = null;
            ExpressionParser parser = new ExpressionParser(ApplicationContext.getContext());
            FirebaseExpression expr = getvars().get(1);
            valueresult = parser.evaluate(expr);

            Log.d("PUTTING","Put " + varName + " as value " + valueresult.toString());
            if (varType.equals("Text")) {
                editor.putString(varName, (String)valueresult);
            } else if (varType.equals("Numeric")) {
                editor.putString(varName, (String)valueresult);
            } else if (varType.equals("Boolean")) {
                editor.putBoolean(varName, ( Boolean.parseBoolean(valueresult.toString())));
            } else {
                editor.putString(varName, (String)valueresult);
            }
            editor.commit();
        return true;
    }
}

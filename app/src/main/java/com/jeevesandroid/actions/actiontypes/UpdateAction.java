package com.jeevesandroid.actions.actiontypes;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jeevesandroid.AppContext;
import com.jeevesandroid.ExpressionParser;
import com.jeevesandroid.firebase.FirebaseExpression;

import java.util.List;
import java.util.Map;

/**
 * Class representing the action block for updating a user attribute
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

    /**
     * This action is of the format 'set X to Y' where X is a user attribute and Y is either
     * another user attribute, an expression, or a simple value. It first gets the name of
     * the attribute X to update, evaluates Y (if it itself is an attribute or expression),
     * and updates the value of X in SharedPreferences to this value
     */
    @Override
    public void execute() {
        SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());
        if(getvars()==null){
            return;
        } //got an empty action!!
        FirebaseExpression variable = getvars().get(0);
        String varName = variable.getname();
        SharedPreferences.Editor editor = preferences.edit();
        Object valueresult;
        ExpressionParser parser = new ExpressionParser();
        FirebaseExpression expr = getvars().get(1);
        valueresult = parser.evaluate(expr);

        Log.d("Update","Put " + varName + " as value " + valueresult.toString());
        editor.putString(varName, (String) valueresult);
        editor.apply();
    }
}

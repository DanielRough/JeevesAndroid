package com.example.daniel.jeeves;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.daniel.jeeves.firebase.FirebaseExpression;
import com.example.daniel.jeeves.firebase.FirebaseVariable;

import java.util.Map;

/**
 * Created by Daniel on 09/06/15.
 */
public class ExpressionParser {

    /**
     * This should ideally figure out whether the expression involving the variable and value evaluates to true or false
     * @param variable
     * @param operation
     * @param value
     * @return
     */

    public static final String AND = "&&";
    public static final String OR = "||";
    public static final String LESS_THAN = "<";
    public static final String GREATER_THAN = ">";
    public static final String EQUALS = "==";
    public static final String NOT_EQUALS = "<>";

    public static final String ADD = "+";
    public static final String SUBTRACT = "-";
    public static final String DIVIDE = "/";
    public static final String MULTIPLY = "*";

    protected Context appContext;
    public ExpressionParser(Context appContext){
        this.appContext = appContext;
    }
    public Object evaluate(FirebaseExpression expr) {

        String type = expr.gettype();
     //   SharedPreferences userPrefs = app.getSharedPreferences(app.getString(R.string.userprefs), Context.MODE_PRIVATE);
        if(expr instanceof FirebaseVariable) {
            //Is it a named variable, or just a standard value?
            if(((FirebaseVariable)expr).getisValue() == false){
                String varname = ((FirebaseVariable)expr).getname();
                Log.d("VARNAME","Varname is " + ((FirebaseVariable)expr).getname());
                Log.d("VARTYPE","Vartype is " + ((FirebaseVariable) expr).getvartype());
                //GET THE HARDCODED STRING OUT OF HERE, THIS IS BAD
                SharedPreferences prefs = appContext.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
                Map<String,?> prefsmap = prefs.getAll();
                return prefsmap.get(varname);
            }
            else{
                return ((FirebaseVariable) expr).getname();
            }
        }
        FirebaseExpression lhs = ((Operation)expr).getLHS();
        FirebaseExpression rhs = ((Operation)expr).getRHS();
        String operation = ((Operation)expr).getOperation();
        if(operation.equals(AND))
            return (boolean)evaluate(lhs) && (boolean)evaluate(rhs);
        else if(operation.equals(OR))
            return (boolean)evaluate(lhs) || (boolean)evaluate(rhs);
        else if(operation.equals(EQUALS)) {
            Log.d("EQUALS","left hand side is " + evaluate(lhs) + " and right hand side is " + evaluate(rhs));
            Log.d("RESULT","but the result is " + (evaluate(lhs) == evaluate(rhs)));
            if(evaluate(lhs) instanceof Integer)
                return (int)evaluate(lhs) == (int)evaluate(rhs);
            else
                return (boolean)evaluate(lhs) == (boolean)evaluate(rhs);
        }
        else if(operation.equals(NOT_EQUALS))
            return evaluate(lhs) != evaluate(rhs);
        else if(operation.equals(LESS_THAN))
            return (int)evaluate(lhs) < (int)evaluate(rhs);
        else if(operation.equals(GREATER_THAN))
            return (int)evaluate(lhs) > (int)evaluate(rhs);
        if(operation.equals(ADD))
            return (int)evaluate(lhs)+(int)evaluate(rhs);
        else if(operation.equals(SUBTRACT))
            return (int)evaluate(lhs)-(int)evaluate(rhs);
        else if(operation.equals(MULTIPLY))
            return (int)evaluate(lhs)*(int)evaluate(rhs);
        else if(operation.equals(DIVIDE))
            return (int)evaluate(lhs)/(int)evaluate(rhs);
        return false;
    }
}

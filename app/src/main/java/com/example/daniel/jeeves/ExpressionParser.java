package com.example.daniel.jeeves;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.daniel.jeeves.firebase.FirebaseExpression;
import com.example.daniel.jeeves.firebase.UserVariable;

import java.util.List;
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

    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String LESS_THAN = "IS LESS THAN";
    public static final String GREATER_THAN = "IS GREATER THAN";
    public static final String EQUALS = "IS EQUAL";
    public static final String NOT_EQUALS = "NOT";

    public static final String ADD = "ADD";
    public static final String SUBTRACT = "SUBTRACT";
    public static final String DIVIDE = "DIVIDE";
    public static final String MULTIPLY = "MULTIPLY";

    protected Context appContext;
    public ExpressionParser(Context appContext){
        this.appContext = appContext;
    }
    public Object evaluate(FirebaseExpression expr) {

        String type = expr.gettype();
     //   SharedPreferences userPrefs = app.getSharedPreferences(app.getString(R.string.userprefs), Context.MODE_PRIVATE);
        if(expr instanceof UserVariable) {
            //Is it a named variable, or just a standard value?
            if(((UserVariable)expr).getisValue() == false){
                String varname = ((UserVariable)expr).getname();
                Log.d("VARNAME","Varname is " + ((UserVariable)expr).getname());
                Log.d("VARTYPE","Vartype is " + ((UserVariable) expr).getvartype());
                //GET THE HARDCODED STRING OUT OF HERE, THIS IS BAD
                SharedPreferences prefs = appContext.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
                Map<String,?> prefsmap = prefs.getAll();
                return prefsmap.get(varname);
            }
            else{
                switch(expr.getvartype()){
                    case "Text": return expr.getvalue();
                    case "Numeric": return Long.parseLong(expr.getvalue());
                    case "Time": return Integer.parseInt(expr.getvalue());
                    case "Boolean" : return Boolean.parseBoolean(expr.getvalue());
                }
                return ((UserVariable) expr).getvalue();
            }
        }
        List<FirebaseExpression> vars = expr.getvariables();
        FirebaseExpression lhs = vars.get(0);
        FirebaseExpression rhs = null;
        if(vars.size() >1) //Sometimes expressions will only have one variable, i.e. NOT(var)
            rhs = vars.get(1);
        String operation = expr.getname();
        if(operation.equals(AND))
            return (boolean)evaluate(lhs) && (boolean)evaluate(rhs);
        else if(operation.equals(OR))
            return (boolean)evaluate(lhs) || (boolean)evaluate(rhs);
        else if(operation.equals(EQUALS)) {
            Log.d("EQUALS","left hand side is " + evaluate(lhs) + " and right hand side is " + evaluate(rhs));
            Log.d("RESULT","but the result is " + (evaluate(lhs) == evaluate(rhs)));
            if(evaluate(lhs) instanceof Long)
                return (long)evaluate(lhs) == (long)evaluate(rhs);
            else
                return (boolean)evaluate(lhs) == (boolean)evaluate(rhs);
        }
        else if(operation.equals(NOT_EQUALS))
            return !(boolean)(evaluate(lhs));
        else if(operation.equals(LESS_THAN))
            return (long)evaluate(lhs) < (long)evaluate(rhs);
        else if(operation.equals(GREATER_THAN))
            return (long)evaluate(lhs) > (long)evaluate(rhs);
        if(operation.equals(ADD))
            return (long)evaluate(lhs)+(long)evaluate(rhs);
        else if(operation.equals(SUBTRACT))
            return (long)evaluate(lhs)-(long)evaluate(rhs);
        else if(operation.equals(MULTIPLY))
            return (long)evaluate(lhs)*(long)evaluate(rhs);
        else if(operation.equals(DIVIDE))
            return (long)evaluate(lhs)/(long)evaluate(rhs);
        return false;
    }
}

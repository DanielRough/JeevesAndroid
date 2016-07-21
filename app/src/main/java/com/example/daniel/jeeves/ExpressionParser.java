package com.example.daniel.jeeves;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.daniel.jeeves.firebase.FirebaseExpression;
import com.example.daniel.jeeves.firebase.UserVariable;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.classifier.SensorDataClassifier;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
        SharedPreferences prefs = appContext.getSharedPreferences("userprefs", Context.MODE_PRIVATE);

        String type = expr.gettype();
     //   SharedPreferences userPrefs = app.getSharedPreferences(app.getString(R.string.userprefs), Context.MODE_PRIVATE);
        if(expr instanceof UserVariable) {
            //Is it a named variable, or just a standard value?
            if(((UserVariable)expr).getisValue() == false){
                String varname = ((UserVariable)expr).getname();
                Log.d("VARNAME","Varname is " + ((UserVariable)expr).getname());
                Log.d("VARTYPE","Vartype is " + ((UserVariable) expr).getvartype());
                String name = ((UserVariable)expr).getname();
                //GET THE HARDCODED STRING OUT OF HERE, THIS IS BAD
                //Map<String,?> prefsmap = prefs.getAll();
                switch(expr.getvartype()){
                    case "Text": return prefs.getString(name,"");
                    case "Numeric": return prefs.getLong(name,0);
                    case "Time": return prefs.getLong(name,0);
                    case "Boolean" : return prefs.getBoolean(name,false);
                }
            }
            else{
                switch(expr.getvartype()){
                    case "Text": return expr.getvalue();
                    case "Numeric": return Long.parseLong(expr.getvalue());
                    case "Time": return Long.parseLong(expr.getvalue());
                    case "Boolean" : return Boolean.parseBoolean(expr.getvalue());
                }
                return ((UserVariable) expr).getvalue();
            }
        }
        List<FirebaseExpression> vars = expr.getvariables();

        //IF VARS IS NULL THEN THIS IS A SURVEY, SENSOR OR TIME EXPRESSION!
        if(vars == null){
            Map<String,Object> params = expr.getparams();
            if(params.containsKey("sensor")){ //a sensor expression
                String sensor = params.get("sensor").toString();
                String returns = params.get("returns").toString();
                Log.d("HELLO","ADFADF");

                //Now we want to poll this sensor here to see what it returns
                try {
                    int sensortype = SensorUtils.getSensorType(sensor);
                    SampleOnceTask sampler = new SampleOnceTask(sensortype);
                    Log.d("GOODBYE","ADFADF");

                    //We need to irritatingly make an exception for location sensor

                    if(sensortype == SensorUtils.SENSOR_TYPE_LOCATION)
                        returns = prefs.getString(returns,"");
                    SensorData data = sampler.execute().get();
                    SensorDataClassifier classifier = SensorUtils.getSensorDataClassifier(sensortype);
                    Log.d("HOPEFULLY GOT SOME","SENSOR DATA");
                    if(classifier.isInteresting(data, SensorConfig.getDefaultConfig(sensortype),returns))
                        return true; //Return true if it returns the result we want!
                    return false;
                } catch (ESException e) {
                    Log.d("EXCEPTION",e.getMessage());
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Log.d("EXCEPTION",e.getMessage());

                    e.printStackTrace();
                } catch (ExecutionException e) {
                    Log.d("EXCEPTION",e.getMessage());

                    e.printStackTrace();
                }
            }


            else if(params.containsKey("timeDiff")){ //a timediff expression
                String beforeAfter = params.get("beforeAfter").toString();
                String timeDiff = params.get("timeDiff").toString();
                HashMap<String,Object> var = (HashMap<String,Object>)params.get("timevar");
                String timevar = prefs.getString(var.get("name").toString(),""); //Get the time the user has specified
                Log.d("TIME VAR", "Time var is " + timevar);
                long timeDiffMills = 0;
                long marginOfError = 0;
                switch(timeDiff){
                    case "1 month": timeDiffMills = 30 * 24 * 3600 *1000; marginOfError = 24*3600*1000; break; //Margin of error of a day
                    case "1 week": timeDiffMills = 7 * 24 * 3600 * 1000; marginOfError = 24*3600*1000; break; //Margin of error of a day
                    case "1 day": timeDiffMills = 24 * 3600 * 1000; marginOfError = 24*3600*1000; break; //Margin of error of a day
                    case "1 hour": timeDiffMills = 3600 * 1000; marginOfError = 3600*1000; break; //Margin of error of an hour
                }
                long currentTime = System.currentTimeMillis();
                long diff = Long.parseLong(timevar) - currentTime;
                if(beforeAfter.equals("before")){
                    if(diff < 0)return false;
                    if(diff < marginOfError)return true;
                    return false;

                }
                else if(beforeAfter.equals("after")){
                    if(diff > 0)return false;
                    if(-(diff) < marginOfError) return true;
                    return false;

                }

            }
        }
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

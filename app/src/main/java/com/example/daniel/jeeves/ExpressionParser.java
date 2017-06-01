package com.example.daniel.jeeves;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.daniel.jeeves.firebase.FirebaseExpression;
import com.example.daniel.jeeves.firebase.UserVariable;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.classifier.SensorDataClassifier;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public static final String AND = "Both True";
    public static final String OR = "Either True";
    public static final String LESS_THAN = "Less Than";
    public static final String GREATER_THAN = "Greater Than";
    public static final String EQUALS = "Equality";
    public static final String NOT_EQUALS = "Not True";
//
//    public static final String ADD = "ADD";
//    public static final String SUBTRACT = "SUBTRACT";
//    public static final String DIVIDE = "DIVIDE";
//    public static final String MULTIPLY = "MULTIPLY";

    protected Context appContext;
    public ExpressionParser(Context appContext){
        this.appContext = appContext;
    }
    public String evaluate(FirebaseExpression expr) {

        SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        if(expr.getisValue() || expr.getisCustom()) {
            //Is it a named variable, or just a standard value?
            if(expr.getisValue() == false){
                Log.d("VARNAME","Varname is " + (expr).getname());
                Log.d("VARTYPE","Vartype is " + ( expr).getvartype());
                String name = (expr).getname();
                //GET THE HARDCODED STRING OUT OF HERE, THIS IS BAD
                switch(expr.getvartype()){
                    case "Location": return userPrefs.getString(name,"");
                    case "Numeric": return Long.toString(userPrefs.getLong(name,0));
                    case "Time": return Long.toString(userPrefs.getLong(name,0));
                    case "Boolean" : if(userPrefs.contains(name)){
                        Log.d("YEAH","Yeah I have " + name + " and it's " + userPrefs.getBoolean(name,false));
                    }
                    else {
                        Log.d("NAH", "Nah mate couldn't find it");
                    }
                        return Boolean.toString(userPrefs.getBoolean(name,false));
                    }
                }
            else{
                Log.i("VAAALUE","Value is " + (expr).getvalue());
                return (expr).getvalue().toString();
            }
        }

        else if(expr.getvariables() == null){
            Map<String,Object> params = expr.getparams();
            if(params.containsKey("selectedSensor")){ //a sensor expression
                String sensor = params.get("selectedSensor").toString();
                String returns = params.get("result").toString();
                Log.d("HELLO","ADFADF");

                //Now we want to poll this sensor here to see what it returns
                try {
                    int sensortype = SensorUtils.getSensorType(sensor);
                    SampleOnceTask sampler = new SampleOnceTask(sensortype);
                    Log.d("GOODBYE","ADFADF");

                    //We need to irritatingly make an exception for location sensor

                    if(sensortype == SensorUtils.SENSOR_TYPE_LOCATION)
                        returns = userPrefs.getString(returns,"");
                    SensorData data = sampler.execute().get();
                    SensorDataClassifier classifier = SensorUtils.getSensorDataClassifier(sensortype);
                    Log.d("HOPEFULLY GOT SOME","SENSOR DATA");
                    if(classifier.isInteresting(data, SensorConfig.getDefaultConfig(sensortype),returns))
                        return "true"; //Return true if it returns the result we want!
                    return "false";
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
                String dateStr = params.get("timeVar").toString();
                DateFormat formatter = new SimpleDateFormat("dd/MM/yy");
                Date date = null;
                try {
                    date = (Date)formatter.parse(dateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long timevar = date.getTime();
                long differenceInMillis = 0;
                long marginOfError = 0;
                switch(timeDiff){
                    case "1 month": differenceInMillis = 30 * 24 * 3600 *1000; marginOfError = 24*3600*1000; break; //Margin of error of a day
                    case "1 week": differenceInMillis = 7 * 24 * 3600 * 1000; marginOfError = 24*3600*1000; break; //Margin of error of a day
                    case "1 day": differenceInMillis = 24 * 3600 * 1000; marginOfError = 24*3600*1000; break; //Margin of error of a day
                    case "1 hour": differenceInMillis = 3600 * 1000; marginOfError = 3600*1000; break; //Margin of error of an hour
                }
                long currentTime = System.currentTimeMillis();
                long diff = timevar - currentTime;
                if(beforeAfter.equals("before")){
                    if(diff < 0)return "false";
                    long error = diff-differenceInMillis;
                    if(Math.abs(error) < marginOfError)return "true";
                    return "false";

                }
                else if(beforeAfter.equals("after")){
                    if(diff > 0)return "false";
                    long error = (-diff)-differenceInMillis;
                    if(Math.abs(error) < marginOfError) return "true";
                    return "false";

                }

            }
            else{
                String varname = ((UserVariable)expr).getname();
                Log.i("VARNAME","Varname is " + (expr).getname());
                Log.i("VARTYPE","Vartype is " + ( expr).getvartype());
                String name = (expr).getname();
                //GET THE HARDCODED STRING OUT OF HERE, THIS IS BAD
                switch(expr.getvartype()){
                    case "Location": return userPrefs.getString(name,"");
                    case "Numeric": return Long.toString(userPrefs.getLong(name,0));
                    case "Time": return Long.toString(userPrefs.getLong(name,0));
                    case "Boolean" : return Boolean.toString(userPrefs.getBoolean(name,false));
                }
            }
        }

        List<FirebaseExpression> vars = expr.getvariables();
        FirebaseExpression lhs = vars.get(0);
        FirebaseExpression rhs = null;
        if(vars.size() >1) //Sometimes expressions will only have one variable, i.e. NOT(var)
            rhs = vars.get(1);
        String operation = expr.getname();
        Log.d("OPERATION","Operation is " + operation);
        if(operation.equals(AND))
            return Boolean.toString(Boolean.parseBoolean(evaluate(lhs)) && Boolean.parseBoolean(evaluate(rhs)));
        else if(operation.equals(OR))
            return Boolean.toString(Boolean.parseBoolean(evaluate(lhs)) || Boolean.parseBoolean(evaluate(rhs)));
        else if(operation.equals(EQUALS)) {
            return Boolean.toString((evaluate(lhs)).equals(evaluate(rhs)));
        }
        else if(operation.equals(NOT_EQUALS))
            return Boolean.toString(!(Boolean.parseBoolean(evaluate(lhs))));
        else if(operation.equals(LESS_THAN))
            return Boolean.toString(Long.parseLong(evaluate(lhs)) < Long.parseLong(evaluate(rhs)));
        else if(operation.equals(GREATER_THAN))
            return Boolean.toString(Long.parseLong(evaluate(lhs)) > Long.parseLong(evaluate(rhs)));
//        if(operation.equals(ADD))
//            return Long.parseLong((String) evaluate(lhs)) + Long.parseLong((String) evaluate(rhs));
//        else if(operation.equals(SUBTRACT))
//            return Long.parseLong((String) evaluate(lhs)) - Long.parseLong((String) evaluate(rhs));
//        else if(operation.equals(MULTIPLY))
//            return Long.parseLong((String) evaluate(lhs)) * Long.parseLong((String) evaluate(rhs));
//        else if(operation.equals(DIVIDE))
//            return Long.parseLong((String) evaluate(lhs)) / Long.parseLong((String) evaluate(rhs));
        return "false";
    }
}

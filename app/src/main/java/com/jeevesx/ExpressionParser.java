package com.jeevesx;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jeevesx.firebase.FirebaseExpression;
import com.jeevesx.sensing.sensormanager.ESException;
import com.jeevesx.sensing.sensormanager.ESSensorManager;
import com.jeevesx.sensing.sensormanager.classifier.SensorDataClassifier;
import com.jeevesx.sensing.sensormanager.config.SensorConfig;
import com.jeevesx.sensing.sensormanager.data.SensorData;
import com.jeevesx.sensing.sensormanager.sensors.SensorUtils;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * A class for parsing the value of complex expressions based on their variables
 * and values.
 */
public class ExpressionParser {

    private static final String AND = "Both True";
    private static final String OR = "Either True";
    private static final String LESS_THAN = "Less Than";
    private static final String GREATER_THAN = "Greater Than";
    private static final String EQUALS = "Equality";
    private static final String NOT_EQUALS = "Not True";

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    //Sensor expression constants
    private static final String SENSOR = "selectedSensor";
    private static final String RESULT = "result";

    //Time expression constants
    private static final String TIME_DIFF = "timeDiff";
    private static final String BEFORE_AFTER = "beforeAfter";
    private static final String BEFORE = "before";
    private static final String AFTER = "after";
    private static final String TIME_VAR = "timeVar";
    private static final String MONTH = "1 month";
    private static final String WEEK = "1 week";
    private static final String DAY = "1 day";

    private static final String TIME_BOUNDS = "timeBoundEarly";
    private static final String TIME_BOUNDS_LATE = "timeBoundLate";
    private static final String DATE_BOUNDS = "dateBoundEarly";
    private static final String DATE_BOUNDS_LATE = "dateBoundLate";

    public ExpressionParser() { }

    public String evaluate(FirebaseExpression expr) {

        SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
        if(expr == null)
            return "0";
        if (expr.getisValue()) {
            return (expr).getvalue();
        }
        else if(expr.getisCustom()){
            String name = (expr).getname();
            if(expr.getvartype() == AppContext.BOOLEAN){
                return (userPrefs.getString(name, "false"));
            }
            return userPrefs.getString(name, "");
        }
        else if (expr.getvariables() == null) {
            Map<String, Object> params = expr.getparams();
            if (params.containsKey(SENSOR)) { //a sensor expression
                String sensor = params.get(SENSOR).toString();
                String returns = params.get(RESULT).toString();
                //Now we want to poll this sensor here to see what it returns
                try {
                    int sensortype = SensorUtils.getSensorType(sensor);
                    SampleOnceTask sampler = new SampleOnceTask(sensortype);
                    //This gets the last known location from our user prefs
                    //It then gets the location required in the test expression
                    //If they are roughly equal, it returns true
                    if (sensortype == SensorUtils.SENSOR_TYPE_LOCATION){
                        String lastLoc = userPrefs.getString("LastLocation", "");
                        //Stores the semantic 'last location'
                        return lastLoc.equals(returns) ? TRUE : FALSE;
                    }
                    //Otherwise we're looking at other sensor data
                    SensorData data = sampler.execute().get();
                    SensorDataClassifier classifier = SensorUtils.getSensorDataClassifier(sensortype);
                    if (classifier.isInteresting(data,
                        SensorConfig.getDefaultConfig(sensortype), returns, false))
                        return TRUE; //Return true if it returns the result we want!
                    return FALSE;
                } catch (Exception e){
                    e.printStackTrace();
                    return FALSE;
                }
            }
            else if (params.containsKey(TIME_BOUNDS)){
                String timeEarly = params.get(TIME_BOUNDS).toString();
                String timeLate = params.get(TIME_BOUNDS_LATE).toString();
                long lTimeEarly = Long.parseLong(timeEarly);
                long lTimeLate = Long.parseLong(timeLate);
                long millis = System.currentTimeMillis();
                return (millis > lTimeEarly && millis < lTimeLate) ? TRUE : FALSE;
            }
            else if (params.containsKey(DATE_BOUNDS)){
                String dateEarly = params.get(DATE_BOUNDS).toString();
                String dateLate = params.get(DATE_BOUNDS_LATE).toString();
                long lDateEarly = Long.parseLong(dateEarly);
                long lDateLong = Long.parseLong(dateLate);
                long millis = System.currentTimeMillis();
                return (millis > lDateEarly && millis < lDateLong) ? TRUE : FALSE;
            }
            else if (params.containsKey(TIME_DIFF)) { //a timediff expression
                String beforeAfter = params.get(BEFORE_AFTER).toString();
                String timeDiff = params.get(TIME_DIFF).toString();
                String dateStr = params.get(TIME_VAR).toString();
                long timevar = Long.parseLong(dateStr); //Milliseconds since epoch
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(timevar);
                long differenceInMillis = 0;
                long marginOfError = 0;
                switch (timeDiff) {
                    case MONTH:
                        differenceInMillis = 30L * 24L * 3600L * 1000L;
                        marginOfError = 24 * 3600 * 1000;
                        break; //Margin of error of a day
                    case WEEK:
                        differenceInMillis = 7L * 24L * 3600L * 1000;
                        marginOfError = 24 * 3600 * 1000;
                        break; //Margin of error of a day
                    case DAY:
                        differenceInMillis = 24L * 3600L * 1000L;
                        marginOfError = 3600 * 1000;
                        break; //Margin of error of an hour
                }
                long currentTime = System.currentTimeMillis();
                c.setTimeInMillis(currentTime);
                long diff = timevar - currentTime;

                if (beforeAfter.equals(BEFORE)) {
                    if (diff < 0) return FALSE;
                    long error = diff - differenceInMillis;
                    return (Math.abs(error) < marginOfError) ? TRUE : FALSE;
                } else if (beforeAfter.equals(AFTER)) {
                    if (diff > 0) return FALSE;
                    long error = (-diff) - differenceInMillis;
                    return (Math.abs(error) < marginOfError) ? TRUE : FALSE;
                }
            }
            else if (params.containsKey("category")){
                String category = params.get("category").toString();
                String result = params.get("result").toString();
                String actualValue = userPrefs.getString(category,"");
                Log.d("VALUE","value of " + category + " IS " + actualValue);
                return (result.equals(actualValue)) ? TRUE : FALSE;
            }
        }

        List<FirebaseExpression> vars = expr.getvariables();
        if(vars == null){
            return FALSE;
        }
        FirebaseExpression lhs = vars.get(0);
        FirebaseExpression rhs = null;
        if (vars.size() > 1) //Sometimes expressions will only have one variable, i.e. NOT(var)
            rhs = vars.get(1);
        String operation = expr.getname();
        switch (operation) {
            case AND:
                return Boolean.toString(Boolean.parseBoolean(evaluate(lhs)) && Boolean.parseBoolean(evaluate(rhs)));
            case OR:
                return Boolean.toString(Boolean.parseBoolean(evaluate(lhs)) || Boolean.parseBoolean(evaluate(rhs)));
            case EQUALS:
                return Boolean.toString((evaluate(lhs)).equals(evaluate(rhs)));
            case NOT_EQUALS:
                return Boolean.toString(!(Boolean.parseBoolean(evaluate(lhs))));
            case LESS_THAN:
                return Boolean.toString(Long.parseLong(evaluate(lhs)) < Long.parseLong(evaluate(rhs)));
            case GREATER_THAN:
                return Boolean.toString(Long.parseLong(evaluate(lhs)) > Long.parseLong(evaluate(rhs)));
        }
        return FALSE;
    }


    //Sensor sampling for evaluating the Sensor Expression
    static class SampleOnceTask extends AsyncTask<Void, Void, SensorData> {
        private final ESSensorManager sensorManager;
        private final int sensorType;

        SampleOnceTask(int sensorType) throws ESException {
            this.sensorType = sensorType;
            sensorManager = ESSensorManager.getSensorManager(AppContext.getContext());
        }

        @Override
        protected SensorData doInBackground(Void... params) {
            try {
                return sensorManager.getDataFromSensor(sensorType);
            } catch (ESException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}

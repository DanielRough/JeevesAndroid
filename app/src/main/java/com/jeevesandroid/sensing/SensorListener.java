package com.jeevesandroid.sensing;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.jeevesandroid.AppContext;
import com.jeevesandroid.firebase.FirebaseUtils;
import com.jeevesandroid.sensing.jsonformatter.JSONFormatter;
import com.jeevesandroid.sensing.sensormanager.ESException;
import com.jeevesandroid.sensing.sensormanager.ESSensorManager;
import com.jeevesandroid.sensing.sensormanager.ESSensorManagerInterface;
import com.jeevesandroid.sensing.sensormanager.SensorDataListener;
import com.jeevesandroid.sensing.sensormanager.classifier.SensorDataClassifier;
import com.jeevesandroid.sensing.sensormanager.config.LocationConfig;
import com.jeevesandroid.sensing.sensormanager.config.SensorConfig;
import com.jeevesandroid.sensing.sensormanager.data.SensorData;
import com.jeevesandroid.sensing.sensormanager.sensors.SensorUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Daniel on 27/04/2016.
 */
public class SensorListener implements SensorDataListener {

    private final Context context;
    public SensorListener(int sensorType)
    {

        this.context = AppContext.getContext();

        try
        {
            ESSensorManagerInterface sensorManager = ESSensorManager.getSensorManager(context);

            if (sensorType == SensorUtils.SENSOR_TYPE_LOCATION)
            {
                sensorManager.setSensorConfig(SensorUtils.SENSOR_TYPE_LOCATION, LocationConfig.ACCURACY_TYPE, LocationConfig.LOCATION_ACCURACY_FINE);
            }
        }
        catch (ESException e)
        {
            Log.d("Error", "ESexception");
        }
    }
    @Override
    public void onDataSensed(SensorData data) {
        JSONFormatter f = JSONFormatter.getJSONFormatter(context, data.getSensorType());
        try {
            String s = f.toString(data);
            JSONObject sensorobject = new JSONObject(s);
            Iterator<String> keys = sensorobject.keys();
            HashMap<String, Object> sensordata = new HashMap<>();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = sensorobject.get(key);
                if(value instanceof JSONArray){
                    value = sensorobject.get(key).toString();
                }
                sensordata.put(key, value);
            }
           // }
            SensorDataClassifier classifier = SensorUtils.getSensorDataClassifier(data.getSensorType());
            String classifiedResult = classifier.getClassification(data, SensorConfig.getDefaultConfig(data.getSensorType()));
            sensordata.put("classification",classifiedResult);
            DatabaseReference patientRef = FirebaseUtils.PATIENT_REF.child("sensordata").child(sensorobject.getString("dataType")).push();
            patientRef.setValue(sensordata);
        } catch (ESException | JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
        @Override
    public void onCrossingLowBatteryThreshold(boolean isBelowThreshold) {

    }
}

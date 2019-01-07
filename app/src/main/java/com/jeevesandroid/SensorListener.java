package com.jeevesandroid;

import android.content.Context;
import android.util.Log;

import com.jeevesandroid.firebase.FirebaseUtils;
import com.google.firebase.database.DatabaseReference;
import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.ESSensorManagerInterface;
import com.ubhave.sensormanager.SensorDataListener;
import com.ubhave.sensormanager.classifier.SensorDataClassifier;
import com.ubhave.sensormanager.config.GlobalConfig;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.config.pull.LocationConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

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

        this.context = ApplicationContext.getContext();

        try
        {
            ESSensorManagerInterface sensorManager = ESSensorManager.getSensorManager(context);
            sensorManager.setGlobalConfig(GlobalConfig.LOW_BATTERY_THRESHOLD, 25);

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
        JSONFormatter f = DataFormatter.getJSONFormatter(context, data.getSensorType());
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
        } catch (DataHandlerException | ESException | JSONException e) {
            e.printStackTrace();
        }
    }
        @Override
    public void onCrossingLowBatteryThreshold(boolean isBelowThreshold) {

    }
}

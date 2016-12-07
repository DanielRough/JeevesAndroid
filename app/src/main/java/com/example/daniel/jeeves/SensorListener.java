package com.example.daniel.jeeves;

import android.content.Context;
import android.util.Log;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.ESSensorManagerInterface;
import com.ubhave.sensormanager.SensorDataListener;
import com.ubhave.sensormanager.config.GlobalConfig;
import com.ubhave.sensormanager.config.pull.LocationConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

/**
 * Created by Daniel on 27/04/2016.
 */
public class SensorListener implements SensorDataListener {

    private final int sensorType;
    private ESSensorManagerInterface sensorManager;
    private final JSONFormatter formatter;

    private boolean isSubscribed;

    public SensorListener(int sensorType)
    {
        this.sensorType = sensorType;
        isSubscribed = false;

        Context context = ApplicationContext.getContext();
        formatter = DataFormatter.getJSONFormatter(context, sensorType);

        try
        {
            sensorManager = ESSensorManager.getSensorManager(context);
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
        if(sensorType == SensorUtils.SENSOR_TYPE_SMS){
        }
    }

    @Override
    public void onCrossingLowBatteryThreshold(boolean isBelowThreshold) {

    }
}

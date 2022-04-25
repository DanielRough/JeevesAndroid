package com.jeevesx.actions.actiontypes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jeevesx.AppContext;
import com.jeevesx.sensing.SensorListener;
import com.jeevesx.SenseService;
import com.jeevesx.sensing.sensormanager.ESException;
import com.jeevesx.sensing.sensormanager.ESSensorManager;
import com.jeevesx.sensing.sensormanager.sensors.SensorUtils;


import java.util.Map;

/**
 * Functionality for sensing data from a specified sensor for
 * a specified period of time
 */
public class CaptureDataAction extends FirebaseAction {

    public CaptureDataAction(Map<String,Object> params){
        setparams(params);
    }

    private void startActivityUpdates(){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(AppContext.STARTACTIVITY);
        AppContext.getContext().sendBroadcast(broadcastIntent);

    }
    private void startLocationUpdates() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(AppContext.STARTLOC);
        AppContext.getContext().sendBroadcast(broadcastIntent);

    }

    public void execute() {
        Context app = AppContext.getContext();
        if(!getparams().containsKey("selectedSensor") || !getparams().containsKey("time"))return;
        String sensor = getparams().get("selectedSensor").toString();
        String startstop = getparams().get("time").toString();

        long timetosense = 0;

        switch(startstop){
            case "1 hr": timetosense = 3600000; break;
            case "10 mins": timetosense = 600000; break;
            case "1 min": timetosense = 60000; break;
            case "ever": timetosense = 0; break;
        }
        Intent broadcastIntent = new Intent();
        switch (sensor) {
            case "Location":
                startLocationUpdates();
                broadcastIntent.setAction(AppContext.STOPLOC);

                break;
            case "Activity":
                startActivityUpdates();
                broadcastIntent.setAction(AppContext.STOPACTIVITY);
                break;
            default:
                try {

                    int sensorType = SensorUtils.getSensorType(sensor);
                    SensorListener listener = SenseService.sensorlisteners.get(sensorType);
                    Log.d("Sensor", "Try and subscribe to " + sensorType);
                    int subscriptionId = ESSensorManager.getSensorManager(app)
                        .subscribeToSensorData(sensorType, listener);
                    Log.d("SUCCESS", "Successfully subscribed to " + sensorType);
                    SenseService.subscribedSensors.put(sensorType, subscriptionId);

                    broadcastIntent.putExtra("sensortype", sensorType);
                    broadcastIntent.putExtra("subid", subscriptionId);
                    broadcastIntent.setAction(AppContext.STOPSENSOR);
                } catch (ESException e) {
                    e.printStackTrace();
                }
                break;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                AppContext.getContext(), 234324243, broadcastIntent, 0);

        if(timetosense > 0) {
            AlarmManager alarmManager = (AlarmManager) AppContext.getContext()
                .getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + (timetosense), pendingIntent);
        }
    }

}

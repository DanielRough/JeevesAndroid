package com.jeevesandroid.actions.actiontypes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jeevesandroid.ApplicationContext;
import com.jeevesandroid.sensing.SensorListener;
import com.jeevesandroid.SenseService;
import com.jeevesandroid.sensing.sensormanager.ESException;
import com.jeevesandroid.sensing.sensormanager.ESSensorManager;
import com.jeevesandroid.sensing.sensormanager.sensors.SensorUtils;


import java.util.Map;

/**
 * Created by Daniel on 26/05/15.
 */
public class CaptureDataAction extends FirebaseAction {

    public CaptureDataAction(Map<String,Object> params){
        setparams(params);
    }

    private void startActivityUpdates(){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ApplicationContext.STARTACTIVITY);
        ApplicationContext.getContext().sendBroadcast(broadcastIntent);

    }
    private void startLocationUpdates() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ApplicationContext.STARTLOC);
        ApplicationContext.getContext().sendBroadcast(broadcastIntent);

    }

    public void execute() {
        Context app = ApplicationContext.getContext();
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
                broadcastIntent.setAction(ApplicationContext.STOPLOC);

                break;
            case "Activity":
                startActivityUpdates();
                broadcastIntent.setAction(ApplicationContext.STOPACTIVITY);
                break;
            default:
                try {

                    int sensorType = SensorUtils.getSensorType(sensor);
                    SensorListener listener = SenseService.sensorlisteners.get(sensorType);
                    Log.d("Sensor", "Try and subscribe to " + sensorType);
                    int subscriptionId = ESSensorManager.getSensorManager(app).subscribeToSensorData(sensorType, listener);
                    Log.d("SUCCESS", "Successfully subscribed to " + sensorType);
                    SenseService.subscribedSensors.put(sensorType, subscriptionId);

                    broadcastIntent.putExtra("sensortype", sensorType);
                    broadcastIntent.putExtra("subid", subscriptionId);
                    broadcastIntent.setAction(ApplicationContext.STOPSENSOR);
                } catch (ESException e) {
                    e.printStackTrace();
                }
                break;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ApplicationContext.getContext(), 234324243, broadcastIntent, 0);

        if(timetosense > 0) {
            AlarmManager alarmManager = (AlarmManager) ApplicationContext.getContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + (timetosense), pendingIntent);
        }
    }

}

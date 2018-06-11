package com.jeevesandroid.actions;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.jeevesandroid.ApplicationContext;
import com.jeevesandroid.SensorListener;
import com.jeevesandroid.SenseService;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.sensors.SensorUtils;

import java.util.Map;

import static android.content.Context.ALARM_SERVICE;
import static com.jeevesandroid.ApplicationContext.STARTACTIVITY;
import static com.jeevesandroid.ApplicationContext.STARTLOC;
import static com.jeevesandroid.ApplicationContext.STOPACTIVITY;
import static com.jeevesandroid.ApplicationContext.STOPLOC;
import static com.jeevesandroid.ApplicationContext.STOPSENSOR;

/**
 * Created by Daniel on 26/05/15.
 */
public class CaptureDataAction extends FirebaseAction {

    public CaptureDataAction(Map<String,Object> params){
        setparams(params);
    }
    static int count = 0;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;

    public void startActivityUpdates(){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(STARTACTIVITY);
        ApplicationContext.getContext().sendBroadcast(broadcastIntent);

    }
    public void startLocationUpdates() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(STARTLOC);
        ApplicationContext.getContext().sendBroadcast(broadcastIntent);


//        //START THE (NOW SEPARATE) LOCATION SERVICE
//
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ApplicationContext.getContext());
//        if (ActivityCompat.checkSelfPermission(ApplicationContext.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ApplicationContext.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//
//        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }
//    private void stopLocationUpdates() {
//        Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction(STOPLOC);
//        broadcastIntent.putExtra("Data", "Broadcast Data");
//        ApplicationContext.getContext().sendBroadcast(broadcastIntent);
//      //  mFusedLocationClient.removeLocationUpdates(mLocationCallback);
//    }    private void stopLocationUpdates() {
//        Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction(STOPLOC);
//        broadcastIntent.putExtra("Data", "Broadcast Data");
//        ApplicationContext.getContext().sendBroadcast(broadcastIntent);
//      //  mFusedLocationClient.removeLocationUpdates(mLocationCallback);
//    }
//
//    protected void createLocationRequest() {
//         mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(10000);
//        mLocationRequest.setFastestInterval(5000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//    }
   // mListe

    public boolean execute() {
        //createLocationRequest();
//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                for (Location location : locationResult.getLocations()) {
//                    Log.d("THING@","Location is " + location.getLatitude() + "," + location.getLongitude());
//
//                }
//            };
//        };

        //    int notificationId = Integer.parseInt("8" + count++);
        Context app = ApplicationContext.getContext();
        if(!getparams().containsKey("selectedSensor") || !getparams().containsKey("time"))return false;
        String sensor = getparams().get("selectedSensor").toString();
        String startstop = getparams().get("time").toString();

        long timetosense = 0;
        Log.d("Startstop","start stop is " + startstop);

        switch(startstop){
            case "1 hr": timetosense = 3600000; break;
            case "10 mins": timetosense = 600000; break;
            case "1 min": timetosense = 60000; break;
            case "ever": timetosense = 0; break;
        }
        Intent broadcastIntent = new Intent();
        //TODO: Actually get this working properly
        if(sensor.equals("Location")) {
            startLocationUpdates();
            broadcastIntent.setAction(STOPLOC);

        }
        else if(sensor.equals("Activity")) {
            startActivityUpdates();
            broadcastIntent.setAction(STOPACTIVITY);
        }
        else{
            try {

                int sensorType = SensorUtils.getSensorType(sensor);
                SensorListener listener = SenseService.sensorlisteners.get(sensorType);
                    Log.d("Sensor", "Away to try and subscribe to " + sensorType);
                    int subscriptionId = ESSensorManager.getSensorManager(app).subscribeToSensorData(sensorType, listener);
                    Log.d("SUCCESS", "Successfully subscribed to " + sensorType);
                    SenseService.subscribedSensors.put(sensorType, subscriptionId);

//        //START THE (NOW SEPARATE) LOCATION SERVICE
                    //      sensorids.add(sensor);

                    //   sensortype = SensorUtils.getSensorType(sensor);
//            SampleOnceTask sampler = new SampleOnceTask(sensortype);
//            SensorData data = sampler.execute().get();
//            SensorDataClassifier classifier = SensorUtils.getSensorDataClassifier(sensortype);
//            String classifiedResult = classifier.getClassification(data, SensorConfig.getDefaultConfig(sensortype));
//            DatabaseReference sensorRef = FirebaseUtils.PATIENT_REF.child(SENSORDATA_KEY).child(sensor).push();
//            Map<String,Object> dataMap = new HashMap<String,Object>();
//            dataMap.put("time",System.currentTimeMillis());
//            dataMap.put("data",classifiedResult);
//            sensorRef.setValue(dataMap);

                broadcastIntent.putExtra("sensortype",sensorType);
                broadcastIntent.putExtra("subid",subscriptionId);
                broadcastIntent.setAction(STOPSENSOR);
       //         ApplicationContext.getContext().sendBroadcast(broadcastIntent);
           //     Intent intent = new Intent(ApplicationContext.getContext(), MyBroadcastReceiver.class);

            } catch (ESException e) {
                e.printStackTrace();
            }
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ApplicationContext.getContext(), 234324243, broadcastIntent, 0);

        if(timetosense > 0) {
            AlarmManager alarmManager = (AlarmManager) ApplicationContext.getContext().getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + (timetosense), pendingIntent);
        }
        return true;
    }

}

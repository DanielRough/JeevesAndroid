package com.jeeves.actions;

import android.content.Context;
import android.util.Log;

import com.jeeves.ApplicationContext;
import com.jeeves.SensorListener;
import com.jeeves.SenseService;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.sensors.SensorUtils;

import java.util.Map;

/**
 * Created by Daniel on 26/05/15.
 */
public class CaptureDataAction extends FirebaseAction {

    public CaptureDataAction(Map<String,Object> params){
        setparams(params);
    }
    static int count = 0;

   // mListe
    public boolean execute() {
        //    int notificationId = Integer.parseInt("8" + count++);
        Context app = ApplicationContext.getContext();
        String sensor = getparams().get("selectedSensor").toString();

        //TODO: Actually get this working properly
        if(sensor.equals("Location"))return false;
        String startstop = getparams().get("startstop").toString();
        int sensortype = 0;
        try {

            int sensorType = SensorUtils.getSensorType(sensor);
        SensorListener listener = SenseService.sensorlisteners.get(sensorType);
        if (startstop.equals("start")) {

            Log.d("Sensor", "Away to try and subscribe to " + sensorType);
            int subscriptionId = ESSensorManager.getSensorManager(app).subscribeToSensorData(sensorType, listener);
            Log.d("SUCCESS", "Successfully subscribed to " + sensorType);
            SenseService.subscribedSensors.put(sensorType, subscriptionId);

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
        }
        else{
            sensorType = SensorUtils.getSensorType(sensor);
                    ESSensorManager.getSensorManager(app).unsubscribeFromSensorData(SenseService.subscribedSensors.get(sensorType));
                    SenseService.sensorlisteners.remove(sensorType);        }
    } catch (ESException e) {
            e.printStackTrace();
        }

        return true;
    }

//    //Need this to sample once in our sensor expression
//    public static class SampleOnceTask extends AsyncTask<Void, Void, SensorData> {
//        private final ESSensorManager sensorManager;
//        private final int sensorType;
//        protected String errorMessage;
//
//        public SampleOnceTask(int sensorType) throws ESException {
//            this.sensorType = sensorType;
//            sensorManager = ESSensorManager.getSensorManager(ApplicationContext.getContext());
//        }
//
//        @Override
//        protected SensorData doInBackground(Void... params) {
//            try {
//                return sensorManager.getDataFromSensor(sensorType);
//            } catch (ESException e) {
//                e.printStackTrace();
//                errorMessage = e.getMessage();
//                return null;
//            }
//        }
//
//    }
}

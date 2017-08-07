package com.example.daniel.jeeves.actions;

import android.content.Context;
import android.os.AsyncTask;

import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.firebase.FirebaseUtils;
import com.google.firebase.database.DatabaseReference;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.classifier.SensorDataClassifier;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.example.daniel.jeeves.firebase.FirebaseUtils.SENSORDATA_KEY;

/**
 * Created by Daniel on 26/05/15.
 */
public class CaptureDataAction extends FirebaseAction {

    public CaptureDataAction(Map<String,Object> params){
        setparams(params);
    }
    static int count = 0;
    @Override

    public boolean execute(){
    //    int notificationId = Integer.parseInt("8" + count++);
        Context app = ApplicationContext.getContext();
        String sensor = getparams().get("selectedSensor").toString();
        int sensortype = 0;
        try {
            sensortype = SensorUtils.getSensorType(sensor);
            SampleOnceTask sampler = new SampleOnceTask(sensortype);
            SensorData data = sampler.execute().get();
            SensorDataClassifier classifier = SensorUtils.getSensorDataClassifier(sensortype);
            String classifiedResult = classifier.getClassification(data, SensorConfig.getDefaultConfig(sensortype));
            DatabaseReference sensorRef = FirebaseUtils.PATIENT_REF.child(SENSORDATA_KEY).child(sensor).push();
            Map<String,Object> dataMap = new HashMap<String,Object>();
            dataMap.put("time",System.currentTimeMillis());
            dataMap.put("data",classifiedResult);
            sensorRef.setValue(dataMap);
        } catch (ESException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return true;
    }

    //Need this to sample once in our sensor expression
    public static class SampleOnceTask extends AsyncTask<Void, Void, SensorData> {
        private final ESSensorManager sensorManager;
        private final int sensorType;
        protected String errorMessage;

        public SampleOnceTask(int sensorType) throws ESException {
            this.sensorType = sensorType;
            sensorManager = ESSensorManager.getSensorManager(ApplicationContext.getContext());
        }

        @Override
        protected SensorData doInBackground(Void... params) {
            try {
                return sensorManager.getDataFromSensor(sensorType);
            } catch (ESException e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
                return null;
            }
        }

    }
}

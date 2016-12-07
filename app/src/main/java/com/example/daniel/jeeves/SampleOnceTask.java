package com.example.daniel.jeeves;

import android.os.AsyncTask;
import android.util.Log;

import com.example.daniel.jeeves.ApplicationContext;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.data.SensorData;

public class SampleOnceTask extends AsyncTask<Void, Void, SensorData>
{
    private final ESSensorManager sensorManager;
    private final int sensorType;
    protected String errorMessage;

    public SampleOnceTask(int sensorType) throws ESException
    {
        this.sensorType = sensorType;
        sensorManager = ESSensorManager.getSensorManager(ApplicationContext.getContext());
    }

    @Override
    protected SensorData doInBackground(Void... params)
    {
        try
        {
            return sensorManager.getDataFromSensor(sensorType);
        }
        catch (ESException e)
        {
            e.printStackTrace();
            errorMessage = e.getMessage();
            return null;
        }
    }

}
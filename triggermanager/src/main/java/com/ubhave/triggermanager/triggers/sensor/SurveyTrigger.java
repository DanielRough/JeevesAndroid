package com.ubhave.triggermanager.triggers.sensor;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.classifier.SensorClassifiers;
import com.ubhave.triggermanager.TriggerException;
import com.ubhave.triggermanager.TriggerReceiver;
import com.ubhave.triggermanager.config.TriggerConfig;
import com.ubhave.triggermanager.config.TriggerManagerConstants;
import com.ubhave.triggermanager.triggers.Trigger;
import com.ubhave.triggermanager.triggers.TriggerUtils;

/**
 * Created by Daniel on 12/05/2016.
 */
public class SurveyTrigger extends Trigger {
    private final static String TRIGGER_NAME = "SurveyTrigger";
    private String surveyName;
    private boolean result;
    private int numTimes;

    public SurveyTrigger(Context context, int id, TriggerReceiver listener, TriggerConfig params) throws TriggerException {
        super(context, id, listener, params);
    }



    @Override
    public void start() throws TriggerException
    {

        super.start();
        surveyName = getSurveyName();
        result = Boolean.parseBoolean(getSurveyResult());
        numTimes = Integer.parseInt(getNumTimes());


    }
    protected String getSurveyResult() throws TriggerException
    { if (params.containsKey(TriggerConfig.SURVEY_RESULT))
    {
        return params.getParameter(TriggerConfig.SURVEY_RESULT).toString();
    }

    else
    {
        throw new TriggerException(TriggerException.MISSING_PARAMETERS, "SURVEY_RESULT not specified in parameters.");
    }

    }

    protected String getNumTimes() throws TriggerException
    { if (params.containsKey(TriggerConfig.SURVEY_NUMBER))
    {
        return params.getParameter(TriggerConfig.SURVEY_NUMBER).toString();
    }

    else
    {
        throw new TriggerException(TriggerException.MISSING_PARAMETERS, "SURVEY_MISSED not specified in parameters.");
    }

    }

    protected String getSurveyName() throws TriggerException
    {
        if (params.containsKey(TriggerConfig.SURVEY_NAME))
        {
            return params.getParameter(TriggerConfig.SURVEY_NAME).toString();
        }

        else
        {
            throw new TriggerException(TriggerException.MISSING_PARAMETERS, "SURVEY NAME not specified in parameters.");
        }
    }
    @Override
    protected String getTriggerTag() {
        return TRIGGER_NAME;
    }

    protected int getRequestCode()
    {
        //return TriggerUtils.TYPE_CLOCK_TRIGGER_ON_INTERVAL;
        return this.triggerId;
    }

    @Override
    protected PendingIntent getPendingIntent()
    {
        Intent intent = new Intent(getActionName());
        intent.putExtra(TRIGGER_ID, triggerId);
        return PendingIntent.getBroadcast(context, getRequestCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    @Override
    public String getActionName() {
        return TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER;
    }

    @Override
    protected void startAlarm() throws TriggerException {
        // TODO Auto-generated method stub

    }
    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        if(listener != null){
            String survey = intent.getStringExtra("surveyName");
            if(survey.equals(surveyName)) { //first check it's the right survey
                boolean result = intent.getBooleanExtra("result", true);
                if (result == this.result) { //then check it's the right result
                    if (result == true) {
                        int completedTimes = intent.getIntExtra("completed", 0);
                        Log.d("COMPLETED","And you've completed it " + numTimes + " times");

                        if (completedTimes == this.numTimes)
                            sendNotification();
                    }
                    if (result == false) {
                        Log.d("MISSED","Missed the survey " + survey);
                        int missedTimes = intent.getIntExtra("missed", 0);
                        Log.d("MISSED","And you've missed it " + numTimes + " times");

                        if (missedTimes == this.numTimes)
                            sendNotification();
                    }
                }
            }
        }
//        if (listener != null)
//        {
//            int id = intent.getIntExtra(TRIGGER_ID, -1);
//            if (triggerId == id)
//            {
//                sendNotification();
//            }
//        }
    }
}

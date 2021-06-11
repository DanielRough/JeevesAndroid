package com.jeevesandroid.triggers.triggers.sensor;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.TriggerReceiver;
import com.jeevesandroid.triggers.config.TriggerConfig;
import com.jeevesandroid.triggers.config.TriggerConstants;
import com.jeevesandroid.triggers.triggers.Trigger;

/**
 * Class representing the Jeeves 'Survey Trigger'. Fires if the survey name specified
 * in its parameters is completed/missed depending on the 'result' parameter
 */
public class SurveyTrigger extends Trigger {
    private final static String TRIGGER_NAME = "SurveyTrigger";
    private String surveyName;
    private boolean result;

    public SurveyTrigger(Context context, int id, TriggerReceiver listener,
                         TriggerConfig params){
        super(context, id, listener, params);
    }

    @Override
    protected void startAlarm(){

    }

    @Override
    public void start() throws TriggerException {
        super.start();
        surveyName = getSurveyName();
        String sResult = getSurveyResult();
        result = sResult.equals("completed");
    }

    private String getSurveyResult() throws TriggerException {
        if (params.containsKey(TriggerConfig.SURVEY_RESULT)) {
            return params.getParameter(TriggerConfig.SURVEY_RESULT).toString();
        }
        else{
            throw new TriggerException("SURVEY_RESULT not specified in parameters.");
        }
    }

    private String getSurveyName() throws TriggerException
    {
        if (params.containsKey(TriggerConfig.SURVEY_NAME)) {
            return params.getParameter(TriggerConfig.SURVEY_NAME).toString();
        }
        else {
            throw new TriggerException("SURVEY NAME not specified in parameters.");
        }
    }
    @Override
    protected String getTriggerTag() {
        return TRIGGER_NAME;
    }

    @Override
    protected PendingIntent getPendingIntent() {
        Intent intent = new Intent(getActionName());
        intent.putExtra(TRIGGER_ID, triggerId);
        return PendingIntent.getBroadcast(context, triggerId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT);
    }
    @Override
    public String getActionName() {
        return TriggerConstants.ACTION_NAME_SURVEY_TRIGGER;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if(listener != null){
            String survey = intent.getStringExtra("surveyname");
            if(survey == null)
                return;
            if(survey.equals(surveyName)) { //first check it's the right survey
                boolean result = intent.getBooleanExtra("result", true);
                if (result == this.result) { //then check it's the right result
                    sendNotification();
                }
            }
        }
    }
}

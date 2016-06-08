package com.ubhave.triggermanager.triggers.sensor;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ubhave.sensormanager.ESException;
import com.ubhave.triggermanager.TriggerException;
import com.ubhave.triggermanager.TriggerReceiver;
import com.ubhave.triggermanager.config.TriggerConfig;
import com.ubhave.triggermanager.config.TriggerManagerConstants;
import com.ubhave.triggermanager.triggers.Trigger;

/**
 * Created by Daniel on 12/05/2016.
 */
public class SurveyTrigger extends Trigger {
    private final static String TRIGGER_NAME = "SurveyTrigger";

    public SurveyTrigger(Context context, int id, TriggerReceiver listener, TriggerConfig params) throws TriggerException {
        super(context, id, listener, params);
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
        return TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER+"_"+triggerId;
    }

    @Override
    protected void startAlarm() throws TriggerException {
        // TODO Auto-generated method stub

    }
}

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
public class ButtonTrigger extends Trigger {
    private final static String TRIGGER_NAME = "ButtonTrigger";
    private String buttonName;

    public ButtonTrigger(Context context, int id, TriggerReceiver listener, TriggerConfig params) throws TriggerException {
        super(context, id, listener, params);
    }



    @Override
    public void start() throws TriggerException
    {

        super.start();
        buttonName = getButtonName();


    }
    protected String getButtonName() throws TriggerException
    {
        if (params.containsKey(TriggerConfig.BUTTON_NAME))
        {
            return params.getParameter(TriggerConfig.BUTTON_NAME).toString();
        }
        else
        {
            throw new TriggerException(TriggerException.MISSING_PARAMETERS, "BUTTON NAME not specified in parameters.");
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
        return TriggerManagerConstants.ACTION_NAME_BUTTON_TRIGGER;
    }

    @Override
    protected void startAlarm() throws TriggerException {
        // TODO Auto-generated method stub

    }
    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        if(listener != null){
            String btn = intent.getStringExtra("buttonName");
            if(btn.equals(buttonName))
                sendNotification();
        }
    }
}

package com.jeevesandroid.triggers.triggers.sensor;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.TriggerReceiver;
import com.jeevesandroid.triggers.config.TriggerConfig;
import com.jeevesandroid.triggers.config.TriggerManagerConstants;
import com.jeevesandroid.triggers.triggers.Trigger;

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
    private String getButtonName() throws TriggerException
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

    private int getRequestCode()
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
    protected void startAlarm() {
        // TODO Auto-generated method stub

    }
    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        if(listener != null){
            String btn = intent.getStringExtra("buttonName");
            Log.d("RECEIVED","RECEIVED BUTTON but mine is " + buttonName);
            Log.d("EXTRA","Extra was " + btn);
            if(btn.equals(buttonName)) {
                Log.d("EQUAL","Indeed, they are equal");
                sendNotification();
            }
        }
    }
}

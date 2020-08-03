package com.jeeves.triggers.triggers.sensor;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.jeeves.triggers.TriggerException;
import com.jeeves.triggers.TriggerReceiver;
import com.jeeves.triggers.config.TriggerConfig;
import com.jeeves.triggers.config.TriggerConstants;
import com.jeeves.triggers.triggers.Trigger;

/**
 * Class representing the Jeeves 'Button Trigger'
 */
public class ButtonTrigger extends Trigger {
    private final static String TRIGGER_NAME = "ButtonTrigger";
    private String buttonName;

    public ButtonTrigger(Context context, int id, TriggerReceiver listener, TriggerConfig params){
        super(context, id, listener, params);
    }

    @Override
    protected void startAlarm(){

    }

    @Override
    public void start() throws TriggerException
    {
        super.start();
        buttonName = getButtonName();
    }

    private String getButtonName() throws TriggerException
    {
        if (params.containsKey(TriggerConfig.BUTTON_NAME)){
            return params.getParameter(TriggerConfig.BUTTON_NAME).toString();
        }
        else{
            throw new TriggerException("BUTTON NAME not specified in parameters.");
        }
    }
    @Override
    protected String getTriggerTag() {
        return TRIGGER_NAME;
    }

    @Override
    protected PendingIntent getPendingIntent()
    {
        Intent intent = new Intent(getActionName());
        intent.putExtra(TRIGGER_ID, triggerId);
        return PendingIntent.getBroadcast(context, triggerId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    @Override
    public String getActionName() {
        return TriggerConstants.ACTION_NAME_BUTTON_TRIGGER;
    }

    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        if(listener != null){
            String btn = intent.getStringExtra("buttonName");
            if(btn.equals(buttonName)) {
                sendNotification();
            }
        }
    }
}

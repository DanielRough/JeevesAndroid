package com.jeevesandroid.triggers.triggers.clock;

import android.content.Context;
import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.TriggerReceiver;
import com.jeevesandroid.triggers.config.TriggerConfig;

/**
 * Simple class to represent a trigger that fires as soon as app is started
 */
public class BeginTrigger extends ScheduledTrigger
{

    private final static String TRIGGER_NAME = "BeginTrigger";

    public BeginTrigger(Context context, int id, final TriggerReceiver listener,
                        final TriggerConfig parameters) throws TriggerException {
        super(context, id, listener, parameters);
    }

    @Override
    protected String getTriggerTag()
    {
        return TRIGGER_NAME;
    }


}

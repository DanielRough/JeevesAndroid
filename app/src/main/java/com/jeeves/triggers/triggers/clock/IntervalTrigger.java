package com.jeeves.triggers.triggers.clock;

import android.content.Context;

import com.jeeves.triggers.TriggerException;
import com.jeeves.triggers.TriggerReceiver;
import com.jeeves.triggers.config.TriggerConfig;

/**
 * A class representing the Jeeves 'Interval Trigger'
 */
public class IntervalTrigger extends ScheduledTrigger {

    private final static String TRIGGER_NAME = "IntervalTrigger";

    public IntervalTrigger(Context context, int id, final TriggerReceiver listener,
                           final TriggerConfig parameters) throws TriggerException
    {
        super(context, id, listener, parameters);
    }

    @Override
    protected String getTriggerTag()
    {
        return TRIGGER_NAME;
    }
}

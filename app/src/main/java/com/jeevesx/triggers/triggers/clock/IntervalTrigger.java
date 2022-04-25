package com.jeevesx.triggers.triggers.clock;

import android.content.Context;

import com.jeevesx.triggers.TriggerException;
import com.jeevesx.triggers.TriggerReceiver;
import com.jeevesx.triggers.config.TriggerConfig;

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

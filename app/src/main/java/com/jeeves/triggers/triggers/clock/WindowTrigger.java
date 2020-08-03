package com.jeeves.triggers.triggers.clock;

import android.content.Context;
import com.jeeves.triggers.TriggerException;
import com.jeeves.triggers.TriggerReceiver;
import com.jeeves.triggers.config.TriggerConfig;

/**
 * Class representing the Jeeves 'Window Trigger' that allows triggers
 * to be fired every X minutes plus/minus Y minutes
 */
public class WindowTrigger extends ScheduledTrigger {

    private final static String TRIGGER_NAME = "WindowTrigger";

    public WindowTrigger(Context context, int id, final TriggerReceiver listener,
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

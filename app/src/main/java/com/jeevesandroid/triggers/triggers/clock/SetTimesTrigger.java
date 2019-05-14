package com.jeevesandroid.triggers.triggers.clock;

import android.app.PendingIntent;
import android.content.Context;
import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.TriggerReceiver;
import com.jeevesandroid.triggers.config.TriggerConfig;

public class SetTimesTrigger extends ScheduledTrigger {

    private final static String TRIGGER_NAME = "TimeOfDayTrigger";

    public SetTimesTrigger(Context context, int id, final TriggerReceiver listener,
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

package com.ubhave.triggermanager.triggers.clock.random;

import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import com.ubhave.triggermanager.ESTriggerManager;
import com.ubhave.triggermanager.TriggerException;
import com.ubhave.triggermanager.TriggerReceiver;
import com.ubhave.triggermanager.config.TriggerConfig;
import com.ubhave.triggermanager.config.TriggerManagerConstants;
import com.ubhave.triggermanager.triggers.TriggerUtils;

import java.util.HashSet;

/**
 * Created by Daniel on 01/06/2016.
 */
public class JeevesIntervalTrigger extends RandomFrequencyTrigger{

    private final ESTriggerManager triggerManager;

    private final static String TRIGGER_NAME = "JeevesIntervalTrigger";
    private HashSet<Integer> randomlySelectedTriggerIds;
    protected final DailyNotificationScheduler dailySchedulerAlarm;

    public JeevesIntervalTrigger(Context context, int id, final TriggerReceiver listener, final TriggerConfig parameters) throws TriggerException
    {
        super(context, id, listener, parameters);
        this.triggerManager = ESTriggerManager.getTriggerManager(context);
        this.randomlySelectedTriggerIds = new HashSet<Integer>();
        this.dailySchedulerAlarm = new DailyNotificationScheduler(context, params, this);

    }

    @Override
    public void start() throws TriggerException
    {
        if (!isRunning)
        {
            dailySchedulerAlarm.start();
            isRunning = true;
        }
    }

    @Override
    public void stop() throws TriggerException
    {
        if (isRunning)
        {
            dailySchedulerAlarm.stop();
            for (Integer triggerId : this.randomlySelectedTriggerIds)
            {
                try
                {
                    triggerManager.removeTrigger(triggerId);
                }
                catch (TriggerException e)
                {
                    e.printStackTrace();
                }
            }
            isRunning = false;
        }
    }

    @Override
    public void onNotificationTriggered(int alarmId)
    {
        if (randomlySelectedTriggerIds.contains(alarmId))
        {
            try
            {
                listener.onNotificationTriggered(this.triggerId);
                triggerManager.removeTrigger(alarmId);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getActionName()
    {
        return null; // Unused
    }

    @Override
    protected PendingIntent getPendingIntent()
    {
        return null; // Unused
    }

    @Override
    protected void startAlarm() throws TriggerException
    {
        // Nothing to do
    }
    @Override
    protected String getTriggerTag()
    {
        return TRIGGER_NAME;
    }

    public void subscribeTriggerFor(long millis)
    {
        try
        {
            TriggerConfig params = new TriggerConfig();
            params.addParameter(TriggerConfig.CLOCK_TRIGGER_DATE_MILLIS, millis);

            int triggerId = triggerManager.addTrigger(TriggerUtils.TYPE_CLOCK_TRIGGER_ONCE, this, params);
            randomlySelectedTriggerIds.add(triggerId);

            if (TriggerManagerConstants.LOG_MESSAGES)
            {
                Log.d("SUBBIED", "Trigger subscribed: " + triggerId);
            }
        }
        catch (TriggerException e)
        {
            e.printStackTrace();
        }
    }

}

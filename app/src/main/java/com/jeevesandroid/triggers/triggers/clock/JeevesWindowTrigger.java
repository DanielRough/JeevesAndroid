package com.jeevesandroid.triggers.triggers.clock;

import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import com.jeevesandroid.triggers.ESTriggerManager;
import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.TriggerReceiver;
import com.jeevesandroid.triggers.config.TriggerConfig;
import com.jeevesandroid.triggers.config.TriggerManagerConstants;
import com.jeevesandroid.triggers.triggers.TriggerUtils;

import java.util.HashSet;

/**
 * Created by Daniel on 01/06/2016.
 */

//This is definitely like a trigger that fires off at specified intervals. A sort of cross between a RandomFrequencyTrigger and a SetTimesTrigger
public class JeevesWindowTrigger extends RandomFrequencyTrigger{

    private final ESTriggerManager triggerManager;

    private final static String TRIGGER_NAME = "JeevesWindowTrigger";
    private HashSet<Integer> randomlySelectedTriggerIds;
    private final DailyNotificationScheduler dailySchedulerAlarm;

    public JeevesWindowTrigger(Context context, int id, final TriggerReceiver listener, final TriggerConfig parameters) throws TriggerException
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
        //When this trigger is finished, we want to get rid of the specific 'one time triggers' that are occurring every day
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
    protected void startAlarm() {
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
            params.addParameter(TriggerConfig.FROM_DATE, millis);

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

/* **************************************************
 Copyright (c) 2012, University of Cambridge
 Neal Lathia, neal.lathia@cl.cam.ac.uk
 Kiran Rachuri, kiran.rachuri@cl.cam.ac.uk

This library was developed as part of the EPSRC Ubhave (Ubiquitous and
Social Computing for Positive Behaviour Change) Project. For more
information, please visit http://www.emotionsense.org

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ************************************************** */

package com.jeevesandroid.triggers.triggers.clock;

import android.app.AlarmManager;
import android.content.Context;
import android.util.Log;

import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.TriggerReceiver;
import com.jeevesandroid.triggers.config.TriggerConfig;
import com.jeevesandroid.triggers.config.TriggerManagerConstants;

import java.util.Calendar;

public class IntervalTrigger extends AbstractClockTrigger
{

    //My current concern is as follows: if the trigger interval falls outwith the early/late boundaries, it won't be fired. However, if the
    //early limit is 10.00 and a trigger tries to execute at 9.59, it could be a few hours after before it finally begins execution. If the
    //creator had the intention that it would ALWAYS be fired on 10am, 12pm, 2pm, etc, then we might need a notification scheduler to make
    //this happen reliably.
    private final static String TRIGGER_NAME = "IntervalTrigger";

    public IntervalTrigger(Context context, int id, final TriggerReceiver listener, final TriggerConfig parameters) throws TriggerException
    {
        super(context, id, listener, parameters);
    }

    @Override
    public String getActionName()
    {
        return TriggerManagerConstants.ACTION_NAME_INTERVAL_TRIGGER;
    }

    //DR trying to make request code unique so that we can distinguish different interval triggers
    @Override
    protected int getRequestCode()
    {
        //return TriggerUtils.TYPE_CLOCK_TRIGGER_ON_INTERVAL;
        return this.triggerId;
    }


    @Override
    protected void startAlarm() throws TriggerException
    {
        int firstTime = (int)getStartDelay();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, (firstTime / 60));
        calendar.set(Calendar.MINUTE, (firstTime % 60));
        calendar.set(Calendar.SECOND,0);
        //I've added this in the hope that it can schedule random triggers for the next day
//			if (calendar.getTimeInMillis() < System.currentTimeMillis())
//			{
//				calendar.add(Calendar.DATE, 1);
//			}
        long firstAlarmTime = calendar.getTimeInMillis();
       // long intervalLengthInMillis = getIntervalLength()*6000;
        Log.d("DATINTERVALLENGTH","interval lenght is " + getIntervalLength());
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstAlarmTime, getIntervalLength(), pendingIntent);
    }

    @Override
    protected String getTriggerTag()
    {
        return TRIGGER_NAME;
    }

    private long getStartDelay() {
        //I THINK TECHNICALLY THE START DELAY IS GOING TO BE THE 'FROM' TIME IN OUR INTERVAL TRIGGER. THIS IS WHEN IT FIRST STARTS
        //if (params.containsKey(TriggerConfig.INTERVAL_TRIGGER_START_DELAY))
        if (params.containsKey(TriggerConfig.LIMIT_BEFORE_HOUR))
        {
            if(params.getParameter(TriggerConfig.LIMIT_BEFORE_HOUR) instanceof Long)
                return (long)params.getParameter(TriggerConfig.LIMIT_BEFORE_HOUR);
            else
                return Long.valueOf(params.getParameter(TriggerConfig.LIMIT_BEFORE_HOUR).toString());
        }
        else
        {
            return 0;
            //throw new TriggerException(TriggerException.MISSING_PARAMETERS, "Parameters must include TriggerConfig.INTERVAL_TRIGGER_START_DELAY");
        }
    }

    //DR Added a bit of type safety
    private long getIntervalLength() throws TriggerException
    {
        if (params.containsKey(TriggerConfig.INTERVAL_TRIGGER_TIME))
        {
            if(params.getParameter(TriggerConfig.INTERVAL_TRIGGER_TIME) instanceof Long)
                return (long)params.getParameter(TriggerConfig.INTERVAL_TRIGGER_TIME);
            else
                return Long.valueOf(params.getParameter(TriggerConfig.INTERVAL_TRIGGER_TIME).toString());
        }
        else
        {
            throw new TriggerException(TriggerException.MISSING_PARAMETERS, "Parameters must include TriggerConfig.INTERVAL_TRIGGER_TIME_MILLIS");
        }

    }
}
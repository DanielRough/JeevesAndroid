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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.TriggerReceiver;
import com.jeevesandroid.triggers.config.TriggerConfig;
import com.jeevesandroid.triggers.config.TriggerConstants;
import com.jeevesandroid.triggers.triggers.Trigger;
import com.jeevesandroid.triggers.triggers.TriggerUtils;

import java.util.Calendar;


/**
 * Base class for time-based triggers. All other time triggers (IntervalTrigger,
 * SetTimesTrigger, WindowTrigger) create a number of these every day
 */
public class OneTimeTrigger extends Trigger
{
    private final static String TRIGGER_NAME = "OneTimeTrigger";

    public OneTimeTrigger(Context context, int id, TriggerReceiver listener,
                          TriggerConfig parameters) {
        super(context, id, listener, parameters);
    }

    @Override
    public String getActionName() {
        return TriggerConstants.ACTION_NAME_ONE_TIME_TRIGGER+"_"+triggerId;
    }

    protected int getRequestCode() {
        return TriggerUtils.TYPE_CLOCK_TRIGGER_ONCE;
    }

    @Override
    protected void startAlarm() throws TriggerException {
        long surveyDate = (Long) params.getParameter(TriggerConfig.FROM_DATE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(surveyDate);

        if (surveyDate > System.currentTimeMillis()) {
            Log.d("OneTimeTrigger", "Scheduled time: "+calendar.getTime().toString());
            //Need to have it as 'setExact' to make sure it is as accurate as possible
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,surveyDate, pendingIntent);
            }
            else{
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,surveyDate, pendingIntent);

            }
        }
        else {
           Log.e("Past yo", "Scheduled time is in the past: "+calendar.getTime().toString());
        }
    }

    @Override
    protected PendingIntent getPendingIntent() {
        Intent intent = new Intent(getActionName());
        intent.putExtra(TRIGGER_ID, triggerId);
        return PendingIntent.getBroadcast(context, getRequestCode(),
            intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    @Override
    protected String getTriggerTag() {
        return TRIGGER_NAME;
    }
}
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

package com.jeevesx.triggers.triggers.clock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.jeevesx.triggers.TriggerReceiver;
import com.jeevesx.triggers.config.TriggerConfig;
import com.jeevesx.triggers.config.TriggerConstants;
import com.jeevesx.triggers.triggers.Trigger;

/**
 * This trigger is only used to reschedule other triggers every 24 hours
 *
 */
public class DailyWakeupTrigger extends Trigger
{

    private final static String TRIGGER_NAME = "DailyWakeupTrigger";

    public DailyWakeupTrigger(Context context, int id, final TriggerReceiver listener,
                              final TriggerConfig parameters) {
        super(context, id, listener, parameters);
    }
    @Override
    protected PendingIntent getPendingIntent() {
        Intent intent = new Intent(getActionName());
        intent.putExtra(TRIGGER_ID, triggerId);
        return PendingIntent.getBroadcast(context, getRequestCode(),
            intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    @Override
    public String getActionName()
    {
        return TriggerConstants.ACTION_NAME_INTERVAL_TRIGGER;
    }

    protected int getRequestCode()
    {
        return this.triggerId;
    }

    /**
     * Sets this trigger to fire every day at midnight. (It used to be flexible but since its
     * only use is to wake up the notification scheduler it makes sense to just hard-wire
     * 24 hours into it)
     */
    @Override
    protected void startAlarm() {
        String time = params.getParameter(TriggerConfig.LIMIT_BEFORE_HOUR).toString();
        long firstTime = Long.parseLong(time);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
            firstTime, 1000 * 60 * 60 * 24, pendingIntent);
    }

    @Override
    protected String getTriggerTag() {
        return TRIGGER_NAME;
    }

}
package com.jeevesandroid.triggers.triggers.clock;

/**
 * Created by Daniel on 14/06/2015.4
 */
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

import android.app.PendingIntent;
import android.content.Context;

import com.jeevesandroid.triggers.ESTriggerManager;
import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.TriggerReceiver;
import com.jeevesandroid.triggers.config.TriggerConfig;
import com.jeevesandroid.triggers.triggers.Trigger;

import java.util.HashSet;

public class BeginTrigger extends Trigger
{

    private final static String TRIGGER_NAME = "BeginTrigger";
    private HashSet<Integer> randomlySelectedTriggerIds;

    public BeginTrigger(Context context, int id, final TriggerReceiver listener, final TriggerConfig parameters) throws TriggerException
    {
        super(context, id, listener, parameters);
        ESTriggerManager triggerManager = ESTriggerManager.getTriggerManager(context);

    }

    @Override
    public void start() {
        if (!isRunning)
        {
            isRunning = true;
            sendNotification();
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


}

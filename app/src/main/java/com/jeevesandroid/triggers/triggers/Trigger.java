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

package com.jeevesandroid.triggers.triggers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.TriggerReceiver;
import com.jeevesandroid.triggers.config.TriggerConfig;
import com.jeevesandroid.triggers.config.TriggerConstants;

import java.util.Calendar;

public abstract class Trigger extends BroadcastReceiver
{
	protected final static String TRIGGER_ID =
		"com.jeevesandroid.triggermanager.triggers.TRIGGER_ID";
	
	protected final AlarmManager alarmManager;
	protected final PendingIntent pendingIntent;

	protected final int triggerId;
	protected final Context context;
	protected final TriggerReceiver listener;

	protected TriggerConfig params;
	protected boolean isRunning;

	public Trigger(Context context, int id, TriggerReceiver listener, TriggerConfig params)
	{
		this.context = context;
		this.triggerId = id;
		this.listener = listener;
		this.params = params;
		this.isRunning = false;

		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		pendingIntent = getPendingIntent();
	}

	protected abstract void startAlarm() throws TriggerException;

	protected abstract PendingIntent getPendingIntent();

	protected abstract String getTriggerTag();
	
	protected abstract String getActionName();

	protected void sendNotification() {
		listener.onNotificationTriggered(triggerId);
	}

	public void stop() throws TriggerException
	{
		if (isRunning)
		{
			if(pendingIntent != null) {
				alarmManager.cancel(pendingIntent);
			}
			try {
				context.unregisterReceiver(this);
			}
			catch(Exception e){
			}
			isRunning = false;
		}
	}

	public void start() throws TriggerException
	{
		if (!isRunning)
		{
			IntentFilter intentFilter = new IntentFilter(getActionName());
			context.registerReceiver(this, intentFilter);	
			startAlarm();
			isRunning = true;
		}
	}

	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		if (listener != null)
		{
			int id = intent.getIntExtra(TRIGGER_ID, -1);
			if (triggerId == id)
			{
				sendNotification();
			}
		}
	}


}

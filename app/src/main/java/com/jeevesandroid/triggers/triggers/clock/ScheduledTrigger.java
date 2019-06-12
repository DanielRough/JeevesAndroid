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

import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import com.jeevesandroid.triggers.ESTriggerManager;
import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.TriggerReceiver;
import com.jeevesandroid.triggers.config.TriggerConfig;
import com.jeevesandroid.triggers.config.TriggerConstants;
import com.jeevesandroid.triggers.triggers.Trigger;
import com.jeevesandroid.triggers.triggers.TriggerUtils;

import java.util.HashSet;

public class ScheduledTrigger extends Trigger implements TriggerReceiver
{
	private final static String LOG_TAG = "ScheduledTrigger";
	
	private final ESTriggerManager triggerManager;
	private final DailyNotificationScheduler dailySchedulerAlarm;
	private HashSet<Integer> randomlySelectedTriggerIds;

	public ScheduledTrigger(Context context, int id, TriggerReceiver listener,
							TriggerConfig params) throws TriggerException {
		super(context, id, listener, params);
		this.triggerManager = ESTriggerManager.getTriggerManager(context);

		this.dailySchedulerAlarm = new DailyNotificationScheduler(context, params, this);
		this.randomlySelectedTriggerIds = new HashSet<>();
	}

	@Override
	protected void startAlarm() {

	}

	public void subscribeTriggerFor(long millis) {
		try {
			TriggerConfig params = new TriggerConfig();
			params.addParameter(TriggerConfig.FROM_DATE, millis);
			int triggerId = triggerManager.addTrigger(TriggerUtils.TYPE_CLOCK_TRIGGER_ONCE, this, params);
			randomlySelectedTriggerIds.add(triggerId);
			Log.d(LOG_TAG, "Trigger subscribed: "+triggerId);
		}
		catch (TriggerException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void start() throws TriggerException {
		if (!isRunning) {
			dailySchedulerAlarm.start();
			isRunning = true;
		}
	}
	
	@Override
	public void stop() throws TriggerException {
		if (isRunning) {
			dailySchedulerAlarm.stop();
			for (Integer triggerId : this.randomlySelectedTriggerIds) {
				try {
					triggerManager.removeTrigger(triggerId);
				}
				catch (TriggerException e) {
					e.printStackTrace();
				}
			}
			isRunning = false;
		}
	}

	@Override
	public void onNotificationTriggered(int alarmId) {
		if (randomlySelectedTriggerIds.contains(alarmId)) {
			try {
				listener.onNotificationTriggered(this.triggerId);
				triggerManager.removeTrigger(alarmId);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected String getTriggerTag() {
		return LOG_TAG;
	}
	
	@Override
	public String getActionName() {
		return null; // Unused
	}
	
	@Override
	protected PendingIntent getPendingIntent() {
		return null; // Unused
	}

}

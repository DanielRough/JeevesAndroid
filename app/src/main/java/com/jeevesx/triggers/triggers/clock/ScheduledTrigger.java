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

import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jeevesx.AppContext;
import com.jeevesx.triggers.ESTriggerManager;
import com.jeevesx.triggers.TriggerException;
import com.jeevesx.triggers.TriggerReceiver;
import com.jeevesx.triggers.config.TriggerConfig;
import com.jeevesx.triggers.triggers.Trigger;
import com.jeevesx.triggers.triggers.TriggerUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

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
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
			SharedPreferences.Editor prefsEditor = prefs.edit();
			//This reloads any existing set of triggerids we have
			HashSet<String> s = new HashSet<>( prefs.getStringSet(AppContext.TRIGGER_TIME_LIST,new HashSet()));
			ArrayList<String> triggerids = new ArrayList<>(s);

			int triggerId = triggerManager.addTrigger(TriggerUtils.TYPE_CLOCK_TRIGGER_ONCE, this, params);
			randomlySelectedTriggerIds.add(triggerId);

			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(millis);
			if (millis > System.currentTimeMillis()) {
				triggerids.add(triggerId + ";" + calendar.getTime().toString());
				Set<String> triggerset = new HashSet<>(triggerids);
				prefsEditor.putStringSet(AppContext.TRIGGER_TIME_LIST, triggerset);
				prefsEditor.commit();
			}
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
					removeScheduledTime(triggerId);
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
				removeScheduledTime(alarmId);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void removeScheduledTime(int alarmId){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
		SharedPreferences.Editor prefsEditor = prefs.edit();
		HashSet<String> s = new HashSet<>( prefs.getStringSet(AppContext.TRIGGER_TIME_LIST,new HashSet()));
		ArrayList<String> triggerids = new ArrayList<>(s);
		ArrayList<String> updated = new ArrayList<>();
		//Remove this trigger from the scheduled list
		for(String trig : triggerids){
			String[] idAndTime = trig.split(";");
			if(idAndTime[0].equals(Integer.toString(alarmId))){
				Log.d("REMOVE","Removing old trigger with tim e" + idAndTime[1]);
				continue;
			}

			updated.add(trig);
		}
		//Update the list in Shared preferences
		Set<String> triggerset = new HashSet<>(updated);
		prefsEditor.putStringSet(AppContext.TRIGGER_TIME_LIST,triggerset);
		prefsEditor.commit();
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

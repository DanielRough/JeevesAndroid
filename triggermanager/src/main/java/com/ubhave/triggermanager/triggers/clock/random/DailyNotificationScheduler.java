package com.ubhave.triggermanager.triggers.clock.random;

import android.content.Context;
import android.util.Log;

import com.ubhave.triggermanager.ESTriggerManager;
import com.ubhave.triggermanager.TriggerException;
import com.ubhave.triggermanager.TriggerReceiver;
import com.ubhave.triggermanager.config.TriggerConfig;
import com.ubhave.triggermanager.config.TriggerManagerConstants;
import com.ubhave.triggermanager.triggers.TriggerUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Random;

public class DailyNotificationScheduler implements TriggerReceiver
{
	private final static long DAILY_INTERVAL = 1000 * 60 * 60 * 24;
	public final static int ERROR = -1;
	
	private final ESTriggerManager triggerManager;
	private final RandomFrequencyTrigger trigger;
	
	private int dailySchedulerId;
	private boolean isSubscribed;
	private final Random random;
	private TriggerConfig params;
	
	public DailyNotificationScheduler(Context context, TriggerConfig params, RandomFrequencyTrigger trigger) throws TriggerException
	{
		random = new Random();
		random.setSeed(System.currentTimeMillis());
		
		this.triggerManager = ESTriggerManager.getTriggerManager(context);
		this.params = params;
		this.trigger = trigger;
		this.isSubscribed = false;
	}
	
	public void start() throws TriggerException {
		if (isSubscribed) {
			stop();
		}

		TriggerConfig params = new TriggerConfig();
		params.addParameter(TriggerConfig.DO_NOT_DISTURB_BEFORE_MINUTES, startDelay());
		params.addParameter(TriggerConfig.MIN_TRIGGER_INTERVAL_MINUTES, schedulerInterval());
		params.addParameter(TriggerConfig.IGNORE_USER_PREFERENCES, true);
		
		dailySchedulerId = triggerManager.addTrigger(TriggerUtils.TYPE_CLOCK_TRIGGER_ON_INTERVAL, this, params);
		Log.i("SCHEUDLER ID","Scheduler ID is " + dailySchedulerId);
		isSubscribed = true;
	}

	private void scheduleStuff(){
		long fromDay = 0;
		long toDay = 0;
		Log.d("TERIGGER TYPE","Trigger type is " + trigger);
		if (params.containsKey(TriggerConfig.FROM_DATE))
			fromDay = new Long(params.getParameter(TriggerConfig.FROM_DATE).toString());
		if (params.containsKey(TriggerConfig.TO_DATE))
			toDay = new Long(params.getParameter(TriggerConfig.TO_DATE).toString());
		long daysSinceEpoch = System.currentTimeMillis() / (24 * 3600 * 1000);
		if (daysSinceEpoch < fromDay || daysSinceEpoch > toDay){
			Log.i("TOO EARLY OR TOO LATE", "to day is " + toDay + " and from day is " + fromDay + " and this very day is " + daysSinceEpoch);
			return;
		}
		else
			Log.i("THIS IS FINE","This is fine because the TO is " + toDay + " and the FROM is " + fromDay + " and this very day is " + daysSinceEpoch);
		if(trigger instanceof SetTimesTrigger)
			scheduleSetTimes(); //Schedules for the set times trigger
		else if(trigger instanceof JeevesIntervalTrigger)
			scheduleIntervalTimes();
		else
			scheduleNotifications(); //Schedules for the random trigger
	}

	//This means that the scheduler does its thing EVERY DAY
	private long schedulerInterval()
	{
	//	if (params.containsKey(TriggerConfig.INTERVAL_TIME_MILLIS))
//		{
//			return (Long) params.getParameter(TriggerConfig.INTERVAL_TIME_MILLIS);
//		}
//		else
//		{
			return DAILY_INTERVAL;
//		}
	}


	//When should we actually start the scheduling? If we have a FROM time, its then. Otherwise, we start afresh on midnight of the next day.
	private long startDelay()
	{
		if (params.containsKey(TriggerConfig.DO_NOT_DISTURB_BEFORE_MINUTES))
		{
			return (Long) params.getParameter(TriggerConfig.DO_NOT_DISTURB_BEFORE_MINUTES);
		}
		else
		{
			// Milliseconds until midnight
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR_OF_DAY, 24 - calendar.get(Calendar.HOUR_OF_DAY));
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			
			return calendar.getTimeInMillis() - System.currentTimeMillis();
		}
	}
	
	public void stop() throws TriggerException
	{
		if (isSubscribed)
		{
			Log.d("Daily", "stop()");
			triggerManager.removeTrigger(dailySchedulerId);
			isSubscribed = false;
		}
	}

	//Actually DailyNotificationScheduler is a TriggerReceiver. I think it works by having a trigger that fires off every day, causing this to reset its schedule
	@Override
	public void onNotificationTriggered(int triggerId)
	{
		if (triggerId == dailySchedulerId)
		{
			scheduleStuff();
		}
	}

	private void scheduleIntervalTimes(){
		//I THINK TECHNICALLY THE START DELAY IS GOING TO BE THE 'FROM' TIME IN OUR INTERVAL TRIGGER. THIS IS WHEN IT FIRST STARTS
		//if (params.containsKey(TriggerConfig.INTERVAL_TRIGGER_START_DELAY))

		long startTime = 0;

		if (params.containsKey(TriggerConfig.DO_NOT_DISTURB_BEFORE_MINUTES))
			startTime = new Long(params.getParameter(TriggerConfig.DO_NOT_DISTURB_BEFORE_MINUTES).toString());
		long endTime = 0;
		if (params.containsKey(TriggerConfig.DO_NOT_DISTURB_AFTER_MINUTES))
			endTime = new Long(params.getParameter(TriggerConfig.DO_NOT_DISTURB_AFTER_MINUTES).toString());
		long intervalTime = 0;
		if (params.containsKey(TriggerConfig.MIN_TRIGGER_INTERVAL_MINUTES))
			intervalTime =  new Long(params.getParameter(TriggerConfig.MIN_TRIGGER_INTERVAL_MINUTES).toString());
		ArrayList<Integer> times = new ArrayList<Integer>();

		long realEndTime = endTime;

		//So if our startTime is 10pm and our end time is 6pm, our endtime becomes 6pm on the NEXT DAY
		if(startTime > endTime)
			endTime = endTime + 1440; //Add a new day onto things
		while(startTime < endTime){
			if(startTime < 1440)
				times.add((int)startTime); //Convert each JSONObject time into a minute-of-day value
			else
				times.add((int)(startTime-1440)); //Accounts for next-day times
			startTime += intervalTime;
			Log.d("STRTTIME: ","Start time is " + startTime + " and endTime is " + endTime + " and interval: " + intervalTime);
		}
		Calendar calendar = Calendar.getInstance();

		if(times.size()>0)
		for (Integer minuteOfDay : times) {
			calendar.set(Calendar.HOUR_OF_DAY, (minuteOfDay / 60));
			calendar.set(Calendar.MINUTE, (minuteOfDay % 60));
			calendar.set(Calendar.SECOND,0);
			//I've added this in the hope that it can schedule random triggers for the next day
			if (calendar.getTimeInMillis() < System.currentTimeMillis())
			{
				calendar.add(Calendar.DATE, 1);
			}
			trigger.subscribeTriggerFor(calendar.getTimeInMillis());
		}
	}
	private void scheduleSetTimes(){
		ArrayList<Integer> times = new ArrayList<Integer>();
		//Map<String,Object> settimes = (Map<String,Object>)params.getParameter(TriggerConfig.DAILY_TIMES);
		Iterator<String> iter = params.getParams().keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			if(key.startsWith("time")) {//The keys are called time0, time1, time2 and so on
				int minuteofday = Integer.parseInt(params.getParams().get(key).toString());
//				String[] hoursmins = timeStr.split(":");
//				int dailyhour = Integer.parseInt(hoursmins[0]);
//				int dailyminute = Integer.parseInt(hoursmins[1]);
//				int minuteofday = dailyhour*60 + dailyminute;
				times.add(minuteofday); //Convert each JSONObject time into a minute-of-day value
			}
		}
		Calendar calendar = Calendar.getInstance();
		for (Integer minuteOfDay : times) {
			calendar.set(Calendar.HOUR_OF_DAY, (minuteOfDay / 60));
			calendar.set(Calendar.MINUTE, (minuteOfDay % 60));
			calendar.set(Calendar.SECOND,0);
			//I've added this in the hope that it can schedule random triggers for the next day
			if (calendar.getTimeInMillis() < System.currentTimeMillis())
			{
				calendar.add(Calendar.DATE, 1);
			}
			trigger.subscribeTriggerFor(calendar.getTimeInMillis());
		}
	}
	private void scheduleNotifications()
	{
		ArrayList<Integer> times = new ArrayList<Integer>();
		int earlyLimit = params.getValueInMinutes(TriggerConfig.DO_NOT_DISTURB_BEFORE_MINUTES);
		int lateLimit = params.getValueInMinutes(TriggerConfig.DO_NOT_DISTURB_AFTER_MINUTES);
		int minInterval = params.getValueInMinutes(TriggerConfig.MIN_TRIGGER_INTERVAL_MINUTES);
		int timeFrame = lateLimit - earlyLimit;
		int numberOfNotifications = timeFrame / minInterval; //The max notifications we can schedule in this space
	//	Log.d("Daily", "scheduleNotifications(), "+numberOfNotifications);
		if (TriggerManagerConstants.LOG_MESSAGES)
		{
			Log.d("Daily Scheduler", "Attempting to schedule: "+numberOfNotifications);
		}

		//This isn't QUITE that simple. What we'll need to do is somehow manually schedule a time within each 'window'
		//I know what I mean and that's not the best way of phrasing it...
//		for (int t=0; t<numberOfNotifications; t++)
//		{
//			boolean entryAdded = false;
//			for (int i=0; i<MAX_SCHEDULING_ATTEMPTS && !entryAdded; i++)
//			{

		int windowEarlyLimit = earlyLimit;
		int windowLateLimit = earlyLimit + minInterval;
		while (windowLateLimit < lateLimit) {
			int time = pickRandomTimeWithinPreferences(windowEarlyLimit, windowLateLimit);
			times.add(time);
			//	if (selectedTimeFitsGroup(time, times, minInterval)) {
			//	for (int j = 0; j < times.size(); j++) {
			//		if (times.get(j) > time) {
			//	times.add(j, time);
//							entryAdded = true;
			//			break;
			//		}
			//}
//					if (!entryAdded)
//					{
//						times.add(time);
//						entryAdded = true;
//					}
			//	}
			windowEarlyLimit = windowLateLimit;
			windowLateLimit += minInterval;
			//	}
		}
//			}
	//	}
		
		if (TriggerManagerConstants.LOG_MESSAGES)
		{
			Log.d("Daily Scheduler", "Selected: "+times.size());
		}
		
		Calendar calendar = Calendar.getInstance();
		for (Integer minuteOfDay : times)
		{
			calendar.set(Calendar.HOUR_OF_DAY, (minuteOfDay / 60));
			calendar.set(Calendar.MINUTE, (minuteOfDay % 60));
			//I've added this in the hope that it can schedule random triggers for the next day
			if (calendar.getTimeInMillis() < System.currentTimeMillis())
		{
			calendar.add(Calendar.DATE, 1);
		}
			trigger.subscribeTriggerFor(calendar.getTimeInMillis());
		}
	}
	
	private int pickRandomTimeWithinPreferences(int earlyLimit, int lateLimit)
	{
	//	int from = max(earlyLimit, currentMinute+1);
		if (lateLimit - earlyLimit > 0)
		{
			return random.nextInt(lateLimit - earlyLimit) + earlyLimit;
		}
		else
		{
			return DailyNotificationScheduler.ERROR;
		}
	}
	
	private boolean selectedTimeFitsGroup(int selectedTime, ArrayList<Integer> times, int minInterval)
	{
		if (selectedTime == ERROR)
		{
			return false;
		}
		else
		{
			for (Integer time : times)
			{
				if (Math.abs(time.intValue() - selectedTime) <= minInterval)
				{
					return false;
				}
			}
			return true;
		}
	}
	
	private int max(int a, int b)
	{
		if (a > b)
		{
			return a;
		}
		else return b;
	}
	
	private int currentMinute()
	{
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);	
		return (60 * hour) + minute;
	}
}

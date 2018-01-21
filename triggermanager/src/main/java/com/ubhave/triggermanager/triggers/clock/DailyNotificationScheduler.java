package com.ubhave.triggermanager.triggers.clock;

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
import java.util.Random;

public class DailyNotificationScheduler implements TriggerReceiver
{
	private final static long DAILY_INTERVAL = 1000 * 60 * 60 * 24;
	public final static int ERROR = -1;
	private final static int MAX_SCHEDULING_ATTEMPTS = 1000;

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
		params.addParameter(TriggerConfig.LIMIT_BEFORE_HOUR, startDelay());
		params.addParameter(TriggerConfig.INTERVAL_TRIGGER_TIME, schedulerInterval());
		params.addParameter(TriggerConfig.IGNORE_USER_PREFERENCES, true);

		dailySchedulerId = triggerManager.addTrigger(TriggerUtils.TYPE_CLOCK_TRIGGER_ON_INTERVAL, this, params);
		Log.i("SCHEUDLER ID","Scheduler ID is " + dailySchedulerId);
		isSubscribed = true;
	}

	private void scheduleStuff(){
		long fromDay = 0;
		long toDay = 0;

		Log.d("TERIGGER TYPE","Trigger type is " + trigger);

		if (params.containsKey(TriggerConfig.FROM_DATE)) {
			//if(params.getParameter(TriggerConfig.FROM_DATE) instanceof Long)
				fromDay = new Long(params.getParameter(TriggerConfig.FROM_DATE).toString()) / (1000 * 60 * 60 * 24);
		}
		if (params.containsKey(TriggerConfig.TO_DATE)) {
		//	if(params.getParameter(TriggerConfig.TO_DATE) instanceof Long)
				toDay = new Long(params.getParameter(TriggerConfig.TO_DATE).toString()) / (1000 * 60 * 60 * 24);
		}
		long daysSinceEpoch = System.currentTimeMillis() / (24 * 3600 * 1000);

		//if fromDay and toDay are both 0, then we have no date constraints and this is triggered every day
		if ((fromDay != 0 || toDay != 0) && (daysSinceEpoch < fromDay || daysSinceEpoch > toDay)){
			Log.i("TOO EARLY OR TOO LATE", "to day is " + toDay + " and from day is " + fromDay + " and this very day is " + daysSinceEpoch);
			try {
				triggerManager.removeTrigger(dailySchedulerId);
			} catch (TriggerException e) {
				e.printStackTrace();
			}
			TriggerConfig params = new TriggerConfig();
			params.addParameter(TriggerConfig.LIMIT_BEFORE_HOUR, 0);
			params.addParameter(TriggerConfig.INTERVAL_TRIGGER_TIME, schedulerInterval());
			params.addParameter(TriggerConfig.IGNORE_USER_PREFERENCES, true);

			try {
				dailySchedulerId = triggerManager.addTrigger(TriggerUtils.TYPE_CLOCK_TRIGGER_ON_INTERVAL, this, params);
				Log.d("Gonna rescheule","For midnight");
			} catch (TriggerException e) {
				e.printStackTrace();
			}
			Log.i("SCHEUDLER ID","Scheduler ID is " + dailySchedulerId);

			return;
		}

		else
			Log.i("THIS IS FINE","This is fine because the TO is " + toDay + " and the FROM is " + fromDay + " and this very day is " + daysSinceEpoch);
		if(trigger instanceof SetTimesTrigger)
			scheduleSetTimes(); //Schedules for the set times trigger
		String fixedRandom = "";
		if(params.containsKey(TriggerConfig.FIXED_RANDOM)){
			fixedRandom = params.getParameter(TriggerConfig.FIXED_RANDOM).toString();
			if(fixedRandom.equals("fixed"))
				scheduleIntervalTimes();
			else
				scheduleNotifications();
		}
//		else if(trigger instanceof JeevesIntervalTrigger)
//			scheduleIntervalTimes();

		else //it's a Jeeves interval trigger!
			scheduleNotifications(); //Schedules for the random trigger
	}

	//This means that the scheduler does its thing EVERY DAY
	private long schedulerInterval()
	{
		return DAILY_INTERVAL;
	}


	//When should we actually start the scheduling? If we have a FROM time, its then. Otherwise, we start afresh on midnight of the next day.
	private long startDelay()
	{
		Calendar calendar = Calendar.getInstance();
		Calendar midnight = Calendar.getInstance();
		midnight.set(Calendar.HOUR_OF_DAY,0);
		midnight.set(Calendar.MINUTE,0);
		midnight.set(Calendar.SECOND,0);
		long startDelay = (calendar.getTimeInMillis()-midnight.getTimeInMillis())/(1000*60);

//		Log.d("Start delay", "My current starting time is " + startDelay);
		return startDelay; //This ought to be the number of minutes since midnight. i.e.: NOW
//		}
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

		long startTime = 0;
		if (params.containsKey(TriggerConfig.LIMIT_BEFORE_HOUR))
			startTime = new Long(params.getParameter(TriggerConfig.LIMIT_BEFORE_HOUR).toString())/60000;
		long endTime = 0;
		if (params.containsKey(TriggerConfig.LIMIT_AFTER_HOUR))
			endTime = new Long(params.getParameter(TriggerConfig.LIMIT_AFTER_HOUR).toString())/60000;
		long intervalTime = 0;
//		if (params.containsKey(TriggerConfig.INTERVAL_TRIGGER_TIME))
//			intervalTime =  new Long(params.getParameter(TriggerConfig.INTERVAL_TRIGGER_TIME).toString());
//		//String granularity = "";
//	//	if (params.containsKey(TriggerConfig.INTERVAL_TRIGGER_TIME)) {
			int numberOfNotifications = Integer.parseInt(params.getParameter(TriggerConfig.INTERVAL_TRIGGER_TIME).toString());
			//granularity = params.getParameter(TriggerConfig.GRANULARITY).toString();
			//if(granularity.equals("hours"))
		//		intervalTime *= 60;
			long totalTime = endTime - startTime;
			long windowLength = totalTime/numberOfNotifications;
	//	}
		ArrayList<Integer> times = new ArrayList<Integer>();

//		long realEndTime = endTime;

		//So if our startTime is 10pm and our end time is 6pm, our endtime becomes 6pm on the NEXT DAY
		if(startTime > endTime)
			endTime = endTime + 1440; //Add a new day onto things
		while(startTime < endTime){
			if(startTime < 1440)
				times.add((int)startTime); //Convert each JSONObject time into a minute-of-day value
			else
				times.add((int)(startTime-1440)); //Accounts for next-day times
			startTime += windowLength;
			Log.d("STRTTIME: ","Start time is " + startTime + " and endTime is " + endTime + " and interval: " + windowLength);
		}
		Calendar calendar = Calendar.getInstance();

		if(times.size()>0)
			for (Integer minuteOfDay : times) {
				calendar.set(Calendar.HOUR_OF_DAY, (minuteOfDay / 60));
				calendar.set(Calendar.MINUTE, (minuteOfDay % 60));
				calendar.set(Calendar.SECOND,0);
//				//I've added this in the hope that it can schedule random triggers for the next day
//				if (calendar.getTimeInMillis() < System.currentTimeMillis())
//				{
//					calendar.add(Calendar.DATE, 1);
//				}
				trigger.subscribeTriggerFor(calendar.getTimeInMillis());
			}
	}
	private void scheduleSetTimes(){
		ArrayList<Long> times = new ArrayList<Long>();
		times = (ArrayList<Long>)params.getParams().get("times");

		Calendar calendar = Calendar.getInstance();
		if(times == null)return;
		for (Long minuteOfDay : times) {
			minuteOfDay = minuteOfDay / 60000;
			calendar.set(Calendar.HOUR_OF_DAY, (int)(minuteOfDay / 60));
			calendar.set(Calendar.MINUTE, (int)(minuteOfDay % 60));
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
		int currentMinute = currentMinute();

		int earlyLimit = params.getValueInMinutes(TriggerConfig.LIMIT_BEFORE_HOUR)/60000;
		int lateLimit = params.getValueInMinutes(TriggerConfig.LIMIT_AFTER_HOUR)/60000;
		int numberOfNotifications = Integer.parseInt(params.getParameter(TriggerConfig.INTERVAL_TRIGGER_TIME).toString());

		//TODO: Change this so that the researcher can specify the minimum difference
		int minInterval = 1;
		//int minInterval = params.getValueInMinutes(TriggerConfig.INTERVAL_WINDOW);
//		if (params.containsKey(TriggerConfig.GRANULARITY)) {
//			String granularity = params.getParameter(TriggerConfig.GRANULARITY).toString();
//			if(granularity.equals("hours"))
//				minInterval *= 60;
//		}
        if(earlyLimit > lateLimit){
            lateLimit += 1440; //Add an extra day onto the late limit so we can schedule shit overnight
        }
//		int timeFrame = lateLimit - earlyLimit;
//		int numberOfNotifications = timeFrame / minInterval; //The max notifications we can schedule in this space
		//	Log.d("Daily", "scheduleNotifications(), "+numberOfNotifications);
//		if (TriggerManagerConstants.LOG_MESSAGES)
//		{
//			Log.d("Daily Scheduler", "Attempting to schedule: "+numberOfNotifications);
//		}
//
//		int windowEarlyLimit = earlyLimit;
//		int windowLateLimit = earlyLimit + minInterval;
//		while (windowLateLimit < lateLimit) {
//			int time = pickRandomTimeWithinPreferences(windowEarlyLimit, windowLateLimit);
//		//	times.add(time);
//			windowEarlyLimit = windowLateLimit;
//            if(windowLateLimit < 1440)
//                times.add((int)time); //Convert each JSONObject time into a minute-of-day value
//            else
//                times.add((int)(time-1440)); //Accounts for next-day times
//			windowLateLimit += minInterval;
//		}

		for (int t=0; t<numberOfNotifications; t++)
		{
			boolean entryAdded = false;
			for (int i=0; i<MAX_SCHEDULING_ATTEMPTS && !entryAdded; i++)
			{
				int time = pickRandomTimeWithinPreferences(currentMinute, earlyLimit, lateLimit);
				if (selectedTimeFitsGroup(time, times, minInterval))
				{
					for (int j=0; j<times.size(); j++)
					{
						if (times.get(j) > time)
						{
							times.add(j, time);
							entryAdded = true;
							break;
						}
					}
					if (!entryAdded)
					{
						times.add(time);
						entryAdded = true;
					}
				}
			}
		}
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

	private int pickRandomTimeWithinPreferences(int currentMinute, int earlyLimit, int lateLimit)
	{
			int from = max(earlyLimit, currentMinute+1);
		if (lateLimit - from > 0)
		{
			return random.nextInt(lateLimit - from) + from;
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

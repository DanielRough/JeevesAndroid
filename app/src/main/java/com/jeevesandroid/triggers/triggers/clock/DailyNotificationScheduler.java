package com.jeevesandroid.triggers.triggers.clock;

import android.content.Context;
import android.util.Log;

import com.jeevesandroid.triggers.ESTriggerManager;
import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.TriggerReceiver;
import com.jeevesandroid.triggers.config.TriggerConfig;
import com.jeevesandroid.triggers.triggers.TriggerUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * Scheduler for a specific trigger (each trigger gets its own DailyNotificationScheduler)
 */
public class DailyNotificationScheduler implements TriggerReceiver
{
	private final static long DAILY_INTERVAL = 1000 * 60 * 60 * 24;
	private final static int ERROR = -1;
	private final static int MAX_SCHEDULING_ATTEMPTS = 1000;

	private final ESTriggerManager triggerManager;
	private final ScheduledTrigger trigger;

	private int dailySchedulerId;
	private boolean isSubscribed;
	private final Random random;
	private TriggerConfig params;

	public DailyNotificationScheduler(Context context, TriggerConfig params, ScheduledTrigger trigger)
		throws TriggerException {
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

		//Get the time until midnight and use this as the start delay for
        //the daily wakeup trigger
		TriggerConfig params = new TriggerConfig();
		Calendar calendar = Calendar.getInstance();
		Calendar midnight = Calendar.getInstance();
		midnight.set(Calendar.HOUR_OF_DAY,0);
		midnight.set(Calendar.MINUTE,0);
		midnight.set(Calendar.SECOND,0);
		long startDelay = (calendar.getTimeInMillis()-midnight.getTimeInMillis())/(1000*60);
		params.addParameter(TriggerConfig.LIMIT_BEFORE_HOUR, startDelay);
		params.addParameter(TriggerConfig.INTERVAL_TRIGGER_TIME,DAILY_INTERVAL);

		//Creates a simple scheduler trigger that wakes up every day and
		//schedules the day's triggers
		dailySchedulerId = triggerManager.addTrigger(
			TriggerUtils.TYPE_DAILY_SCHEDULER, this, params);
		isSubscribed = true;
	}

	private void scheduleStuff(){
		long fromDay = 0;
		long toDay = 0;
		if (params.containsKey(TriggerConfig.FROM_DATE)) {
			fromDay = Long.valueOf(params.getParameter(TriggerConfig.FROM_DATE).toString()) /
				(1000 * 60 * 60 * 24);
		}
		if (params.containsKey(TriggerConfig.TO_DATE)) {
			toDay = Long.valueOf(params.getParameter(TriggerConfig.TO_DATE).toString()) /
				(1000 * 60 * 60 * 24);
		}
		long daysSinceEpoch = System.currentTimeMillis() / (24 * 3600 * 1000);


		if ((fromDay != 0 || toDay != 0) && (daysSinceEpoch < fromDay || daysSinceEpoch > toDay)) {
			try {
				triggerManager.removeTrigger(dailySchedulerId);
				return;
			} catch (TriggerException e) {
				e.printStackTrace();
			}
		}
		/*
			TriggerConfig params = new TriggerConfig();
			params.addParameter(TriggerConfig.LIMIT_BEFORE_HOUR, startDelay());
			params.addParameter(TriggerConfig.INTERVAL_TRIGGER_TIME,DAILY_INTERVAL);
			params.addParameter(TriggerConfig.IGNORE_USER_PREFERENCES, true);

			try {
				dailySchedulerId = triggerManager.addTrigger(TriggerUtils.TYPE_DAILY_SCHEDULER,
					this, params);
			} catch (TriggerException e) {
				e.printStackTrace();
			}
			return;
		}
		*/
		if(trigger instanceof SetTimesTrigger) {
			scheduleSetTimes(); //Schedules for the set times trigger
		}
		String fixedRandom = "";
		if(params.containsKey(TriggerConfig.FIXED_RANDOM)){
			fixedRandom = params.getParameter(TriggerConfig.FIXED_RANDOM).toString();
			if(fixedRandom.equals("fixed")) {
                scheduleIntervalTimes();
            }
			else {
                scheduleNotifications();
            }
		}
		else if(trigger instanceof WindowTrigger) {
			scheduleWindowedTimes();
		}
	}

	public void stop() throws TriggerException
	{
		if (isSubscribed) {
			triggerManager.removeTrigger(dailySchedulerId);
			isSubscribed = false;
		}
	}

	@Override
	public void onNotificationTriggered(int triggerId) {
		if (triggerId == dailySchedulerId) {
			scheduleStuff();
		}
	}

    /**
     * If this method is called, we have a Window Trigger. This method schedules the
     * windowed times.
     */
	private void scheduleWindowedTimes(){
		int earlyLimit = params.getValueInMinutes(TriggerConfig.LIMIT_BEFORE_HOUR)/60000;
		int lateLimit = params.getValueInMinutes(TriggerConfig.LIMIT_AFTER_HOUR)/60000;
		int minInterval = params.getValueInMinutes(TriggerConfig.INTERVAL_TRIGGER_TIME);
		int windowSize = params.getValueInMinutes(TriggerConfig.INTERVAL_TRIGGER_WINDOW);
		ArrayList<Integer> times = new ArrayList<Integer>();

		if(earlyLimit > lateLimit){
			lateLimit += 1440; //Add an extra day onto the late limit so we can schedule overnight
		}

		while(earlyLimit < lateLimit){
			int earlyWinTime = earlyLimit - windowSize;
			int lateWinTime = earlyLimit + windowSize;
			int winTime = random.nextInt(lateWinTime-earlyWinTime)+earlyWinTime;
			if(winTime < 1440)
				times.add(winTime); //Convert each JSONObject time into a minute-of-day value
			else
				times.add((winTime-1440)); //Accounts for next-day times
			earlyLimit += minInterval;
		}
		Calendar calendar = Calendar.getInstance();

		if(times.size()>0) {
            for (Integer minuteOfDay : times) {
                calendar.set(Calendar.HOUR_OF_DAY, (minuteOfDay / 60));
                calendar.set(Calendar.MINUTE, (minuteOfDay % 60));
                calendar.set(Calendar.SECOND, 0);
                trigger.subscribeTriggerFor(calendar.getTimeInMillis());
            }
        }
	}

    /**
     * If this is called, we have a simple trigger that fires at regularly spaced intervals.
     * Scheduling these is quite straightforward
     */
	private void scheduleIntervalTimes(){

        int earlyLimit = params.getValueInMinutes(TriggerConfig.LIMIT_BEFORE_HOUR)/60000;
        int lateLimit = params.getValueInMinutes(TriggerConfig.LIMIT_AFTER_HOUR)/60000;
		int numberOfNotifications = Integer.parseInt(
			params.getParameter(TriggerConfig.INTERVAL_TRIGGER_TIME).toString());
		long totalTime = lateLimit - earlyLimit;
		long windowLength = totalTime/numberOfNotifications;
		ArrayList<Integer> times = new ArrayList<Integer>();

		//if our startTime is 10pm and our end time is 6pm, our endtime becomes 6pm on the NEXT DAY
		if(earlyLimit > lateLimit)
			lateLimit = lateLimit + 1440; //Add a new day onto things
		while(earlyLimit < lateLimit){
			if(earlyLimit < 1440)
				times.add(earlyLimit); //Convert each JSONObject time into a minute-of-day value
			else
				times.add(earlyLimit-1440); //Accounts for next-day times
            earlyLimit += windowLength;
		}
		Calendar calendar = Calendar.getInstance();

		if(times.size()>0) {
			for (Integer minuteOfDay : times) {
				calendar.set(Calendar.HOUR_OF_DAY, (minuteOfDay / 60));
				calendar.set(Calendar.MINUTE, (minuteOfDay % 60));
				calendar.set(Calendar.SECOND, 0);
				trigger.subscribeTriggerFor(calendar.getTimeInMillis());
			}
		}
	}

    /**
     * If this is called, we have a Set Times trigger and just need to fire it
     * at all the specified times
     */
	private void scheduleSetTimes(){
		ArrayList<Long> times = (ArrayList<Long>)params.getParams().get("times");

		Calendar calendar = Calendar.getInstance();
		if(times == null)return;
		for (Long minuteOfDay : times) {
			minuteOfDay = minuteOfDay / 60000;
			calendar.set(Calendar.HOUR_OF_DAY, (int)(minuteOfDay / 60));
			calendar.set(Calendar.MINUTE, (int)(minuteOfDay % 60));
			calendar.set(Calendar.SECOND,0);
			if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
				calendar.add(Calendar.DATE, 1);
			}
			trigger.subscribeTriggerFor(calendar.getTimeInMillis());
		}
	}

    /**
     * If this is called, we have an interval trigger but designed to schedule
     * the triggers randomly within this window.
     */
	private void scheduleNotifications() {
		ArrayList<Integer> times = new ArrayList<Integer>();
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int currentMinute = (60 * hour) + minute;

		int earlyLimit = params.getValueInMinutes(TriggerConfig.LIMIT_BEFORE_HOUR)/60000;
		int lateLimit = params.getValueInMinutes(TriggerConfig.LIMIT_AFTER_HOUR)/60000;
		int numberOfNotifications = Integer.parseInt(
			params.getParameter(TriggerConfig.INTERVAL_TRIGGER_TIME).toString());

		//TODO: Change this so that the researcher can specify the minimum difference
		int minInterval = 1;
        if(earlyLimit > lateLimit){
            lateLimit += 1440; //Add an extra day onto the late limit so we can schedule shit overnight
        }
		for (int t=0; t<numberOfNotifications; t++) {
			boolean entryAdded = false;
			for (int i=0; i<MAX_SCHEDULING_ATTEMPTS && !entryAdded; i++) {
				int time = pickRandomTimeWithinPreferences(currentMinute, earlyLimit, lateLimit);
				if (selectedTimeFitsGroup(time, times, minInterval)) {
					for (int j=0; j<times.size(); j++) {
						if (times.get(j) > time) {
							times.add(j, time);
							entryAdded = true;
							break;
						}
					}
					if (!entryAdded) {
						times.add(time);
						entryAdded = true;
					}
				}
			}
		}
		for (Integer minuteOfDay : times) {
			calendar.set(Calendar.HOUR_OF_DAY, (minuteOfDay / 60));
			calendar.set(Calendar.MINUTE, (minuteOfDay % 60));
			if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
				calendar.add(Calendar.DATE, 1);
			}
			trigger.subscribeTriggerFor(calendar.getTimeInMillis());
		}
	}

    /**
     * Picks a random time between an early and late limit, or between the current time and late
     * limit if we are already between the early and late limits
     * @param currentMinute Integer current minute of the day
     * @param earlyLimit Integer early minute bound of scheduling
     * @param lateLimit Integer late minute bound of scheduling
     * @return Integer randomly chosen minute
     */
	private int pickRandomTimeWithinPreferences(int currentMinute, int earlyLimit, int lateLimit) {
		int from = earlyLimit > (currentMinute+1) ? earlyLimit : currentMinute+1;
		if (lateLimit - from > 0) {
			return random.nextInt(lateLimit - from) + from;
		}
		else {
			return DailyNotificationScheduler.ERROR;
		}
	}

    /**
     * Checks whether a randomly scheduled time fits with the other randomly scheduled times
     * within a given interval
     * @param selectedTime Integer time to test
     * @param times List of already scheduled times
     * @param minInterval Integer minimum duration between two times
     * @return Whether this time fits with the already scheduled times
     */
	private boolean selectedTimeFitsGroup(int selectedTime, ArrayList<Integer> times, int minInterval) {
		if (selectedTime == ERROR) {
			return false;
		}
		else {
			for (Integer time : times) {
				if (Math.abs(time.intValue() - selectedTime) <= minInterval) {
					return false;
				}
			}
			return true;
		}
	}

}

package com.jeevesandroid.triggers.triggers.clock;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jeevesandroid.AppContext;
import com.jeevesandroid.triggers.ESTriggerManager;
import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.TriggerReceiver;
import com.jeevesandroid.triggers.config.TriggerConfig;
import com.jeevesandroid.triggers.triggers.TriggerUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
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
    private boolean isScheduledTrigger = false;

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

        //Get the time until midnight and use this as the default start delay for
        //the daily wakeup trigger
        if(params.containsKey(TriggerConfig.IS_SCHEDULED) &&
            Boolean.parseBoolean(params.getParameter(TriggerConfig.IS_SCHEDULED).toString())) {
            isScheduledTrigger = true;
            Log.d("SCHEDULED","We are a scheduled trigger");
        }
        else{
            isScheduledTrigger = false;
            Log.d("NO_SCHEDULE","We are NOT a scheduled trigger");

        }
        updateDailyScheduler(1440);
        isSubscribed = true;
        scheduleStuff();

    }

    private void updateDailyScheduler(long time) throws TriggerException {
        TriggerConfig params = new TriggerConfig();
        Calendar calendar = Calendar.getInstance();
        long startDelay = 0;
        if(isScheduledTrigger){
            startDelay = time;
            calendar.setTimeInMillis(startDelay);
        }
        else {
            if (time > 1440) {
                calendar.add(Calendar.DATE, 1); //Increment for next day values
                time -= 1440;
            }
            calendar.set(Calendar.HOUR_OF_DAY, (int) (time / 60));
            calendar.set(Calendar.MINUTE, ((int) time % 60));
            calendar.set(Calendar.SECOND, 58);
            startDelay = calendar.getTimeInMillis();
        }
        params.addParameter(TriggerConfig.LIMIT_BEFORE_HOUR, startDelay);
        params.addParameter(TriggerConfig.INTERVAL_TRIGGER_TIME,DAILY_INTERVAL);

        Log.d("WAKEUP","Daily scheduler will wake up at " + calendar.getTime().toString());
        triggerManager.removeTrigger(dailySchedulerId);
        dailySchedulerId = triggerManager.addTrigger(
            TriggerUtils.TYPE_DAILY_SCHEDULER, this, params);
    }
    /**
     * Schedule all the trigger times for this day
     */
    private void scheduleStuff(){
        long fromDay = 0;
        long toDay = 0;
        Log.d("HERE","Are we even here " + trigger.toString());
        if(trigger instanceof BeginTrigger){
            Calendar c = Calendar.getInstance();
            trigger.subscribeTriggerFor(c.getTimeInMillis() + 3000);
            Log.d("SUBBEG","This morning");
            try {
                stop();
                Log.d("STOPPED","That's the end of Begin");
            } catch (TriggerException e) {
                e.printStackTrace();
            }
        }
        if (params.containsKey(TriggerConfig.FROM_DATE)) {

            fromDay = Long.valueOf(params.getParameter(TriggerConfig.FROM_DATE).toString()) /
                (1000 * 60 * 60 * 24);
        }
        if (params.containsKey(TriggerConfig.TO_DATE)) {
            toDay = Long.valueOf(params.getParameter(TriggerConfig.TO_DATE).toString()) /
                (1000 * 60 * 60 * 24);
        }
        long daysSinceEpoch = System.currentTimeMillis() / (24 * 3600 * 1000);
        if (/*(fromDay != 0 || toDay != 0) && */(daysSinceEpoch < fromDay || daysSinceEpoch > toDay)) {
            try {
                Log.d("Removal","Removing daily notification scheduler");
                triggerManager.removeTrigger(dailySchedulerId);
                return;
            } catch (TriggerException e) {
                e.printStackTrace();
            }
        }

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
        else if(trigger instanceof BeginTrigger){
            trigger.subscribeTriggerFor(0);
        }
    }

    public void stop() throws TriggerException
    {
        if (isSubscribed) {
            triggerManager.removeTrigger(dailySchedulerId);
            isSubscribed = false;
        }
    }

    /**
     * The DailyNotificationScheduler receives a notification from the DailyWakeupTrigger, which
     * causes it to reschedule this particular trigger's times
     * @param triggerId ID of trigger that has fired
     */
    @Override
    public void onNotificationTriggered(int triggerId) {

        //Is this a trigger that relies on 'schedule' times?
        if (triggerId == dailySchedulerId) {
            if(isScheduledTrigger) {
                Map<String, Object> scheduleVars = AppContext.getProject().getscheduleAttrs();
                String fromTime = scheduleVars.get(AppContext.WAKE_TIME).toString();
                String toTime = scheduleVars.get(AppContext.SLEEP_TIME).toString();

                SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(AppContext.getContext());
                long currentTo = Long.parseLong(prefs.getString(AppContext.SLEEP_TIME,"0"));
                Calendar c = Calendar.getInstance();
                //Only update if the new day is upon us
                if(currentTo < c.getTimeInMillis()) {
                    SharedPreferences.Editor editor = prefs.edit();
                    int scheduleDay = prefs.getInt(AppContext.SCHEDULE_DAY, 1);
                    Log.d("SCHEDDAY", "Schedule day is " + scheduleDay);
                    String answer = prefs.getString(AppContext.SCHEDULE_PREF + scheduleDay, "");
                    if (answer.isEmpty())
                        return; //Something's gone wrong
                    scheduleDay++;
                    editor.putInt(AppContext.SCHEDULE_DAY, scheduleDay);
                    String[] startEndTimes = answer.split(":");

                    editor.putString(fromTime, startEndTimes[0]);
                    editor.putString(toTime, startEndTimes[1]);
                    Log.d("NEWFROM", "name: " + fromTime + " and value: " + startEndTimes[0]);
                    Log.d("NEWTO", "name: " + toTime + " and value: " + startEndTimes[1]);
                    //This will restart the trigger, but if it doesn't, return anyway
                    editor.commit();
                }
                return;
            }
            scheduleStuff();
        }
    }

    /**
     * If this method is called, we have a Window Trigger. This method schedules the
     * windowed times.
     */
    private void scheduleWindowedTimes(){
        Log.d("WINDOWED","Scheduling windowed times");

        //If these are 'schedule times', these aren't minutes since midnight, but instead are
        //minutes since the epoch...
        long earlyLimit = params.getLongValue(TriggerConfig.LIMIT_BEFORE_HOUR)/60000;
        long lateLimit = params.getLongValue(TriggerConfig.LIMIT_AFTER_HOUR)/60000;

        int minInterval = params.getValue(TriggerConfig.INTERVAL_TRIGGER_TIME);
        int windowSize = params.getValue(TriggerConfig.INTERVAL_TRIGGER_WINDOW);
        ArrayList<Long> times = new ArrayList<Long>();

        if(earlyLimit > lateLimit){
            lateLimit += 1440; //Add an extra day onto the late limit so we can schedule overnight
        }

        while(earlyLimit < lateLimit){
            long earlyWinTime = earlyLimit - windowSize;
            long lateWinTime = earlyLimit + windowSize;
            long winTime = random.nextInt((int)(lateWinTime-earlyWinTime))+earlyWinTime;
            times.add(winTime); //Convert each JSONObject time into a minute-of-day value
            earlyLimit += minInterval;
        }
        Calendar calendar = Calendar.getInstance();

        boolean nextDay = false;

        if(times.size()>0) {
            for (int i = 0; i < times.size(); i++){
                //Straightforward
                long time = times.get(i);
                if(isScheduledTrigger){
                    time *=60000;
                    times.set(i,time);
                    calendar.setTimeInMillis(time);
                }
                else {
                    if (time > 1440) {
                        time -= 1440;
                        if (!nextDay) {
                            nextDay = true;
                            calendar.add(Calendar.DATE, 1); //Increment for next day values
                        }
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, (int)(time / 60));
                    calendar.set(Calendar.MINUTE, (int)(time % 60));
                    calendar.set(Calendar.SECOND, 0);
                }
                trigger.subscribeTriggerFor(calendar.getTimeInMillis());
                Log.d("WINDOWED","Scheduled time for " + calendar.getTime().toString());
            }
            try {
                if(times.size() > 0)
                    updateDailyScheduler(times.get(times.size()-1));
            } catch (TriggerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * If this is called, we have a simple trigger that fires at regularly spaced intervals.
     * Scheduling these is quite straightforward
     */
    private void scheduleIntervalTimes(){
        Log.d("INTERVAL","Scheduling interval times");

        //If these are 'schedule times', these aren't minutes since midnight, but instead are
        //minutes since the epoch...
        long earlyLimit = params.getLongValue(TriggerConfig.LIMIT_BEFORE_HOUR)/60000;
        long lateLimit = params.getLongValue(TriggerConfig.LIMIT_AFTER_HOUR)/60000;
        int numberOfNotifications = Integer.parseInt(
            params.getParameter(TriggerConfig.INTERVAL_TRIGGER_TIME).toString());
        if(earlyLimit > lateLimit)
            lateLimit = lateLimit + 1440; //Add a new day onto things
        long totalTime = lateLimit - earlyLimit;
        long windowLength = totalTime/numberOfNotifications;
        Log.d("WindowLEngth","It is " + windowLength);
        ArrayList<Long> times = new ArrayList<>();

        //if our startTime is 10pm and our end time is 6pm, our endtime becomes 6pm on the NEXT DAY
        while(earlyLimit < lateLimit){
            times.add(earlyLimit);
            earlyLimit += windowLength;
        }
        Calendar calendar = Calendar.getInstance();
        boolean nextDay = false;
        if(times.size()>0) {
            for (int i = 0; i < times.size(); i++){
                //Straightforward
                long time = times.get(i);
                if(isScheduledTrigger){
                    time *=60000;
                    times.set(i,time);
                    calendar.setTimeInMillis(time);
                }
                else {
                    if (time > 1440) {
                        time -= 1440;
                        if (!nextDay) {
                            nextDay = true;
                            calendar.add(Calendar.DATE, 1); //Increment for next day values
                        }
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, (int)(time / 60));
                    calendar.set(Calendar.MINUTE, (int)(time % 60));
                    calendar.set(Calendar.SECOND, 0);
                }
                Log.d("INTERVAL", "Scheduled time for " + calendar.getTime().toString());
                trigger.subscribeTriggerFor(calendar.getTimeInMillis());
            }
            try {
                if(times.size() > 0)
                    updateDailyScheduler(times.get(times.size()-1));
            } catch (TriggerException e) {
                e.printStackTrace();
            }        }
    }

    /**
     * If this is called, we have a Set Times trigger and just need to fire it
     * at all the specified times
     */
    private void scheduleSetTimes(){
        Log.d("SETTIMES","Scheduling set times");

        ArrayList<Long> times = (ArrayList<Long>)params.getParams().get("times");

        Calendar calendar = Calendar.getInstance();
        if(times == null)return;
        for (Long minuteOfDay : times) {
            //Straightforward
            if(isScheduledTrigger){
                calendar.setTimeInMillis(minuteOfDay);
            }
            else {
                minuteOfDay = minuteOfDay / 60000;
                calendar.set(Calendar.HOUR_OF_DAY, (int) (minuteOfDay / 60));
                calendar.set(Calendar.MINUTE, (int) (minuteOfDay % 60));
                calendar.set(Calendar.SECOND, 0);
                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.add(Calendar.DATE, 1);
                }
            }
            Log.d("SETTIMES","Scheduled time for " + calendar.getTime().toString());
            trigger.subscribeTriggerFor(calendar.getTimeInMillis());
        }
        try {
            if(times.size() > 0) {
                long lasttime = times.get(times.size() - 1);
                updateDailyScheduler((int) lasttime);
            }
        } catch (TriggerException e) {
            e.printStackTrace();
        }
    }

    /**
     * If this is called, we have an interval trigger but designed to schedule
     * the triggers randomly within this window.
     */
    private void scheduleNotifications() {
        Log.d("RANDOM","Scheduling random times");

        ArrayList<Long> times = new ArrayList<Long>();
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        long currentTime = 0;
        //Straightforward
        if(isScheduledTrigger){ //in ms since epoch
            currentTime = calendar.getTimeInMillis() / 60000;
        }
        else { //in minutes from midnight
            currentTime = (60 * hour) + minute;
        }
        //If these are 'schedule times', these aren't minutes since midnight, but instead are
        //minutes since the epoch...
        long earlyLimit = params.getLongValue(TriggerConfig.LIMIT_BEFORE_HOUR)/60000;
        long lateLimit = params.getLongValue(TriggerConfig.LIMIT_AFTER_HOUR)/60000;
        int numberOfNotifications = Integer.parseInt(
            params.getParameter(TriggerConfig.INTERVAL_TRIGGER_TIME).toString());

        int minInterval = 1;
        if(earlyLimit > lateLimit){
            lateLimit += 1440; //Add an extra day onto the late limit so we can schedule shit overnight
        }
        Log.d("EARLATE","early is " + earlyLimit + " and late is " + lateLimit);
        for (int t=0; t<numberOfNotifications; t++) {
            boolean entryAdded = false;
            for (int i=0; i<MAX_SCHEDULING_ATTEMPTS && !entryAdded; i++) {
                long time = pickRandomTimeWithinPreferences(currentTime, earlyLimit, lateLimit);
                Log.d("Time","time is " + time);
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
        boolean nextDay = false;
        for (int i = 0; i < times.size(); i++){
            //Straightforward
            long time = times.get(i);
            if(isScheduledTrigger){
                time *=60000;
                times.set(i,time);
                calendar.setTimeInMillis(time);
            }
            else {
                if (time > 1440) {
                    time -= 1440;
                    if (!nextDay) {
                        nextDay = true;
                        calendar.add(Calendar.DATE, 1);
                    }
                }
                calendar.set(Calendar.HOUR_OF_DAY, (int)(time / 60));
                calendar.set(Calendar.MINUTE, (int)(time % 60));
                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.add(Calendar.DATE, 1);
                }
            }
            Log.d("RANDOM","Scheduled time for " + calendar.getTime().toString());
            trigger.subscribeTriggerFor(calendar.getTimeInMillis());
        }
        try {
            if(times.size() > 0)
                updateDailyScheduler(times.get(times.size()-1));
        } catch (TriggerException e) {
            e.printStackTrace();
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
    private long pickRandomTimeWithinPreferences(long currentMinute, long earlyLimit, long lateLimit) {
        long from = earlyLimit > (currentMinute+1) ? earlyLimit : currentMinute+1;
        if (lateLimit - from > 0) {
            return random.nextInt((int)(lateLimit - from)) + from;
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
    private boolean selectedTimeFitsGroup(long selectedTime, ArrayList<Long> times, long minInterval) {
        if (selectedTime == ERROR) {
            return false;
        }
        else {
            for (Long time : times) {
                if (Math.abs(time - selectedTime) <= minInterval) {
                    return false;
                }
            }
            return true;
        }
    }

}

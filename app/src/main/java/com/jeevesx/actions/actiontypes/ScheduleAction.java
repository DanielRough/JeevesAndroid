package com.jeevesx.actions.actiontypes;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jeevesx.AppContext;
import com.jeevesx.mainscreens.ScheduleActivity;

import java.util.Map;

/**
 * Class representing the action block for updating a user schedule
 */
public class ScheduleAction extends FirebaseAction {

    public ScheduleAction(Map<String,Object> params){
        setparams(params);
    }

    @Override
    public void execute() {
        Log.d("SCHED","We scheduling?");

        final Context app = AppContext.getContext();

        Map<String,Object> scheduleVars = AppContext.getProject().getscheduleAttrs();
        if(scheduleVars == null){
            Log.d("NOPEE","Returning from schedule");
            return;
        }
        String startDate = scheduleVars.get(AppContext.START_DATE).toString();
        String endDate = scheduleVars.get(AppContext.END_DATE).toString();
        String wakeTime = scheduleVars.get(AppContext.WAKE_TIME).toString();
        String sleepTime = scheduleVars.get(AppContext.SLEEP_TIME).toString();
        int triggerType = (int)getparams().get(AppContext.TRIG_TYPE);

        Intent resultIntent = new Intent(app, ScheduleActivity.class);
        resultIntent.putExtra(AppContext.TRIG_TYPE,triggerType);
        resultIntent.putExtra(AppContext.START_DATE, startDate);
        resultIntent.putExtra(AppContext.END_DATE, endDate);
        resultIntent.putExtra(AppContext.WAKE_TIME, wakeTime);
        resultIntent.putExtra(AppContext.SLEEP_TIME, sleepTime);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        app.startActivity(resultIntent);

    }
}

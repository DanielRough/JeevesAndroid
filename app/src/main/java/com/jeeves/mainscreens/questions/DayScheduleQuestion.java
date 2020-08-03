package com.jeeves.mainscreens.questions;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.jeeves.AppContext;
import com.jeeves.R;
import com.jeeves.firebase.FirebaseQuestion;
import com.jeeves.mainscreens.ScheduleActivity;
import com.jeeves.mainscreens.SurveyActivity;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;

public class DayScheduleQuestion extends Question{

    public ScheduleActivity activity;
    public DayScheduleQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity,questions,answers);
        this.activity = (ScheduleActivity)activity;
    }


    @Override
    public void handle(int position) {
        FirebaseQuestion question = questions.get(position);
        final String scheduleDay = question.getquestionId();
        final Calendar calendar_start = Calendar.getInstance();
        final Calendar calendar_end = Calendar.getInstance();
        final Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);


        DatePicker datePickerWake = qView.findViewById(R.id.datePickerWake);
        DatePicker datePickerSleep = qView.findViewById(R.id.datePickerSleep);

        datePickerWake.init(calendar_start.get(Calendar.YEAR),calendar_start.get(Calendar.MONTH),calendar_start.get(Calendar.DAY_OF_MONTH),new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar_start.set(Calendar.YEAR, year);
                calendar_start.set(Calendar.MONTH, monthOfYear);
                calendar_start.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                long msStart = calendar_start.getTimeInMillis();
                long msEnd = calendar_end.getTimeInMillis();
                answers.set(currentIndex, msStart + ":" + msEnd);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(AppContext.SCHEDULE_PREF + scheduleDay,msStart + ":" + msEnd);
                editor.apply();
            }
        });
        datePickerSleep.init(calendar_end.get(Calendar.YEAR),calendar_end.get(Calendar.MONTH),calendar_end.get(Calendar.DAY_OF_MONTH),new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar_end.set(Calendar.YEAR, year);
                calendar_end.set(Calendar.MONTH, monthOfYear);
                calendar_end.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                long msStart = calendar_start.getTimeInMillis();
                long msEnd = calendar_end.getTimeInMillis();
                answers.set(currentIndex, msStart + ":" + msEnd);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(AppContext.SCHEDULE_PREF + scheduleDay,msStart + ":" + msEnd);
                editor.apply();
            }
        });
        removeYearField(datePickerWake);
        removeYearField(datePickerSleep);
        TimePicker tpicker_start = qView.findViewById(R.id.timePickerStart);
        tpicker_start.setIs24HourView(true);
        final TimePicker tpicker_end = qView.findViewById(R.id.timePickerEnd);
        tpicker_end.setIs24HourView(true);
        String answer = answers.get(currentIndex);
        String prevAnswer = (currentIndex <= 2 ? "" : answers.get(currentIndex-1));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());

        if (!answer.isEmpty()) {
            String[] start_end = answer.split(":");
            calendar_start.setTimeInMillis(Long.parseLong(start_end[0]));
            calendar_end.setTimeInMillis(Long.parseLong(start_end[1]));

            datePickerWake.updateDate(calendar_start.get(Calendar.YEAR),calendar_start.get(Calendar.MONTH),calendar_start.get(Calendar.DAY_OF_MONTH));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tpicker_start.setHour(calendar_start.get(Calendar.HOUR_OF_DAY));
                tpicker_start.setMinute(calendar_start.get(Calendar.MINUTE));
            }
            else{
                tpicker_start.setCurrentHour(calendar_start.get(Calendar.HOUR_OF_DAY));
                tpicker_start.setCurrentMinute(calendar_start.get(Calendar.MINUTE));
            }

            datePickerSleep.updateDate(calendar_end.get(Calendar.YEAR),calendar_end.get(Calendar.MONTH),calendar_end.get(Calendar.DAY_OF_MONTH));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tpicker_end.setHour(calendar_end.get(Calendar.HOUR_OF_DAY));
                tpicker_end.setMinute(calendar_end.get(Calendar.MINUTE));
            }
            else{
                tpicker_end.setCurrentHour(calendar_end.get(Calendar.HOUR_OF_DAY));
                tpicker_end.setCurrentMinute(calendar_end.get(Calendar.MINUTE));
            }
        }
        //Some duplicate code in here but all it does is update our dates/times to be the previous answer + 1 day
        else if(!prevAnswer.isEmpty()){
            String[] start_end = prevAnswer.split(":");
            calendar_start.setTimeInMillis(Long.parseLong(start_end[0]) + (24*3600*1000));
            calendar_end.setTimeInMillis(Long.parseLong(start_end[1]) + (24*3600*1000));
            datePickerWake.updateDate(calendar_start.get(Calendar.YEAR),calendar_start.get(Calendar.MONTH),calendar_start.get(Calendar.DAY_OF_MONTH));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tpicker_start.setHour(calendar_start.get(Calendar.HOUR_OF_DAY));
                tpicker_start.setMinute(calendar_start.get(Calendar.MINUTE));
            }
            else{
                tpicker_start.setCurrentHour(calendar_start.get(Calendar.HOUR_OF_DAY));
                tpicker_start.setCurrentMinute(calendar_start.get(Calendar.MINUTE));
            }

            datePickerSleep.updateDate(calendar_end.get(Calendar.YEAR),calendar_end.get(Calendar.MONTH),calendar_end.get(Calendar.DAY_OF_MONTH));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tpicker_end.setHour(calendar_end.get(Calendar.HOUR_OF_DAY));
                tpicker_end.setMinute(calendar_end.get(Calendar.MINUTE));
            }
            else{
                tpicker_end.setCurrentHour(calendar_end.get(Calendar.HOUR_OF_DAY));
                tpicker_end.setCurrentMinute(calendar_end.get(Calendar.MINUTE));
            }
        }
        //Day schedule questions work slightly differently - they are not stored in user attributes,
        //but instead are stored in shared preferences
        else{

            //Pref is something like 'schedule_pref1' or 'schedule_pref77'
            String scheduleTimeStr = prefs.getString(AppContext.SCHEDULE_PREF + scheduleDay,"");
            if(!scheduleTimeStr.equals("")) {
                String[] scheduleTimes = scheduleTimeStr.split(":");
                String startTime = scheduleTimes[0];
                String endTime = scheduleTimes[1];
                calendar_start.setTimeInMillis(Long.parseLong(startTime));
                calendar_end.setTimeInMillis(Long.parseLong(endTime));
                answers.set(currentIndex,
                    (calendar_start.getTimeInMillis()) +
                        ":" + (calendar_end.getTimeInMillis()));
                datePickerWake.updateDate(calendar_start.get(Calendar.YEAR),calendar_start.get(Calendar.MONTH),calendar_start.get(Calendar.DAY_OF_MONTH));
                datePickerSleep.updateDate(calendar_end.get(Calendar.YEAR),calendar_end.get(Calendar.MONTH),calendar_end.get(Calendar.DAY_OF_MONTH));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tpicker_start.setHour(calendar_start.get(Calendar.HOUR_OF_DAY));
                    tpicker_start.setMinute(calendar_start.get(Calendar.MINUTE));
                    tpicker_end.setHour(calendar_end.get(Calendar.HOUR_OF_DAY));
                    tpicker_end.setMinute(calendar_end.get(Calendar.MINUTE));
                }
                else{
                    tpicker_start.setCurrentHour(calendar_start.get(Calendar.HOUR_OF_DAY));
                    tpicker_start.setCurrentMinute(calendar_start.get(Calendar.MINUTE));
                    tpicker_end.setCurrentHour(calendar_end.get(Calendar.HOUR_OF_DAY));
                    tpicker_end.setCurrentMinute(calendar_end.get(Calendar.MINUTE));
                }
            }
            else{

                answers.set(currentIndex,calendar_start.getTimeInMillis() + ":" + calendar_end.getTimeInMillis());
                SharedPreferences.Editor editor = prefs.edit();
                long msStart = calendar_start.getTimeInMillis();
                long msEnd = calendar_end.getTimeInMillis();
                editor.putString(AppContext.SCHEDULE_PREF + scheduleDay,msStart + ":" + msEnd);
                editor.apply();
            }
            //Here we'll check what the original start date was and set calendar_start to that.
            String startDate = answers.get(0);
            Calendar newCalendar = Calendar.getInstance();
            newCalendar.setTimeInMillis(Long.parseLong(startDate));
            if(currentIndex == 2 && calendar_start.get(Calendar.DAY_OF_MONTH) != newCalendar.get(Calendar.DAY_OF_MONTH)){
                datePickerWake.updateDate(newCalendar.get(Calendar.YEAR),
                    newCalendar.get(Calendar.MONTH),
                    newCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerSleep.updateDate(newCalendar.get(Calendar.YEAR),
                    newCalendar.get(Calendar.MONTH),
                    newCalendar.get(Calendar.DAY_OF_MONTH));
            }
        }

        tpicker_start.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                calendar_start.set(Calendar.HOUR_OF_DAY, i);
                calendar_start.set(Calendar.MINUTE, i1);

                long msStart = calendar_start.getTimeInMillis();
                long msEnd = calendar_end.getTimeInMillis();
                answers.set(currentIndex, msStart + ":" + msEnd);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(AppContext.SCHEDULE_PREF + scheduleDay,msStart + ":" + msEnd);
                editor.apply();

            }
        });
        tpicker_end.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                calendar_end.set(Calendar.HOUR_OF_DAY, i);
                calendar_end.set(Calendar.MINUTE, i1);
                long msStart = calendar_start.getTimeInMillis();
                long msEnd = calendar_end.getTimeInMillis();
                answers.set(currentIndex, msStart + ":" + msEnd);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(AppContext.SCHEDULE_PREF + scheduleDay,msStart + ":" + msEnd);
                editor.apply();
            }
        });
    }

    /**
     * Nifty method to remove the year from the date picker (saves some space)
     * @param picker
     */
    public void removeYearField(DatePicker picker){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int yearSpinnerId = Resources.getSystem().getIdentifier("year", "id", "android");
            if (yearSpinnerId != 0) {
                View yearSpinner = picker.findViewById(yearSpinnerId);
                if (yearSpinner != null) {
                    yearSpinner.setVisibility(View.GONE);
                }
            }
        }
        else {
            try {
                Field f[] = picker.getClass().getDeclaredFields();
                for (Field field : f) {
                    if (field.getName().equals("mYearPicker") || field.getName().equals("mYearSpinner")) {
                        field.setAccessible(true);
                        Object yearPicker = field.get(picker);
                        ((View) yearPicker).setVisibility(View.GONE);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public int getLayoutId() {
        return R.layout.qu_schedule;
    }
}

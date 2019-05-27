package com.jeevesandroid.mainscreens.questions;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.jeevesandroid.AppContext;
import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseQuestion;
import com.jeevesandroid.mainscreens.ScheduleActivity;
import com.jeevesandroid.mainscreens.SurveyActivity;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;

public class DayScheduleQuestion extends Question{

    public ScheduleActivity activity;
    public DayScheduleQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity,questions,answers);
        this.activity = (ScheduleActivity)activity;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void handle(int position) {
        FirebaseQuestion question = questions.get(position);
        final String scheduleDay = question.getquestionId();
        final Calendar calendar_start = Calendar.getInstance();
        final Calendar calendar_end = Calendar.getInstance();
        final Calendar midnight = Calendar.getInstance();
        //Will cause problems
//        final Button btnReturn = qView.findViewById(R.id.btnReturn);
//        btnReturn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                activity.saveAndReturn();
//            }
//        });
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());

        if (!answer.isEmpty()) {
            String[] start_end = answer.split(":");
            calendar_start.setTimeInMillis(Long.parseLong(start_end[0]));
            calendar_end.setTimeInMillis(Long.parseLong(start_end[1]));

            datePickerWake.updateDate(calendar_start.get(Calendar.YEAR),calendar_start.get(Calendar.MONTH),calendar_start.get(Calendar.DAY_OF_MONTH));
            tpicker_start.setHour(calendar_start.get(Calendar.HOUR_OF_DAY));
            tpicker_start.setMinute(calendar_start.get(Calendar.MINUTE));

            datePickerSleep.updateDate(calendar_end.get(Calendar.YEAR),calendar_end.get(Calendar.MONTH),calendar_end.get(Calendar.DAY_OF_MONTH));
            tpicker_end.setHour(calendar_end.get(Calendar.HOUR_OF_DAY));
            tpicker_end.setMinute(calendar_end.get(Calendar.MINUTE));
        }
        //Day schedule questions work slightly differently - they are not stored in user attributes,
        //but instead are stored in shared preferences
        else{
            //Pref is something like 'schedule_pref1' or 'schedule_pref77'
            String scheduleTimeStr = prefs.getString(AppContext.SCHEDULE_PREF + scheduleDay,"");

            if(scheduleTimeStr.equals("")) {
                answers.set(currentIndex,calendar_start.getTimeInMillis() + ":" + calendar_end.getTimeInMillis());
                SharedPreferences.Editor editor = prefs.edit();
                long msStart = calendar_start.getTimeInMillis();
                long msEnd = calendar_end.getTimeInMillis();
                editor.putString(AppContext.SCHEDULE_PREF + scheduleDay,msStart + ":" + msEnd);
                editor.apply();
            }
                else{
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
                tpicker_start.setHour(calendar_start.get(Calendar.HOUR_OF_DAY));
                tpicker_start.setMinute(calendar_start.get(Calendar.MINUTE));
                tpicker_end.setHour(calendar_end.get(Calendar.HOUR_OF_DAY));
                tpicker_end.setMinute(calendar_end.get(Calendar.MINUTE));
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

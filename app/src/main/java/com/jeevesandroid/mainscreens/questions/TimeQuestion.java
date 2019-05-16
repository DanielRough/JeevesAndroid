package com.jeevesandroid.mainscreens.questions;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseQuestion;
import com.jeevesandroid.mainscreens.SurveyActivity;

import java.util.Calendar;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TimeQuestion extends Question{

    public TimeQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity,questions,answers);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void handle(int position) {
        final Calendar calendar = Calendar.getInstance();
        final Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        TimePicker tpicker = qView.findViewById(R.id.timePicker2);
        String answer = answers.get(currentIndex);
        tpicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                calendar.set(Calendar.HOUR_OF_DAY, i);
                calendar.set(Calendar.MINUTE, i1);
                long msFromMidnight = calendar.getTimeInMillis() - midnight.getTimeInMillis();
                answers.set(currentIndex, Long.toString(msFromMidnight));
            }
        });

        if (!answer.isEmpty()) {
            calendar.setTimeInMillis(midnight.getTimeInMillis() + Long.parseLong(answer));
            tpicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            tpicker.setMinute(calendar.get(Calendar.MINUTE));
        } else {
            answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()- midnight.getTimeInMillis()));
        }


    }

    @Override
    public int getLayoutId() {
        return R.layout.qu_time;
    }
}

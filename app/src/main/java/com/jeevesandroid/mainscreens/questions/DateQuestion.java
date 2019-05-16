package com.jeevesandroid.mainscreens.questions;

import android.widget.DatePicker;

import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseQuestion;
import com.jeevesandroid.mainscreens.SurveyActivity;

import java.util.Calendar;
import java.util.List;

/**
 * Handler class for the Date question type
 */
public class DateQuestion extends Question{

    public DateQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity,questions,answers);
    }

    public int getLayoutId(){
        return R.layout.qu_date;
    }

    @Override
    public void handle(int position) {
        final Calendar calendar = Calendar.getInstance();
        final DatePicker picker = qView.findViewById(R.id.datePicker2);
        String answer = answers.get(currentIndex);
        picker.init(calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            new DatePicker.OnDateChangedListener() {

                @Override
                public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
                    calendar.set(year, month, day);
                    answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()));
                }
            });

        if (!answer.isEmpty()) {
            calendar.setTimeInMillis(Long.parseLong(answer));
            picker.updateDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        } else {
            answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()));
        }

    }
}

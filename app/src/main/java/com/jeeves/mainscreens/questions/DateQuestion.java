package com.jeeves.mainscreens.questions;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.DatePicker;

import com.jeeves.AppContext;
import com.jeeves.R;
import com.jeeves.firebase.FirebaseQuestion;
import com.jeeves.mainscreens.SurveyActivity;

import java.util.Calendar;
import java.util.Date;
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
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
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
        }
        else if(questions.get(currentIndex).getassignedVar() != null){
            String var = questions.get(currentIndex).getassignedVar();
            Log.d("Assigned","assigned var is " + var);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
            String varval = prefs.getString(var,"");
            if(varval.equals(""))
                answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()));
            else{
                //DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date d = new Date();
                d.setTime(Long.parseLong(varval));
                //Date savedDate = formatter.format(varval);
                calendar.setTime(d);
                answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()));
                picker.updateDate(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));


            }
        }
        else {
            answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()));
        }

    }
}

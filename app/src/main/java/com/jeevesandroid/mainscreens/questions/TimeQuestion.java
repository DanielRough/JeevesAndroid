package com.jeevesandroid.mainscreens.questions;

import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;

import com.jeevesandroid.AppContext;
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


        if (!answer.isEmpty()) {
            calendar.setTimeInMillis(midnight.getTimeInMillis() + Long.parseLong(answer));
            tpicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            tpicker.setMinute(calendar.get(Calendar.MINUTE));
        }
        else if(questions.get(currentIndex).getassignedVar() != null){
            String var = questions.get(currentIndex).getassignedVar();
            Log.d("Assigned","assigned var is " + var);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
            String varval = prefs.getString(var,"");
            if(varval.equals(""))
                answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()- midnight.getTimeInMillis()));
            else{
                calendar.setTimeInMillis(midnight.getTimeInMillis() + Long.parseLong(varval));
                answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()- midnight.getTimeInMillis()));
                tpicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
                tpicker.setMinute(calendar.get(Calendar.MINUTE));
            }
        }
        else {
            answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()- midnight.getTimeInMillis()));
        }
        tpicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                calendar.set(Calendar.HOUR_OF_DAY, i);
                calendar.set(Calendar.MINUTE, i1);
                long msFromMidnight = calendar.getTimeInMillis() - midnight.getTimeInMillis();
                answers.set(currentIndex, Long.toString(msFromMidnight));
            }
        });

    }

    @Override
    public int getLayoutId() {
        return R.layout.qu_time;
    }
}

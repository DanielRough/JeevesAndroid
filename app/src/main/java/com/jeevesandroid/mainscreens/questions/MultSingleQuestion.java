package com.jeevesandroid.mainscreens.questions;

import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseQuestion;
import com.jeevesandroid.mainscreens.SurveyActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MultSingleQuestion extends Question{
    private RadioGroup grpMultSingle;

    public MultSingleQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity,questions,answers);
    }

    @Override
    public void handle(int position) {
        grpMultSingle = qView.findViewById(R.id.grpMultSingle);
        grpMultSingle.removeAllViews();
        Map<String, Object> myparams = questions.get(position).getparams();
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        Iterator<Object> opts = options.values().iterator();
        ArrayList<RadioButton> allButtons = new ArrayList<>();
        while (opts.hasNext()) {
            String option = opts.next().toString();
            final RadioButton button = new RadioButton(context);
            button.setText(option);
            button.setTextSize(40);
            grpMultSingle.addView(button);
            allButtons.add(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    answers.set(currentIndex, button.getText().toString());
                    Log.d("ANSWER","Answer is " + button.getText().toString());
                    //If we're rapidly transitioning, skip right away
                    if(context.getIsFast())
                        context.nextQ();
                }
            });
        }
        String answer = answers.get(currentIndex);
        if (!answer.isEmpty())
            for (RadioButton but : allButtons) {
                if (but.getText().equals(answer))
                    but.setChecked(true);
            }
        else
            answers.set(currentIndex, "");
    }

    @Override
    public int getLayoutId() {
        return R.layout.qu_multsingle;
    }

}

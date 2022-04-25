package com.jeevesx.mainscreens.questions;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.jeevesx.R;
import com.jeevesx.firebase.FirebaseQuestion;
import com.jeevesx.mainscreens.SurveyActivity;

import java.util.List;

/**
 * Handler class for the True/False question type
 */
public class BooleanQuestion extends Question{
    private RadioGroup grpBool;

    public BooleanQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity, questions, answers);
    }

    public int getLayoutId(){
        return R.layout.qu_bool;
    }

    @Override
    public void handle(int position) {
        grpBool = qView.findViewById(R.id.grpBool);
        grpBool.removeAllViews();
        RadioButton trueButton = new RadioButton(context);
        trueButton.setText(context.getResources().getString(R.string.yes));
        trueButton.setTextSize(40);
        RadioButton falseButton = new RadioButton(context);
        falseButton.setText(context.getResources().getString(R.string.no));
        falseButton.setTextSize(40);
        grpBool.addView(trueButton);
        grpBool.addView(falseButton);
        trueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answers.set(currentIndex, "true");
                if(context.getIsFast())
                    context.nextQ();
            }
        });
        falseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answers.set(currentIndex, "false");
                if(context.getIsFast())
                    context.nextQ();
            }
        });

        String answer = answers.get(currentIndex);
        if (answer != null && !answer.equals(""))
            if (answer.equals("true"))
                trueButton.setChecked(true);
            else
                falseButton.setChecked(true);
        else
            answers.set(currentIndex, "");
    }
}

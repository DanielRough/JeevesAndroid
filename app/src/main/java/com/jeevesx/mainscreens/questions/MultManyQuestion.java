package com.jeevesx.mainscreens.questions;

import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.jeevesx.R;
import com.jeevesx.firebase.FirebaseQuestion;
import com.jeevesx.mainscreens.SurveyActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultManyQuestion extends Question {
    private LinearLayout grpMultMany;
    private final ArrayList<CheckBox> allBoxes = new ArrayList<>();

    public MultManyQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity,questions,answers);
    }


    @Override
    public void handle(int position) {
        grpMultMany = qView.findViewById(R.id.grpMultMany);

        grpMultMany.removeAllViews();
        allBoxes.clear();

        Map<String, Object> myparams = questions.get(position).getparams();
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        for (Object o : options.values()) {
            String option = o.toString();
            CheckBox box = new CheckBox(context);
            box.setText(option);
            box.setGravity(Gravity.FILL);
            box.setTextSize(32);
            box.setPadding(box.getPaddingLeft(),
                box.getPaddingTop() - 10,
                box.getPaddingRight(),
                box.getPaddingBottom());
            grpMultMany.addView(box);
            allBoxes.add(box);
            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    StringBuilder newanswers = new StringBuilder();
                    for (CheckBox allBox : allBoxes) {
                        if (allBox.isChecked())
                            newanswers.append(allBox.getText().toString()).append(",");
                    }
                    answers.set(currentIndex, newanswers.toString());
                }
            });
        }

        String answer = answers.get(currentIndex);
        if (!answer.isEmpty()) {
            String[] allanswers = answer.split(",");
            for (String ans : allanswers) {
                for (CheckBox box : allBoxes)
                    if (box.getText().equals(ans))
                        box.setChecked(true);

            }
        } else
            answers.set(currentIndex, "");

    }

    @Override
    public int getLayoutId() {
        return R.layout.qu_multmany;
    }
}

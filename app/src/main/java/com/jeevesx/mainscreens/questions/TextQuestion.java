package com.jeevesx.mainscreens.questions;

import android.view.View;
import android.widget.TextView;

import com.jeevesx.R;
import com.jeevesx.firebase.FirebaseQuestion;
import com.jeevesx.mainscreens.SurveyActivity;

import java.util.List;
import java.util.Map;

public class TextQuestion extends Question{
    private TextView txtPresent;

    public TextQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity,questions,answers);
    }

    @Override
    public void handle(int position) {
        txtPresent = qView.findViewById(R.id.txtPresent);
        Map<String, Object> myparams = questions.get(position).getparams();
        txtPresent.setVisibility(View.VISIBLE);
        if(myparams == null)return;
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        String textToShow = (String)options.get("text");
        txtPresent.setText(textToShow);
    }

    @Override
    public int getLayoutId() {
        return R.layout.qu_text;
    }

}

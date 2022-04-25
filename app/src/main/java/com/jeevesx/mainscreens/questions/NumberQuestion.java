package com.jeevesx.mainscreens.questions;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.jeevesx.R;
import com.jeevesx.firebase.FirebaseQuestion;
import com.jeevesx.mainscreens.SurveyActivity;

import java.util.List;

public class NumberQuestion extends Question{
    private EditText txtNumeric;

    public NumberQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity,questions,answers);
    }
    public void handle(int position) {
        txtNumeric = qView.findViewById(R.id.txtNumeric);

        if (answers.get(currentIndex) != null)
            txtNumeric.setText(answers.get(currentIndex));
        else {
            answers.set(currentIndex, "");
            txtNumeric.setText("");
        }
        txtNumeric.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            public void afterTextChanged(Editable s) {
                answers.set(currentIndex, txtNumeric.getText().toString());
            }
        });
        txtNumeric.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    context.hideKeyboard(v);
                }
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.qu_numeric;
    }

}

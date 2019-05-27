package com.jeevesandroid.mainscreens.questions;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseQuestion;
import com.jeevesandroid.mainscreens.SurveyActivity;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

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

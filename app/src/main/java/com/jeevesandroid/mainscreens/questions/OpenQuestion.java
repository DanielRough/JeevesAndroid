package com.jeevesandroid.mainscreens.questions;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseQuestion;
import com.jeevesandroid.mainscreens.SurveyActivity;
import java.util.List;


public class OpenQuestion extends Question{
    private EditText txtOpenEnded;
    List<String> answers;
    SurveyActivity context;
    List<FirebaseQuestion> questions;

    public OpenQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity,questions,answers);
        context= activity;
        this.questions = questions;
        this.answers = answers;
    }


    public void handle(int position){
        txtOpenEnded = qView.findViewById(R.id.txtOpenEnded);
        String answer = answers.get(position);
        if (!answer.isEmpty())
            txtOpenEnded.setText(answer);
        else {
            answers.set(currentIndex, "");
            txtOpenEnded.setText("");
        }
        txtOpenEnded.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                answers.set(currentIndex, txtOpenEnded.getText().toString());
                Log.d("SETTING","setting " + currentIndex + " to " + txtOpenEnded.getText().toString());

            }
        });
        txtOpenEnded.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
        return R.layout.qu_open;
    }

}

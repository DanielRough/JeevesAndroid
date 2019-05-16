package com.jeevesandroid.mainscreens.questions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jeevesandroid.firebase.FirebaseQuestion;
import com.jeevesandroid.mainscreens.SurveyActivity;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public abstract class Question {
    List<String> answers;
    SurveyActivity context;
    List<FirebaseQuestion> questions;
    int currentIndex;
    View qView;

    public Question(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        context= activity;
        this.questions = questions;
        this.answers = answers;
    }
    public View getView(int position, int currentIndex, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(LAYOUT_INFLATER_SERVICE);
        View qView = inflater.inflate(getLayoutId(), parent, false);
        this.currentIndex = currentIndex;
        this.qView = qView;
        handle(position);
        return qView;
    }

    public abstract void handle(int position);
    public abstract int getLayoutId();
}

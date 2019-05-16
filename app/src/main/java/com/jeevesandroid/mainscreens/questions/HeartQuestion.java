package com.jeevesandroid.mainscreens.questions;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.chrisbanes.photoview.PhotoView;
import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseQuestion;
import com.jeevesandroid.mainscreens.SurveyActivity;
import com.jeevesandroid.sensing.heartrate.HeartRateMonitor;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Handler class for the heart rate question type
 */
public class HeartQuestion extends Question {

    public HeartQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity,questions,answers);
    }


    @Override
    public void handle(int position) {
        if(answers.get(currentIndex).isEmpty()){
            Button btnStart = qView.findViewById(R.id.btnStart);
            btnStart.setText(context.getResources().getString(R.string.startsensing));
            btnStart.setEnabled(true);
            PhotoView heartview = qView.findViewById(R.id.heartview);
            heartview.setImageResource(R.drawable.finger);
        }
        else{
            Button btnStart = qView.findViewById(R.id.btnStart);
            btnStart.setText(context.getResources().getString(R.string.heartrate));
            btnStart.setEnabled(false);
            PhotoView heartview = qView.findViewById(R.id.heartview);
            heartview.setImageResource(R.drawable.fingerdone);
        }
        Button btnStart = qView.findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, HeartRateMonitor.class);
                context.startActivityForResult(intent, 1234);
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.qu_heart;
    }
}

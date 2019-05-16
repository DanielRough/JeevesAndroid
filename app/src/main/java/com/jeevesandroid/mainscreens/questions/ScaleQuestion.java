package com.jeevesandroid.mainscreens.questions;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseQuestion;
import com.jeevesandroid.mainscreens.SurveyActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ScaleQuestion extends Question{
    private SeekBar seekBar;

    public ScaleQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity,questions,answers);
    }

    @Override
    public void handle(int position) {
        seekBar = qView.findViewById(R.id.seekBar);
        Map<String, Object> myparams = questions.get(position).getparams();
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        int entries = Integer.parseInt(options.get("number").toString());
        ArrayList<String> labels = (ArrayList<String>) options.get("labels");
        TextView txtBegin = qView.findViewById(R.id.txtBegin);
        TextView txtMiddle = qView.findViewById(R.id.txtMiddle);
        TextView txtEnd = qView.findViewById(R.id.txtEnd);
        TextView[] views = new TextView[]{txtBegin,txtMiddle,txtEnd};
        for(int i = 0; i < views.length; i++){
            if(labels.get(i) != null)
                views[i].setText(labels.get(i));
        }
        String answer = answers.get(currentIndex);
        seekBar.setMax(entries);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                Log.d("PREOG","set rogress to " + progress);
                answers.set(currentIndex, Integer.toString(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(context.getIsFast())
                    context.nextQ();
            }
        });
        //Do this here so that it gets filled with a default value if the user doesn't touch it
        if (!answer.isEmpty())
            seekBar.setProgress(Integer.parseInt(answer));
        else {
            seekBar.setProgress(entries / 2);
            answers.set(currentIndex, Integer.toString(entries/2));
        }

    }

    @Override
    public int getLayoutId() {
        return R.layout.qu_scale;
    }
}

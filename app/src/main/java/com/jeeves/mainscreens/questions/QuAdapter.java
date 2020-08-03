package com.jeeves.mainscreens.questions;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.jeeves.firebase.FirebaseQuestion;
import com.jeeves.mainscreens.ScheduleActivity;
import com.jeeves.mainscreens.SurveyActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jeeves.AppContext.AUDIO;
import static com.jeeves.AppContext.BOOLEAN;
import static com.jeeves.AppContext.DATE;
import static com.jeeves.AppContext.GEO;
import static com.jeeves.AppContext.HEART;
import static com.jeeves.AppContext.IMAGEPRESENT;
import static com.jeeves.AppContext.MULT_MANY;
import static com.jeeves.AppContext.MULT_SINGLE;
import static com.jeeves.AppContext.NUMERIC;
import static com.jeeves.AppContext.OPEN_ENDED;
import static com.jeeves.AppContext.SCALE;
import static com.jeeves.AppContext.SCHEDULE;
import static com.jeeves.AppContext.TEXTPRESENT;
import static com.jeeves.AppContext.TIME;
import static com.jeeves.AppContext.TIMELIST;

public class QuAdapter extends BaseAdapter {

    List<FirebaseQuestion> questions;
    List<String> answers;
    final SurveyActivity context;
    Map<String,Question> questionTypes;
    int currentPos;
    public void handleResult(int requestCode, int resultCode, Intent data){
        ((Question)getItem(currentPos)).handleActivityResult(requestCode,resultCode,data);
    }
    public void updateQsandAs(List<FirebaseQuestion> questions,List<String> answers){
        this.questions = questions;
        this.answers = answers;
    }
    public QuAdapter(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers){
        context = activity;
        this.questions = questions;
        this.answers = answers;
        questionTypes = new HashMap<String,Question>();
        questionTypes.put(OPEN_ENDED,new OpenQuestion(activity,questions,answers));
        questionTypes.put(MULT_SINGLE,new MultSingleQuestion(activity,questions,answers));
        questionTypes.put(MULT_MANY,new MultManyQuestion(activity,questions,answers));
        questionTypes.put(SCALE,new ScaleQuestion(activity,questions,answers));
        questionTypes.put(DATE,new DateQuestion(activity,questions,answers));
        questionTypes.put(TIME,new TimeQuestion(activity,questions,answers));
        questionTypes.put(GEO,new GeoQuestion(activity,questions,answers));
        questionTypes.put(BOOLEAN,new BooleanQuestion(activity,questions,answers));
        questionTypes.put(NUMERIC,new NumberQuestion(activity,questions,answers));
        questionTypes.put(HEART,new HeartQuestion(activity,questions,answers));
        questionTypes.put(AUDIO,new AudioQuestion(activity,questions,answers));
        questionTypes.put(IMAGEPRESENT,new ImageQuestion(activity,questions,answers));
        questionTypes.put(TEXTPRESENT,new TextQuestion(activity,questions,answers));
        questionTypes.put(TIMELIST,new TimeListQuestion(activity,questions,answers));
        if(activity instanceof ScheduleActivity)
            questionTypes.put(SCHEDULE,new DayScheduleQuestion(activity,questions,answers));
    }
    @Override
    public int getCount() {
        return questions.size();
    }

    @Override
    public Object getItem(int position) {
        String questionType = questions.get(position).getquestionType();
        return questionTypes.get(questionType);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        currentPos = position;
        String questionType = questions.get(position).getquestionType();
        return questionTypes.get(questionType).getView(position,context.getCurrentIndex(),parent);
    }
}

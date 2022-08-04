package com.jeevesx.mainscreens;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.jeevesx.AppContext;
import com.jeevesx.R;
import com.jeevesx.firebase.FirebaseQuestion;
import com.jeevesx.firebase.FirebaseUtils;
import com.jeevesx.mainscreens.questions.QuAdapter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduleActivity extends SurveyActivity{

   // String startTimeVar;
   // String endTimeVar;
    QuAdapter customAdapter;
    public int getCurrentIndex(){
        return currentIndex;
    }
    private DatabaseReference scheduleRef;

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

     public void finishSurvey(){
         List<String> schedList = new ArrayList<>();
         for (int i = 0; i < answers.size(); i++) {
            String answer = answers.get(i);
            FirebaseQuestion correspondingQuestion = questions.get(i);

            if(i>1 && !answers.get(i).isEmpty()){
                schedList.add(answer);
            }
             SharedPreferences prefs = PreferenceManager
                 .getDefaultSharedPreferences(AppContext.getContext());
             SharedPreferences.Editor editor = prefs.edit();
            //For the date questions
            if (correspondingQuestion.getassignedVar() != null) {
                String varname = correspondingQuestion.getassignedVar();
                if (!answer.isEmpty()) {
                    editor.putString(varname, answer); //Put the variable into the var
                    Log.d("PREFCHANGE","name: " + varname + " and value: " + answer);
                }
            }

             editor.putInt(AppContext.SCHEDULE_DAY,1);
             editor.apply();
        }
        scheduleRef.setValue(schedList);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("AND SO", "IT BEGINS");
        String startDateVar = getIntent().getStringExtra(AppContext.START_DATE);
        String endDateVar = getIntent().getStringExtra(AppContext.END_DATE);
       // startTimeVar = getIntent().getStringExtra(AppContext.WAKE_TIME);
        //endTimeVar = getIntent().getStringExtra(AppContext.SLEEP_TIME);
        scheduleRef = FirebaseUtils.PATIENT_REF.child("schedule");

        questions = new ArrayList<>();
        FirebaseQuestion startDate = new FirebaseQuestion();
        startDate.setquestionType(AppContext.DATE);
        startDate.setQuestionText("Enter your scheduled START date");
        startDate.setassignedVar(startDateVar);
        questions.add(startDate);
        FirebaseQuestion endDate = new FirebaseQuestion();
        endDate.setquestionType(AppContext.DATE);
        endDate.setQuestionText("Now enter your scheduled END date");
        endDate.setassignedVar(endDateVar);
        questions.add(endDate);
        answers = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            answers.add("");
        }
        customAdapter = new QuAdapter(this, questions, answers);
        simpleAdapterViewFlipper.setAdapter(customAdapter);
        launchQuestion(questions.get(0));
    }

     public void launchQuestion(FirebaseQuestion question) {
        String questionText = question.getquestionText();
        TextView questionView = findViewById(R.id.txtQuestion);
        questionView.setText(questionText);
        simpleAdapterViewFlipper.setDisplayedChild(currentIndex);
         txtQNo = findViewById(R.id.txtQno);
         if(currentIndex > 1)
             txtQNo.setText(AppContext.NUMBERNAMES[currentIndex-2] + " day");//"Day " + (currentIndex-1));
         else
             txtQNo.setText("Update schedule");
    }

    public void backQ() {
        currentIndex--;
        if (currentIndex < 1)
            btnBack.setEnabled(false);
        launchQuestion(questions.get(currentIndex));
    }

    public void nextQ() {
        //Here this means that we're on the 'end date' question and away to move forward
        if(currentIndex == 1){
            //Get rid of all previously created schedule questions, if any
            List dateQs = new ArrayList();
            dateQs.add(questions.get(0));
            dateQs.add(questions.get(1));
            questions.retainAll(dateQs);

            String startDateStr = answers.get(0);
            String endDateStr = answers.get(1);
            if(Long.parseLong(endDateStr) < Long.parseLong(startDateStr)){
                Toast.makeText(this,"Your end date must be after your start date!",Toast.LENGTH_SHORT).show();
                return;
            }
            List dateAs = new ArrayList();
            dateAs.add(startDateStr);
            dateAs.add(endDateStr);
            answers.retainAll(dateAs);
            //Calculate the number of day-schedule questions needed
            final Calendar calendarStart = Calendar.getInstance();
            final Calendar calendarEnd = Calendar.getInstance();
            calendarStart.setTimeInMillis(Long.parseLong(startDateStr));
            calendarEnd.setTimeInMillis(Long.parseLong(endDateStr));
            int count = 1;

            //Add however many schedle qusetions we need between the start and end dates.
            do {
                FirebaseQuestion scheduleQ = new FirebaseQuestion();
                scheduleQ.setquestionType(AppContext.SCHEDULE);
                scheduleQ.setQuestionText("Please enter your wake and sleep times for your " + AppContext.NUMBERNAMES[count-1] + " day ");
                scheduleQ.setQuestionId(Integer.toString(count));

                questions.add(scheduleQ);
                answers.add("");
                calendarStart.add(Calendar.DAY_OF_MONTH,1);
                count++;
            }while(calendarStart.compareTo(calendarEnd) < 0);
            customAdapter.updateQsandAs(questions,answers);
            simpleAdapterViewFlipper.setAdapter(customAdapter);
        }
        if(currentIndex > 1){
            String times = answers.get(currentIndex);
            String[] startEnd = times.split(":");
            long start = Long.parseLong(startEnd[0]);
            long end = Long.parseLong(startEnd[1]);
            if(end <= start){
                Toast.makeText(this,"Your sleep time must be after your wake time",Toast.LENGTH_SHORT).show();
                return;
            }
            long startDate = Long.parseLong(answers.get(0));
            if(start < startDate){
                Toast.makeText(this,"First wake time must be on or after your scheduled start date",Toast.LENGTH_SHORT).show();
                return;
            }
            if(currentIndex > 2){
                String oldTimes = answers.get(currentIndex-1);
                startEnd = oldTimes.split(":");
                long oldEnd = Long.parseLong(startEnd[1]);
                if(start <= oldEnd){
                    Toast.makeText(this,"Wake time must be after your sleep time on the previous day!",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            long endDate = Long.parseLong(answers.get(1));
            if ((end > endDate)){
                AlertDialog.Builder finishalert = new AlertDialog.Builder(this);
                finishalert.setTitle("Your schedule has been updated");
                finishalert.setPositiveButton("Return", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finishSurvey();
                    }
                });
                finishalert.setCancelable(false); //Once they're done they're done
                finishalert.show();
                return;
            }
        }

        currentIndex++;
        if (currentIndex >= questions.size()){
            AlertDialog.Builder finishalert = new AlertDialog.Builder(this);
            finishalert.setTitle("Your schedule has been updated");
            finishalert.setPositiveButton("Return", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finishSurvey();
                }
            });
            finishalert.setCancelable(false); //Once they're done they're done
            finishalert.show();
            return; //safety net
        }
        btnBack.setEnabled(true);
        launchQuestion(questions.get(currentIndex));
    }
}
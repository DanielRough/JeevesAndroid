package com.jeevesandroid.mainscreens;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterViewFlipper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.jeevesandroid.AppContext;
import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseQuestion;
import com.jeevesandroid.firebase.FirebaseSurvey;
import com.jeevesandroid.firebase.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.jeevesandroid.mainscreens.questions.QuAdapter;
import com.jeevesandroid.triggers.config.TriggerConstants;
import com.jeevesandroid.triggers.triggers.TriggerUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import static com.jeevesandroid.AppContext.DATE;
import static com.jeevesandroid.AppContext.TIME;

public class SurveyActivity extends AppCompatActivity {

    private AdapterViewFlipper simpleAdapterViewFlipper;
    private final Handler handler = new Handler();
    private List<FirebaseQuestion> questions;
    private int currentIndex = 0;
    private List<String> answers; //For storing user's question data as we flip through
    private Button btnNext;
    private Button btnBack;
    private TextView txtQNo;
    private int currentQuestionCount = 0;
    private DatabaseReference surveyRef;
    private DatabaseReference completedSurveysRef;
    private boolean finished = false;
    private long timeSent = 0;
    private long initTime = 0;
    private int triggerType = 0;
    private FirebaseSurvey currentsurvey = null;

    public boolean getIsFast(){
        return currentsurvey.getfastTransition();
    }
    public Button getBtnNext(){
         return btnNext;
     }public Button getBtnBack(){
         return btnBack;
     }
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =
            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public int getCurrentIndex(){
        return currentIndex;
    }
    protected void onStop() {
        super.onStop();
        if(currentsurvey != null) //Sometimes it's null if activity is accessed from lock screen
            currentsurvey.setanswers(answers); //Save the partially completed stuff
        surveyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                surveyRef.removeEventListener(this);
                if (!finished)
                    surveyRef.setValue(currentsurvey);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }


    /**
     * All the functions that need to be performed when the user finishes a survey.
     *
     * - Format and concatenate all survey answers
     * - Update any variables contingent on answers
     * - Encrypt these answers
     * - Push answers and survey metadata to the database
     * - Send a 'completed' broadcast to listening survey triggers
     */
    private void finishSurvey(){
        currentsurvey.setanswers(null); //remove the unencoded answers
        currentsurvey.settimeFinished(System.currentTimeMillis());
        ArrayList<String> changedVariables = new ArrayList<>();
        StringBuilder allAnswers = new StringBuilder();
        for (int i = 0; i < answers.size(); i++) {
            String answer = answers.get(i);
            FirebaseQuestion correspondingQuestion = questions.get(i);
            String qType = correspondingQuestion.getquestionType();
            //Dates have a special format
            if(qType.equals(DATE)){
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(Long.parseLong(answer));
                    allAnswers.append(formatter.format(calendar.getTime())).append(";");
            }
            //Format time to be number of milliseconds since midnight on that day
            else if(qType.equals(TIME)){
                    final Calendar midnight = Calendar.getInstance();
                    midnight.set(Calendar.HOUR_OF_DAY, 0);
                    midnight.set(Calendar.MINUTE, 0);
                    DateFormat formatter = new SimpleDateFormat("hh:mm:ss", Locale.UK);
                    Calendar calendar = Calendar.getInstance();
                    long millitime = Long.parseLong(answer) + midnight.getTimeInMillis();
                    calendar.setTimeInMillis(millitime);
                    allAnswers.append(formatter.format(calendar.getTime())).append(";");
            }
            else{
                    allAnswers.append(answer).append(";");
            }

            //If this answer is assign to a variable
            if (correspondingQuestion.getassignedVar() != null) {
                SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(AppContext.getContext());
                String varname = currentsurvey.getquestions().get(i).getassignedVar();
                changedVariables.add(varname);
                SharedPreferences.Editor editor = prefs.edit();
                if (!answer.isEmpty()) {
                    editor.putString(varname, answer); //Put the variable into the var
                }
                editor.apply();
            }
        }
        //Encode answers with a symmetric key
        currentsurvey.setencodedAnswers(FirebaseUtils.symmetricEncryption(allAnswers.toString()));
        currentsurvey.setencodedKey(FirebaseUtils.getSymmetricKey());

        SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());

        Map<String,Object> surveymap = new HashMap<>();
        surveymap.put(AppContext.STATUS,1);

        //Then this was a button trigger and the init time doesn't count
        if(triggerType != TriggerUtils.TYPE_SENSOR_TRIGGER_BUTTON && initTime > timeSent)
            surveymap.put(AppContext.INIT_TIME,initTime-timeSent);
        surveymap.put(AppContext.COMPLETE,System.currentTimeMillis());
        surveymap.put(AppContext.TRIG_TYPE,triggerType);
        surveymap.put(AppContext.UID,prefs.getString(AppContext.UID,""));
        surveymap.put("encodedAnswers",currentsurvey.getencodedAnswers());
        surveymap.put("encodedKey",currentsurvey.getencodedKey());
        FirebaseUtils.SURVEY_REF.child(currentsurvey.getsurveyId()).push().setValue(surveymap);
        //Update the various Survey-relevant variables

        SharedPreferences.Editor editor = prefs.edit();
        //Update total completed surveys
        long totalCompletedSurveyCount = prefs.getLong(AppContext.COMPLETED_SURVEYS, 0);
        totalCompletedSurveyCount++;
        editor.putLong(AppContext.COMPLETED_SURVEYS, totalCompletedSurveyCount);

        //Update number of times THIS survey has been completed
        long thisCompletedSurveyCount = prefs.getLong(currentsurvey.gettitle() + "-Completed", 0);
        thisCompletedSurveyCount++;
        editor.putLong(currentsurvey.gettitle() + "-Completed", thisCompletedSurveyCount);
        editor.apply();

        //SEND A BROADCAST TO LISTENING SURVEY TRIGGERS
        Intent intended = new Intent();
        intended.setAction(TriggerConstants.ACTION_NAME_SURVEY_TRIGGER);
        intended.putExtra(AppContext.SURVEY_NAME, currentsurvey.gettitle());
        intended.putExtra("completed", thisCompletedSurveyCount);
        intended.putExtra("result", true);
        intended.putExtra(AppContext.TIME_SENT, currentsurvey.gettimeSent());
        intended.putExtra("changedVariables", changedVariables);
        sendBroadcast(intended);

        surveyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference newPostRef = completedSurveysRef.push();
                newPostRef.setValue(currentsurvey); //Maybe this needs tobe made explicit?
                surveyRef.removeEventListener(this);
                surveyRef.removeValue();
                handler.removeCallbacksAndMessages(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        finished = true;
        editor.putBoolean(AppContext.FINISHED_INTRODUCTION,true);
        editor.commit();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        setContentView(R.layout.activity_missed_survey);
        ActionBar actionBar = getSupportActionBar();

        setContentView(R.layout.activity_survey);

        String surveyid = getIntent().getStringExtra(AppContext.SURVEY_ID);
        initTime = getIntent().getLongExtra(AppContext.INIT_TIME,0);
        timeSent = getIntent().getLongExtra(AppContext.TIME_SENT,0);
        triggerType = getIntent().getIntExtra(AppContext.TRIG_TYPE,0);

        //If this is the Begin Trigger, there is nothing to go back to
        if(triggerType == TriggerUtils.TYPE_CLOCK_TRIGGER_BEGIN){
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);
        }
        else{
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }


        DatabaseReference missedRef = FirebaseUtils.SURVEY_REF.child(surveyid).child("missed");
        surveyRef = FirebaseUtils.PATIENT_REF.child("incomplete").child(surveyid);
        completedSurveysRef = FirebaseUtils.PATIENT_REF.child("complete");
        txtQNo = findViewById(R.id.txtQno);
        txtQNo.setText(getResources().getString(R.string.question) + " " + currentQuestionCount);


        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setEnabled(false);

        simpleAdapterViewFlipper =  findViewById(R.id.adapterViewFlipper);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextQ();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backQ();
            }
        });



        missedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long expiryTime = currentsurvey.getexpiryTime();
                if(expiryTime == 0)return;
                long expiryMillis = expiryTime * 60 * 1000;
                long deadline = timeSent + expiryMillis;
                long timeToGo = deadline - System.currentTimeMillis();
                if (timeToGo <= 0) {
                    final AlertDialog.Builder warningalert = new AlertDialog.Builder(getInstance());
                    warningalert.setCancelable(false);
                    warningalert.setTitle("Sorry, your time to complete this survey has expired");
                    if(currentsurvey.getfastTransition()){
                        finish();
                        return;
                    }
                    warningalert.setPositiveButton("Return", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    });
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!getInstance().isDestroyed())
                                //If the activity isn't running we don't want the timeout to happen
                                warningalert.show();
                        }
                    }, timeToGo);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        surveyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentsurvey = snapshot.getValue(FirebaseSurvey.class);
                if (currentsurvey != null) {
                    surveyRef.removeEventListener(this);
                    questions = currentsurvey.getquestions();

                    if (currentsurvey.getbegun()) {
                        timeSent = getIntent().getLongExtra("timeSent", 0);
                        answers = currentsurvey.getanswers(); //Pre-populated answers
                        int size = answers.size();
                        for (int i = 0; i < (questions.size() - size); i++)
                            answers.add("");
                    } else {
                        timeSent = getIntent().getLongExtra("timeSent", 0);
                        answers = new ArrayList<>();
                        for (int i = 0; i < questions.size(); i++)
                            answers.add("");
                    }
                    QuAdapter customAdapter = new QuAdapter(getInstance(), questions, answers);
                    simpleAdapterViewFlipper.setAdapter(customAdapter);
                    currentsurvey.setbegun(); //Confirm that this survey has been started
                    launchQuestion(questions.get(0), "forward");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private SurveyActivity getInstance() {
        return this;
    }

    private void launchQuestion(FirebaseQuestion question, String direction) {

        String questionText = question.getquestionText();

        //QUESTION SKIPPING
        FirebaseQuestion conditionQuestion = null;
        if (question.getconditionQuestion() != null)
            conditionQuestion = question.getconditionQuestion();

        if (conditionQuestion != null) {
            String questionid = conditionQuestion.getquestionId();
            int conditionQuestionIndex = 0;
            String conditionQuestionType = "";
            for (int i = 0; i < questions.size(); i++) {
                if (questions.get(i).getquestionId().equals(questionid)) {
                    conditionQuestionIndex = i;
                    conditionQuestionType = questions.get(i).getquestionType();
                    break;
                }
            }
            String expectedanswer = question.getconditionConstraints();
            String actualanswer = answers.get(conditionQuestionIndex);
            boolean satisfied = false;
            String[] constraints = expectedanswer.split(";");
            if (actualanswer.length() > 0)
                if (constraints.length > 1) {
                    switch (constraints[0]) {
                        case "less than":
                            if (Integer.parseInt(actualanswer) < Integer.parseInt(constraints[1]))
                                satisfied = true;
                            break;
                        case "more than":
                            if (Integer.parseInt(actualanswer) > Integer.parseInt(constraints[1]))
                                satisfied = true;
                            break;
                        case "equal to":
                            if (Integer.parseInt(actualanswer) == Integer.parseInt(constraints[1]))
                                satisfied = true;
                            break;
                        case "before":
                        case "after":
                            long constraintDateTime = Long.parseLong(constraints[1]);
                            long actualDateTime = Long.parseLong(actualanswer);
                            Calendar c = Calendar.getInstance();
                            c.set(Calendar.HOUR_OF_DAY, 0);
                            c.set(Calendar.MINUTE, 0);
                            c.set(Calendar.SECOND, 0);
                            long constraintT = 0;
                            if (conditionQuestionType.equals(DATE))
                                constraintT = constraintDateTime;
                            else if (Objects.equals(conditionQuestionType, TIME))
                                constraintT = c.getTimeInMillis() + constraintDateTime;

                            if (constraints[0].equals("before") && actualDateTime < constraintT)
                                satisfied = true;
                            if (constraints[0].equals("after") && actualDateTime > constraintT)
                                satisfied = true;
                            break;
                        default:
                            break;
                    }
                } else {
                    String[] potentialActualAnswers = actualanswer.split(";");
                    for (String answer : potentialActualAnswers) {
                        //If we just have one part, then the part is our expected answer
                        if (constraints[0].equals(answer))
                            satisfied = true;
                    }
                }
            if (!satisfied) {
                if (direction.equals("forward"))
                    nextQ();
                else if (direction.equals("back"))
                    backQ();
                return;
            }
        }
        if (direction.equals("forward"))
            currentQuestionCount++;
        else if (direction.equals("back"))
            currentQuestionCount--;

        TextView questionView = findViewById(R.id.txtQuestion);
        txtQNo.setText(getResources().getString(R.string.question) + " " + currentQuestionCount);
        simpleAdapterViewFlipper.setDisplayedChild(currentQuestionCount-1);
        questionView.setText(questionText);
    }

    public void backQ() {
        currentIndex--;
        if (currentIndex < 1)
            btnBack.setEnabled(false);
        launchQuestion(questions.get(currentIndex), "back");
    }


    public void nextQ() {
        if(currentIndex == questions.size())
            return;
        if(questions.get(currentIndex).getisMandatory() && answers.get(currentIndex).isEmpty()) {
            Toast.makeText(this,"This question is mandatory!",Toast.LENGTH_SHORT).show();
            return;
        }
        currentIndex++;
        if (currentIndex == questions.size()) {
            //If we want to skip extra dialogue boxes, just finish without dialogue box.
            if (currentsurvey.getfastTransition()){
                finishSurvey();
                return;
         }
            else {
                AlertDialog.Builder finishalert = new AlertDialog.Builder(this);
                finishalert.setTitle("Thank you!");
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
        if (currentIndex > questions.size()) return; //safety net
        btnBack.setEnabled(true);
        launchQuestion(questions.get(currentIndex), "forward");
    }
}
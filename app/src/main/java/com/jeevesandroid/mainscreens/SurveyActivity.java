package com.jeevesandroid.mainscreens;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jeevesandroid.AppContext;
import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseQuestion;
import com.jeevesandroid.firebase.FirebaseSurvey;
import com.jeevesandroid.firebase.FirebaseUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.jeevesandroid.sensing.heartrate.HeartRateMonitor;
import com.jeevesandroid.triggers.config.TriggerConstants;
import com.jeevesandroid.triggers.triggers.TriggerUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.jeevesandroid.AppContext.AUDIO;
import static com.jeevesandroid.AppContext.BOOLEAN;
import static com.jeevesandroid.AppContext.DATE;
import static com.jeevesandroid.AppContext.GEO;
import static com.jeevesandroid.AppContext.HEART;
import static com.jeevesandroid.AppContext.IMAGEPRESENT;
import static com.jeevesandroid.AppContext.MULT_MANY;
import static com.jeevesandroid.AppContext.MULT_SINGLE;
import static com.jeevesandroid.AppContext.NUMERIC;
import static com.jeevesandroid.AppContext.OPEN_ENDED;
import static com.jeevesandroid.AppContext.SCALE;
import static com.jeevesandroid.AppContext.TEXTPRESENT;
import static com.jeevesandroid.AppContext.TIME;

public class SurveyActivity extends AppCompatActivity
    implements GoogleApiClient.OnConnectionFailedListener,
    OnMapReadyCallback,MediaPlayer.OnPreparedListener {

    private static final ArrayList<String> viewFlipperOrdering = new ArrayList<>();
    static {viewFlipperOrdering.addAll(Arrays.asList(
        OPEN_ENDED,
        MULT_SINGLE,
        MULT_MANY,
        SCALE,
        DATE,
        GEO,
        BOOLEAN,
        NUMERIC,
        TIME,
        IMAGEPRESENT,
        TEXTPRESENT,
        HEART,
        AUDIO));}
    private final Handler handler = new Handler();
    private List<FirebaseQuestion> questions;
    private int currentIndex = 0;
    private List<String> answers; //For storing user's question data as we flip through
    private AlertDialog.Builder finishalert;
    private AlertDialog.Builder warningalert;
    private Button btnNext;
    private Button btnBack;
    private ViewFlipper viewFlipper;
    private EditText txtOpenEnded;
    private EditText txtNumeric;
    private RadioGroup grpBool;
    private RadioGroup grpMultSingle;
    private SeekBar seekBar;
 private LinearLayout grpMultMany;
    private PhotoView photoView;
    private TextView txtPresent;
    private long finalscore = 0;
    private TextView txtQNo;
    private int currentQuestionCount = 0;
    private GoogleMap map;
    private final int PLACE_PICKER_REQUEST = 1;
    private DatabaseReference surveyRef;
    private DatabaseReference missedRef;
    private DatabaseReference completedSurveysRef;
    private SharedPreferences prefs;
    private boolean finished = false;
    private long timeSent = 0;
    private long initTime = 0;
    private int triggerType = 0;
    private FirebaseSurvey currentsurvey = null;
    private final ArrayList<CheckBox> allBoxes = new ArrayList<>();

    private Map<String, Object> myparams; //The parameters of the current question

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
    private void finishSurvey(){
        currentsurvey.setanswers(null); //remove the unencoded answers
        currentsurvey.settimeFinished(System.currentTimeMillis());
        ArrayList<String> changedVariables = new ArrayList<>();
        StringBuilder concatAnswers = new StringBuilder();
        for (int i = 0; i < answers.size(); i++) {
            String answer = answers.get(i);
            FirebaseQuestion correspondingQuestion = questions.get(i);
            //Let's format the dates and times nicely
            switch (correspondingQuestion.getquestionType()) {
                case DATE: {
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(Long.parseLong(answer));
                    concatAnswers.append(formatter.format(calendar.getTime())).append(";");
                    break;
                }
                case TIME: {
                    final Calendar midnight = Calendar.getInstance();
                    midnight.set(Calendar.HOUR_OF_DAY, 0);
                    midnight.set(Calendar.MINUTE, 0);

                    DateFormat formatter = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
                    Calendar calendar = Calendar.getInstance();

                    Long millitime = Long.parseLong(answer) + midnight.getTimeInMillis();
                    calendar.setTimeInMillis(millitime);
                    concatAnswers.append(formatter.format(calendar.getTime())).append(";");
                    break;
                }
                default:
                    concatAnswers.append(answer).append(";");
                    break;
            }
            if (correspondingQuestion.getassignedVar() != null) { //If we need to assign this answer to a variable
                String varname = currentsurvey.getquestions().get(i).getassignedVar();
                changedVariables.add(varname);
                SharedPreferences.Editor editor = prefs.edit();
                if (!answer.isEmpty()) {
                    editor.putString(varname, answer); //Put the variable into the var
                }
                editor.apply();
            }
            if (correspondingQuestion.getparams() != null && correspondingQuestion.getparams().containsKey("assignToScore"))
                if (!answer.isEmpty())
                    finalscore += Long.parseLong(answer);
        }
        //Encode answers with a symmetric key
        currentsurvey.setencodedAnswers(FirebaseUtils.symmetricEncryption(concatAnswers.toString()));
        currentsurvey.setencodedKey(FirebaseUtils.getSymmetricKey());
        currentsurvey.setscore(finalscore);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());

        Map<String,Object> surveymap = new HashMap<>();
        surveymap.put(AppContext.STATUS,1);
        if(triggerType != TriggerUtils.TYPE_SENSOR_TRIGGER_BUTTON && initTime > timeSent) //Then this was a button trigger and the init time doesn't count
            surveymap.put(AppContext.INIT_TIME,initTime-timeSent);
        surveymap.put(AppContext.COMPLETE,System.currentTimeMillis());
        surveymap.put(AppContext.TRIG_TYPE,triggerType);
        surveymap.put(AppContext.UID,prefs.getString(AppContext.UID,""));
        surveymap.put("encodedAnswers",currentsurvey.getencodedAnswers());
        surveymap.put("encodedKey",currentsurvey.getencodedKey());
        FirebaseUtils.SURVEY_REF.child(currentsurvey.getsurveyId()).push().setValue(surveymap);
        //Update the various Survey-relevant variables

        SharedPreferences.Editor editor = prefs.edit();
        long oldscore = prefs.getLong(AppContext.LAST_SURVEY_SCORE, 0);
        long difference = finalscore - oldscore;
        editor.putLong(AppContext.LAST_SURVEY_SCORE, finalscore);
        editor.putLong(AppContext.SURVEY_SCORE_DIFF, difference);
        long totalCompletedSurveyCount = prefs.getLong(AppContext.COMPLETED_SURVEYS, 0);
        totalCompletedSurveyCount++;
        editor.putLong(AppContext.COMPLETED_SURVEYS, totalCompletedSurveyCount);
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error", "we have an error THERE");

            }
        });
        finished = true;

        //We should write this to Shared Preferences so that,
        // if the app closes, we know we've already
        //finished the introductory survey
        editor.putBoolean(AppContext.FINISHED_INTRODUCTION,finished);
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
        //FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

        if(triggerType == TriggerUtils.TYPE_CLOCK_TRIGGER_BEGIN){
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);
        }
        else{
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());

        missedRef = FirebaseUtils.SURVEY_REF.child(surveyid).child("missed");
        surveyRef = FirebaseUtils.PATIENT_REF.child("incomplete").child(surveyid);
        completedSurveysRef = FirebaseUtils.PATIENT_REF.child("complete");

        txtOpenEnded = findViewById(R.id.txtOpenEnded);
        txtNumeric = findViewById(R.id.txtNumeric);
        grpBool = findViewById(R.id.grpBool);
        grpMultMany = findViewById(R.id.grpMultMany);
        grpMultSingle = findViewById(R.id.grpMultSingle);
       // grpScale = ((RadioGroup) findViewById(R.id.grpScale));
        seekBar = findViewById(R.id.seekBar);
        photoView = findViewById(R.id.photo_view2);
        txtPresent = findViewById(R.id.txtPresent);
        txtQNo = findViewById(R.id.txtQno);
        txtQNo.setText(String.format(getResources().getString(R.string.question),currentQuestionCount));

        finishalert = new AlertDialog.Builder(this);
        finishalert.setTitle("Thank you!");

        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setEnabled(false);

        viewFlipper = findViewById(R.id.viewFlipper);

        Animation slide_in_left = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left);
        Animation slide_out_right = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right);
        viewFlipper.setInAnimation(slide_in_left);
        viewFlipper.setOutAnimation(slide_out_right);

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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Patient finished the survey!
        finishalert.setPositiveButton("Return", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finishSurvey();
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

                    warningalert = new AlertDialog.Builder(getInstance());
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
                            if (!getInstance().isDestroyed()) //If the activity isn't running we don't want the timeout to happen
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

                    currentsurvey.setbegun(); //Confirm that this survey has been started

                    /*
                    long expiryTime = currentsurvey.getexpiryTime();
                    long expiryMillis = expiryTime * 60 * 1000;
                    long deadline = timeSent + expiryMillis;
                    long timeToGo = deadline - System.currentTimeMillis();

                    Log.d("FOUND YOU","Time to go is " +timeToGo);
                    //User has missed the survey, having previously triggered the notification
                    if (timeToGo <= 0) {

                        warningalert = new AlertDialog.Builder(getInstance());
                        warningalert.setCancelable(false);
                        warningalert.setTitle("Sorry, your time to complete this survey has expired");
                        if(currentsurvey.getfastTransition()){
                            finish();
                        }
                        warningalert.setPositiveButton("Return", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                finish();
                            }
                        });
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!getInstance().isDestroyed()) //If the activity isn't running we don't want the timeout to happen
                                    warningalert.show();
                            }
                        }, timeToGo);
                    }*/
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

    private void handleOpenEnded() {
        String answer = answers.get(currentIndex);
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
            }
        });
        txtOpenEnded.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

    }

    private void handleImageView(){
        photoView.setVisibility(View.VISIBLE);
        if(myparams == null)return;
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        String imageName = (String)options.get("image");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference gsReference = storage
            .getReferenceFromUrl("gs://jeeves-27914.appspot.com/" + imageName);
        final File localFile;
        File externalDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if(!externalDir.exists())
            externalDir.mkdirs();

        localFile = new File(externalDir,imageName);
        if(localFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            photoView.setImageBitmap(myBitmap);
            return;
        }
        gsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
            Bitmap myBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            photoView.setImageBitmap(myBitmap);
            }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception exception) {
            exception.printStackTrace();
            }
        });
    }
    private void handleTextView(){
        txtPresent.setVisibility(View.VISIBLE);
        if(myparams == null)return;
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        String textToShow = (String)options.get("text");
        txtPresent.setText(textToShow);
    }
    private void handleMultSingle() {
        grpMultSingle.removeAllViews();
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        Iterator<Object> opts = options.values().iterator();
        ArrayList<RadioButton> allButtons = new ArrayList<>();
        while (opts.hasNext()) {
            String option = opts.next().toString();
            final RadioButton button = new RadioButton(this);
            button.setText(option);
            button.setTextSize(40);
            grpMultSingle.addView(button);
            allButtons.add(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    answers.set(currentIndex, button.getText().toString());
                    //If we're rapidly transitioning, skip right away
                    if(currentsurvey.getfastTransition())
                        nextQ();
                }
            });
        }
        String answer = answers.get(currentIndex);
        if (!answer.isEmpty())
            for (RadioButton but : allButtons) {
                if (but.getText().equals(answer))
                    but.setChecked(true);
            }
        else
            answers.set(currentIndex, "");
    }

    private void handleMultMany() {
        grpMultMany.removeAllViews();
        allBoxes.clear();
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        for (Object o : options.values()) {
            String option = o.toString();
            CheckBox box = new CheckBox(this);
            box.setText(option);
            box.setTextSize(40);
            grpMultMany.addView(box);
            allBoxes.add(box);
            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    StringBuilder newanswers = new StringBuilder();
                    for (CheckBox allBox : allBoxes) {
                        if (allBox.isChecked())
                            newanswers.append(allBox.getText().toString()).append(",");
                    }
                    answers.set(currentIndex, newanswers.toString());
                }
            });
        }

        String answer = answers.get(currentIndex);
        if (!answer.isEmpty()) {
            String[] allanswers = answer.split(",");
            for (String ans : allanswers) {
                for (CheckBox box : allBoxes)
                    if (box.getText().equals(ans))
                        box.setChecked(true);

            }
        } else
            answers.set(currentIndex, "");

    }

    private void handleScale() {
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        int entries = Integer.parseInt(options.get("number").toString());
        ArrayList<String> labels = (ArrayList<String>) options.get("labels");
        TextView txtBegin = findViewById(R.id.txtBegin);
        TextView txtMiddle = findViewById(R.id.txtMiddle);
        TextView txtEnd = findViewById(R.id.txtEnd);
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
                if(currentsurvey.getfastTransition())
                    nextQ();            }
        });
        //Do this here so that it gets filled with a default value if the user doesn't touch it
        if (!answer.isEmpty())
            seekBar.setProgress(Integer.parseInt(answer));
        else {
            seekBar.setProgress(entries / 2);
            answers.set(currentIndex, Integer.toString(entries/2));
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void handleDate() {
        final Calendar calendar = Calendar.getInstance();
        final DatePicker picker = findViewById(R.id.datePicker2);
        String answer = answers.get(currentIndex);
        picker.init(calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
                answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()));
            }
        });

        if (!answer.isEmpty()) {
            calendar.setTimeInMillis(Long.parseLong(answer));
            picker.updateDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        }
        else if(questions.get(currentIndex).getassignedVar() != null){
            String var = questions.get(currentIndex).getassignedVar();
            Log.d("Assigned","assigned var is " + var);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
            String varval = prefs.getString(var,"");
            if(varval.equals(""))
                answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()));
            else{
                //DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date d = new Date();
                d.setTime(Long.parseLong(varval));
                    //Date savedDate = formatter.format(varval);
                    calendar.setTime(d);
                    answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()));
                    picker.updateDate(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));


            }
        }
        else {
            answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()));
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void handleTime() {
        final Calendar calendar = Calendar.getInstance();
        final Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        TimePicker tpicker = findViewById(R.id.timePicker2);
        String answer = answers.get(currentIndex);
        if (!answer.isEmpty()) {
            calendar.setTimeInMillis(midnight.getTimeInMillis() + Long.parseLong(answer));
            tpicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            tpicker.setMinute(calendar.get(Calendar.MINUTE));
        }
        else if(questions.get(currentIndex).getassignedVar() != null){
            String var = questions.get(currentIndex).getassignedVar();
            Log.d("Assigned","assigned var is " + var);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
            String varval = prefs.getString(var,"");
            if(varval.equals(""))
                answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()- midnight.getTimeInMillis()));
            else{
                calendar.setTimeInMillis(midnight.getTimeInMillis() + Long.parseLong(varval));
                answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()- midnight.getTimeInMillis()));
                tpicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
                tpicker.setMinute(calendar.get(Calendar.MINUTE));
            }
        }
        else {
            answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()- midnight.getTimeInMillis()));
        }
        tpicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                calendar.set(Calendar.HOUR_OF_DAY, i);
                calendar.set(Calendar.MINUTE, i1);
                long msFromMidnight = calendar.getTimeInMillis() - midnight.getTimeInMillis();
                answers.set(currentIndex, Long.toString(msFromMidnight));
            }
        });


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = place.getName().toString();

                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                TextView txtPlaceName = findViewById(R.id.txtPlaceName);
                txtPlaceName.setText(toastMsg);
                LatLng coords = place.getLatLng();
                map.moveCamera(CameraUpdateFactory.newLatLng(coords));
                map.addMarker(new MarkerOptions()
                        .position(coords)
                        .title(place.getName().toString()));
                String answer = coords.latitude + ":" + coords.longitude + ";";
                answers.set(currentIndex, answer);
            }
        }
        if(requestCode == 1234){ //code i've defined for heart but cba making a constant
            Button btnStart = findViewById(R.id.btnStart);
            btnStart.setText(getResources().getString(R.string.heartrate));
            btnStart.setEnabled(false);
            PhotoView heartview = findViewById(R.id.heartview);
            heartview.setImageResource(R.drawable.fingerdone);
            int result = data.getIntExtra("result",0);
            answers.set(currentIndex,Integer.toString(result));

        }

    }

    private void handleGeo() {
        final TextView txtPlaceName = findViewById(R.id.txtPlaceName);

        String answer = answers.get(currentIndex);
        if (!answer.isEmpty()) {
            String[] locationbits = answer.split(":");
            double latitude = Double.parseDouble(locationbits[0]);
            double longitude = Double.parseDouble(locationbits[1]);
            LatLng coords = new LatLng(latitude, longitude);
            String placename = locationbits[2];
            txtPlaceName.setText(placename);
            map.moveCamera(CameraUpdateFactory.newLatLng(coords));
            map.addMarker(new MarkerOptions()
                    .position(coords)
                    .title(placename));
        } else {
            answers.set(currentIndex, "");

        }

        Button btnPlacePicker = findViewById(R.id.btnPlacePicker);

        btnPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(getInstance()), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void handleNumeric() {
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
                    hideKeyboard(v);
                }
            }
        });
    }

    private void handleBoolean() {
        grpBool.removeAllViews();
        RadioButton trueButton = new RadioButton(this);
        trueButton.setText(getResources().getString(R.string.yes));
        trueButton.setTextSize(40);
        RadioButton falseButton = new RadioButton(this);
        falseButton.setText(getResources().getString(R.string.no));
        falseButton.setTextSize(40);
        grpBool.addView(trueButton);
        grpBool.addView(falseButton);
        trueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answers.set(currentIndex, "true");
                if(currentsurvey.getfastTransition())
                    nextQ();
            }
        });
        falseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answers.set(currentIndex, "false");
                if(currentsurvey.getfastTransition())
                    nextQ();
            }
        });

        String answer = answers.get(currentIndex);
        if (answer != null && !answer.equals(""))
            if (answer.equals("true"))
                trueButton.setChecked(true);
            else
                falseButton.setChecked(true);
        else
            answers.set(currentIndex, "");
    }

    private void handleAudio(){
        if(myparams == null)return;
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        final String audioName = (String)options.get("audio");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference gsReference = storage
            .getReferenceFromUrl("gs://jeeves-27914.appspot.com/" + audioName);
        final File localFile;
        final File externalDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if(!externalDir.exists())
            externalDir.mkdirs();
        final View.OnClickListener pauseListenr = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getInstance(),
                    "Please listen to the whole clip!",Toast.LENGTH_SHORT).show();
            }
        };
        final Button btnStart = findViewById(R.id.audioBtnStart);
        final Button btnPause = findViewById(R.id.audioBtnPause);

        btnNext.setOnClickListener(pauseListenr);
        btnBack.setOnClickListener(pauseListenr);
        final MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextQ();
                    }
                });
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        backQ();
                    }
                });
                btnStart.setEnabled(true);
                btnPause.setEnabled(false);
                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btnStart.setEnabled(false);
                        btnPause.setEnabled(true);
                        btnNext.setOnClickListener(pauseListenr);
                        btnBack.setOnClickListener(pauseListenr);
                        }
                });
                btnPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btnStart.setEnabled(true);
                        btnPause.setEnabled(false);
                    }
                });
                mediaPlayer.stop();
                mediaPlayer.reset();
                try {
                    File f = new File(externalDir,audioName);
                    final Uri myUri = Uri.fromFile(new File(f.getAbsolutePath()));
                    mediaPlayer.setDataSource(getApplicationContext(), myUri);
                    mediaPlayer.setOnPreparedListener(getInstance());
                    mediaPlayer.prepareAsync(); // prepare async to not block main thread

                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }
        });
        localFile = new File(externalDir,audioName);
        final Uri myUri = Uri.fromFile(new File(localFile.getAbsolutePath()));
        if(localFile.exists()){
            try {
                mediaPlayer.setDataSource(getApplicationContext(), myUri);
                mediaPlayer.setOnPreparedListener(getInstance());
                mediaPlayer.prepareAsync(); // prepare async to not block main thread
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        gsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
            try {
                mediaPlayer.setDataSource(getApplicationContext(), myUri);
                mediaPlayer.setOnPreparedListener(getInstance());
                mediaPlayer.prepareAsync(); // prepare async to not block main thread
            } catch (IOException e) {
                e.printStackTrace();
            }                }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception exception) {
            exception.printStackTrace();
            }
        });

        PhotoView audio = findViewById(R.id.audioPhotoview);
        audio.setImageResource(R.drawable.finger);


    }
    /** Called when MediaPlayer is ready */
    public void onPrepared(final MediaPlayer player) {
        final Button btnStart = findViewById(R.id.audioBtnStart);
        final Button btnPause = findViewById(R.id.audioBtnPause);
        PhotoView audioview = findViewById(R.id.audioPhotoview);
        audioview.setImageResource(R.drawable.audio);
        btnStart.setEnabled(true);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStart.setEnabled(false);
                btnPause.setEnabled(true);
                player.start();
            }
        });
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStart.setEnabled(true);
                btnPause.setEnabled(false);
                player.pause();
            }
        });
    }
    private void handleHeart(){
        if(answers.get(currentIndex).isEmpty()){
            Button btnStart = findViewById(R.id.btnStart);
            btnStart.setText(getResources().getString(R.string.startsensing));
            btnStart.setEnabled(true);
            PhotoView heartview = findViewById(R.id.heartview);
            heartview.setImageResource(R.drawable.finger);
        }
        else{
            Button btnStart = findViewById(R.id.btnStart);
            btnStart.setText(getResources().getString(R.string.heartrate));
            btnStart.setEnabled(false);
            PhotoView heartview = findViewById(R.id.heartview);
            heartview.setImageResource(R.drawable.fingerdone);
        }
        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getInstance(), HeartRateMonitor.class);
                startActivityForResult(intent, 1234);
            }
        });
    }

    private void launchQuestion(FirebaseQuestion question, String direction) {

        String questionText = question.getquestionText();
        String questionType = question.getquestionType();
        myparams = question.getparams();

        //QUESTION SKIPPING
        FirebaseQuestion conditionQuestion = null;
//            if(myparams != null)
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
            //TODO: This whole section is absolutely shite and it's just to get it working.
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
                            long constraintMillis = 0;
                            if (conditionQuestionType.equals(DATE))
                                constraintMillis = constraintDateTime;
                            else if (Objects.equals(conditionQuestionType, TIME))
                                constraintMillis = c.getTimeInMillis() + constraintDateTime;

                            if (constraints[0].equals("before") && actualDateTime < constraintMillis)
                                satisfied = true;
                            if (constraints[0].equals("after") && actualDateTime > constraintMillis)
                                satisfied = true;
                            break;
                    }
                } else {
                    String[] potentialActualAnswers = actualanswer.split(";");
                    for (String answer : potentialActualAnswers) {
                        if (constraints[0].equals(answer)) //If we just have one part, then the part is our expected answer
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
        txtQNo.setText(String.format(getResources().getString(R.string.question),currentQuestionCount));

        if(questionType.equals(IMAGEPRESENT)){
            questionView.setVisibility(View.INVISIBLE);
            viewFlipper.setVisibility(View.INVISIBLE);
            txtPresent.setVisibility(View.INVISIBLE);
            handleImageView();
            return;
        }
        else if(questionType.equals(TEXTPRESENT)){

            ScrollView scroller = findViewById(R.id.scroll);
            scroller.setVisibility(View.VISIBLE);
            photoView.setVisibility(View.INVISIBLE);
            questionView.setVisibility(View.INVISIBLE);
            viewFlipper.setVisibility(View.INVISIBLE);

            handleTextView();
            return;

        }

        questionView.setVisibility(View.VISIBLE);
        viewFlipper.setVisibility(View.VISIBLE);
        txtPresent.setVisibility(View.INVISIBLE);
        photoView.setVisibility(View.INVISIBLE);
        ScrollView scroller = findViewById(R.id.scroll);
        scroller.setVisibility(View.INVISIBLE);
    //Cheap hack for now
        switch (questionType) {
            case HEART:
                viewFlipper.setDisplayedChild(11);
                break;
            case AUDIO:
                viewFlipper.setDisplayedChild(12);
                break;
            default:
                viewFlipper.setDisplayedChild(viewFlipperOrdering.indexOf(questionType));
                break;
        }
        switch (questionType) {
            case OPEN_ENDED:
                handleOpenEnded();
                break;
            case MULT_SINGLE:
                handleMultSingle();
                break;
            case MULT_MANY:
                handleMultMany();
                break;
            case SCALE:
                handleScale();
                break;
            case DATE:
                handleDate();
                break;
            case TIME:
                handleTime();
                break;
            case GEO:
                handleGeo();
                break;
            case BOOLEAN:
                handleBoolean();
                break;
            case NUMERIC:
                handleNumeric();
                break;
            case HEART:
                handleHeart();
                break;
            case AUDIO:
                handleAudio();
                break;
        }
        questionView.setText(questionText);
    }

    private void backQ() {
        currentIndex--;
        if (currentIndex < 1)
            btnBack.setEnabled(false);
        launchQuestion(questions.get(currentIndex), "back");
    }


    private void nextQ() {
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
                finishalert.setCancelable(false); //Once they're done they're done
                finishalert.show();
                return;
            }
        }
        if (currentIndex > questions.size()) return; //safety net
        btnBack.setEnabled(true);
        launchQuestion(questions.get(currentIndex), "forward");
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        LatLng sydney = new LatLng(-33.867, 151.206);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));
        map.getUiSettings().setZoomControlsEnabled(true);
        Criteria criteria = new Criteria();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        double lat, lng;
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
        } else {
            lat = 56.341703;
            lng = -2.792;
        }
        LatLng coordinate = new LatLng(lat, lng);
        map.moveCamera(CameraUpdateFactory.zoomTo(10));

        map.moveCamera(CameraUpdateFactory.newLatLng(coordinate)); //This oughta put our camera at the current location
       // String latlong = "Lat: " + coordinate.latitude + ",  Long: " + coordinate.longitude;
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng GoogleMap) {
                MarkerOptions options = new MarkerOptions();
                MarkerOptions opts = options.position(GoogleMap);
                map.clear();
                map.addMarker(opts);
                answers.set(currentIndex, opts.getPosition().latitude + ":" + opts.getPosition().longitude);
                map.moveCamera(CameraUpdateFactory.newLatLng(GoogleMap)); //This oughta put our camera at the current location
                map.animateCamera(CameraUpdateFactory.zoomTo(16));
            }
        });
    }

}
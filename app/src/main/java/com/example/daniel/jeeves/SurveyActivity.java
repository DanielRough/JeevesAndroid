package com.example.daniel.jeeves;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.daniel.jeeves.firebase.FirebaseQuestion;
import com.example.daniel.jeeves.firebase.FirebaseSurvey;
import com.example.daniel.jeeves.firebase.FirebaseUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ubhave.triggermanager.config.TriggerManagerConstants;
import com.ubhave.triggermanager.triggers.TriggerUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SurveyActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {
    public static final int OPEN_ENDED = 1;
    public static final int MULT_SINGLE = 2;
    public static final int MULT_MANY = 3;
    public static final int SCALE = 4;
    public static final int DATE = 5;
    public static final int GEO = 6;
    public static final int BOOLEAN = 7;
    public static final int NUMERIC = 8;
    public static final int TIME = 9;
    final Handler handler = new Handler();
    List<FirebaseQuestion> questions;
    int currentIndex = 0;
    List<String> answers; //For storing user's question data as we flip through
    AlertDialog.Builder finishalert;
    AlertDialog.Builder warningalert;
    Button btnNext;
    Button btnBack;
    ViewFlipper viewFlipper;
    EditText txtOpenEnded;
    EditText txtNumeric;
    RadioGroup grpBool;
    RadioGroup grpMultSingle;
    RadioGroup grpScale;
    LinearLayout grpMultMany;
    long finalscore = 0;
    TextView txtQNo;
    int currentQuestionCount = 1;
    String latlong;
    Animation slide_in_left, slide_out_right;
    GoogleMap map;
    int PLACE_PICKER_REQUEST = 1;
    DatabaseReference surveyRef;
    DatabaseReference completedSurveysRef;
    FirebaseAuth mFirebaseAuth;
    SharedPreferences prefs;
    boolean finished = false;
    long timeSent = 0;
    long initTime = 0;
    int triggerType = 0;
    FirebaseSurvey currentsurvey = null;
    ArrayList<CheckBox> allBoxes = new ArrayList<CheckBox>();

    private String surveyid;
    private Map<String, Object> myparams; //The parameters of the current question
    private GoogleApiClient mGoogleApiClient;

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    protected void onStop() {
        super.onStop();
        currentsurvey.setanswers(answers); //Save the partially completed stuff
        surveyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                surveyRef.removeEventListener(this);
                if (finished == false)
                    surveyRef.setValue(currentsurvey);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Error", "we have an error here");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_missed_survey);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_survey);

        surveyid = getIntent().getStringExtra("surveyid");
        initTime = getIntent().getLongExtra("initTime",0);
        timeSent = getIntent().getLongExtra("timeSent",0);
        triggerType = getIntent().getIntExtra("triggertype",0);
        mFirebaseAuth = FirebaseAuth.getInstance();

        prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());

        surveyRef = FirebaseUtils.PATIENT_REF.child("incomplete").child(surveyid);
        completedSurveysRef = FirebaseUtils.PATIENT_REF.child("complete");

        txtOpenEnded = ((EditText) findViewById(R.id.txtOpenEnded));
        txtNumeric = ((EditText) findViewById(R.id.txtNumeric));
        grpBool = ((RadioGroup) findViewById(R.id.grpBool));
        grpMultMany = ((LinearLayout) findViewById(R.id.grpMultMany));
        grpMultSingle = ((RadioGroup) findViewById(R.id.grpMultSingle));
        grpScale = ((RadioGroup) findViewById(R.id.grpScale));

        txtQNo = ((TextView) findViewById(R.id.txtQno));
        txtQNo.setText("Question 1");
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        finishalert = new AlertDialog.Builder(this);
        finishalert.setTitle("Thank you!");

        btnNext = ((Button) findViewById(R.id.btnNext));
        btnBack = ((Button) findViewById(R.id.btnBack));
        btnBack.setEnabled(false);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);

        slide_in_left = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left);
        slide_out_right = AnimationUtils.loadAnimation(this,
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
                currentsurvey.setanswers(null); //remove the unencoded answers
                currentsurvey.settimeFinished(System.currentTimeMillis());
                ArrayList<String> changedVariables = new ArrayList<String>();
                String concatAnswers = "";
                for (int i = 0; i < answers.size(); i++) {
                    String answer = answers.get(i);
                    FirebaseQuestion correspondingQuestion = questions.get(i);
                    concatAnswers += answer + ";";
                    if (correspondingQuestion.getassignedVar() != null) { //If we need to assign this answer to a variable
                        String varname = currentsurvey.getquestions().get(i).getassignedVar();
                        changedVariables.add(varname);
                        SharedPreferences.Editor editor = prefs.edit();
                        if (!answer.isEmpty()) {
                            editor.putString(varname, answer); //Put the variable into the var
                        }
                        editor.commit();
                    }
                    if (correspondingQuestion.getparams() != null && correspondingQuestion.getparams().containsKey("assignToScore"))
                        if (!answer.isEmpty())
                            finalscore += Long.parseLong(answer);
                }
                currentsurvey.setencodedAnswers(FirebaseUtils.encodeAnswers(concatAnswers));
                currentsurvey.setscore(finalscore);

                //Add the relevant information to the survey ref of this project
                //We need to know that it was completed, the time it took for the patient to begin, and the time it took them to finish.
                //We also need to know what it was that triggered the survey (i.e. button press, sensor trigger, etc).
                Map<String,Object> surveymap = new HashMap<String,Object>();
                surveymap.put("status",1);
                if(triggerType != TriggerUtils.TYPE_SENSOR_TRIGGER_BUTTON) //Then this was a button trigger and the init time doesn't count
                    surveymap.put("inittime",initTime-timeSent);
                surveymap.put("completetime",System.currentTimeMillis()-initTime);
                surveymap.put("trigger",triggerType);
                FirebaseUtils.SURVEY_REF.child(currentsurvey.gettitle()).push().setValue(surveymap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d("RESUUUUUULT","Data could not be saved " + databaseError.getMessage());
                        } else {
                            Log.d("RESUUUUUULT","Data saved successfully.");
                        }
                    }
                });
                //Update the various Survey-relevant variables
                SharedPreferences.Editor editor = prefs.edit();
                long oldscore = prefs.getLong("Last Survey Score", 0);
                long difference = finalscore - oldscore;
                editor.putLong("Last Survey Score", finalscore);
                editor.putLong("Survey Score Difference", difference);
                long totalCompletedSurveyCount = prefs.getLong("Completed Surveys", 0);
                totalCompletedSurveyCount++;
                editor.putLong("Completed Surveys", totalCompletedSurveyCount);
                long thisCompletedSurveyCount = prefs.getLong(currentsurvey.gettitle() + "-Completed", 0);
                thisCompletedSurveyCount++;
                editor.putLong(currentsurvey.gettitle() + "-Completed", thisCompletedSurveyCount);
                editor.commit();

                //SEND A BROADCAST TO LISTENING SURVEY TRIGGERS
                Intent intended = new Intent();
                intended.setAction(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER);
                intended.putExtra("surveyName", currentsurvey.gettitle());
                intended.putExtra("completed", thisCompletedSurveyCount);
                intended.putExtra("result", true);
                intended.putExtra("timeSent", currentsurvey.gettimeSent()); //need this for removing SurveyAction notifications
                intended.putExtra("changedVariables", changedVariables);
                Log.d("NAME", currentsurvey.gettitle());
                Log.d("COMPLETED", thisCompletedSurveyCount + "!");
                sendBroadcast(intended);
                surveyRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        DatabaseReference newPostRef = completedSurveysRef.push();
                        newPostRef.setValue(currentsurvey); //Maybe this needs tobe made explicit?
                        surveyRef.removeEventListener(this);
                        surveyRef.removeValue();
                        handler.removeCallbacksAndMessages(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("Error", "we have an error THERE");

                    }
                });
                finished = true;
                finish();
            }
        });

        surveyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                currentsurvey = snapshot.getValue(FirebaseSurvey.class);
                if (currentsurvey != null) {
                    surveyRef.removeEventListener(this);
                    questions = currentsurvey.getquestions();

                    if (currentsurvey.getbegun()) {
//                        if (getIntent().getBooleanExtra("manual", false)) {
                            timeSent = getIntent().getLongExtra("timeSent", 0);
//                        } else {
//                            timeSent = currentsurvey.gettimeSent();
//                        }
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


                    long expiryTime = currentsurvey.getexpiryTime();
                    long expiryMillis = expiryTime * 60 * 1000;
                    long deadline = timeSent + expiryMillis;
                    Log.d("TImeSSENT",timeSent+"");
                    Log.d("Expeiry millis",expiryMillis+"");
                    long timeToGo = deadline - System.currentTimeMillis();
                    Log.d("TIME TO GO","Time to go is " + timeToGo);
                    //User has missed the survey, having previously triggered the notification
                    if (timeToGo > 0) {

                        warningalert = new AlertDialog.Builder(getInstance());
                        warningalert.setCancelable(false);
                        warningalert.setTitle("Sorry, your time to complete this survey has expired");
                        warningalert.setPositiveButton("Return", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intended = new Intent();
                                intended.setAction(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER);
                                intended.putExtra("surveyName", currentsurvey.gettitle());
                                intended.putExtra("result", false);

                                //This is the same thing as in the 'Survey Action' class. It adds the missed
                                //survey information to the database (as soon as the user misses it of course)
                                Map<String,Object> surveymap = new HashMap<String,Object>();
                                surveymap.put("status",0);
                                if(initTime != timeSent) //Then this was a button trigger and the init time doesn't count
                                    surveymap.put("inittime",initTime-timeSent);
//                                surveymap.put("inittime",initTime);
                                surveymap.put("trigger",triggerType);
                                FirebaseUtils.SURVEY_REF.child(currentsurvey.gettitle()).push().setValue(surveymap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Log.d("RESUUUUUULT","Data could not be saved " + databaseError.getMessage());
                                        } else {
                                            Log.d("RESUUUUUULT","Data saved successfully.");
                                        }
                                    }
                                });

                                prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
                                SharedPreferences.Editor editor = prefs.edit();
                                long totalMissedSurveyCount = prefs.getLong("Missed Surveys", 0);
                                totalMissedSurveyCount++;
                                editor.putLong("Missed Surveys", totalMissedSurveyCount);
                                editor.commit();
                                long thisMissedSurveyCount = prefs.getLong(currentsurvey.gettitle() + "-Missed", 0);
                                thisMissedSurveyCount++;
                                editor.putLong(currentsurvey.gettitle() + "-Missed", thisMissedSurveyCount);
                                editor.commit();
                                intended.putExtra("missed", thisMissedSurveyCount);
                                sendBroadcast(intended);
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
                    Log.d("Questions", "questions are " + questions.toString());
                    launchQuestion(questions.get(0), "forward");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Error", "we have an error EVERYWHERE" + databaseError.getDetails() + databaseError.getMessage());
                Log.d("CAUSE", databaseError.getDetails());
            }

        });


    }

    public SurveyActivity getInstance() {
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

    private void handleMultSingle() {
        grpMultSingle.removeAllViews();
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        Iterator<Object> opts = options.values().iterator();
        ArrayList<RadioButton> allButtons = new ArrayList<RadioButton>();
        while (opts.hasNext()) {
            String option = opts.next().toString();
            final RadioButton button = new RadioButton(this);
            button.setText(option);
            button.setTextSize(20);
            grpMultSingle.addView(button);
            allButtons.add(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    answers.set(currentIndex, button.getText().toString());
                }
            });
        }
        String answer = answers.get(currentIndex);
        if (!answer.isEmpty())
            for (RadioButton but : allButtons) {
                if (but.getText().equals(answer.toString()))
                    but.setChecked(true);
            }
        else
            answers.set(currentIndex, "");
    }

    private void handleMultMany() {
        grpMultMany.removeAllViews();
        allBoxes.clear();
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        Iterator<Object> opts = options.values().iterator();
        while (opts.hasNext()) {
            String option = opts.next().toString();
            CheckBox box = new CheckBox(this);
            box.setText(option);
            box.setTextSize(20);
            grpMultMany.addView(box);
            allBoxes.add(box);
            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String newanswers = "";
                    for (CheckBox allBox : allBoxes) {
                        if (allBox.isChecked())
                            newanswers += allBox.getText().toString() + ";";
                    }
                    answers.set(currentIndex, newanswers);
                }
            });
        }

        String answer = answers.get(currentIndex);
        if (!answer.isEmpty()) {
            String[] allanswers = answer.split(";");
            for (String ans : allanswers) {
                for (CheckBox box : allBoxes)
                    if (box.getText().equals(ans))
                        box.setChecked(true);

            }
        } else
            answers.set(currentIndex, "");

    }

    private void handleScale() {
        grpScale.removeAllViews();
        grpScale.clearCheck();
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        int entries = Integer.parseInt(options.get("number").toString());
        ArrayList<String> labels = (ArrayList<String>) options.get("labels");
        String answer = answers.get(currentIndex);
        for (int i = 0; i < entries; i++) {
            final RadioButton button = new RadioButton(this);
            button.setId(i + 1);
            button.setText((i + 1) + "   " + labels.get(i));
            button.setTextSize(20);
            grpScale.addView(button);
            if (!answer.isEmpty() && Integer.parseInt(answer) == (i + 1)) {
                button.setChecked(true);
            }
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    answers.set(currentIndex, Integer.toString(button.getId()));
                }
            });
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void handleDate() {
        final Calendar calendar = Calendar.getInstance();
        final DatePicker picker = (DatePicker) findViewById(R.id.datePicker2);
        String answer = answers.get(currentIndex);
        picker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
                answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()));
            }
        });

        if (!answer.isEmpty()) {
            String time = answer.toString();
            calendar.setTimeInMillis(Long.parseLong(time));
            picker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        } else {
            answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()));
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void handleTime() {
        final Calendar calendar = Calendar.getInstance();
        final Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        TimePicker tpicker = (TimePicker) findViewById(R.id.timePicker2);
        String answer = answers.get(currentIndex);
        tpicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                calendar.set(Calendar.HOUR_OF_DAY, i);
                calendar.set(Calendar.MINUTE, i1);
                long msFromMidnight = calendar.getTimeInMillis() - midnight.getTimeInMillis();
                answers.set(currentIndex, Long.toString(msFromMidnight));

            }
        });

        if (!answer.isEmpty()) {
            String time = answer.toString();
            calendar.setTimeInMillis(midnight.getTimeInMillis() + Long.parseLong(time));
            tpicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            tpicker.setMinute(calendar.get(Calendar.MINUTE));
        } else {
            answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()- midnight.getTimeInMillis()));
        }


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = place.getName().toString();

                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                TextView txtPlaceName = (TextView) findViewById(R.id.txtPlaceName);
                txtPlaceName.setText(toastMsg);
                LatLng coords = place.getLatLng();
                map.moveCamera(CameraUpdateFactory.newLatLng(coords)); //This oughta put our camera at the current location
                map.addMarker(new MarkerOptions()
                        .position(coords)
                        .title(place.getName().toString()));
                String answer = coords.latitude + ";" + coords.longitude + ";";
                answers.set(currentIndex, answer);
            }
        }
    }

    private void handleGeo() throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        final TextView txtPlaceName = (TextView) findViewById(R.id.txtPlaceName);

        String answer = answers.get(currentIndex);
        if (!answer.isEmpty()) {
            String location = answer.toString();
            String[] locationbits = location.split(";");
            double latitude = Double.parseDouble(locationbits[0]);
            double longitude = Double.parseDouble(locationbits[1]);
            LatLng coords = new LatLng(latitude, longitude);
            String placename = locationbits[2];
            txtPlaceName.setText(placename);
            map.moveCamera(CameraUpdateFactory.newLatLng(coords)); //This oughta put our camera at the current location
            map.addMarker(new MarkerOptions()
                    .position(coords)
                    .title(placename));
        } else {
            answers.set(currentIndex, "");

        }

        Button btnPlacePicker = (Button) findViewById(R.id.btnPlacePicker);

        btnPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(getInstance()), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

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
        trueButton.setText("Yes");
        trueButton.setTextSize(24);
        RadioButton falseButton = new RadioButton(this);
        falseButton.setText("No");
        falseButton.setTextSize(24);
        grpBool.addView(trueButton);
        grpBool.addView(falseButton);
        trueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answers.set(currentIndex, "true");
            }
        });
        falseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answers.set(currentIndex, "false");
            }
        });

        String answer = answers.get(currentIndex);
        if (answer != null && !answer.equals(""))
            if (answer.toString().equals("true"))
                trueButton.setChecked(true);
            else
                falseButton.setChecked(true);
        else
            answers.set(currentIndex, "");
    }

    private void launchQuestion(FirebaseQuestion question, String direction) {

        String questionText = question.getquestionText();
        int questionType = (int) question.getquestionType();
        myparams = question.getparams();

        //QUESTION SKIPPING
        FirebaseQuestion conditionQuestion = null;
//            if(myparams != null)
        if (question.getconditionQuestion() != null)
            conditionQuestion = question.getconditionQuestion();

        if (conditionQuestion != null) {
            String questionid = conditionQuestion.getquestionId();
            int conditionQuestionIndex = 0;
            long conditionQuestionType = 0;
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
                            if (conditionQuestionType == DATE)
                                constraintMillis = constraintDateTime;
                            else if (conditionQuestionType == TIME)
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
            if (satisfied) {
            } else { //This should hopefully skip to the next question
                if (direction.equals("forward"))
                    nextQ();
                else if (direction.equals("back"))
                    backQ();
                return;
            }

        }
        txtQNo.setText("Question " + (currentQuestionCount));

        viewFlipper.setDisplayedChild(questionType - 1);
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
                try {
                    handleGeo();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
                break;
            case BOOLEAN:
                handleBoolean();
                break;
            case NUMERIC:
                handleNumeric();
                break;
        }
        TextView questionView = (TextView) findViewById(R.id.txtQuestion);
        questionView.setText(questionText);
    }

    public void backQ() {
        currentIndex--;

        if (currentIndex < 1)
            btnBack.setEnabled(false);
        currentQuestionCount--;
        launchQuestion(questions.get(currentIndex), "back");

    }


    public void nextQ() {
        currentIndex++;
        currentQuestionCount++;
        if (currentIndex == questions.size()) {
            finishalert.setCancelable(false); //Once they're done they're done
            finishalert.show();
            return;
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        latlong = "Lat: " + coordinate.latitude + ",  Long: " + coordinate.longitude;
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng GoogleMap) {
                MarkerOptions options = new MarkerOptions();
                MarkerOptions opts = options.position(GoogleMap);
                map.clear();
                map.addMarker(opts);
                answers.set(currentIndex, opts.getPosition().latitude + ";" + opts.getPosition().longitude);
                Log.d("MAP", "just set it to " + answers.get(currentIndex));
                map.moveCamera(CameraUpdateFactory.newLatLng(GoogleMap)); //This oughta put our camera at the current location
                map.animateCamera(CameraUpdateFactory.zoomTo(16));
            }
        });
    }

}
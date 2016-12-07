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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.daniel.jeeves.firebase.FirebaseQuestion;
import com.example.daniel.jeeves.firebase.FirebaseSurvey;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ubhave.triggermanager.config.TriggerManagerConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import DateTimePicker.DateTimePicker;

public class SurveyActivity extends AppCompatActivity  implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {
    private int currentQuestion = 0;
    private String surveyid;
    List<FirebaseQuestion> questions;
    private Map<String, Object> myparams; //The parameters of the current question
    List<Map<String, String>> questiondata; //For storing user's question data as we flip through
    Map<String, String> currentData;
    AlertDialog.Builder finishalert;
    AlertDialog.Builder warningalert;
    Button btnNext;
    Button btnBack;
    ViewFlipper viewFlipper;
    EditText txtOpenEnded;
    EditText txtNumeric;
    Switch switchBool;
    RadioGroup grpMultSingle;
    LinearLayout grpMultMany;
    RatingBar ratingBar;

    TextView txtQNo;
    String latlong, locationGroup;
    public static final int OPEN_ENDED = 1;
    public static final int MULT_SINGLE = 2;
    public static final int MULT_MANY = 3;
    public static final int SCALE = 4;
    public static final int DATETIME = 5;
    public static final int GEO = 6;
    public static final int BOOLEAN = 7;
    public static final int NUMERIC = 8;
    Animation slide_in_left, slide_out_right;
    GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    int PLACE_PICKER_REQUEST = 1;
    private DateTimePicker picker;
    DatabaseReference surveyRef;
    DatabaseReference completedSurveysRef;
    FirebaseAuth mFirebaseAuth;

    //  Firebase firebaseSurvey;
   // Firebase completedSurveys;
//    DatabaseReference firebaseSurvey;
//    DatabaseReference completedSurveys;
    final Handler handler = new Handler();
    boolean finished = false;
    long timeSent = 0;

    FirebaseSurvey currentsurvey = null;

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    protected void onStop() {
        super.onStop();
        Log.d("STOPPED", "Gotta stop here");
        currentsurvey.setanswers(questiondata); //Save the partially completed stuff
        surveyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                surveyRef.removeEventListener(this);
                Map<String, Object> value = (Map<String, Object>) snapshot.getValue();
                //completedSurveys.setValue(value);
                if (finished == false)
                    surveyRef.setValue(currentsurvey);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Error","we have an error here");
            }
        });
    }

    private int missedSurveys;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_missed_survey);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_survey);


        surveyid = getIntent().getStringExtra("surveyid");

        Context app = ApplicationContext.getContext();
        mFirebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        String userid = user.getUid();
        prefs = app.getSharedPreferences("userprefs", Context.MODE_PRIVATE);
        String surveyname = getIntent().getStringExtra("name");
         missedSurveys = prefs.getInt(surveyname,0);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        surveyRef = database.getReference("JeevesData").child("patients").child(userid).child("incomplete").child(surveyname).child(surveyid);
        completedSurveysRef = database.getReference("JeevesData").child("patients").child(userid).child("complete");

        txtOpenEnded = ((EditText) findViewById(R.id.txtOpenEnded));
        txtNumeric = ((EditText) findViewById(R.id.txtNumeric));
        switchBool = ((Switch) findViewById(R.id.switchBool));
        grpMultMany = ((LinearLayout) findViewById(R.id.grpMultMany));
        grpMultSingle = ((RadioGroup) findViewById(R.id.grpMultSingle));
        ratingBar = ((RatingBar) findViewById(R.id.ratingBar));
        picker = ((DateTimePicker) findViewById(R.id.DateTimePicker));
        DateTimePicker.DateWatcher watcher = new DateTimePicker.DateWatcher() {

            @Override
            public void onDateChanged(Calendar c) {
                currentData.put("text",picker.getYear()+":"+picker.getMonth()+":"+picker.getDay()+":"+picker.getHour()+":"+picker.getMinute());
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, picker.getYear());
                cal.set(Calendar.MONTH, picker.getMonth(picker.getMonth()));
                cal.set(Calendar.DAY_OF_MONTH, picker.getDay());
                cal.set(Calendar.HOUR_OF_DAY, picker.getHour());
                cal.set(Calendar.MINUTE, picker.getMinute());
                Date dateRepresentation = cal.getTime();

                currentData.put("answer",Long.toString(dateRepresentation.getTime()));
                Log.d("CHANGED",currentData.get("answer"));

            }
        };
        DateTimePicker.TimeWatcher timewatcher = new DateTimePicker.TimeWatcher() {
            @Override
            public void onTimeChanged(int h, int m, int am_pm) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, picker.getYear());
                cal.set(Calendar.MONTH, picker.getMonth(picker.getMonth()));
                cal.set(Calendar.DAY_OF_MONTH, picker.getDay());
                cal.set(Calendar.HOUR_OF_DAY, picker.getHour());
                cal.set(Calendar.MINUTE, picker.getMinute());
                Date dateRepresentation = cal.getTime();

                currentData.put("answer",Long.toString(dateRepresentation.getTime()));
                currentData.put("text",picker.getYear()+":"+picker.getMonth()+":"+picker.getDay()+":"+picker.getHour()+":"+picker.getMinute());
                Log.d("CHANGED",currentData.get("answer"));


            }
        };
        picker.setDateChangedListener(watcher);
        picker.setTimeChangedListener(timewatcher);
        txtQNo = ((TextView) findViewById(R.id.txtQno));
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        finishalert = new AlertDialog.Builder(this);
        finishalert.setTitle("Thank you!");
        // alert.setMessage("Message");
        btnNext = ((Button) findViewById(R.id.btnNext));
        btnBack = ((Button) findViewById(R.id.btnBack));
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
        //        setUpMapIfNeeded();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        finishalert.setPositiveButton("Return", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                currentsurvey.setanswers(questiondata);
                currentsurvey.settimeFinished(System.currentTimeMillis());
                long finalscore = 0;
                for (int i = 0; i < questiondata.size(); i++){
                    Map<String, String> stringStringMap = questiondata.get(i);
                    String answer = stringStringMap.get("answer");
                    if(currentsurvey.getquestions().get(i).getparams() != null && !currentsurvey.getquestions().get(i).getparams().get("assignedVar").equals("")){ //If we need to assign this answer to a variable
                        String varname = currentsurvey.getquestions().get(i).getparams().get("assignedVar").toString();
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(varname,answer); //Put the variable into the var
                        Log.d("VARIABLE ASSIGN", "Assigned the variable " + varname + " to value " + answer);
                        editor.commit();
                    }
                    if(stringStringMap.containsKey("score")){
                        String value = stringStringMap.get("score");
                        long score = Long.parseLong(value);
                        finalscore += score; // Accumulate scores!
                    }
                }
                currentsurvey.setscore(finalscore);
                SharedPreferences.Editor editor = prefs.edit();
                long oldscore = prefs.getLong("Last Survey Score",0);
                long difference = finalscore - oldscore;
                editor.putLong("Last Survey Score",finalscore);
                editor.putLong("Survey Score Difference", difference);
                long completedSurveyCount = prefs.getLong("Completed Surveys",0);
                completedSurveyCount++;
                editor.putLong("Completed Surveys", completedSurveyCount);
                editor.commit();

                //SEND A BROADCAST TO LISTENING SURVEY TRIGGERS
                Intent intended = new Intent();
                intended.setAction(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER);
                intended.putExtra("surveyName",currentsurvey.getname());
                sendBroadcast(intended);
                surveyRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();
                        DatabaseReference newPostRef = completedSurveysRef.push();
                        newPostRef.setValue(currentsurvey); //Maybe this needs tobe made explicit?
                        surveyRef.removeEventListener(this);
                        surveyRef.removeValue();
                        handler.removeCallbacksAndMessages(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("Error","we have an error THERE");

                    }
                });
                finished = true;
                finish();
            }
        });

        surveyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d("ERROR","ACtually no errorTTT!");

                currentsurvey = snapshot.getValue(FirebaseSurvey.class);
             //   surveyRef.removeEventListener(this);
                if (currentsurvey != null) {
                    surveyRef.removeEventListener(this);
                    questions = currentsurvey.getquestions();

                    if (currentsurvey.getbegun()) {
                        if(getIntent().getBooleanExtra("manual",false)) {
                            timeSent = getIntent().getLongExtra("timeSent", 0);
                            Log.d("ERROR","ACtually no error!");
                        }
                            else {
                            timeSent = currentsurvey.gettimeSent();
                            Log.d("ERROR","Nope didn't work");
                        }
                            questiondata = currentsurvey.getanswers(); //Pre-populated answers
                   //     if (questiondata.size() < questions.size())
                            for (int i = 0; i < (questions.size() - questiondata.size()); i++)
                                questiondata.add(new HashMap<String, String>());
                    } else {
                        Log.d("ERROR","ACtually no error777!");

                        timeSent = getIntent().getLongExtra("timeSent", 0);
                        questiondata = new ArrayList<>();
                        for (int i = 0; i < questions.size(); i++)
                            questiondata.add(new HashMap<String, String>());
                    }

                        currentsurvey.setbegun(); //Confirm that this survey has been started


                    long expirytime = currentsurvey.gettimeAlive() * 1000;
                    long timeGone = System.currentTimeMillis() - timeSent;
                    long timeLeft = expirytime - timeGone;
                    if(expirytime > 0) {
                        warningalert = new AlertDialog.Builder(getInstance());
                        warningalert.setTitle("Sorry, your time to complete this survey has expired");
                        warningalert.setPositiveButton("Return", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intended = new Intent();
                                intended.setAction(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER);
                                intended.putExtra("surveyName",currentsurvey.getname());
                                intended.putExtra("result",false);
                                missedSurveys++; //The user has officially missed this survey

                                SharedPreferences prefs = getSharedPreferences("userprefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                long missedSurveyCount = prefs.getLong("Missed Surveys",0);
                                missedSurveyCount++;
                                editor.putLong("Missed Surveys", missedSurveyCount);
                                editor.putInt(currentsurvey.getname(),missedSurveys);
                                editor.commit();

                                //But wait, how many have they missed already?
                                intended.putExtra("missed",missedSurveys);
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
                        }, timeLeft);
                    }
                    Log.d("Questions", "questions are " + questions.toString());
                    launchQuestion(questions.get(0));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Error","we have an error EVERYWHERE");

            }

        });


    }

    public SurveyActivity getInstance() {
        return this;
    }

    private void handleOpenEnded() {
        String answer = currentData.get("answer");
        if (answer != null && !answer.equals(""))
            txtOpenEnded.setText(answer);
        else {
            currentData.put("answer", "");
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
                currentData.put("answer", txtOpenEnded.getText().toString());
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
            grpMultSingle.addView(button);
            allButtons.add(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentData.put("answer", button.getText().toString());
                }
            });
        }
        String answer = currentData.get("answer");
        if (answer != null && !answer.equals(""))
            for (RadioButton but : allButtons) {
                if (but.getText().equals(answer.toString()))
                    but.setChecked(true);
            }
        else
            currentData.put("answer", "");
    }

    ArrayList<CheckBox> allBoxes = new ArrayList<CheckBox>();

    private void handleMultMany() {
        grpMultMany.removeAllViews();
        allBoxes.clear();
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        Iterator<Object> opts = options.values().iterator();
        while (opts.hasNext()) {
            String option = opts.next().toString();
            CheckBox box = new CheckBox(this);
            box.setText(option);
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
                    currentData.put("answer", newanswers);
                }
            });
        }

        String answer = currentData.get("answer");
        if (answer != null && !answer.equals("")) {
            String[] allanswers = answer.split(";");
            for (String ans : allanswers) {
                for (CheckBox box : allBoxes)
                    if (box.getText().equals(ans))
                        box.setChecked(true);

            }
        } else
            currentData.put("answer", "");

    }

    private void handleScale() {

        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        int to = Integer.parseInt(options.get("to").toString());
        ratingBar.setNumStars(to);
        String answer = currentData.get("answer");
        if (answer != null && !answer.equals(""))
            ratingBar.setRating(Integer.parseInt(answer.toString()));
        else {
            currentData.put("answer", "");
            ratingBar.setProgress(0);
        }
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                currentData.put("answer", Integer.toString((int)rating));
                if((boolean)myparams.get("assignToScore") == true) //Only log the score if we're supposed to
                    currentData.put("score", Integer.toString(((int)rating)));
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void handleDateTime() {
        String answer = currentData.get("textanswer");
        if (answer != null && !answer.equals("")) {
            String time = answer.toString();
            String[] dayshoursmins = time.split(":");
                picker.setYear(dayshoursmins[0]);
                picker.setMonth(dayshoursmins[1]);
                picker.setDay(dayshoursmins[2]);
                picker.setHour(dayshoursmins[3]);
                picker.setMinute(dayshoursmins[4]);
//            timePicker.setHour(Integer.parseInt(hoursmins[0]));
//            timePicker.setMinute(Integer.parseInt(hoursmins[1]));
        } else {
            currentData.put("textanswer", "");
//            timePicker.setHour(0);
//            timePicker.setMinute(0);
        }


    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = place.getName().toString();

                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                TextView txtPlaceName = (TextView)findViewById(R.id.txtPlaceName);
                txtPlaceName.setText(toastMsg);
                LatLng coords = place.getLatLng();
                map.moveCamera(CameraUpdateFactory.newLatLng(coords)); //This oughta put our camera at the current location
                map.addMarker(new MarkerOptions()
                        .position(coords)
                        .title(place.getName().toString()));
                String answer = "";
                answer = coords.latitude + ";" + coords.longitude + ";" + place.getName().toString();
                currentData.put("answer",answer);
            }
        }
    }

    private void handleGeo() throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        final TextView txtPlaceName = (TextView)findViewById(R.id.txtPlaceName);

        String answer = currentData.get("answer");
        if (answer != null && !answer.equals("")) {
            String location = answer.toString();
            String[] locationbits = location.split(";");
            double latitude = Double.parseDouble(locationbits[0]);
            double longitude = Double.parseDouble(locationbits[1]);
            LatLng coords = new LatLng(latitude,longitude);
            String placename = locationbits[2];
            txtPlaceName.setText(placename);
            map.moveCamera(CameraUpdateFactory.newLatLng(coords)); //This oughta put our camera at the current location
            map.addMarker(new MarkerOptions()
                    .position(coords)
                    .title(placename));
        } else {
            currentData.put("answer", "");

        }

        Button btnPlacePicker = (Button)findViewById(R.id.btnPlacePicker);

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
            if (currentData.get("answer") != null)
                txtNumeric.setText(currentData.get("answer"));
            else {
                currentData.put("answer", "");
                txtNumeric.setText("");
            }
                txtNumeric.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    currentData.put("answer", txtNumeric.getText().toString());
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
            if (currentData.get("answer") != null)
                switchBool.setChecked(Boolean.parseBoolean(currentData.get("answer").toString()));
            else {
                currentData.put("answer", "");
                switchBool.setChecked(false);
            }
                switchBool.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            currentData.put("answer", Boolean.toString(isChecked));
                        }
                    }
            );
        }

        private void launchQuestion(FirebaseQuestion question) {
            String questionText = question.getquestionText();
            int questionType = (int) question.getquestionType();
            myparams = question.getparams();
            txtQNo.setText("Question " + (currentQuestion+1) + "/"+questiondata.size());
            viewFlipper.setDisplayedChild(questionType - 1);
            currentData = questiondata.get(currentQuestion);

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
                case DATETIME:
                    handleDateTime();
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
            //  viewFlipper.showPrevious();
            Log.d("BACK", "Going back");
            currentQuestion--;
            if (currentQuestion < 1)
                btnBack.setEnabled(false);
            launchQuestion(questions.get(currentQuestion));
        }


        public void nextQ() {
            // viewFlipper.showNext();
            Log.d("FWD", "Going forward");
            currentQuestion++;
            if (currentQuestion == questions.size()) {
                Log.d("FINISH", "IAMFINISHED");
                finishalert.setCancelable(false); //Once they're done they're done
                finishalert.show();
                return;
                //    finish();
            }

            btnBack.setEnabled(true);
            launchQuestion(questions.get(currentQuestion));
        }




    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Error","we have an error OSJDFLKADSJFLKAS");

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        LatLng sydney = new LatLng(-33.867, 151.206);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
                map.moveCamera(CameraUpdateFactory.newLatLng(GoogleMap)); //This oughta put our camera at the current location
                map.animateCamera(CameraUpdateFactory.zoomTo(16));
            }
        });
        //  mMap.animateCamera(CameraUpdateFactory.zoomBy(13));
        // Initialize map options. For example:
        // mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

    }

}
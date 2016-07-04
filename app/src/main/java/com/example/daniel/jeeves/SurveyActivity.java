package com.example.daniel.jeeves;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
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
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.example.daniel.jeeves.firebase.FirebaseQuestion;
import com.example.daniel.jeeves.firebase.FirebaseSurvey;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.lang.reflect.Method;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SurveyActivity extends AppCompatActivity implements OnMapReadyCallback{
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
        TimePicker timePicker;
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
        Firebase firebaseSurvey;
        Firebase completedSurveys;
        final Handler handler = new Handler();
    long timeSent = 0;

    FirebaseSurvey currentsurvey = null;

    public void hideKeyboard(View view) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        @Override
        protected void onStop(){
            super.onStop();
            Log.d("STOPPED", "Gotta stop here");
            currentsurvey.setanswers(questiondata); //Save the partially completed stuff
            firebaseSurvey.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    firebaseSurvey.removeEventListener(this);
                    Map<String,Object> value = (Map<String,Object>)snapshot.getValue();
                    //completedSurveys.setValue(value);
                    firebaseSurvey.setValue(currentsurvey);
                }
                @Override public void onCancelled(FirebaseError error) { }
            });
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_survey);


            surveyid = getIntent().getStringExtra("surveyid");

            Context app = ApplicationContext.getContext();
            SharedPreferences prefs = app.getSharedPreferences("userprefs",Context.MODE_PRIVATE);
            String userid = prefs.getString("userid","null");
            firebaseSurvey = new Firebase("https://incandescent-torch-8695.firebaseio.com/patients/"+userid+"/incomplete/"+surveyid);
            completedSurveys = new Firebase("https://incandescent-torch-8695.firebaseio.com/patients/"+userid+"/complete");
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            txtOpenEnded = ((EditText) findViewById(R.id.txtOpenEnded));
            txtNumeric = ((EditText) findViewById(R.id.txtNumeric));
            switchBool = ((Switch) findViewById(R.id.switchBool));
            grpMultMany = ((LinearLayout) findViewById(R.id.grpMultMany));
            grpMultSingle = ((RadioGroup) findViewById(R.id.grpMultSingle));
            ratingBar = ((RatingBar) findViewById(R.id.ratingBar));
            timePicker = ((TimePicker) findViewById(R.id.timePicker));


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

            finishalert.setPositiveButton("Return", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    currentsurvey.setanswers(questiondata);
                    currentsurvey.settimeFinished(System.currentTimeMillis());
//                    firebaseSurvey.child("answers").setValue(questiondata);
//                    firebaseSurvey.child("timeFinished").setValue(System.currentTimeMillis());
                    firebaseSurvey.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            Map<String,Object> value = (Map<String,Object>)snapshot.getValue();
                            Firebase newPostRef = completedSurveys.push();
                            newPostRef.setValue(currentsurvey); //Maybe this needs tobe made explicit?
                            //completedSurveys.setValue(value);
                            firebaseSurvey.removeEventListener(this);
                            firebaseSurvey.removeValue();
                            handler.removeCallbacksAndMessages(null);
                        }
                        @Override public void onCancelled(FirebaseError error) { }
                    });
                    Intent data = new Intent();
                    data.putExtra("surveykey",surveyid);
                    if (getParent() == null) {
                        setResult(Activity.RESULT_OK, data);
                    } else {
                        getParent().setResult(Activity.RESULT_OK, data);
                    }
                    finish();
                }
            });

            String surveyname = getIntent().getStringExtra("name");
            //TextView texty = (TextView) findViewById(R.id.txtSurveyName);
           // texty.setText("Survey: " + surveyname);
//            List<FirebaseSurvey> surveys = ApplicationContext.getProject().getsurveys();
//            for (FirebaseSurvey survey : surveys) {
//                Log.d("Here", "SURVEY NAME IS " + survey.getname());
//                if (survey.getname().equals(surveyname)) {
//                    currentsurvey = survey;
//                    break;
//                }
//            }

            firebaseSurvey.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Log.d("SNAPPYSHOT",snapshot.getValue().toString());
                    currentsurvey = snapshot.getValue(FirebaseSurvey.class);
                    firebaseSurvey.removeEventListener(this);
                    if (currentsurvey != null) {
                        questions = currentsurvey.getquestions();

                        if(currentsurvey.getbegun()) {
                            timeSent = currentsurvey.gettimeSent();
                            questiondata = currentsurvey.getanswers(); //Pre-populated answers
                            if(questiondata.size() < questions.size())
                                for(int i = 0; i < (questions.size() - questiondata.size()); i++)
                                    questiondata.add(new HashMap<String,String>());
                        }
                        else {
                            timeSent = getIntent().getLongExtra("timeSent", 0);
                            questiondata = new ArrayList<>();
                            for (int i = 0; i < questions.size(); i++)
                                questiondata.add(new HashMap<String, String>());
                        }
                            currentsurvey.setbegun(); //Confirm that this survey has been started


                        long expirytime = currentsurvey.gettimeAlive()*1000;
                        long timeGone = System.currentTimeMillis() - timeSent;
                        long timeLeft = expirytime - timeGone;
                        warningalert = new AlertDialog.Builder(getInstance());
                        warningalert.setTitle("Sorry, your time to complete this survey has expired");
                        warningalert.setPositiveButton("Return", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent data = new Intent();
                                data.putExtra("surveykey",surveyid);
                                if (getParent() == null) {
                                    setResult(Activity.RESULT_OK, data);
                                } else {
                                    getParent().setResult(Activity.RESULT_OK, data);
                                }
                                finish();
                            }
                        });
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!getInstance().isDestroyed()) //If the activity isn't running we don't want the timeout to happen
                                warningalert.show();

                            }
                        }, timeLeft);

                        Log.d("Questions", "questions are " + questions.toString());
                        launchQuestion(questions.get(0));
                    }
                }
                @Override public void onCancelled(FirebaseError error) {
                    Log.d("ERROR",error.toString());
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
                        currentData.put("answer",button.getText().toString());
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
                                if(allBox.isChecked())
                                    newanswers += allBox.getText().toString() + ";";
                            }
                            currentData.put("answer",newanswers);
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
                ratingBar.setRating(Float.parseFloat(answer.toString()));
            else {
                currentData.put("answer", "");
                ratingBar.setProgress(0);
            }
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    currentData.put("answer",Float.toString(rating));
                }
            });
        }

        @TargetApi(Build.VERSION_CODES.M)
        private void handleDateTime() {
            String answer = currentData.get("answer");
            if (answer != null && !answer.equals("")) {
                String time = answer.toString();
                String[] hoursmins = time.split(":");
                timePicker.setHour(Integer.parseInt(hoursmins[0]));
                timePicker.setMinute(Integer.parseInt(hoursmins[1]));
            } else {
                currentData.put("answer", "");
                timePicker.setHour(0);
                timePicker.setMinute(0);
            }
                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                    currentData.put("answer", Integer.toString(hourOfDay) + ":" + Integer.toString(minute));
                }
            });
        }

        private void handleGeo() {

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
                    handleGeo();
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
            btnNext.setText("Next");
            if (currentQuestion == 0)
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
            if (currentQuestion == questions.size() - 1)
                btnNext.setText("Finish");
            launchQuestion(questions.get(currentQuestion));
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
            ((TextView) findViewById(R.id.txtLatLong)).setText("Lat: " + coordinate.latitude + ",  Long: " + coordinate.longitude);
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
                    ((TextView) findViewById(R.id.txtLatLong)).setText("Lat: " + GoogleMap.latitude + ",  Long: " + GoogleMap.longitude);
                    latlong = "Lat: " + GoogleMap.latitude + ",  Long: " + GoogleMap.longitude;
                }
            });
            //  mMap.animateCamera(CameraUpdateFactory.zoomBy(13));
            // Initialize map options. For example:
            // mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        }


}
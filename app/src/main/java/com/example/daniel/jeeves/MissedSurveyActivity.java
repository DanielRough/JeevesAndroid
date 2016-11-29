package com.example.daniel.jeeves;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.daniel.jeeves.firebase.FirebasePatient;
import com.example.daniel.jeeves.firebase.FirebaseSurvey;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MissedSurveyActivity extends ListActivity {
    static final int PICK_CONTACT_REQUEST = 1;  // The request code
    ListView list;
    Context app = ApplicationContext.getContext();
    SharedPreferences prefs = app.getSharedPreferences("userprefs", Context.MODE_PRIVATE);
   // final String userid = prefs.getString("userid", "null");
    FirebaseAuth mFirebaseAuth;
    HashMap<String,FirebaseSurvey> tempMap;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("RESUME","I have been resumed");
        final ArrayList<String> array = new ArrayList<String>();
        mFirebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        String userid = user.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference firebaseSurvey = database.getReference("JeevesData").child("patients").child(userid);
//        final DatabaseReference firebaseSurvey = new Firebase("https://incandescent-torch-8695.firebaseio.com/JeevesData/patients/" + userid);
        firebaseSurvey.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
               // firebaseSurvey.removeEventListener(this); //Get rid of it again or it causes problems
                Log.i("OOGABOOGAGFJGFJKH", "zobhkhuhkhjhjkhjkhjkhjhjhjkhjkhj");

                Log.d("Snappyshotooooo", snapshot.getValue().toString());
                //FirebasePatient patient = (FirebasePatient)snapshot.getValue();
                FirebasePatient patient = (FirebasePatient) snapshot.getValue(FirebasePatient.class); //THIS IS HOW YOU DO IT TO AVOID MAKING IT A HASHMAP
                Map<String, Map<String,FirebaseSurvey>> missedSurveys = patient.getincomplete(); //Oh wow this is horrific. Absolutely horrific.
                if (missedSurveys == null) return;
                ArrayList<FirebaseSurvey> surveynames = new ArrayList<FirebaseSurvey>();
                Iterator<String> iter = missedSurveys.keySet().iterator();
                    while (iter.hasNext()) {
                        String key = iter.next();
                       // Log.d("INCOMPLETE SURVEY", key);
                        Map<String,FirebaseSurvey> surveys = missedSurveys.get(key);
                        Iterator<String> surveyiter = surveys.keySet().iterator();
                        while (surveyiter.hasNext()) {
                            String surveykey = surveyiter.next();
                         //   Log.d("INCOMPLETE SURVEY", surveykey);
                            FirebaseSurvey survey = surveys.get(surveykey);
                            long timeToGo = survey.getexpiryTime() - System.currentTimeMillis();
                            int minutes = (int)(timeToGo /60000);
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            final long timeAlive = survey.gettimeAlive();
                            String dateString = formatter.format(new Date(survey.gettimeSent()));
//                            if(timeAlive > 0)
//                                valueView.setText("Sent at " + dateString + "\nExpiring in " + (minutes+1) + " minutes");
                            if (survey.getexpiryTime() > System.currentTimeMillis()|| survey.getexpiryTime() == 0) {
                                survey.setkey(surveykey);
                                surveynames.add(survey);
                                if(timeAlive > 0)
                                array.add(survey.getname() + "\nSent at " + dateString + "\nExpiring in " + (minutes+1) + " minutes");
                                else
                                    array.add(survey.getname() + "\nSent at " + dateString);
                            }
                        }
                }
                Log.i("SURVEY NAMES: ", surveynames.toString());
                adapter=new ArrayAdapter<String>(getInstance(),
                        android.R.layout.simple_list_item_1,
                        array);
                setListAdapter(adapter);
                list = (ListView)findViewById(android.R.id.list);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
                    {
                        Intent resultIntent = new Intent(getInstance(), SurveyActivity.class);
//                        resultIntent.putExtra("surveyid",surveyKey);
//                        resultIntent.putExtra("name",surveyName);
//                        resultIntent.putExtra("timeSent",timeSent);
//                        result.get(position).setbegun(); //Confirm that this survey has been started
//
//                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//                        //   resultIntent.addFlags(Intent.FLAG_ACTIVITY_T);
//                        startActivityForResult(resultIntent,position); //So we know which one to delete
                    }
                });
            //    CustomAdapter adapter = new CustomAdapter(getInstance(), surveynames);
             //   surveyList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_missed_survey);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
//         list = (ListView)findViewById(android.R.id.list);
////
//       Log.d("OOGABOOGAGFJGFJKH", "jhgjyghfydfty");
////
//
//        mFirebaseAuth = FirebaseAuth.getInstance();
//
//        FirebaseUser user = mFirebaseAuth.getCurrentUser();
//        String userid = user.getUid();
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        final DatabaseReference firebaseSurvey = database.getReference("JeevesData").child("patients").child(userid);
//  //      final Firebase firebaseSurvey = new Firebase("https://incandescent-torch-8695.firebaseio.com/JeevesData/patients/" + userid);
//        firebaseSurvey.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//               firebaseSurvey.removeEventListener(this); //Get rid of it again or it causes problems
//
//                Log.i("Snappyshot99999999", snapshot.getValue().toString());
//                //FirebasePatient patient = (FirebasePatient)snapshot.getValue();
//                FirebasePatient patient = (FirebasePatient)snapshot.getValue(FirebasePatient.class); //THIS IS HOW YOU DO IT TO AVOID MAKING IT A HASHMAP
//                Map<String, Map<String,FirebaseSurvey>> missedSurveys = patient.getincomplete(); //Oh wow this is horrific. Absolutely horrific.
//                if (missedSurveys == null) return;
//                ArrayList<FirebaseSurvey> surveynames = new ArrayList<FirebaseSurvey>();
//                Iterator<String> iter = missedSurveys.keySet().iterator();
//                while (iter.hasNext()) {
//                    String key = iter.next();
//                    Map<String,FirebaseSurvey> surveys = missedSurveys.get(key);
//                    Iterator<String> surveyiter = surveys.keySet().iterator();
//                    while (surveyiter.hasNext()) {
//                        String surveykey = surveyiter.next();
//                        Log.i("INCOMPLETE SURVEY", surveykey);
//                        FirebaseSurvey survey = surveys.get(surveykey);
//                        if (survey.getexpiryTime() > System.currentTimeMillis() || survey.getexpiryTime() == 0) {
//                            survey.setkey(surveykey);
//                            surveynames.add(survey);
//                        }
//                    }
//                }final String surveyName = result.get(position).getname();
//                final String surveyKey = result.get(position).getkey();
//                final long timeSent = result.get(position).gettimeSent();
//                //Log.d("SURVEY NAMES: ", surveynames.toString());
////                CustomAdapter adapter = new CustomAdapter(getInstance(), surveynames);
//                list.setAdapter(adapter);
//
//                list.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        View item = list.getSelectedView();
//
//                        Intent resultIntent = new Intent(getInstance(), SurveyActivity.class);
//                        resultIntent.putExtra("surveyid",surveyKey);
//                        resultIntent.putExtra("name",surveyName);
//                        resultIntent.putExtra("timeSent",timeSent);
//                        result.get(position).setbegun(); //Confirm that this survey has been started
//
//                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//                        //   resultIntent.addFlags(Intent.FLAG_ACTIVITY_T);
//                        startActivityForResult(resultIntent,position); //So we know which one to delete
//                    }
//                });
            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//
//
//
//        });

//    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.i("WHYYYYYYY","Why have I been destroyed?");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("Result code iiiiis", "It's " + resultCode);
        Log.i("FINITO","FIIIINIIIIISHED");
    //    surveyList.get
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
            }
        }
    }
    public void updateList(){

    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.i("STOPPED","Ih ave been stopped");
    }
    public Activity getInstance(){
        return this;
    }

}

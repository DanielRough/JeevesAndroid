package com.jeevesandroid.mainscreens;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;
import com.jeevesandroid.AppContext;
import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseSurvey;
import com.jeevesandroid.firebase.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * This shows any surveys that the user has either ignored
 * or partially completed, and allows the user to select them for completion.
 */
public class MissedSurveyActivity extends AppCompatActivity {
    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_missed_survey);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        final FirebaseDatabase database = FirebaseUtils.getDatabase();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
        //It can happen...
        if(FirebaseUtils.PATIENT_REF == null){
            FirebaseUtils.PATIENT_REF = database
                .getReference(FirebaseUtils.PATIENTS_KEY)
                .child(prefs.getString(AppContext.UID, ""));
        }
        //Get the top 10 most recently missed surveys out of here
        Query myTopPostsQuery = FirebaseUtils.PATIENT_REF
            .child(AppContext.INCOMPLETE)
            .orderByChild(AppContext.TIME_SENT).limitToLast(10);
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final ArrayList<FirebaseSurvey> surveys = new ArrayList<>();
                final ArrayList<String> surveynames = new ArrayList<>();
                ArrayList<DataSnapshot> children = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    children.add(0,postSnapshot);
                }
                for (DataSnapshot postSnapshot : children) {
                    FirebaseSurvey survey = postSnapshot.getValue(FirebaseSurvey.class);
                    String id = postSnapshot.getKey();
                    if(survey == null){
                        return;
                    }
                    survey.setkey(id);
                    long expiryTime = survey.getexpiryTime();
                    long expiryMillis = expiryTime * 60 * 1000;
                    long deadline = survey.gettimeSent() + expiryMillis;
                    String name = survey.gettitle();

                    SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(AppContext.getContext());
                    boolean isAvailable = prefs.getBoolean(survey.getsurveyId(),false);
                    //Check whether this survey should be available
                    //This way we only add the most recent survey
                    if (!surveynames.contains(name) && isAvailable){
                        if(deadline > System.currentTimeMillis() || survey.getexpiryTime() == 0) {
                            surveys.add(survey);
                            surveynames.add(name);
                        }
                    }

                }
                list = findViewById(android.R.id.list);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int pos, long l) {
                    Intent resultIntent = new Intent(
                        MissedSurveyActivity.this, SurveyActivity.class);
                    FirebaseSurvey clickedSurvey = surveys.get(pos);


                    resultIntent.putExtra(AppContext.SURVEY_ID, clickedSurvey.getkey());
                    resultIntent.putExtra(AppContext.SURVEY_NAME, clickedSurvey.gettitle());
                    resultIntent.putExtra(AppContext.TIME_SENT, clickedSurvey.gettimeSent());
                    resultIntent.putExtra(AppContext.TRIG_TYPE, clickedSurvey.gettriggerType());
                    clickedSurvey.setbegun(); //Confirm that this survey has been started
                    startActivity(resultIntent);
                    }
                });
                MissedSurveyItem adapter = new MissedSurveyItem(MissedSurveyActivity.this, surveys);
                list.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }

        });
    }

    /**
     * Custom list item
     */
    class MissedSurveyItem extends BaseAdapter {
        final ArrayList<FirebaseSurvey> result;
        final Activity context;

        MissedSurveyItem(Activity mainActivity, ArrayList<FirebaseSurvey> prgmNameList) {
            result = prgmNameList;
            context = mainActivity;
        }

        @Override
        public int getCount() {
            return result.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.survey_list, parent, false);
            TextView labelView = rowView.findViewById(R.id.labelView);
            TextView valueView = rowView.findViewById(R.id.valueView);
            TextView startedView = rowView.findViewById(R.id.startedView);

            // 4. Set the text for textView
            labelView.setText(result.get(position).gettitle());
            final long timeSent = result.get(position).gettimeSent();
            long expiryTime = result.get(position).getexpiryTime();
            long expiryMillis = expiryTime * 60 * 1000;
            long deadline = result.get(position).gettimeSent() + expiryMillis;
            long timeToGo = deadline - System.currentTimeMillis();

            boolean begun = result.get(position).getbegun();
            if (begun)
                startedView.setText(getResources().getString(R.string.partially_completed));

            int minutes = (int) (timeToGo / 60000);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.UK);
            String dateString = formatter.format(new Date(timeSent));
            String minute_str = String.format(Locale.UK,"%d",minutes+1);
            if (timeToGo > 0) {
                valueView.setText(String.format(getResources().getString(R.string.sent_expiring),
                    dateString, minute_str));
            }
            else {
                valueView.setText(String.format(
                    getResources().getString(R.string.sent_not_expiring), dateString));
            }
            return rowView;
        }

    }
}

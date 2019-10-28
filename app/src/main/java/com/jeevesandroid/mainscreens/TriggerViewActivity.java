package com.jeevesandroid.mainscreens;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jeevesandroid.AppContext;
import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseSurvey;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TriggerViewActivity extends AppCompatActivity {
    private ListView lstTriggers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_trigger_view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        lstTriggers = findViewById(R.id.lstTriggers);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        HashSet<String> s = new HashSet<>( prefs.getStringSet(AppContext.TRIGGER_TIME_LIST,new HashSet()));
        ArrayList<String> triggerids = new ArrayList<>(s);
        ArrayList<String> trigtimes = new ArrayList<>();
        for(String trig : triggerids){
            Log.d("TRGGG",trig);
            trigtimes.add(trig.split(";")[1]);
        }
        Collections.sort(trigtimes);
        MissedSurveyItem adapter = new MissedSurveyItem(TriggerViewActivity.this, trigtimes);
        lstTriggers.setAdapter(adapter);
    }
    /**
     * Custom list item
     */
    class MissedSurveyItem extends BaseAdapter {
        final ArrayList<String> result;
        final Activity context;

        MissedSurveyItem(Activity mainActivity, ArrayList<String> prgmNameList) {
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
            labelView.setText(result.get(position));
            valueView.setText("");
            labelView.setTextSize(18);

            return rowView;
        }

    }
}

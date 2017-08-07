package com.example.daniel.jeeves;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.daniel.jeeves.firebase.FirebaseProject;
import com.example.daniel.jeeves.firebase.FirebaseSurvey;
import com.example.daniel.jeeves.firebase.UserVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CheckVariablesActivity extends AppCompatActivity {
    SharedPreferences.Editor prefseditor;
    ArrayList<Map<String,String>> listmaplist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_variables);

        ListView listVars = (ListView)findViewById(R.id.listVars);
        listmaplist = new ArrayList<Map<String,String>>();
        FirebaseProject currentproject = ApplicationContext.getProject();
        ArrayList<String> surveyvars = new ArrayList<String>();
        for(FirebaseSurvey survey : currentproject.getsurveys()){
            surveyvars.add(survey.gettitle()+"-Completed");
            surveyvars.add(survey.gettitle()+"-Missed");
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        prefseditor = preferences.edit();
        //TODO: Consider storing these variables in the database rather than doing this stuff manually
        Map<String,String> listmap = new HashMap<String,String>();

        for(String survey : surveyvars){
            listmap.put("name",survey);
            listmap.put("vartype","Numeric");
            listmap.put("value",Long.toString(preferences.getLong(survey,0)));
            listmaplist.add(listmap);
            listmap = new HashMap<String,String>();
        }
        String missed = Long.toString(preferences.getLong("Missed Surveys",0));
        listmap.put("name","Missed Surveys");
        listmap.put("vartype","Numeric");
        listmap.put("value",missed);
        listmaplist.add(listmap);

        listmap = new HashMap<String,String>();
        String completed = Long.toString(preferences.getLong("Completed Surveys",0));
        listmap.put("name","Completed Surveys");
        listmap.put("vartype","Numeric");
        listmap.put("value",completed);
        listmaplist.add(listmap);

        listmap = new HashMap<String,String>();
        String lastScore = Long.toString(preferences.getLong("Last Survey Score",0));
        listmap.put("name","Last Survey Score");
        listmap.put("vartype","Numeric");
        listmap.put("value",lastScore);
        listmaplist.add(listmap);

        listmap = new HashMap<String,String>();
        String difference = Long.toString(preferences.getLong("Survey Score Difference",0));
        listmap.put("name","Survey Score Difference");
        listmap.put("vartype","Numeric");
        listmap.put("value",difference);
        listmaplist.add(listmap);

        listmap = new HashMap<String,String>();
        String lastloc = preferences.getString("LastLocation","");
        listmap.put("name","Last Location");
        listmap.put("vartype","Location");
        listmap.put("value",lastloc);

        listmaplist.add(listmap);
        for (UserVariable userVariable : currentproject.getvariables()) {
            String name = userVariable.getname();
            String vartype = userVariable.getvartype();
            String value = "";
            Log.d("Name","Name is " + name);
            try {
                switch (vartype) {
                    case "Numeric":
                        value = preferences.getString(name, "");
                        break;
                    case "Boolean":
                        value = Boolean.toString(preferences.getBoolean(name, false));
                        break;
                    default:
                        value = preferences.getString(name, "");
                }
            }
            catch(ClassCastException e){
                Log.d("ExCEption","Couldn't cast " + name + " to type " + vartype);
                value = "err";
            }
            listmap = new HashMap<String,String>();
            listmap.put("name",name);
            listmap.put("vartype",vartype);
            listmap.put("value",value);
            listmaplist.add(listmap);
        }
        UserVariableItem adapter = new UserVariableItem(CheckVariablesActivity.this, listmaplist);
        listVars.setAdapter(adapter);

        Button btnClear = (Button)findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Map<String, String> stringStringMap : listmaplist) {
                    String name = stringStringMap.get("name");
                    prefseditor.remove(name);
                    prefseditor.commit();
                }
            }
        });
    }

    /**
     * Created by Daniel on 24/06/16.
     */
    public static class UserVariableItem extends BaseAdapter {
        ArrayList<Map<String,String>>  result;

        Activity context;
        private static LayoutInflater inflater=null;
        public UserVariableItem(Activity mainActivity, ArrayList<Map<String,String>> varsList) {
            // TODO Auto-generated constructor stub
            result=varsList;
            context=mainActivity;
            inflater = (LayoutInflater)context.
                    getSystemService(LAYOUT_INFLATER_SERVICE);
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

            // 2. Get rowView from inflater
            View rowView = inflater.inflate(R.layout.user_variable_list, parent, false);

            // 3. Get the two text view from the rowView
            TextView labelView = (TextView) rowView.findViewById(R.id.labelView);
            TextView valueView = (TextView) rowView.findViewById(R.id.valueView);
            TextView startedView = (TextView) rowView.findViewById(R.id.startedView);
            LinearLayout layout = (LinearLayout)rowView.findViewById(R.id.listlayout);
            // 4. Set the text for textView
            labelView.setText(result.get(position).get("name"));
            final String vartype = result.get(position).get("vartype");
            switch(vartype){
                case "Location":layout.setBackgroundColor(Color.RED);break;
                case "Time":layout.setBackgroundColor(Color.BLUE);break;
                case "Date":layout.setBackgroundColor(Color.MAGENTA);break;
                case "Numeric":layout.setBackgroundColor(Color.GREEN);break;
                case "Boolean":layout.setBackgroundColor(Color.CYAN);break;

            }
            final String value = result.get(position).get("value");
            valueView.setText(value);

            return rowView;
        }

    }
}

package com.example.daniel.jeeves;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class CheckScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_check_schedule);
//
//        ListView listVars = (ListView)findViewById(R.id.listTriggers);
//        listmaplist = new ArrayList<Map<String,String>>();
//        FirebaseProject currentproject = ApplicationContext.getProject();
//        ArrayList<String> surveyvars = new ArrayList<String>();
//        for(FirebaseSurvey survey : currentproject.getsurveys()){
//            surveyvars.add(survey.gettitle()+"-Completed");
//            surveyvars.add(survey.gettitle()+"-Missed");
//        }
//
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
//        SharedPreferences.Editor prefseditor = preferences.edit();
//        //TODO: Consider storing these variables in the database rather than doing this stuff manually
//        Map<String,String> listmap = new HashMap<String,String>();
//
//        listmaplist.add(listmap);
//        for (UserVariable userVariable : currentproject.getvariables()) {
//            String name = userVariable.getname();
//            String vartype = userVariable.getvartype();
//            String value = "";
//            Log.d("Name","Name is " + name);
//            try {
//                switch (vartype) {
//                    case "Numeric":
//                        value = preferences.getString(name, "");
//                        break;
//                    case "Boolean":
//                        value = Boolean.toString(preferences.getBoolean(name, false));
//                        break;
//                    default:
//                        value = preferences.getString(name, "");
//                }
//            }
//            catch(ClassCastException e){
//                Log.d("ExCEption","Couldn't cast " + name + " to type " + vartype);
//                value = "err";
//            }
//            listmap = new HashMap<String,String>();
//            listmap.put("name",name);
//            listmap.put("vartype",vartype);
//            listmap.put("value",value);
//            listmaplist.add(listmap);
//        }
//        CheckVariablesActivity.UserVariableItem adapter = new CheckVariablesActivity.UserVariableItem(CheckVariablesActivity.this, listmaplist);
//        listVars.setAdapter(adapter);
    }
}

package com.example.daniel.jeeves;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.daniel.jeeves.firebase.FirebaseProject;
import com.example.daniel.jeeves.firebase.UserVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckVariablesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_variables);

        ListView listVars = (ListView)findViewById(R.id.listVars);
        ArrayList<Map<String,String>> listmaplist = new ArrayList<Map<String,String>>();
        FirebaseProject currentproject = ApplicationContext.getProject();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        for (UserVariable userVariable : currentproject.getvariables()) {
            String name = userVariable.getname();
            String vartype = userVariable.getvartype();
            String value = "";
            Log.d("Name","Name is " + name);
            try {
                switch (vartype) {
                    case "Numeric":
                        value = Long.toString(preferences.getLong(name, 0));
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
            Map<String,String> listmap = new HashMap<String,String>();
            listmap.put("name",name);
            listmap.put("vartype",vartype);
            listmap.put("value",value);
            listmaplist.add(listmap);
        }
        UserVariableItem adapter = new UserVariableItem(CheckVariablesActivity.this, listmaplist);
        listVars.setAdapter(adapter);
    }
}

package com.jeevesandroid.mainscreens;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.jeevesandroid.AppContext;
import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseProject;
import com.jeevesandroid.firebase.FirebaseUI;
import com.jeevesandroid.triggers.config.TriggerConstants;

import java.util.List;

/**
 * Activity that displays all the buttons and labels configured by the user
 * in the Jeeves Desktop application.
 * Buttons fire off an intent that gets picked up by Broadcast Receivers
 * in the Button trigger.
 */
public class SelfReportActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_monitor);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        FirebaseProject currentProject = AppContext.getProject();
        List<FirebaseUI> uielements = currentProject.getuidesign();
        LinearLayout customlayout = findViewById(R.id.customLayout);
        customlayout.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        customlayout.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,50,0,50);
        for (FirebaseUI uielement : uielements) {
            if(uielement.getname().equals(AppContext.BUTTON)){
                final Button button = (Button)getLayoutInflater()
                    .inflate(R.layout.buttontemplate,null);
                button.setText(uielement.gettext());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(TriggerConstants.ACTION_NAME_BUTTON_TRIGGER);
                    intent.putExtra("buttonName",button.getText());
                    sendBroadcast(intent);
                    }
                });
                customlayout.addView(button,params);
            }
            else if(uielement.getname().equals(AppContext.LABEL)){
                TextView view = new TextView(getApplicationContext());
                view.setText(uielement.gettext());
                view.setTextSize(20);
                view.setTextColor(Color.BLACK);
                view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                customlayout.addView(view,params);
            }
        }
    }
}


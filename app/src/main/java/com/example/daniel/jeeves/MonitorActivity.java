package com.example.daniel.jeeves;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.daniel.jeeves.firebase.FirebaseProject;
import com.example.daniel.jeeves.firebase.FirebaseUI;
import com.ubhave.triggermanager.config.TriggerManagerConstants;
import java.util.List;

public class MonitorActivity extends AppCompatActivity {
    List<FirebaseUI> uielements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_monitor);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

                FirebaseProject currentProject = ApplicationContext.getProject();
                uielements = currentProject.getuidesign();
                LinearLayout customlayout = (LinearLayout)findViewById(R.id.customLayout);
                customlayout.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                customlayout.removeAllViews();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(0,50,0,50);
                for (FirebaseUI uielement : uielements) {
                    if(uielement.getname().equals("BUTTON")){
                        final Button button = (Button)getLayoutInflater().inflate(R.layout.buttontemplate, null);

                  //      final Button button = new Button(getApplicationContext(),null,com.android.internal.R.attr.buttonStyle);
                        button.setText(uielement.gettext());
                  //      button.setBackgroundColor(Color.LTGRAY);
                   //     button.setTextColor(Color.BLACK);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.setAction(TriggerManagerConstants.ACTION_NAME_BUTTON_TRIGGER);
                                intent.putExtra("buttonName",button.getText());
                                sendBroadcast(intent);
                            }
                        });
                        customlayout.addView(button,params);
                    }
                    else if(uielement.getname().equals("LABEL")){
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


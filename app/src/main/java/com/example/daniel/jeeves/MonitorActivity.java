package com.example.daniel.jeeves;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.daniel.jeeves.firebase.FirebaseProject;
import com.example.daniel.jeeves.firebase.FirebaseUI;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.ubhave.triggermanager.config.TriggerManagerConstants;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MonitorActivity extends AppCompatActivity {
    Firebase myFirebaseRef;
    List<FirebaseUI> uielements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_monitor);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        SharedPreferences prefs = this.getSharedPreferences("userprefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        myFirebaseRef = new Firebase("https://incandescent-torch-8695.firebaseio.com/JeevesData/projects/SimpleTest");
        Log.d("HEREWEGO", "Updating le config");
        myFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d("Snappyshot", snapshot.getValue().toString());
                FirebaseProject post = snapshot.getValue(FirebaseProject.class);
                uielements = post.getuidesign();
                LinearLayout customlayout = (LinearLayout)findViewById(R.id.customLayout);
                customlayout.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                customlayout.removeAllViews();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(0,50,0,50);
              //  customlayout.setDividerPadding(50);
                for (FirebaseUI uielement : uielements) {
                    if(uielement.getname().equals("BUTTON")){
                       final Button button = new Button(getApplicationContext());
                        button.setText(uielement.gettext());
                        button.setBackgroundColor(Color.LTGRAY);
                        button.setTextColor(Color.BLACK);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.setAction(TriggerManagerConstants.ACTION_NAME_BUTTON_TRIGGER);
                                intent.putExtra("buttonName",button.getText());
                                sendBroadcast(intent);
                            }
                        });
                    //    button.set
                     //   customlayout.setShadowLayer(2f, -3, 3, Color.BLACK);

                        //        customlayout.addView(button);
                        customlayout.addView(button,params);
                  //      button.setWidth(button.getMinWidth());

                    }
                    else if(uielement.getname().equals("LABEL")){
                        TextView view = new TextView(getApplicationContext());
                        view.setText(uielement.gettext());
                        view.setTextSize(20);
                        view.setTextColor(Color.BLACK);
                        view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        Log.d("TEXTVIEW","Set textview text to " + uielement.gettext());
                  //      customlayout.addView(view);
                        customlayout.addView(view,params);


                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

    }

    }


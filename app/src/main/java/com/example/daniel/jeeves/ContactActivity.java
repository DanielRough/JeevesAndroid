package com.example.daniel.jeeves;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Map;

public class ContactActivity extends AppCompatActivity {
    AlertDialog.Builder finishalert;

    EditText txtContactResearcher;
    String researcherno;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_contact);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        SharedPreferences prefs = this.getSharedPreferences("userprefs",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        researcherno = prefs.getString("researcherno","");
        final String userid = prefs.getString("userid", "null");
        finishalert = new AlertDialog.Builder(this);
        finishalert.setTitle("Your message has been sent!");
        Button btnContactResearcher = (Button) findViewById(R.id.btnContactResearcher);
        txtContactResearcher = (EditText) findViewById(R.id.txtContactResearcher);
        finishalert.setPositiveButton("Return", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                finish();
            }
        });


            btnContactResearcher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message = txtContactResearcher.getText().toString();
                    if(researcherno != null && !researcherno.equals("")) {
                        SmsManager sms = SmsManager.getDefault();
                        sms.sendTextMessage(researcherno, null, message, null, null);
                    }
                    Firebase firebaseFeedback = new Firebase("https://incandescent-torch-8695.firebaseio.com/patients/" + userid + "/feedback/" + System.currentTimeMillis());
                    firebaseFeedback.setValue(txtContactResearcher.getText().toString());
                    finishalert.setCancelable(false); //Once they're done they're done
                    finishalert.show();
                    return;
                }
            });
        }

}

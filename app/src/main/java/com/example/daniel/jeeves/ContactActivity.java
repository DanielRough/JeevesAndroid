package com.example.daniel.jeeves;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.example.daniel.jeeves.firebase.FirebaseUtils;
import com.google.firebase.database.DatabaseReference;

import static com.example.daniel.jeeves.ApplicationContext.FEEDBACK;

public class ContactActivity extends AppCompatActivity {
    AlertDialog.Builder finishalert;
    EditText txtContactResearcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_contact);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

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
//                    if(researcherno != null && !researcherno.equals("")) {
//                        SmsManager sms = SmsManager.getDefault();
//                        sms.sendTextMessage(researcherno, null, message, null, null);
//                    }
                final DatabaseReference firebaseFeedback = FirebaseUtils.PATIENT_REF.child(FEEDBACK).child(Long.toString(System.currentTimeMillis()));
                firebaseFeedback.setValue(txtContactResearcher.getText().toString());
                finishalert.setCancelable(false); //Once they're done they're done
                finishalert.show();
                return;
            }
        });
    }

}

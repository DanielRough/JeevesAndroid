package com.jeevesandroid.mainscreens;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jeevesandroid.AppContext;
import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ContactActivity extends AppCompatActivity {
    private EditText txtContactResearcher;
    private ListView lstMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Add a listener that checks whether the researcher sends feedback to the participant
        DatabaseReference patientRef = FirebaseUtils.PATIENT_REF.child(AppContext.FEEDBACK);
        patientRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TreeMap<String,String> sortedFeedback = new TreeMap<>(Collections.reverseOrder());
                HashMap<String,String> feedback = (HashMap<String,String>)dataSnapshot.getValue();
                if(feedback == null)return;
                sortedFeedback.putAll(feedback);
                AppContext.feedback = sortedFeedback;
                refreshList();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_contact);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Button btnContactResearcher = findViewById(R.id.btnContactResearcher);
        txtContactResearcher = findViewById(R.id.txtContactResearcher);
        lstMessages = findViewById(R.id.lstMessages);



        //Write the participant's message to the database, keyed by the time
        //the message was sent
        btnContactResearcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            final DatabaseReference firebaseFeedback =
                FirebaseUtils.PATIENT_REF.child(AppContext.FEEDBACK)
                    .child(Long.toString(System.currentTimeMillis()));
            firebaseFeedback.setValue("Participant: " + txtContactResearcher.getText().toString());
            }
        });
        refreshList();
    }

    private void refreshList(){
        ArrayList<String> varsList = new ArrayList<>();
        Map<String,String> feedback = AppContext.getFeedback();
        for (Map.Entry<String, String> message : feedback.entrySet()) {

            String date = message.getKey();
            String sender = message.getValue().split(":")[0];
            String messageText = message.getValue().split(":")[1];
            sender = sender.equals("Participant") ? "You" : "Researcher";
            varsList.add(date + ":" + sender + ":" + messageText);
        }

        MessageItem adapter = new MessageItem(ContactActivity.this, varsList);
        lstMessages.setAdapter(adapter);
    }

    /**
     * Class representing an entry in the list of researcher/participant feedback.
     * Lays out the necessary information (date, sender, text) and colours it depending
     * on the sender
     */
    static class MessageItem extends BaseAdapter {
        final ArrayList<String> result;

        final Activity context;

        MessageItem(Activity mainActivity, ArrayList<String> varsList) {
            result = varsList;
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

            //Get rowView from inflater
            View rowView = inflater.inflate(R.layout.message_list, parent,false);

            //Get the text views from the rowView
            TextView txtSender = rowView.findViewById(R.id.txtSender);
            TextView txtMessage = rowView.findViewById(R.id.txtMessage);
            TextView txtDate = rowView.findViewById(R.id.txtDate);

            //Set the text for textView
            String sendermessage = result.get(position);
            String date = sendermessage.split(":")[0];
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            String dateStr = sdf.format(new Date(Long.parseLong(date)));
            String sender = sendermessage.split(":")[1];
            LinearLayout layout = rowView.findViewById(R.id.listLayout);

            //Conditional colouring
            if(sender.equals("You")) {
                layout.setBackgroundColor(Color.WHITE);
            }
            else {
                layout.setBackgroundColor(Color.LTGRAY);
            }
            String message = sendermessage.split(":")[2];
            txtSender.setText(sender);
            txtMessage.setText(message);
            txtDate.setText(dateStr);
            return rowView;
        }

    }
}

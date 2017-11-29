package com.jeeves;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
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

import com.jeeves.R;
import com.jeeves.firebase.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static com.jeeves.ApplicationContext.FEEDBACK;

public class ContactActivity extends AppCompatActivity {
    AlertDialog.Builder finishalert;
    EditText txtContactResearcher;
    ListView lstMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DatabaseReference patientRef = FirebaseUtils.PATIENT_REF.child("feedback");
        patientRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TreeMap<String,String> sortedFeedback = new TreeMap<String,String>(Collections.reverseOrder());
                HashMap<String,String> feedback = (HashMap<String,String>)dataSnapshot.getValue();
                if(feedback == null)return;
                sortedFeedback.putAll(feedback);
                ApplicationContext.feedback = sortedFeedback; //What a bloody awful line
                refreshList();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_contact);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Button btnContactResearcher = (Button) findViewById(R.id.btnContactResearcher);
        txtContactResearcher = (EditText) findViewById(R.id.txtContactResearcher);
        lstMessages = (ListView) findViewById(R.id.lstMessages);



        btnContactResearcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = txtContactResearcher.getText().toString();
//                    if(researcherno != null && !researcherno.equals("")) {
//                        SmsManager sms = SmsManager.getDefault();
//                        sms.sendTextMessage(researcherno, null, message, null, null);
//                    }
                final DatabaseReference firebaseFeedback = FirebaseUtils.PATIENT_REF.child(FEEDBACK).child(Long.toString(System.currentTimeMillis()));
                firebaseFeedback.setValue("Patient: " + txtContactResearcher.getText().toString());

                return;
            }
        });
        refreshList();
    }

    public void refreshList(){
        ArrayList<String> varsList = new ArrayList<String>();
        Map<String,String> feedback = ApplicationContext.getFeedback();
        Iterator<Map.Entry<String,String>> messageIter = feedback.entrySet().iterator();
        while(messageIter.hasNext()){

            Map.Entry<String,String> message = messageIter.next();
            String date = message.getKey();
            String sender = message.getValue().split(":")[0];
            String messageText = message.getValue().split(":")[1];
            if(sender.equals("Patient")){
                sender = "You";
            }
            else{
                sender = "Clinician";
            }
            varsList.add(date + ":" + sender + ":" + messageText);
        }

        MessageItem adapter = new MessageItem(ContactActivity.this, varsList);
        lstMessages.setAdapter(adapter);
    }
    public static class MessageItem extends BaseAdapter {
        ArrayList<String> result;

        Activity context;
        private static LayoutInflater inflater = null;

        public MessageItem(Activity mainActivity, ArrayList<String> varsList) {
            // TODO Auto-generated constructor stub
            result = varsList;
            context = mainActivity;
            inflater = (LayoutInflater) context.
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
            View rowView = inflater.inflate(R.layout.message_list, parent, false);

            // 3. Get the two text view from the rowView
            TextView txtSender = (TextView) rowView.findViewById(R.id.txtSender);
            TextView txtMessage = (TextView) rowView.findViewById(R.id.txtMessage);
            TextView txtDate = (TextView) rowView.findViewById(R.id.txtDate);
            // 4. Set the text for textView
            String sendermessage = result.get(position);
            String date = sendermessage.split(":")[0];
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String dateStr = sdf.format(new Date(Long.parseLong(date)));
            String sender = sendermessage.split(":")[1];
            LinearLayout layout = (LinearLayout)rowView.findViewById(R.id.listLayout);
            if(sender.equals("You"))
                layout.setBackgroundColor(Color.WHITE);
            else
                layout.setBackgroundColor(Color.LTGRAY);
            String message = sendermessage.split(":")[2];
            txtSender.setText(sender);
            txtMessage.setText(message);
            txtDate.setText(dateStr);
            return rowView;
        }

    }
}

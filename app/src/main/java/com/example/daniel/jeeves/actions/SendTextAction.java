package com.example.daniel.jeeves.actions;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.util.Log;

import com.example.daniel.jeeves.ApplicationContext;

import java.util.Map;


/**
 * Created by Daniel on 27/05/15.
 */
public class SendTextAction extends FirebaseAction {

    public SendTextAction(Map<String,Object> params){

    }
    @Override
    public void execute() {
        Context app = ApplicationContext.getContext();
        String recipient = getparams().get("recipient").toString();
        String message = getparams().get("msgtext").toString();
        Log.d("ACTIONSMS", "SENT AN SMS");
        SharedPreferences pref = app.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        SmsManager sms = SmsManager.getDefault();
        String toSend = "";
        switch(recipient){
            case "Researcher": toSend = pref.getString("researcherno",""); break;
            case "Last sender": toSend = pref.getString("lastSender","");break;
            case "Emergency contact": toSend = pref.getString("contact",""); break;
        }
        Log.d("SENDING","Sending a message to " + toSend);
        if(toSend != "")
            sms.sendTextMessage(toSend, null, message, null, null);
    }
}

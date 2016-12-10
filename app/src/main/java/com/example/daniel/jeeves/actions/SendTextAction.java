package com.example.daniel.jeeves.actions;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.login.MainActivity;

import java.util.Map;


/**
 * Created by Daniel on 27/05/15.
 */
public class SendTextAction extends FirebaseAction {

    public SendTextAction(Map<String,Object> params){
        setparams(params);

    }
    @Override
    public void execute() {
        Context app = ApplicationContext.getContext();
        String recipient = getparams().get("recipient").toString();
        String message = getparams().get("msgtext").toString();
        Log.d("ACTIONSMS", "SENT AN SMS");
        SmsManager sms = SmsManager.getDefault();
        String toSend = "";
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        String userPhone = preferences.getString("userphone","0");

        switch(recipient){
          //  case "Researcher": toSend = pref.getString("researcherno",""); break;
          //  case "Last sender": toSend = pref.getString("lastSender","");break;
          //  case "Emergency contact": toSend = pref.getString("contact",""); break;
            case "User" : toSend =userPhone; break;
        }
        Log.d("SENDING","Sending a message to " + toSend);
        if(toSend != "")
            sms.sendTextMessage(toSend, null, message, null, null);
    }
}

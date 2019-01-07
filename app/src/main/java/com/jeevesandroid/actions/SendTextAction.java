package com.jeevesandroid.actions;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;

import com.jeevesandroid.ApplicationContext;

import java.util.Map;


/**
 * Created by Daniel on 27/05/15.
 */
@SuppressWarnings("ALL")
public class SendTextAction extends FirebaseAction {

    public SendTextAction(Map<String,Object> params){
        setparams(params);

    }
    @Override
    public void execute() {
        String recipient;
        String number;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());

        if(getparams().get("recipient") instanceof Map){
            recipient = ((Map<String,Object>)getparams().get("recipient")).get("name").toString();
                number = preferences.getString(recipient,"");
        }
        else if(getparams().get("recipient") != null)
            number = (getparams().get("recipient").toString());
        else
            return;
        if(!getparams().containsKey("msgtext"))return;
        String message = getparams().get("msgtext").toString();
        SmsManager sms = SmsManager.getDefault();
        if (ContextCompat.checkSelfPermission(ApplicationContext.getContext(), Manifest.permission.WRITE_CALENDAR)
                == PackageManager.PERMISSION_GRANTED) {
            sms.sendTextMessage("0" +number, null, message, null, null);
        }
    }
}

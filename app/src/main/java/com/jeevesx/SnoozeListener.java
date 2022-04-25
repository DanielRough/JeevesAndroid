package com.jeevesx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SnoozeListener extends BroadcastReceiver {

    //the method will be fired when the alarm is triggerred
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());
        Toast.makeText(context,"Snooze finished",Toast.LENGTH_LONG).show();
        SharedPreferences.Editor prefseditor = prefs.edit();
        prefseditor.putBoolean(AppContext.SNOOZE,false);
        prefseditor.apply();
    }

}
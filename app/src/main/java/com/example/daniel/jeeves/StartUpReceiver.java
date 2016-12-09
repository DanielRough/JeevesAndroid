package com.example.daniel.jeeves;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.daniel.jeeves.login.MainActivity;

/**
 * Created by Daniel on 09/12/2016.
 */

/**
 * I'm hoping that this class allows me to autostart the app when the user switches their phone on or reboots it
 */
public class StartUpReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent aIntent) {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
}

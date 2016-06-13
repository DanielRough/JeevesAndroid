package com.example.daniel.jeeves.actions;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.example.daniel.jeeves.ApplicationContext;


/**
 * Created by Daniel on 27/05/15.
 */
public class SpeakerAction extends FirebaseAction {

    @Override
    public void execute() {
        Log.d("ACTIONMUTE", "MUTED PHONE");
        Context app = ApplicationContext.getContext();

        String volume = getparams().get("volume").toString();
        AudioManager audioManager = (AudioManager)app.getSystemService(Context.AUDIO_SERVICE);
        if(volume.equals("Off"))
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        else
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }
}

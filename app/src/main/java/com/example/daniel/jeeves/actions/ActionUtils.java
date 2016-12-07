package com.example.daniel.jeeves.actions;

import android.util.Log;

/**
 * Created by Daniel on 27/05/15.
 */
public class ActionUtils {
    public static final String NAME_PROMPT_ACTION 			= "PROMPT ACTION";
    public static final String NAME_MESSAGE_ACTION 	= "SEND TEXT ACTION";
    public static final String NAME_SEND_SURVEY_ACTION			= "SURVEY ACTION";
    public static final String NAME_SPEAKER_PHONE_ACTION 		= "SPEAKER ACTION";
    public static final String NAME_UPDATE_USER_ACTION = "UPDATE VARIABLE ACTION";
    public static final String NAME_WAIT_ACTION = "WAIT ACTON";

    //IControl com.example.daniel.jeeves.actions
    public static final String NAME_IF_CONTROL = "control_if";

    public static FirebaseAction create(FirebaseAction baseAction){
        switch(baseAction.getname()){
            case NAME_PROMPT_ACTION: return new PromptAction(baseAction.getparams());
            case NAME_MESSAGE_ACTION: return new SendTextAction(baseAction.getparams());
            case NAME_SEND_SURVEY_ACTION: return new SurveyAction(baseAction.getparams());
            case NAME_SPEAKER_PHONE_ACTION: return new SpeakerAction(baseAction.getparams());
            case NAME_UPDATE_USER_ACTION: return new UpdateAction(baseAction.getparams());
            case NAME_WAIT_ACTION: return new WaitingAction(baseAction.getparams());
            case NAME_IF_CONTROL: return new IfControl(baseAction.getparams());
        }
        return null;
    }
}

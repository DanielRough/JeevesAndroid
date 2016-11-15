package com.example.daniel.jeeves.actions;

/**
 * Created by Daniel on 27/05/15.
 */
public class ActionUtils {
    public static final String NAME_PROMPT_ACTION 			= "PROMPT ACTION";
    public static final String NAME_MESSAGE_ACTION 	= "type_message";
    public static final String NAME_SEND_SURVEY_ACTION			= "type_survey";
    public static final String NAME_SPEAKER_PHONE_ACTION 		= "type_speaker";
    public static final String NAME_UPDATE_USER_ACTION = "type_update_user";
    public static final String NAME_WAIT_ACTION = "type_wait";

    //IControl com.example.daniel.jeeves.actions
    public static final String NAME_IF_CONTROL = "control_if";

    public static FirebaseAction create(FirebaseAction baseAction){
        switch(baseAction.getname()){
            case NAME_PROMPT_ACTION: return new PromptAction();
            case NAME_MESSAGE_ACTION: return new SendTextAction();
            case NAME_SEND_SURVEY_ACTION: return new SurveyAction();
            case NAME_SPEAKER_PHONE_ACTION: return new SpeakerAction();
            case NAME_UPDATE_USER_ACTION: return new UpdateAction();
            case NAME_WAIT_ACTION: return new WaitingAction();
            case NAME_IF_CONTROL: return new IfControl();
        }
        return null;
    }
}

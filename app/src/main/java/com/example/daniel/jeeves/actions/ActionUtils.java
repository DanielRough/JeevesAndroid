package com.example.daniel.jeeves.actions;

/**
 * Created by Daniel on 27/05/15.
 */
public class ActionUtils {
    public static final String NAME_PROMPT_ACTION 			= "Send Prompt";
    public static final String NAME_MESSAGE_ACTION 	= "Send SMS";
    public static final String NAME_SEND_SURVEY_ACTION			= "Send Survey";
    public static final String NAME_SPEAKER_PHONE_ACTION 		= "Adjust phone volume";
    public static final String NAME_UPDATE_USER_ACTION = "Update patient attribute";
    public static final String NAME_WAIT_ACTION = "Wait";

    //IControl com.example.daniel.jeeves.actions
    public static final String NAME_IF_CONTROL = "If Condition";

    public static FirebaseAction create(FirebaseAction baseAction){
        switch(baseAction.getname()){
            case NAME_PROMPT_ACTION: return new PromptAction(baseAction.getparams());// new PromptAction(baseAction);
            case NAME_MESSAGE_ACTION: return new SendTextAction(baseAction.getparams());
            case NAME_SEND_SURVEY_ACTION: return new SurveyAction(baseAction.getparams());
            case NAME_SPEAKER_PHONE_ACTION: return new SpeakerAction(baseAction.getparams());
            case NAME_UPDATE_USER_ACTION: return new UpdateAction(baseAction.getparams(),baseAction.getvars());
            case NAME_WAIT_ACTION: return new WaitingAction(baseAction.getparams());
            case NAME_IF_CONTROL: return new IfControl(baseAction.getparams(),baseAction.getcondition(),baseAction.getactions());
        }
        return null;
    }
}

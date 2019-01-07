package com.jeevesandroid.actions;

import android.util.Log;

/**
 * Created by Daniel on 27/05/15.
 */
public class ActionUtils {
    public static final String ACTIONS = "actions";
    public static final String ACTIONSETID = "actionsetid";
    private static final String NAME_PROMPT_ACTION 			= "Prompt User";
    private static final String NAME_MESSAGE_ACTION 	= "Send SMS";
    private static final String NAME_SEND_SURVEY_ACTION			= "Send Survey";
    private static final String NAME_CAPTURE_DATA_ACTION 		= "Sense Data";
    private static final String NAME_UPDATE_USER_ACTION = "Update User Attribute";
    private static final String NAME_WAIT_ACTION = "Snooze App";
    private static final String NAME_IF_CONTROL = "If Condition";
    private static final String NAME_WHILE_CONTROL = "While Condition";



    public static FirebaseAction create(FirebaseAction baseAction){
        switch(baseAction.getname()){
            case NAME_PROMPT_ACTION: return new PromptAction(baseAction.getparams());
            case NAME_MESSAGE_ACTION: return new SendTextAction(baseAction.getparams());
            case NAME_SEND_SURVEY_ACTION: return new SurveyAction(baseAction.getparams());
            case NAME_CAPTURE_DATA_ACTION: return new CaptureDataAction(baseAction.getparams());
            case NAME_UPDATE_USER_ACTION: return new UpdateAction(baseAction.getparams(),baseAction.getvars());
            case NAME_WAIT_ACTION: return new WaitingAction(baseAction.getparams());
            case NAME_IF_CONTROL: return new IfControl(baseAction.getparams(),baseAction.getcondition(),baseAction.getactions());
            case NAME_WHILE_CONTROL: return new WhileControl(baseAction.getparams(),baseAction.getcondition(),baseAction.getactions(),baseAction.getname());
        }
        return null;
    }
}

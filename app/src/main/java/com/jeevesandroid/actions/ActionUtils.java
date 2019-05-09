package com.jeevesandroid.actions;

import com.jeevesandroid.actions.actiontypes.CaptureDataAction;
import com.jeevesandroid.actions.actiontypes.FirebaseAction;
import com.jeevesandroid.actions.actiontypes.IfControl;
import com.jeevesandroid.actions.actiontypes.PromptAction;
import com.jeevesandroid.actions.actiontypes.SurveyAction;
import com.jeevesandroid.actions.actiontypes.UpdateAction;
import com.jeevesandroid.actions.actiontypes.WaitingAction;
import com.jeevesandroid.actions.actiontypes.WhileControl;

/**
 * Created by Daniel on 27/05/15.
 */
public class ActionUtils {
    public static final String ACTIONS = "actions";
    public static final String ACTIONSETID = "actionsetid";
    public static final String NAME_PROMPT_ACTION 			= "Prompt User";
   // public static final String NAME_MESSAGE_ACTION 	= "Send SMS";
    public static final String NAME_SEND_SURVEY_ACTION			= "Send Survey";
    public static final String NAME_CAPTURE_DATA_ACTION 		= "Sense Data";
    public static final String NAME_UPDATE_USER_ACTION = "Update User Attribute";
    public static final String NAME_WAIT_ACTION = "Snooze App";
    public static final String NAME_IF_CONTROL = "If Condition";
    public static final String NAME_WHILE_CONTROL = "While Condition";



    public static FirebaseAction create(FirebaseAction baseAction){
        switch(baseAction.getname()){
            case NAME_PROMPT_ACTION: return new PromptAction(baseAction.getparams());
          //  case NAME_MESSAGE_ACTION: return new SendTextAction(baseAction.getparams());
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

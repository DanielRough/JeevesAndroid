package com.jeevesandroid;

import android.app.Application;
import android.content.Context;

import com.jeevesandroid.actions.actiontypes.FirebaseAction;
import com.jeevesandroid.firebase.FirebaseProject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Application class to provide the global context.
 */
public class AppContext extends Application {
    //STart stop location
    public static final String STARTLOC = "startloc";
    public static final String STOPLOC = "stoploc";
    public static final String STOPSENSOR = "stopsensor";
    public static final String STARTACTIVITY = "startactivity";
    public static final String STOPACTIVITY = "stopactivity";

    //UIDesign constants
    public static final String BUTTON = "button";
    public static final String LABEL = "label";

    //Other important constants
    public static final String STUDY_NAME = "studyname";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String UID = "uid";
    public static final String DEVELOPER_ID = "developerid";
    public static final String FINISHED_INTRODUCTION = "finished";

    //Contact
    public static final String FEEDBACK = "feedback";

    //Schedule parameters
    public static final String START_DATE = "startdate";
    public static final String END_DATE = "enddate";
    public static final String WAKE_TIME = "waketime";
    public static final String SLEEP_TIME = "sleeptime";
    public static final String SCHEDULE_PREF = "schedule_pref";
    public static final String SCHEDULE_DAY = "schedule_day";

    //Survey-based constants
    //Stuff relevant to survey actions
    public static final String INCOMPLETE = "incomplete";
    public static final String COMPLETE = "complete";
    public static final String TIME_SENT = "timeSent";
    public static final String INIT_TIME = "initTime";
    public static final String NOTIF_ID = "notificationid";
    public static final String SURVEY_NAME = "surveyname";
    public static final String SCHEDULE_SURVEY = "schedule_survey";
    public static final String WAS_INIT = "initialised";
    public static final String TRIG_TYPE = "triggerType";
    public static final String SURVEY_ID = "surveyId";
    public static final String DEADLINE = "deadline";
    public static final String STATUS = "status";

    //Question types
    public static final String OPEN_ENDED = "OPEN_ENDED";
    public static final String MULT_SINGLE = "MULT_SINGLE";
    public static final String MULT_MANY = "MULT_MANY";
    public static final String SCALE = "SCALE";
    public static final String DATE = "DATE";
    public static final String GEO = "GEO";
    public static final String BOOLEAN = "BOOLEAN";
    public static final String NUMERIC = "NUMERIC";
    public static final String TIME = "TIME";
    public static final String IMAGEPRESENT = "IMAGEPRESENT";
    public static final String TEXTPRESENT = "TEXTPRESENT";
    public static final String HEART = "HEART";
    public static final String AUDIO = "AUDIO";
    public static final String SCHEDULE = "SCHEDULE";

    //Specific variable names
    public static final String MISSED_SURVEYS = "Missed Surveys";
    public static final String COMPLETED_SURVEYS = "Completed Surveys";

    private static AppContext instance;
    private static FirebaseProject currentproject;

    public static final String SNOOZE = "Snooze";

    public AppContext()
    {
        instance = this;
    }

    public static FirebaseProject getProject(){ return currentproject;}

    public static void setCurrentproject(FirebaseProject proj){
        currentproject = proj;
    }


    public static Context getContext()
    {
        return instance;
    }

    public static TreeMap<String,String> feedback;

    public static TreeMap<String,String> getFeedback(){
        if(feedback == null)
            feedback = new TreeMap<>();
        return feedback;
    }

    //A rather terrible way of passing actions to execute when a location trigger is fired
    //(or an activity trigger)
    private static HashMap<String,List<FirebaseAction>> locationActions;
    private static HashMap<String,List<FirebaseAction>> activityActions;
    private static ArrayList<String> requiredSensors;

    public static ArrayList<String> getRequiredSensors(){
        if(requiredSensors == null)
            requiredSensors = new ArrayList<>();
        return requiredSensors;
    }
    public static HashMap<String,List<FirebaseAction>> getActivityActions(){
        if(activityActions == null)
            activityActions = new HashMap<>();
        return activityActions;
    }
    public static HashMap<String,List<FirebaseAction>> getLocationActions(){
        if(locationActions == null)
            locationActions = new HashMap<>();
        return locationActions;
    }

}

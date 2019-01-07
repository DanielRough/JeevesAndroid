package com.jeevesandroid;

import android.app.Application;
import android.content.Context;

import com.jeevesandroid.actions.FirebaseAction;
import com.jeevesandroid.firebase.FirebaseProject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Application class to provide the global context.
 */
public class ApplicationContext extends Application
{

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
//    public static final String PHONE = "phone";
    public static final String UID = "uid";
    public static final String DEVELOPER_ID = "developerid";

    public static final String FINISHED_INTRODUCTION = "finished";
    //Contact
    public static final String FEEDBACK = "feedback";

    //Survey-based constants
    //Stuff relevant to survey actions
    public static final String INCOMPLETE = "incomplete";
    public static final String COMPLETE = "complete";
    public static final String TIME_SENT = "timeSent";
    public static final String INIT_TIME = "initTime";
    public static final String NOTIF_ID = "notificationid";
    public static final String SURVEY_NAME = "surveyname";
    public static final String WAS_INIT = "initialised";
    public static final String TRIG_TYPE = "triggerType";
    public static final String SURVEY_ID = "surveyId";
    public static final String DEADLINE = "deadline";
    public static final String STATUS = "status";

    //Variable constants
    public static final String LOCATION = "Location";
    public static final String NUMERIC = "Numeric";
    public static final String BOOLEAN = "Boolean";
    public static final String TIME = "Clock";
    public static final String DATE = "Date";

    //Specific variable names
    public static final String LAST_SURVEY_SCORE = "Last Survey Score";
    public static final String MISSED_SURVEYS = "Missed Surveys";
    public static final String COMPLETED_SURVEYS = "Completed Surveys";
    public static final String SURVEY_SCORE_DIFF = "Survey Score Difference";

    private static ApplicationContext instance;
    private static FirebaseProject currentproject;

// --Commented out by Inspection START (1/1/2019 6:21 PM):
    public ApplicationContext()
    {
        instance = this;
    }
// --Commented out by Inspection STOP (1/1/2019 6:21 PM)

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

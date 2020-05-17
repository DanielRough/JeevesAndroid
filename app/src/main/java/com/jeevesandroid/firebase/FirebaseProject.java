package com.jeevesandroid.firebase;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 28/04/2016.
 */
@IgnoreExtraProperties
public class FirebaseProject {

    private String id;
    private String name;
    private String pubKey;
    private final List<FirebaseSurvey> surveys = new ArrayList<>();
    private final List<FirebaseTrigger> triggers = new ArrayList<>();
    private final List<FirebaseUI> uidesign = new ArrayList<>();
    private final List<String> sensors = new ArrayList<>();
    private String type;
    private final List<FirebaseVariable> variables = new ArrayList<>();
    private String researcherno;
    private boolean hasSchedule;
    private Map<String,Object> scheduleAttrs;
    private boolean isDebug;


    public FirebaseProject() {
        // empty default constructor, necessary for Firebase to be able to deserialize
    }
    public String getid() {
        return id;
    }

    public String getname() {
        return name;
    }

    public String getpubKey(){
        return pubKey;
    }

    public String getresearcherno() { return researcherno; }

    public List<FirebaseSurvey> getsurveys() {
        return surveys;
    }

    public List<FirebaseTrigger> gettriggers() {
       return triggers;
    }

    public List<FirebaseVariable> getvariables() { return variables; }

    public List<FirebaseUI> getuidesign(){ return uidesign; }

    public List<String> getsensors() { return sensors; }

    public boolean gethasSchedule(){ return hasSchedule; }

    public Map<String,Object> getscheduleAttrs(){ return scheduleAttrs; }

    public boolean getisDebug(){ return isDebug;}

}



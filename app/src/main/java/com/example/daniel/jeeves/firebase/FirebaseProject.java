package com.example.daniel.jeeves.firebase;

import com.example.daniel.jeeves.firebase.FirebaseTrigger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 28/04/2016.
 */
public class FirebaseProject {

    public String getdescription() {
        return description;
    }

    public long getid() {
        return id;
    }

    public String getname() {
        return name;
    }

    public String gettype() {
        return type;
    }

    public long getxPos() {
        return xPos;
    }

    public long getyPos() {
        return yPos;
    }

    public String getresearcherno() { return researcherno; }
    String description;
    long id;
    String name;
    List<FirebaseSurvey> surveys = new ArrayList<>();
    List<FirebaseTrigger> triggers = new ArrayList<>();
    String type;
    List<Object> variables = new ArrayList<>();
    List<Object> expressions = new ArrayList<>();
    long xPos;
    long yPos;
    String researcherno;
    long maxNotifications;

    public FirebaseProject() {
        // empty default constructor, necessary for Firebase to be able to deserialize blog posts
    }
    public List<FirebaseSurvey> getsurveys() {
        return surveys;
    }
    public List<FirebaseTrigger> gettriggers() {
        return triggers;
    }
    public List<Object> getvariables() { return variables; }
    public List<Object> getexpressions() { return expressions; }
    public long getmaxNotifications(){ return maxNotifications;}
}

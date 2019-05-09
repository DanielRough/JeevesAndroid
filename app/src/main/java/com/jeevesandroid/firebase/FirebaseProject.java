package com.jeevesandroid.firebase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 28/04/2016.
 */
public class FirebaseProject {

    private String description;
    private String id;
    private String name;
    private boolean isPublic;
    private String pubKey;
    private final List<FirebaseSurvey> surveys = new ArrayList<>();
    private final List<FirebaseTrigger> triggers = new ArrayList<>();
    private final List<FirebaseUI> uidesign = new ArrayList<>();
    private final List<String> sensors = new ArrayList<>();
    private String type;
    private final List<FirebaseVariable> variables = new ArrayList<>();
    private final List<FirebaseExpression> expressions = new ArrayList<>();
    private long xPos;
    private long yPos;
    private String researcherno;
    private long maxNotifications;
    public FirebaseProject() {
        // empty default constructor, necessary for Firebase to be able to deserialize blog posts
    }
//
// --Commented out by Inspection START (5/8/2019 4:26 PM):
    public String getdescription() {
       return description;
    }

    public String getid() {
        return id;
    }

    public String getname() {
        return name;
    }
//
    public String gettype() {
        return type;
    }
//
// --Commented out by Inspection STOP (5/8/2019 4:26 PM)
    public long getxPos() {
        return xPos;
    }

    public long getyPos() {
        return yPos;
    }

    public boolean getisPublic(){
        return isPublic;
    }

    public void setisPublic(boolean isPublic){
        this.isPublic = isPublic;
    }

    public String getpubKey(){
        return pubKey;
    }

    public void setpubKey(String pubKey){
        this.pubKey = pubKey;
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
    public List<FirebaseExpression> getexpressions() { return expressions; }
    public List<String> getsensors() { return sensors; }
    public long getmaxNotifications(){ return maxNotifications;}
}

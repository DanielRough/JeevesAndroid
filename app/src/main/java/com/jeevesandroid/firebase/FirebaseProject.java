package com.jeevesandroid.firebase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 28/04/2016.
 */
public class FirebaseProject {

    String description;
    String id;
    String name;
    boolean isPublic;
    String pubKey;
    List<FirebaseSurvey> surveys = new ArrayList<>();
    List<FirebaseTrigger> triggers = new ArrayList<>();
    List<FirebaseUI> uidesign = new ArrayList<>();
    List<String> sensors = new ArrayList<>();
    String type;
    List<UserVariable> variables = new ArrayList<>();
    List<FirebaseExpression> expressions = new ArrayList<>();
    long xPos;
    long yPos;
    String researcherno;
    long maxNotifications;
    public FirebaseProject() {
        // empty default constructor, necessary for Firebase to be able to deserialize blog posts
    }

    public String getdescription() {
        return description;
    }

    public String getid() {
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
    public List<UserVariable> getvariables() { return variables; }
    public List<FirebaseUI> getuidesign(){ return uidesign; }
    public List<FirebaseExpression> getexpressions() { return expressions; }
    public List<String> getsensors() { return sensors; }
    public long getmaxNotifications(){ return maxNotifications;}
}

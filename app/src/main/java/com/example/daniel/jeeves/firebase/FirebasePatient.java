package com.example.daniel.jeeves.firebase;

import java.util.Map;

/**
 * Created by Daniel on 16/06/16.
 */
public class FirebasePatient {

    public String name;
    public String email;
    public String currentStudy;

    public FirebasePatient(){

    }

    public FirebasePatient(String name, String email){
        this.name = name;
        this.email = email;
    }
    public Map<String,Object> feedback;

    public Map<String,Object> getfeedback(){ return feedback; }
    public Map<String,FirebaseSurvey> incomplete;
    public Map<String,FirebaseSurvey> complete;

    public String getemail(){ return email; }
    public String getname(){
        return name;
    }
    public String getcurrentStudy(){ return currentStudy; }

    public Map<String,FirebaseSurvey> getincomplete(){
        return incomplete;
    }
    public Map<String,FirebaseSurvey> getcomplete(){
        return complete;
    }
}

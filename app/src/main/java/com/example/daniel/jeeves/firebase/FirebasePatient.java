package com.example.daniel.jeeves.firebase;

import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 16/06/16.
 */
public class FirebasePatient {

    public String address;
    public String firstName;
    public String lastName;
    public String phone;
    public String email;

    public Map<String,Object> feedback;

    public Map<String,Object> getfeedback(){ return feedback; }
    public Map<String,FirebaseSurvey> incomplete;
    public Map<String,FirebaseSurvey> complete;

    public String getemail(){ return email; }
    public String getaddress(){
        return address;
    }

    public String getfirstName(){
        return firstName;
    }
    public String getlastName(){
        return lastName;
    }
    public String getphone(){
        return phone;
    }

    public Map<String,FirebaseSurvey> getincomplete(){
        return incomplete;
    }
    public Map<String,FirebaseSurvey> getcomplete(){
        return complete;
    }
}

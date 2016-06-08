package com.example.daniel.jeeves.firebase;

import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 29/04/2016.
 */
public class FirebaseSurvey {
    public long getexpiryTime() {
        return expiryTime;
    }

    public long getid() {
        return id;
    }

    public String getname() {
        return name;
    }

    public String getdescription() { return description; }

    public List<FirebaseQuestion> getquestions() {
        return questions;
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

    long expiryTime;
        long id;
        String name;
        List<FirebaseQuestion> questions;
        String description;
        String type;
        long xPos;
        long yPos;


}

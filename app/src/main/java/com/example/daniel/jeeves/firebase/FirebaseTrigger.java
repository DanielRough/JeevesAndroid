package com.example.daniel.jeeves.firebase;

import com.example.daniel.jeeves.actions.FirebaseAction;

import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 29/04/2016.
 */
public class FirebaseTrigger {

    public long getclocktype() {
        return clocktype;
    }

    public String getdescription() {
        return description;
    }

    public long getid() {
        return id;
    }

    public String getname() {
        return name;
    }

    public Map<String,Object> getparams() {
        return params;
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

    public long clocktype;
    public String description;
    public long id;
    public String name;
    public Map<String,Object> params;

    public List<FirebaseAction> getactions() {
        return actions;
    }

    public List<FirebaseAction> actions;
    public String type;
    public long xPos;
    public long yPos;
}

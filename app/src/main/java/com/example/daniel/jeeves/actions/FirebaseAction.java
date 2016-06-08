package com.example.daniel.jeeves.actions;

import com.example.daniel.jeeves.actions.IAction;
import com.shaded.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Daniel on 26/05/15.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class FirebaseAction implements Serializable,IAction {
    public String getdescription() {
        return description;
    }

 //   public abstract void execute();

    public long getid() {
        return id;
    }

    public String getname() {
        return name;
    }

    public Map<String, Object> getparams() {
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

    public String description;
    public long id;
    public String name;
    public Map<String,Object> params;
    public String type;
    public long xPos;
    public long yPos;

    public void execute(){

    }
}

package com.jeevesandroid.actions.actiontypes;

import com.jeevesandroid.firebase.FirebaseExpression;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 26/05/15.
 */

@SuppressWarnings("unused")
public class FirebaseAction implements Serializable {
    private boolean manual;

    List<FirebaseAction> actions;
    FirebaseExpression condition;
    private String description;
    private long id;
    private String name;
    private Map<String,Object> params;
    private String type;
    private long xPos;
    private long yPos;
    private List<FirebaseExpression> vars;


    public long getid() {
        return id;
    }

    public String getname() {
        return name;
    }

    void setname(String name){
        this.name = name;
    }
    public Map<String, Object> getparams() {
        return params;
    }

    public void setparams(Map<String,Object> params){
        this.params = params;
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

    public void execute(){
    }

    void setvars(List<FirebaseExpression> vars){
        this.vars = vars;
    }

    public List<FirebaseExpression> getvars(){
        return vars;
    }

    public void setManual(boolean manual){
        this.manual = manual;
    }

    public boolean getmanual(){
        return manual;
    }

    public FirebaseExpression getcondition(){
        return condition;
    }
    public List<FirebaseAction> getactions() {
        return actions;
    }
}

package com.example.daniel.jeeves.actions;

import com.example.daniel.jeeves.firebase.FirebaseExpression;
import com.example.daniel.jeeves.firebase.FirebaseQuestion;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 26/05/15.
 */

public class FirebaseAction implements Serializable,IAction {
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

    private String description;
    private long id;
    private String name;
    private Map<String,Object> params;
    private String type;
    private long xPos;
    private long yPos;
    private List<FirebaseExpression> vars;
    public boolean execute(){
    return true;
    }

    public void setvars(List<FirebaseExpression> vars){
        this.vars = vars;
    }
    public List<FirebaseExpression> getvars(){
        return vars;
    }
    public boolean manual;
    public void setManual(boolean manual){
        this.manual = manual;
    }
    public boolean getmanual(){
        return manual;
    }

    public List<FirebaseAction> actions;
    public FirebaseExpression condition;

    public FirebaseExpression getcondition(){
        return condition;
    }
    public List<FirebaseAction> getactions() {
        return actions;
    }
}

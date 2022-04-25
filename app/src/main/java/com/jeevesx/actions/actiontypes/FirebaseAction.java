package com.jeevesx.actions.actiontypes;

import com.google.firebase.database.IgnoreExtraProperties;
import com.jeevesx.firebase.FirebaseExpression;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 26/05/15.
 */
@IgnoreExtraProperties
public class FirebaseAction implements Serializable {
    List<FirebaseAction> actions;
    FirebaseExpression condition;
    private String name;
    private Map<String,Object> params;
    private List<FirebaseExpression> vars;

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
    public void execute(){
    }
    void setvars(List<FirebaseExpression> vars){
        this.vars = vars;
    }
    public List<FirebaseExpression> getvars(){
        return vars;
    }
    public FirebaseExpression getcondition(){
        return condition;
    }
    public List<FirebaseAction> getactions() {
        return actions;
    }
}

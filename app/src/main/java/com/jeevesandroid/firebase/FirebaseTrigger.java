package com.jeevesandroid.firebase;

import com.jeevesandroid.actions.actiontypes.FirebaseAction;

import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 29/04/2016.
 */
public class FirebaseTrigger {

    private long clocktype;
    private String description;
    private String triggerId;
    private String name;
    private Map<String,Object> params;
    private List<FirebaseAction> actions;
    private List<FirebaseExpression> times;
    private List<String> variables;
    private String type;
    private long xPos;
    private long yPos;
    private FirebaseExpression dateFrom;
    private FirebaseExpression dateTo;
    private FirebaseExpression timeFrom;
    private FirebaseExpression timeTo;
    private FirebaseExpression location;

    public long getclocktype() {
        return clocktype;
    }

    public String getdescription() {
        return description;
    }

    public String gettriggerId() {
        return triggerId;
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

   public List<FirebaseAction> getactions() {
       return actions;
    }
//
    public void setvariables(List<String> variables){
        this.variables = variables;
    }

    public List<String> getvariables(){
        return variables;
    }

    public void settimes(List<FirebaseExpression> times){
        this.times = times;
    }

    public List<FirebaseExpression> gettimes(){
        return times;
    }

    public FirebaseExpression getdateFrom() {
        return dateFrom;
    }

    public FirebaseExpression getdateTo() {
        return dateTo;
    }

    public FirebaseExpression gettimeFrom() {
        return timeFrom;
    }

    public FirebaseExpression gettimeTo() {
        return timeTo;
    }

    public FirebaseExpression getlocation() { return location; }

    public void setdateFrom(FirebaseExpression dateFrom) {
        this.dateFrom = dateFrom;
    }

    public void setdateTo(FirebaseExpression dateTo) {
        this.dateTo = dateTo;
    }

    public void settimeFrom(FirebaseExpression timeFrom) {
        this.timeFrom = timeFrom;
    }


    public void settimeTo(FirebaseExpression timeTo) {
        this.timeTo = timeTo;
    }

    public void setlocation(FirebaseExpression location) { this.location = location; }
}

package com.jeeves.firebase;

import com.google.firebase.database.IgnoreExtraProperties;
import com.jeeves.actions.actiontypes.FirebaseAction;

import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 29/04/2016.
 */
@IgnoreExtraProperties
public class FirebaseTrigger {

    private String triggerId;
    private String name;
    private Map<String,Object> params;
    private List<FirebaseAction> actions;
    private List<FirebaseExpression> times;
    private List<String> variables;
    private String type;
    private FirebaseExpression dateFrom;
    private FirebaseExpression dateTo;
    private FirebaseExpression timeFrom;
    private FirebaseExpression timeTo;
    private FirebaseExpression location;

    public String gettriggerId() {
        return triggerId;
    }

    public String getname() {
        return name;
    }

    public Map<String,Object> getparams() {
        return params;
    }

    public List<FirebaseAction> getactions() {
       return actions;
    }

    public List<String> getvariables(){
        return variables;
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

}

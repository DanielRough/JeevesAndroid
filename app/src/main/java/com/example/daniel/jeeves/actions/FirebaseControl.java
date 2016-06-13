package com.example.daniel.jeeves.actions;

import com.example.daniel.jeeves.firebase.FirebaseExpression;
import com.firebase.client.Firebase;
import com.shaded.fasterxml.jackson.annotation.JsonIgnore;
import com.shaded.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 03/06/2016.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class FirebaseControl extends FirebaseAction implements Serializable,IControl{

    private List<FirebaseAction> actions;
    private FirebaseExpression condition;

    public FirebaseExpression getcondition(){
        return condition;
    }
    public List<FirebaseAction> getactions() {
        return actions;
    }

}

package com.jeevesandroid.actions;

import com.jeevesandroid.firebase.FirebaseExpression;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Daniel on 03/06/2016.
 */
//@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class FirebaseControl extends FirebaseAction implements Serializable{

    private List<FirebaseAction> actions;
    private FirebaseExpression condition;

    public FirebaseExpression getcondition(){
        return condition;
    }
    public List<FirebaseAction> getactions() {
        return actions;
    }

}

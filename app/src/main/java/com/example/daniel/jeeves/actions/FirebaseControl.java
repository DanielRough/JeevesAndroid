package com.example.daniel.jeeves.actions;

import com.example.daniel.jeeves.firebase.FirebaseExpression;

import java.util.Map;

/**
 * Created by Daniel on 03/06/2016.
 */
public abstract class FirebaseControl extends FirebaseAction{

    public FirebaseExpression condition;
    public FirebaseExpression getcondition() {
        return condition;
    }

}

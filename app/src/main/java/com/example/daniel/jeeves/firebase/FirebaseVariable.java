package com.example.daniel.jeeves.firebase;

import com.example.daniel.jeeves.firebase.FirebaseExpression;

/**
 * Created by Daniel on 29/04/2016.
 */
public class FirebaseVariable extends FirebaseExpression {

    protected long index;
    protected boolean isValue;

    public long getindex(){
        return index;
    }

    public boolean getisValue(){
        return isValue;
    }
}

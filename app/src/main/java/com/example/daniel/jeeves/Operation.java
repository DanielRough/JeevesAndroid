package com.example.daniel.jeeves;

import com.example.daniel.jeeves.firebase.FirebaseExpression;

/**
 * Created by Daniel on 11/06/15.
 * This will be a class that extends FirebaseExpression, it takes two expressions and an operator String
 *
 */
public class Operation extends FirebaseExpression {

    protected FirebaseExpression leftside;
    protected FirebaseExpression rightside;
    protected String operation;

    public Operation(FirebaseExpression lhs, FirebaseExpression rhs, String operation, String type){
        //super(type);
        this.leftside = lhs;
        this.rightside = rhs;
        this.operation = operation;
    }
    public FirebaseExpression getLHS(){
        return leftside;
    }
    public FirebaseExpression getRHS(){
        return rightside;
    }
    public String getOperation(){
        return operation;
    }
}

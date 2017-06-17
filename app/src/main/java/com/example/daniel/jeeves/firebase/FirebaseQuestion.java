package com.example.daniel.jeeves.firebase;

import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 25/05/2016.
 */
public class FirebaseQuestion {

    public String questionId;
    public FirebaseQuestion conditionQuestion;
    public String conditionConstraints;
    public String type;
    public long questionType;
    public String questionText;
    public String assignedVar;
    public boolean isMandatory;
    public Map<String,Object> params;
    long xPos;
    long yPos;

    public boolean getisMandatory() {return isMandatory; }

    public void setisMandatory(boolean isMandatory){ this.isMandatory = isMandatory; }

    public long getquestionType(){
        return questionType;
    }

    public String getquestionText(){
        return questionText;
    }

    public String getassignedVar(){
        return assignedVar;
    }

    public Map<String,Object> getparams(){
        return params;
    }

    public void setconditionQuestion(FirebaseQuestion q){
        this.conditionQuestion = q;
    }

    public FirebaseQuestion getconditionQuestion(){
        return conditionQuestion;
    }

    public void setconditionConstraints(String q){
        this.conditionConstraints = q;
    }

    public String getconditionConstraints(){
        return conditionConstraints;
    }

    public void setquestionId(String questionId){
        this.questionId = questionId;
    }

    public String getquestionId(){
        return questionId;
    }

    public String gettype(){
        return type;
    }

    public void settype(String type){
        this.type = type;
    }

    public long getxPos() {
        return xPos;
    }

    public long getyPos() {
        return yPos;
    }
}

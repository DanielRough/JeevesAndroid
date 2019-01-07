package com.jeevesandroid.firebase;

import java.util.Map;

/**
 * Created by Daniel on 25/05/2016.
 */
public class FirebaseQuestion {

    private String questionId;
    private FirebaseQuestion conditionQuestion;
    private String conditionConstraints;
    private String type;
    private String questionType;
    private String questionText;
    private String assignedVar;
    private boolean isMandatory;
    private Map<String,Object> params;
    private long xPos;
    private long yPos;

    public boolean getisMandatory() {return isMandatory; }

    public void setisMandatory(boolean isMandatory){ this.isMandatory = isMandatory; }

    public String getquestionType(){
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

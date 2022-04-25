package com.jeevesx.firebase;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

/**
 * Created by Daniel on 25/05/2016.
 */
@IgnoreExtraProperties
public class FirebaseQuestion {

    private String questionId;
    private FirebaseQuestion conditionQuestion;
    private String conditionConstraints;
    private String type;
    private String questionType;
    private String questionText;
    private String assignedVar;
    private boolean isMandatory;
    private Map<String, Object> params;

    public boolean getisMandatory() {
        return isMandatory;
    }

    public String getquestionType() {
        return questionType;
    }
    public void setquestionType(String type){this.questionType = type;}

    public String getquestionText() {
        return questionText;
    }
    public void setQuestionText(String text){this.questionText = text;}

    public String getassignedVar() {
        return assignedVar;
    }
    public void setassignedVar(String var){this.assignedVar = var;}

    public Map<String, Object> getparams() {
        return params;
    }

    public FirebaseQuestion getconditionQuestion() {
        return conditionQuestion;
    }

    public String getconditionConstraints() {
        return conditionConstraints;
    }

    public String getquestionId() {
        return questionId;
    }
    public void setQuestionId(String id){this.questionId = id; }

}
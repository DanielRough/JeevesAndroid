package com.example.daniel.jeeves.firebase;

import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 25/05/2016.
 */
public class FirebaseQuestion {

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

    public long questionType;
    public String questionText;
    public String assignedVar;
    public Map<String,Object> params;
}

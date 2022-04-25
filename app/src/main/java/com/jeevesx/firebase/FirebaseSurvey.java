package com.jeevesx.firebase;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

/**
 * Created by Daniel on 29/04/2016.
 */
@IgnoreExtraProperties
public class FirebaseSurvey {

    private int triggerType;
    private long expiryTime;
    private long timeSent;
    private long timeFinished;
    private long id;
    private List<FirebaseQuestion> questions;
    private String type;
    private String title;
    private String surveyId;
    private String encodedAnswers;
    private String encodedKey;
    private boolean begun; //Has the user begun completing the survey?
    private boolean fastTransition;
    private List<String> answers;
    private String key;
    public FirebaseSurvey(){

    }

    public boolean getfastTransition(){ return fastTransition;}
    public int gettriggerType() { return triggerType; }
    public void settriggerType(int triggerType) { this.triggerType = triggerType; }
    public long getexpiryTime() {
        return expiryTime;
    }
    public List<FirebaseQuestion> getquestions() {
        return questions;
    }
    public List<String> getanswers(){ return answers; }
    public long gettimeSent(){ return timeSent; }
    public String getencodedAnswers(){
        return encodedAnswers;
    }
    public void setencodedAnswers(String encodedAnswers){
        this.encodedAnswers = encodedAnswers;
    }
    public String getencodedKey() { return encodedKey; }
    public void setencodedKey(String encodedKey) { this.encodedKey = encodedKey; }
    public String getsurveyId(){
        return surveyId;
    }
    public String gettitle(){ return title; }
    public void setkey(String key){
        this.key = key;
    }
    public String getkey(){
        return key;
    }
    public void settimeSent(long timeSent){ this.timeSent = timeSent;}
    public void setanswers( List<String> answers){
        this.answers = answers;
    }
    public void settimeFinished(long timeFinished){
        this.timeFinished = timeFinished;
    }
    public long gettimeFinished(){ return timeFinished; }
    public boolean getbegun(){return begun;}
    public void setbegun(){this.begun = true;}
}

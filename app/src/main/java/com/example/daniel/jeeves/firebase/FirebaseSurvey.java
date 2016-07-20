package com.example.daniel.jeeves.firebase;

import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 29/04/2016.
 */
public class FirebaseSurvey {

    public FirebaseSurvey(){

    }
    public long getexpiryTime() {
        return expiryTime;
    }

    public long getid() {
        return id;
    }

    public String getname() {
        return name;
    }

    public String getdescription() { return description; }

    public List<FirebaseQuestion> getquestions() {
        return questions;
    }

    public  List<Map<String, String>> getanswers(){ return answers; }

    public String gettype() {
        return type;
    }

    public long getxPos() {
        return xPos;
    }

    public long getyPos() {
        return yPos;
    }

    public long gettimeSent(){ return timeSent; }

    public long gettimeAlive(){ return timeAlive; }
    public long gettimeFinished(){ return timeFinished; }

    public long getscore(){ return score; }
    public void setscore(long score){this.score = score;}
    long timeAlive;
    long expiryTime;
    long timeSent;
    long timeFinished;
    long score;
        long id;
        String name;
        List<FirebaseQuestion> questions;
        String description;
        String type;
        long xPos;
        long yPos;
    boolean begun; //Has the user begun completing the survey?
    List<Map<String, String>> answers;
    String key;
    public void setkey(String key){
        this.key = key;
    }
    public String getkey(){
        return key;
    }
    public void setanswers( List<Map<String, String>> answers){
        this.answers = answers;
    }
    public void settimeFinished(long timeFinished){
        this.timeFinished = timeFinished;
    }
    public boolean getbegun(){return begun;}
    public void setbegun(){this.begun = true;}
}

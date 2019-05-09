package com.jeevesandroid.firebase;

import java.util.List;

/**
 * Created by Daniel on 29/04/2016.
 */
public class FirebaseVariable extends FirebaseExpression {
    private boolean isRandom = false;
    private List<String> randomOptions;
    public boolean getisRandom() {
        return isRandom;
    }
    //public void setisRandom(boolean isRandom) {
      //  this.isRandom = isRandom;
    //}

    public List<String> getrandomOptions(){
        return randomOptions;
    }
    //public void setrandomOptions(List<String> options) {
      //  this.randomOptions = options;
    //}

    private boolean isCustom;

    public boolean getisCustom(){
        return isCustom;
    }
}

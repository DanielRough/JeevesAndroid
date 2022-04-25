package com.jeevesx.firebase;

import java.util.List;

/**
 * Class representing a User Attribute block
 */
public class FirebaseVariable extends FirebaseExpression {
    private boolean isRandom = false;
    private List<String> randomOptions;
    private boolean isCustom;
    public boolean getisRandom() {
        return isRandom;
    }

    public List<String> getrandomOptions(){
        return randomOptions;
    }


    public boolean getisCustom(){
        return isCustom;
    }
}

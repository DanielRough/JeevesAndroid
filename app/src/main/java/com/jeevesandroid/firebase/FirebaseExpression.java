package com.jeevesandroid.firebase;

import com.shaded.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 10/06/15.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class",defaultImpl = FirebaseExpression.class)
public class FirebaseExpression implements Serializable{

 private String description;
    private long id;
    private String name;
    private String type;
    private List<FirebaseExpression> variables;
    private String vartype;
    private long xPos;
    private long yPos;
    private String value;
    private Map<String,Object> params;
    private long index;
    private boolean isValue;
    private boolean isCustom;
    public String getdescription() { return description; }
    public long getid() {
        return id;
    }

    public String getname() {
        return name;
    }

    public String gettype() {
        return type;
    }

    public List<FirebaseExpression> getvariables() {
        return variables;
    }

    public String getvartype() {
        return vartype;
    }

    public Map<String,Object> getparams() {
        return params;
    }

    public long getxPos() {
        return xPos;
    }

    public long getyPos() {
        return yPos;
    }

    public String getvalue(){return value; }

    public boolean getisCustom(){ return isCustom; }

    public long getindex(){
        return index;
    }

    public boolean getisValue(){
        return isValue;
    }

}

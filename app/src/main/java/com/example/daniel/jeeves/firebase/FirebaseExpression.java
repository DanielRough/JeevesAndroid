package com.example.daniel.jeeves.firebase;

import com.shaded.fasterxml.jackson.annotation.JsonIgnore;
import com.shaded.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 10/06/15.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class",defaultImpl = FirebaseExpression.class)
public class FirebaseExpression implements Serializable{

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

    protected String description;
    protected long id;
    protected String name;
    protected String type;
    protected List<FirebaseExpression> variables;
    protected String vartype;
    protected long xPos;
    protected long yPos;
    protected String value;
    protected Map<String,Object> params;
    protected long index;
    protected boolean isValue;
    protected boolean isCustom;

    public boolean getisCustom(){ return isCustom; }

    public long getindex(){
        return index;
    }

    public boolean getisValue(){
        return isValue;
    }

}

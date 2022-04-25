package com.jeevesx.firebase;

import com.google.firebase.database.IgnoreExtraProperties;
import com.shaded.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 10/06/15.
 */
@IgnoreExtraProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class",defaultImpl = FirebaseExpression.class)
public class FirebaseExpression implements Serializable{

    protected String name;
    protected List<FirebaseExpression> variables;
    protected String vartype;
    protected String value;
    protected Map<String,Object> params;
    protected boolean isValue;
    protected boolean isCustom;

    public String getname() {
        return name;
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
    public String getvalue(){return value; }
    public boolean getisCustom(){ return isCustom; }
    public boolean getisValue(){
        return isValue;
    }

}

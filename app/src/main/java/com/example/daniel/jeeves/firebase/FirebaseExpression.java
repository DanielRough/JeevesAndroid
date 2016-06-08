package com.example.daniel.jeeves.firebase;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Daniel on 10/06/15.
 */
public class FirebaseExpression {

    protected String description;

    public String getdescription() {
        return description;
    }

    public long getid() {
        return id;
    }

    public String getname() {
        return name;
    }

    public String gettype() {
        return type;
    }

    public List<Object> getvariables() {
        return variables;
    }

    public String getvartype() {
        return vartype;
    }

    public long getxPos() {
        return xPos;
    }

    public long getyPos() {
        return yPos;
    }

    protected long id;
    protected String name;
    protected String type;
    protected List<Object> variables;
    protected String vartype;
    protected long xPos;
    protected long yPos;

}

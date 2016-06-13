package com.example.daniel.jeeves.actions;

import com.shaded.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Created by Daniel on 17/05/2016.
 */
public interface IAction {

    public void execute();
}

package com.example.daniel.jeeves.actions;

import com.example.daniel.jeeves.actions.FirebaseAction;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Daniel on 08/06/15.
 */
public interface IControl extends Serializable {

public ArrayList<FirebaseAction> getControlActions();
}


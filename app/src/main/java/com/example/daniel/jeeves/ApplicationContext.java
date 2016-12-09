package com.example.daniel.jeeves;

import android.app.Application;
import android.content.Context;

import com.example.daniel.jeeves.firebase.FirebaseProject;

/**
 * Application class to provide the global context.
 */
public class ApplicationContext extends Application
{

    private static ApplicationContext instance;
    private static FirebaseProject currentproject;
    public ApplicationContext()
    {
        instance = this;
    }

    public static boolean hasStartedSensing = false;
    public static FirebaseProject getProject(){ return currentproject;}

    public static void setCurrentproject(FirebaseProject proj){
        currentproject = proj;
    }

    public static Context getContext()
    {
        return instance;
    }

}

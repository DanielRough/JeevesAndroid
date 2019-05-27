package com.jeevesandroid;/* **************************************************
 Copyright (c) 2012, University of Cambridge
 Neal Lathia, neal.lathia@cl.cam.ac.uk
This demo application was developed as part of the EPSRC Ubhave (Ubiquitous and
Social Computing for Positive Behaviour Change) Project. For more
information, please visit http://www.emotionsense.org
Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.
THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ************************************************** */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jeevesandroid.actions.ActionExecutorService;
import com.jeevesandroid.actions.ActionUtils;
import com.jeevesandroid.actions.actiontypes.FirebaseAction;
import com.jeevesandroid.firebase.FirebaseExpression;
import com.jeevesandroid.firebase.FirebaseTrigger;
import com.jeevesandroid.triggers.ESTriggerManager;
import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.TriggerReceiver;
import com.jeevesandroid.triggers.config.TriggerConfig;

import java.util.ArrayList;
import java.util.Map;

class TriggerListener implements TriggerReceiver {

    private final ESTriggerManager triggerManager;
    private final int triggerType;
    private int triggerSubscriptionId;
    private final Context serviceContext;
    private ArrayList<FirebaseAction> actionsToPerform;

    public TriggerListener(int triggerType, Context c) throws TriggerException {
        this.triggerType = triggerType;
        this.serviceContext = c;
        this.triggerManager = ESTriggerManager.getTriggerManager(AppContext.getContext());
        Log.d("Listener","New listener of type " + triggerType);
    }


    public void subscribeToTrigger(final FirebaseTrigger trigger, ArrayList<FirebaseAction> actions) {
        TriggerConfig config = new TriggerConfig();
        SharedPreferences varPrefs = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getContext());
        Map<String, Object> params = trigger.getparams();

        Map<String,Object> scheduleVars = AppContext.getProject().getscheduleAttrs();
        String fromTime = scheduleVars.get(AppContext.WAKE_TIME).toString();
        String toTime = scheduleVars.get(AppContext.SLEEP_TIME).toString();
        Log.d("FROMTO","From time is " + fromTime + " and to time is " + toTime);

        //This is for the set-times trigger I think
        ArrayList<Long> times = new ArrayList<>();
        if(trigger.gettimes() != null){
            for(FirebaseExpression time : trigger.gettimes()){
                if(time.getisValue()){
                    times.add(Long.parseLong(time.getvalue()));
                }
                else{
                    if(time.getname().equals(fromTime) || time.getname().equals(toTime))
                        config.addParameter(TriggerConfig.IS_SCHEDULED,true);
                    times.add(Long.parseLong(varPrefs.getString(time.getname(),"0")));
                }
            }
            config.addParameter("times",times);
        }
        if(trigger.getparams() != null) {
            for (String param : params.keySet()) {
                Object value = params.get(param);
                config.addParameter(param, value);
            }
        }


        //Now try and find what variables we have
        if(trigger.getdateFrom() != null){
            String name = trigger.getdateFrom().getname();
            config.addParameter(TriggerConfig.FROM_DATE,varPrefs.getString(name,"0"));
        }
        if(trigger.gettimeFrom() != null){
            String name = trigger.gettimeFrom().getname();
            Log.d("TRIGFROM","Trig from is " + name);
            if(name.equals(fromTime)){
                config.addParameter(TriggerConfig.IS_SCHEDULED,true);
            }
            config.addParameter(TriggerConfig.LIMIT_BEFORE_HOUR,varPrefs.getString(name,"0"));
        }
        if(trigger.getdateTo() != null){
            String name = trigger.getdateTo().getname();
            config.addParameter(TriggerConfig.TO_DATE,varPrefs.getString(name,"0"));
        }
        if(trigger.gettimeTo() != null){
            String name = trigger.gettimeTo().getname();
            Log.d("TRIGTO","Trig to is " + name);
            if(name.equals(toTime)){
                config.addParameter(TriggerConfig.IS_SCHEDULED,true);
            }
            config.addParameter(TriggerConfig.LIMIT_AFTER_HOUR,varPrefs.getString(name,"0"));
        }
        try {
            actionsToPerform = new ArrayList<>();
            for (FirebaseAction action : actions) {
                actionsToPerform.add(ActionUtils.create(action));
            }
            triggerSubscriptionId = triggerManager.addTrigger(triggerType, this, config);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unsubscribeFromTrigger() {
        try {
            triggerManager.removeTrigger(triggerSubscriptionId);
        } catch (TriggerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the conditions for the trigger are satisfied. Starts an ActionExecutorService
     * to begin executing the contained actions
     * @param triggerId Integer ID of the satisfied trigger
     */
    @Override
    public void onNotificationTriggered(int triggerId) {
        Log.d("Executing","Executing Trigger " + triggerId);
        Intent actionIntent = new Intent(serviceContext, ActionExecutorService.class);
        actionIntent.putExtra(ActionUtils.ACTIONS, actionsToPerform);
        actionIntent.putExtra(AppContext.TRIG_TYPE, triggerType);
        serviceContext.startService(actionIntent);
    }





}

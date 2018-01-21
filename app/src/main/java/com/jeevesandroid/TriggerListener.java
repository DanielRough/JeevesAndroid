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
import android.util.Log;

import com.jeevesandroid.actions.ActionUtils;
import com.jeevesandroid.actions.FirebaseAction;
import com.ubhave.triggermanager.ESTriggerManager;
import com.ubhave.triggermanager.TriggerException;
import com.ubhave.triggermanager.TriggerReceiver;
import com.ubhave.triggermanager.config.TriggerConfig;

import java.util.ArrayList;

import static com.jeevesandroid.ApplicationContext.TRIG_TYPE;
import static com.jeevesandroid.actions.ActionUtils.ACTIONS;

public class TriggerListener implements TriggerReceiver {


// ...

    private final ESTriggerManager triggerManager;
    private String triggerId;
    private int triggerType, triggerSubscriptionId;
    private Context serviceContext;
    private ArrayList<FirebaseAction> actionsToPerform;
    private TriggerConfig params;
    Intent actionIntent;
   // private static int locTriggerId = 0;

    public TriggerListener(int triggerType, Context c) throws TriggerException {
        this.triggerType = triggerType;
        this.serviceContext = c;
        this.triggerManager = ESTriggerManager.getTriggerManager(ApplicationContext.getContext());

    }


    public void subscribeToTrigger(final TriggerConfig params, ArrayList<FirebaseAction> actions, String triggerId) {
        //     this.actions = actions;
        try {
            this.triggerId = triggerId;
            this.params = params;
            actionsToPerform = new ArrayList<>();
            for (FirebaseAction action : actions) {

                actionsToPerform.add(ActionUtils.create(action));
                Log.d("Action is ", action.getname());

            }
//            for(FirebaseAction a : actionsToPerform){
//                Log.d("AND THIppppp","IS" + a.toString());
//            }
            triggerSubscriptionId = triggerManager.addTrigger(triggerType, this, params);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unsubscribeFromTrigger(String caller) {
        try {
            triggerManager.removeTrigger(triggerSubscriptionId);
       ///     SubscriptionIds.removeSubscription(triggerId);
        } catch (TriggerException e) {
            e.printStackTrace();
        }
    }

    //Here's the method that gets called when the conditions are fulfilled. It starts the 'ActionExecutorService' to begin going through dem actions
    @Override
    public void onNotificationTriggered(int triggerId) {
        actionIntent = new Intent(serviceContext, ActionExecutorService.class);
        actionIntent.putExtra(ACTIONS, actionsToPerform);
        Log.d("NOTIFICATION","WAS TRIGGERED");
        actionIntent.putExtra(TRIG_TYPE, triggerType);
        serviceContext.startService(actionIntent);
    }





}

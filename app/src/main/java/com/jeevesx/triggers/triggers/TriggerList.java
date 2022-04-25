/* **************************************************
 Copyright (c) 2012, University of Cambridge
 Neal Lathia, neal.lathia@cl.cam.ac.uk
 Kiran Rachuri, kiran.rachuri@cl.cam.ac.uk

This library was developed as part of the EPSRC Ubhave (Ubiquitous and
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

package com.jeevesx.triggers.triggers;

import android.content.Context;

import com.jeevesx.triggers.AbstractSubscriptionList;
import com.jeevesx.triggers.TriggerException;
import com.jeevesx.triggers.TriggerReceiver;
import com.jeevesx.triggers.config.TriggerConfig;
import com.jeevesx.triggers.triggers.clock.BeginTrigger;
import com.jeevesx.triggers.triggers.clock.DailyWakeupTrigger;
import com.jeevesx.triggers.triggers.clock.IntervalTrigger;
import com.jeevesx.triggers.triggers.clock.WindowTrigger;
import com.jeevesx.triggers.triggers.clock.OneTimeTrigger;
import com.jeevesx.triggers.triggers.clock.SetTimesTrigger;
import com.jeevesx.triggers.triggers.sensor.ButtonTrigger;
import com.jeevesx.triggers.triggers.sensor.ImmediateSensorTrigger;
import com.jeevesx.triggers.triggers.sensor.SurveyTrigger;

public class TriggerList extends AbstractSubscriptionList<Trigger>
{
	public static Trigger createTrigger(Context context, int type, int id, TriggerReceiver listener,
										TriggerConfig params) throws TriggerException
	{
		if (type == TriggerUtils.TYPE_CLOCK_TRIGGER_ONCE) {
			return new OneTimeTrigger(context, id, listener, params);
		}
		else if (type == TriggerUtils.TYPE_DAILY_SCHEDULER) {
			return new DailyWakeupTrigger(context, id, listener, params);
		}
		else if (type == TriggerUtils.TYPE_JEEVES_TRIGGER_ON_INTERVAL) {
			return new IntervalTrigger(context,id,listener,params);
		}
		else if (type == TriggerUtils.TYPE_JEEVES_TRIGGER_WINDOW) {
			return new WindowTrigger(context,id,listener,params);
		}
		else if (type == TriggerUtils.TYPE_CLOCK_TRIGGER_BEGIN) {
			return new BeginTrigger(context,id,listener,params);
		}
		else if (type == TriggerUtils.TYPE_CLOCK_TRIGGER_SETTIMES) {
			return new SetTimesTrigger(context, id, listener, params);
		}
		else {
			return getSensorTrigger(context, type, id, listener, params);
		}
	}
	
	private static Trigger getSensorTrigger(Context context, int type, int id, TriggerReceiver listener,
											TriggerConfig params) throws TriggerException {
		if (type == TriggerUtils.TYPE_SENSOR_TRIGGER_IMMEDIATE) {
			return new ImmediateSensorTrigger(context, id, listener, params);
		}
		else if (type == TriggerUtils.TYPE_SENSOR_TRIGGER_BUTTON) {
			return new ButtonTrigger(context, id, listener, params);
		}
		else if (type == TriggerUtils.TYPE_SENSOR_TRIGGER_SURVEY) {
			return new SurveyTrigger(context, id, listener, params);
		}
		else {
			throw new TriggerException("Unknown trigger.");
		}
	}

	@Override
	public void remove(int triggerId) throws TriggerException {
		Trigger s = map.get(triggerId);
		if (s != null) {
			s.stop();
		}
		super.remove(triggerId);
	}

}

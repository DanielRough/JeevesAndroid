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

package com.jeevesandroid.triggers.config;

import java.util.HashMap;

public class TriggerConfig
{	

	//Constant determining whether this is a scheduled trigger
	public final static String IS_SCHEDULED = "isScheduled";

	//Start and end dates of particular triggers
	public final static String FROM_DATE = "dateFrom";
	public final static String TO_DATE = "dateTo";

	// Time Boundaries
	public static final String LIMIT_BEFORE_HOUR = "limitBeforeHour";
	public static final String LIMIT_AFTER_HOUR = "limitAfterHour";
	public static final String INTERVAL_TRIGGER_TIME = "intervalTriggerTime";
	public static final String INTERVAL_TRIGGER_WINDOW = "intervalTriggerWindow";
	public static final String FIXED_RANDOM = "fixedRandom";

	// Sensor Based Triggers
	public final static String SENSOR_TYPE = "selectedSensor";
	public final static String INTERESTING_VALUE = "result";
	public final static String NOTIFICATION_PROBABILITY = "notificationProb";

	//BUTTON NAME
	public static final String BUTTON_NAME = "selectedButton";
	private final HashMap<String, Object> parameters;

	//SURVEY NAME
	public static final String SURVEY_NAME = "selectedSurvey";
	public static final String SURVEY_RESULT = "result";

	public TriggerConfig()
	{
		parameters = new HashMap<>();
	}
	
	public void addParameter(final String key, final Object value)
	{
		parameters.put(key, value);
	}

	public HashMap<String,Object> getParams(){
		return parameters;
	}
	public Object getParameter(final String key)
	{
		if (parameters.containsKey(key))
		{
			return parameters.get(key);
		}
		else
		{
			return defaultValue(key);
		}
	}
	
	private Object defaultValue(final String key)
	{
		if (key.equals(LIMIT_BEFORE_HOUR)) {
			return TriggerConstants.DEFAULT_DO_NOT_DISTURB_BEFORE_MINUTES;
		}
		else if (key.equals(LIMIT_AFTER_HOUR)) {
			return TriggerConstants.DEFAULT_DO_NOT_DISTURB_AFTER_MINUTES;
		}
		else if (key.equals(INTERVAL_TRIGGER_TIME)) {
			return TriggerConstants.DEFAULT_MIN_TRIGGER_INTERVAL_MINUTES;
		}
		else {
			System.err.println("Key not found: "+key);
			return null;
		}
	}
	
	public boolean containsKey(String key)
	{
		return parameters.containsKey(key);
	}

	public long getLongValue(String key){
		return Long.parseLong(getParameter(key).toString());
	}
	public int getValue(String key)
	{
		return Integer.parseInt(getParameter(key).toString());
	}
}

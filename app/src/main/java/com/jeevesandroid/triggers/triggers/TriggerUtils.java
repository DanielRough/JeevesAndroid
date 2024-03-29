package com.jeevesandroid.triggers.triggers;

import com.jeevesandroid.sensing.sensormanager.sensors.SensorUtils;
import com.jeevesandroid.triggers.TriggerException;
import com.jeevesandroid.triggers.config.TriggerConstants;

public class TriggerUtils
{

	public static final int TYPE_CLOCK_TRIGGER_BEGIN			= 10010;
	public static final int TYPE_CLOCK_TRIGGER_ONCE 			= 10000;
	public static final int TYPE_DAILY_SCHEDULER 				= 10001;
	public static final int TYPE_SENSOR_TRIGGER_IMMEDIATE 		= 10003;
	public static final int TYPE_CLOCK_TRIGGER_SETTIMES			= 10006;
	public static final int TYPE_SENSOR_TRIGGER_BUTTON			= 10007;
	public static final int TYPE_JEEVES_TRIGGER_ON_INTERVAL		= 10008;
	public static final int TYPE_JEEVES_TRIGGER_WINDOW 			= 10012;
	public static final int TYPE_SENSOR_TRIGGER_SURVEY			= 10009;
	public static final int TYPE_SENSOR_TRIGGER_LOCATION		= 10011;

	private static final int SENSOR_TRIGGER_BLUETOOTH		= SensorUtils.SENSOR_TYPE_BLUETOOTH;
	private static final int SENSOR_TRIGGER_MICROPHONE 		= SensorUtils.SENSOR_TYPE_MICROPHONE;
	private static final int SENSOR_TRIGGER_WIFI			= SensorUtils.SENSOR_TYPE_WIFI;
	private static final int SENSOR_TRIGGER_BUTTON			= SensorUtils.SENSOR_TYPE_BUTTON;
	private static final int SENSOR_TRIGGER_SURVEY			= SensorUtils.SENSOR_TYPE_SURVEY;


	private static final String NAME_ACCELEROMETER 				= "Accelerometer";
	private static final String NAME_BLUETOOTH					= "Bluetooth";
	private static final String NAME_MICROPHONE					= "Microphone";
	private static final String NAME_WIFI						= "WiFi";
	private static final String NAME_BUTTON						= "Button";
	private static final String NAME_SURVEY						= "Survey";


	private static final String NAME_JEEVES_TRIGGER_BEGIN		= "Begin Trigger";
	private static final String NAME_CLOCK_TRIGGER_ONCE 			= "type_clock_once";
	private static final String NAME_DAILY_SCHEDULER = "type_clock_interval";
	private static final String NAME_SENSOR_TRIGGER_IMMEDIATE 	= "Sensor Trigger";
	private static final String NAME_SENSOR_TRIGGER_BUTTON		= "Button Trigger";
	private static final String NAME_CLOCK_TRIGGER_SETTIMES		= "Set Times Trigger";
	private static final String NAME_JEEVES_TRIGGER_ON_INTERVAL 	= "Repeated Time Trigger";
	private static final String NAME_JEEVES_TRIGGER_WINDOW 	= "Interval Trigger";
	private static final String NAME_SENSOR_TRIGGER_SURVEY		= "Survey Trigger";
	private static final String NAME_SENSOR_TRIGGER_LOCATION		= "Location Trigger";



	private static final String[] SENSOR_NAMES = new String[]{
		NAME_ACCELEROMETER,
		NAME_BLUETOOTH,
		NAME_MICROPHONE,
		NAME_WIFI,
		NAME_BUTTON,
		NAME_SURVEY
	};

	private static final int[] SENSOR_IDS = new int[]{
		SENSOR_TRIGGER_BLUETOOTH,
		SENSOR_TRIGGER_MICROPHONE,
		SENSOR_TRIGGER_WIFI,
		SENSOR_TRIGGER_BUTTON,
		SENSOR_TRIGGER_SURVEY
	};
	private static final String[] ALL_NAMES = new String[]{
		NAME_JEEVES_TRIGGER_BEGIN,
		NAME_CLOCK_TRIGGER_ONCE,
		NAME_DAILY_SCHEDULER,
		NAME_SENSOR_TRIGGER_IMMEDIATE,
		NAME_CLOCK_TRIGGER_SETTIMES,
		NAME_SENSOR_TRIGGER_BUTTON,
		NAME_JEEVES_TRIGGER_ON_INTERVAL,
		NAME_JEEVES_TRIGGER_WINDOW,
		NAME_SENSOR_TRIGGER_SURVEY,
		NAME_SENSOR_TRIGGER_LOCATION
	};

	private static final int[] ALL_IDS = new int[]{
		TYPE_CLOCK_TRIGGER_BEGIN,
		TYPE_CLOCK_TRIGGER_ONCE,
        TYPE_DAILY_SCHEDULER,
		TYPE_SENSOR_TRIGGER_IMMEDIATE,
		TYPE_CLOCK_TRIGGER_SETTIMES,
		TYPE_SENSOR_TRIGGER_BUTTON,
		TYPE_JEEVES_TRIGGER_ON_INTERVAL,
		TYPE_JEEVES_TRIGGER_WINDOW,
		TYPE_SENSOR_TRIGGER_SURVEY,
		TYPE_SENSOR_TRIGGER_LOCATION

	};

	private static final String[] ALL_ACTIONS = new String[]{
		TriggerConstants.ACTION_NAME_ONE_TIME_TRIGGER,
		TriggerConstants.ACTION_NAME_INTERVAL_TRIGGER,
		TriggerConstants.ACTION_NAME_RANDOM_DAY_TRIGGER,
		TriggerConstants.ACTION_NAME_SENSOR_TRIGGER_IMMEDIATE,
		TriggerConstants.ACTION_NAME_SENSOR_TRIGGER_DELAYED,
		TriggerConstants.ACTION_NAME_DAY_INTERVAL_TRIGGER
	};

	public static String getTriggerName(int type) throws TriggerException
	{
		switch (type)
		{
			case TYPE_CLOCK_TRIGGER_BEGIN: return NAME_JEEVES_TRIGGER_BEGIN;
			case TYPE_CLOCK_TRIGGER_ONCE: return NAME_CLOCK_TRIGGER_ONCE;
			case TYPE_DAILY_SCHEDULER: return NAME_DAILY_SCHEDULER;
			case TYPE_SENSOR_TRIGGER_IMMEDIATE: return NAME_SENSOR_TRIGGER_IMMEDIATE;
			case TYPE_CLOCK_TRIGGER_SETTIMES: return NAME_CLOCK_TRIGGER_SETTIMES;
			case TYPE_SENSOR_TRIGGER_BUTTON: return NAME_SENSOR_TRIGGER_BUTTON;
			case TYPE_SENSOR_TRIGGER_SURVEY: return NAME_SENSOR_TRIGGER_SURVEY;
			case TYPE_SENSOR_TRIGGER_LOCATION: return NAME_SENSOR_TRIGGER_LOCATION;
			case TYPE_JEEVES_TRIGGER_ON_INTERVAL: return NAME_JEEVES_TRIGGER_ON_INTERVAL;
			case TYPE_JEEVES_TRIGGER_WINDOW: return NAME_JEEVES_TRIGGER_WINDOW;
			default: throw new TriggerException("Unknown trigger: "+type);
		}
	}

	public static int getSensorType(final String sensorName) throws TriggerException
	{
		for (int i=0; i<SENSOR_NAMES.length; i++)
		{
			if (SENSOR_NAMES[i].equals(sensorName))
			{
				return SENSOR_IDS[i];
			}
		}
		throw new TriggerException("Unknown trigger: "+sensorName);
	}

	public static int getTriggerType(final String triggerName) throws TriggerException
	{
		for (int i=0; i<ALL_NAMES.length; i++)
		{
			if (ALL_NAMES[i].equals(triggerName))
			{
				return ALL_IDS[i];
			}
		}
		throw new TriggerException("Unknown trigger: "+triggerName);
	}

	public static String getTriggerActionName(final String triggerType) throws TriggerException
	{
		for (int i=0; i<ALL_NAMES.length; i++)
		{
			if (ALL_NAMES[i].equals(triggerType))
			{
				return ALL_ACTIONS[i];
			}
		}
		throw new TriggerException("Unknown trigger: "+triggerType);
	}
}

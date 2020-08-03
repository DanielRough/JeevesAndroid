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

package com.jeeves.sensing.sensormanager.config;

import com.jeeves.sensing.sensormanager.sensors.SensorUtils;

import java.util.HashMap;

public class SensorConfig implements Cloneable
{
	// sampling window size sets the data capture duration from the sensor, like
	// accelerometer sampling window
	public final static String SENSE_WINDOW_LENGTH_MILLIS = "SENSE_WINDOW_LENGTH_MILLIS";

	// number of sampling cycles sets the number of times a sensor samples the
	// data, and this is relevant for sensors like Bluetooth, Wifi, where there
	// is no fixed sampling window and the amount of sampling time
	// depends on the number of devices in the environment. the no. of cycles
	// sets the number of scans (wifi or bluetooth) to be performed
	public final static String NUMBER_OF_SENSE_CYCLES = "NUMBER_OF_SENSE_CYCLES";

	// length of sensing window per cycle of sensing, this is relevant for
	// bluetooth and wifi sensors where sense window is a function of number of
	// devices in the environment. the lengths are defined in the Constants
	// class
	public final static String SENSE_WINDOW_LENGTH_PER_CYCLE_MILLIS = "SENSE_WINDOW_LENGTH_PER_CYCLE_MILLIS";

	// this is the sleep interval between two consecutive sensor samplings
	public final static String POST_SENSE_SLEEP_LENGTH_MILLIS = "POST_SENSE_SLEEP_LENGTH_MILLIS";
	/*
	 * Config Keys
	 */
	public final static String DATA_SET_RAW_VALUES = "RAW_DATA";
	public final static String DATA_EXTRACT_FEATURES = "EXTRACT_FEATURES";
	
	// data preferences
	public final static boolean GET_RAW_DATA = true;
	public final static boolean GET_PROCESSED_DATA = false;

	public SensorConfig clone()
	{
		SensorConfig clonedSensorConfig = new SensorConfig();
		for (String key : configParams.keySet())
		{
			Object obj = configParams.get(key);
			clonedSensorConfig.setParameter(key, obj);
		}
		return clonedSensorConfig;
	}
	
	public static SensorConfig getDefaultConfig(int sensorType)
	{
		SensorConfig sensorConfig = new SensorConfig();
		switch (sensorType)
		{
		case SensorUtils.SENSOR_TYPE_BLUETOOTH:
			sensorConfig = BluetoothConfig.getDefault();
			break;
		case SensorUtils.SENSOR_TYPE_LOCATION:
			sensorConfig = LocationConfig.getDefault();
			break;
		case SensorUtils.SENSOR_TYPE_MICROPHONE:
			sensorConfig = MicrophoneConfig.getDefault();
			break;
		case SensorUtils.SENSOR_TYPE_WIFI:
			sensorConfig = WifiConfig.getDefault();
			break;
		}
		return sensorConfig;
	}
	protected final HashMap<String, Object> configParams;

	public SensorConfig()
	{
		configParams = new HashMap<String, Object>();
	}

	public void setParameter(final String parameterName, final Object parameterValue)
	{
		configParams.put(parameterName, parameterValue);
	}

	public Object getParameter(final String parameterName)
	{
		Object parameterValue = null;
		if (configParams.containsKey(parameterName))
		{
			parameterValue = configParams.get(parameterName);
		}
		return parameterValue;
	}

	public boolean containsParameter(final String parameterName)
	{
		return configParams.containsKey(parameterName);
	}

	public void removeParameter(final String parameterName)
	{
		configParams.remove(parameterName);
	}

}

/* **************************************************
 Copyright (c) 2012, University of Cambridge
 Neal Lathia, neal.lathia@cl.cam.ac.uk

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

package com.jeevesandroid.sensing.jsonformatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.jeevesandroid.sensing.jsonformatter.pull.BluetoothFormatter;
import com.jeevesandroid.sensing.jsonformatter.pull.LocationFormatter;
import com.jeevesandroid.sensing.jsonformatter.pull.MicrophoneFormatter;
import com.jeevesandroid.sensing.jsonformatter.pull.WifiFormatter;
import com.jeevesandroid.sensing.sensormanager.ESException;
import com.jeevesandroid.sensing.sensormanager.config.SensorConfig;
import com.jeevesandroid.sensing.sensormanager.data.SensorData;
import com.jeevesandroid.sensing.sensormanager.sensors.SensorUtils;

/**
 * Class for formatting any sensed data into JSON for storage in Firebase
 */
public abstract class JSONFormatter
{
	protected final static String SENSOR_TYPE = "dataType";
	protected final static String SENSE_TIME = "senseStartTime";
	protected final static String SENSE_TIME_MILLIS = "senseStartTimeMillis";
	
	private final static String UNKNOWN_SENSOR = "Unknown";
	protected final static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS dd MM yyyy Z z", Locale.US);
	private final static String SLEEP_LENGTH = "postSenseSleepMillis";

	protected final Context applicationContext;
	protected final int sensorType;

	protected void addGenericConfig(JSONObject json, SensorConfig config) throws JSONException
	{
		if (config != null)
		{
			Long sleepLength = (Long) config.getParameter(SLEEP_LENGTH);
			if (sleepLength != null)
			{
				json.put(SLEEP_LENGTH, sleepLength);
			}
		}
	}

	public static JSONFormatter getJSONFormatter(final Context c, final int sensorType)
	{
		switch (sensorType)
		{
			case SensorUtils.SENSOR_TYPE_BLUETOOTH:
				return new BluetoothFormatter(c);
			case SensorUtils.SENSOR_TYPE_LOCATION:
				return new LocationFormatter(c);
			case SensorUtils.SENSOR_TYPE_MICROPHONE:
				return new MicrophoneFormatter(c);
			case SensorUtils.SENSOR_TYPE_WIFI:
				return new WifiFormatter(c);
			default:
				return null;
		}
	}
	public JSONFormatter(final Context c, final int sensorType)
	{
		applicationContext = c;
		this.sensorType = sensorType;
	}

	public JSONObject toJSON(final SensorData data)// throws DataHandlerException
	{
		JSONObject json = new JSONObject();
		if (data != null)
		{
			try
			{
				addGenericData(json, data);
				addSensorSpecificData(json, data);
				SensorConfig config = data.getSensorConfig();
				if (config != null)
				{
					addGenericConfig(json, config);
					addSensorSpecificConfig(json, config);
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return json;
	}

	public String toString(final SensorData data) throws Exception
	{
		JSONObject jsonData = toJSON(data);
		if (jsonData != null)
		{
			return jsonData.toString();
		}
		else
		{
			return null;
		}
	}

	protected void addGenericData(final JSONObject json, final SensorData data)
	{
		try
		{
			json.put(SENSE_TIME, createTimeStamp(data.getTimestamp()));
			json.put(SENSE_TIME_MILLIS, data.getTimestamp());
			try
			{
				String sensorName = SensorUtils.getSensorName(data.getSensorType());
				json.put(SENSOR_TYPE, sensorName);
			}
			catch (ESException e)
			{
				e.printStackTrace();
				json.put(SENSOR_TYPE, UNKNOWN_SENSOR);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	protected String createTimeStamp(final long time)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return formatter.format(calendar.getTime());
	}


	protected abstract void addSensorSpecificData(final JSONObject json, final SensorData data) throws JSONException;

	protected abstract void addSensorSpecificConfig(final JSONObject json, final SensorConfig config) throws JSONException;

	protected String getString(final String key, final JSONObject data)
	{
		try
		{
			String value = (String) data.get(key);
			return value;
		}
		catch (Exception e)
		{
			return null;
		}
	}


}

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

package com.jeevesx.sensing.sensormanager.sensors;

import android.content.Context;
import android.util.Log;

import com.jeevesx.sensing.sensormanager.ESException;
import com.jeevesx.sensing.sensormanager.classifier.BluetoothDataClassifier;
import com.jeevesx.sensing.sensormanager.classifier.LocationDataClassifier;
import com.jeevesx.sensing.sensormanager.classifier.MicrophoneDataClassifier;
import com.jeevesx.sensing.sensormanager.classifier.SensorDataClassifier;
import com.jeevesx.sensing.sensormanager.classifier.WifiDataClassifier;

import java.util.ArrayList;

public class SensorUtils
{

	public final static int SENSOR_TYPE_BATTERY = 5002;
	public final static int SENSOR_TYPE_BLUETOOTH = 5003;
	public final static int SENSOR_TYPE_LOCATION = 5004;
	public final static int SENSOR_TYPE_MICROPHONE = 5005;
	public final static int SENSOR_TYPE_WIFI = 5010;
	public final static int SENSOR_TYPE_STEP_COUNTER = 5025;
	public final static int SENSOR_TYPE_SURVEY = 5027;
	public final static int SENSOR_TYPE_BUTTON = 5028;

	public final static String SENSOR_NAME_BLUETOOTH = "Bluetooth";
	public final static String SENSOR_NAME_LOCATION = "Location";
	public final static String SENSOR_NAME_MICROPHONE = "Microphone";
	public final static String SENSOR_NAME_WIFI = "WiFi";

	private static SensorEnum getSensor(int sensorType) throws ESException
	{
		for (SensorEnum s : SensorEnum.values())
		{
			if (s.getType() == sensorType)
			{
				return s;
			}
		}
		throw new ESException(ESException.UNKNOWN_SENSOR_TYPE, "Unknown sensor type " + sensorType);
	}

	public static String getSensorName(int sensorType) throws ESException
	{
		return getSensor(sensorType).getName();
	}

	public static int getSensorType(final String sensorName) throws ESException
	{
		for (SensorEnum s : SensorEnum.values())
		{
			if (s.getName().equals(sensorName))
			{
				return s.getType();
			}
		}
		throw new ESException(ESException.UNKNOWN_SENSOR_NAME, "Unknown sensor name " + sensorName);
	}

	public static ArrayList<SensorInterface> getAllSensors(final Context applicationContext)
	{
		ArrayList<SensorInterface> sensors = new ArrayList<>();
		for (SensorEnum s : SensorEnum.values())
		{
			try
			{
				SensorInterface sensor = getSensor(s.getType(), applicationContext);
				sensors.add(sensor);

			}
			catch (ESException e)
			{
					Log.d("SensorUtils", "Warning: " + e.getMessage());
			}
		}
		return sensors;
	}

	private static SensorInterface getSensor(int id, Context context) throws ESException
	{
		switch (id)
		{
		case SENSOR_TYPE_BLUETOOTH:
			return BluetoothSensor.getSensor(context);
		case SENSOR_TYPE_LOCATION:
			return LocationSensor.getSensor(context);
		case SENSOR_TYPE_MICROPHONE:
			return MicrophoneSensor.getSensor(context);
		case SENSOR_TYPE_WIFI:
			return WifiSensor.getSensor(context);
		default:
			throw new ESException(ESException.UNKNOWN_SENSOR_TYPE, "Unknown sensor id: " + id);
		}
	}

	public static SensorDataClassifier getSensorDataClassifier(int sensorType) throws ESException
	{
		switch (sensorType)
		{
		case SensorUtils.SENSOR_TYPE_BLUETOOTH:
			return new BluetoothDataClassifier();
		case SensorUtils.SENSOR_TYPE_LOCATION:
			return new LocationDataClassifier();
		case SensorUtils.SENSOR_TYPE_MICROPHONE:
			return new MicrophoneDataClassifier();
		case SensorUtils.SENSOR_TYPE_WIFI:
			return new WifiDataClassifier();
		default:
			throw new ESException(ESException.UNKNOWN_SENSOR_TYPE, "Sensor data classifier not support for the sensor type " + sensorType);
		}
	}
}

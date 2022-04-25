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

package com.jeevesx.sensing.jsonformatter.pull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;

import com.jeevesx.sensing.jsonformatter.JSONFormatter;
import com.jeevesx.sensing.sensormanager.config.LocationConfig;
import com.jeevesx.sensing.sensormanager.config.SensorConfig;
import com.jeevesx.sensing.sensormanager.data.LocationData;
import com.jeevesx.sensing.sensormanager.data.SensorData;
import com.jeevesx.sensing.sensormanager.sensors.SensorUtils;

public class LocationFormatter extends JSONFormatter
{
	private final static String LATITUDE = "latitude";
	private final static String LONGITUDE = "longitude";
	private final static String ACCURACY = "accuracy";
	private final static String SPEED = "speed";
	private final static String BEARING = "bearing";
	private final static String PROVIDER = "provider";
	private final static String TIME = "time";
	private final static String LOCAL_TIME = "local_time";
	private final static String DATA = "locations";

	private final static String LOCATION_ACCURACY = "configAccuracy";
	private final static String UNKNOWN_STRING = "unknown";
	private final static double UNKNOWN_DOUBLE = 0.0;
	private final static long UNKNOWN_LONG = 0;

	public LocationFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_LOCATION);
	}

	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException
	{
		LocationData locationData = (LocationData) data;
		List<Location> locations = locationData.getLocations();
		
		if (locations != null && !locations.isEmpty())
		{
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS dd MM yyyy Z z", Locale.US);
			
			JSONArray array = new JSONArray();
			for (Location location : locations)
			{
				JSONObject locationJSON = new JSONObject();
				if (location != null)
				{
					locationJSON.put(LATITUDE, location.getLatitude());
					locationJSON.put(LONGITUDE, location.getLongitude());
					locationJSON.put(ACCURACY, location.getAccuracy());
					locationJSON.put(SPEED, location.getSpeed());
					locationJSON.put(BEARING, location.getBearing());
					locationJSON.put(PROVIDER, location.getProvider());
					locationJSON.put(TIME, location.getTime());
					
					calendar.setTimeInMillis(location.getTime());
					locationJSON.put(LOCAL_TIME, formatter.format(calendar.getTime()));
				}
				else
				{
					locationJSON.put(LATITUDE, UNKNOWN_DOUBLE);
					locationJSON.put(LONGITUDE, UNKNOWN_DOUBLE);
					locationJSON.put(ACCURACY, UNKNOWN_DOUBLE);
					locationJSON.put(SPEED, UNKNOWN_DOUBLE);
					locationJSON.put(BEARING, UNKNOWN_DOUBLE);
					locationJSON.put(PROVIDER, UNKNOWN_STRING);
					locationJSON.put(TIME, UNKNOWN_LONG);
				}
				array.put(locationJSON);
			}
			json.put(DATA, array);
		}

	}

	@Override
	protected void addSensorSpecificConfig(JSONObject json, SensorConfig config) throws JSONException
	{
		json.put(LOCATION_ACCURACY, config.getParameter(LocationConfig.ACCURACY_TYPE));
	}

}

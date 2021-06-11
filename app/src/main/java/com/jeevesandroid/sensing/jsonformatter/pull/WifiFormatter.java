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

package com.jeevesandroid.sensing.jsonformatter.pull;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.jeevesandroid.sensing.jsonformatter.JSONFormatter;
import com.jeevesandroid.sensing.sensormanager.config.SensorConfig;
import com.jeevesandroid.sensing.sensormanager.data.SensorData;
import com.jeevesandroid.sensing.sensormanager.data.WifiData;
import com.jeevesandroid.sensing.sensormanager.data.WifiScanResult;
import com.jeevesandroid.sensing.sensormanager.sensors.SensorUtils;


public class WifiFormatter extends JSONFormatter
{
	private final static String SCAN_RESULT = "scanResult";
	private final static String SSID = "ssid";
	private final static String BSSID = "bssid";
	private final static String CAPABILITIES = "capabilities";
	private final static String LEVEL = "level";
	private final static String FREQUENCY = "frequency";

	private final static String UNAVAILABLE = "unavailable";
	private final static String SENSE_CYCLES = "senseCycles";
	
	public WifiFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_WIFI);
	}

	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException
	{
		WifiData wifiData = (WifiData) data;
		ArrayList<WifiScanResult> results = wifiData.getWifiScanData();
		JSONArray resultJSON = new JSONArray();
		if (results != null)
		{
			json.put(UNAVAILABLE, false);
			for (WifiScanResult result : results)
			{
				JSONObject scanJSON = new JSONObject();
				scanJSON.put(SSID, result.getSsid());
				scanJSON.put(BSSID, result.getBssid());
				scanJSON.put(CAPABILITIES, result.getCapabilities());
				scanJSON.put(LEVEL, result.getLevel());
				scanJSON.put(FREQUENCY, result.getFrequency());
				resultJSON.put(scanJSON);
			}
		}
		else
		{
			json.put(UNAVAILABLE, true);
		}
		json.put(SCAN_RESULT, resultJSON);
	}

	@Override
	protected void addSensorSpecificConfig(JSONObject json, SensorConfig config) throws JSONException
	{
		if (config != null)
		{
			json.put(SENSE_CYCLES, config.getParameter(SensorConfig.NUMBER_OF_SENSE_CYCLES));
		}
	}
}

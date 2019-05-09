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
import com.jeevesandroid.sensing.sensormanager.data.BluetoothData;
import com.jeevesandroid.sensing.sensormanager.data.ESBluetoothDevice;
import com.jeevesandroid.sensing.sensormanager.data.SensorData;
import com.jeevesandroid.sensing.sensormanager.sensors.SensorUtils;

public class BluetoothFormatter extends JSONFormatter
{	
	private final static String DEVICES = "devices";
	private final static String TIME_STAMP = "timeStamp";
	private final static String ADDRESS = "address";
	private final static String NAME = "name";
	private final static String RSSI = "rssi";

	private final static String SENSE_CYCLES = "senseCycles";

	public BluetoothFormatter(final Context context)
	{
		super(context, SensorUtils.SENSOR_TYPE_BLUETOOTH);
	}
	
	@Override
	protected void addSensorSpecificData(JSONObject json, SensorData data) throws JSONException
	{
		BluetoothData bluetoothData = (BluetoothData) data;
		ArrayList<ESBluetoothDevice> devices = bluetoothData.getBluetoothDevices();

		if (devices != null)
		{
			JSONArray neighbours = new JSONArray();
			for (ESBluetoothDevice device : devices)
			{
				JSONObject neighbour = new JSONObject();
				neighbour.put(ADDRESS, device.getBluetoothDeviceAddress());
				neighbour.put(NAME, device.getBluetoothDeviceName());
				neighbour.put(RSSI, device.getRssi());
				neighbour.put(TIME_STAMP, device.getTimestamp());
				neighbours.put(neighbour);
			}
			json.put(DEVICES, neighbours);
		}
	}

	@Override
	protected void addSensorSpecificConfig(JSONObject json, SensorConfig config) throws JSONException
	{
		json.put(SENSE_CYCLES, config.getParameter(SensorConfig.NUMBER_OF_SENSE_CYCLES));
	}

}

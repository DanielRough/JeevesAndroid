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

package com.jeevesandroid.sensing.sensormanager.data;

import com.jeevesandroid.sensing.sensormanager.config.SensorConfig;
import com.jeevesandroid.sensing.sensormanager.sensors.SensorUtils;

import java.util.ArrayList;

public class WifiData extends SensorData
{

	private ArrayList<WifiScanResult> wifiScanData;

	public WifiData(long senseStartTimestamp, SensorConfig sensorConfig)
	{
		super(senseStartTimestamp, sensorConfig);
	}

	public void setWifiScanData(ArrayList<WifiScanResult> wifiScanData)
	{
		this.wifiScanData = wifiScanData;
	}

	public ArrayList<WifiScanResult> getWifiScanData()
	{
		return wifiScanData;
	}

	public int getSensorType()
	{
		return SensorUtils.SENSOR_TYPE_WIFI;
	}

}

package com.jeeves.sensing.sensormanager.process;

import android.content.Context;

import com.jeeves.sensing.sensormanager.config.SensorConfig;
import com.jeeves.sensing.sensormanager.data.WifiData;
import com.jeeves.sensing.sensormanager.data.WifiScanResult;

import java.util.ArrayList;

public class WifiProcessor extends AbstractProcessor
{
	public WifiProcessor(final Context c, boolean rw, boolean sp)
	{
		super(c, rw, sp);
	}

	public WifiData process(long pullSenseStartTimestamp, ArrayList<WifiScanResult> wifiScanResults, SensorConfig sensorConfig)
	{
		WifiData wifiData = new WifiData(pullSenseStartTimestamp, sensorConfig);
		if (setRawData)
		{
			wifiData.setWifiScanData(wifiScanResults);
		}
		return wifiData;
	}

}

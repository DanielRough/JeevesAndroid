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

package com.jeevesandroid.sensing.sensormanager.sensors;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.jeevesandroid.sensing.sensormanager.ESException;
import com.jeevesandroid.sensing.sensormanager.config.SensorConfig;
import com.jeevesandroid.sensing.sensormanager.data.WifiData;
import com.jeevesandroid.sensing.sensormanager.data.WifiScanResult;
import com.jeevesandroid.sensing.sensormanager.process.WifiProcessor;

import java.util.ArrayList;
import java.util.List;

public class WifiSensor extends AbstractSensor
{
	private static final String TAG = "WifiSensor";
	private static final String[] REQUIRED_PERMISSIONS = new String[]{
		Manifest.permission.ACCESS_WIFI_STATE,
		Manifest.permission.ACCESS_NETWORK_STATE,
		Manifest.permission.CHANGE_WIFI_STATE
	};

	private static WifiSensor wifiSensor;
	private static Object lock = new Object();
	
	private WifiManager wifiManager;
	private BroadcastReceiver wifiReceiver;
	private ArrayList<WifiScanResult> wifiScanResults;
	private int cyclesRemaining;
	private WifiData wifiData;

	public static WifiSensor getSensor(final Context context) throws ESException
	{
		if (wifiSensor == null)
		{
			synchronized (lock)
			{
				if (wifiSensor == null)
				{
					if (allPermissionsGranted(context, REQUIRED_PERMISSIONS))
					{
						wifiSensor = new WifiSensor(context);
					}
					else
					{
						throw new ESException(ESException.PERMISSION_DENIED, SensorUtils.SENSOR_NAME_WIFI);
					}
				}
			}
		}
		return wifiSensor;
	}

	private WifiSensor(Context context)
	{
		super(context);
		wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		wifiReceiver = new BroadcastReceiver()
		{
			public void onReceive(Context context, Intent intent)
			{
				List<ScanResult> wifiList = wifiManager.getScanResults();
				for (ScanResult result : wifiList)
				{
					WifiScanResult wifiScanResult = new WifiScanResult(result.SSID, result.BSSID, result.capabilities, result.level, result.frequency);
					wifiScanResults.add(wifiScanResult);
				}

				cyclesRemaining -= 1;
				if ((cyclesRemaining > 0) && (wifiManager.isWifiEnabled()))
				{
					wifiManager.startScan();
				}
				else
				{
					notifySenseCyclesComplete();
				}
			}

		};
	}

	protected String getLogTag()
	{
		return TAG;
	}

	public int getSensorType()
	{
		return SensorUtils.SENSOR_TYPE_WIFI;
	}

	protected WifiData getMostRecentRawData()
	{
		return wifiData;
	}

	protected void processSensorData()
	{
		WifiProcessor processor = (WifiProcessor) getProcessor();
		wifiData = processor.process(pullSenseStartTimestamp, wifiScanResults, sensorConfig.clone());
	}

	protected boolean startSensing()
	{
		try
		{
			wifiScanResults = null;
			if (wifiManager.isWifiEnabled())
			{
				wifiScanResults = new ArrayList<WifiScanResult>();
				cyclesRemaining = (Integer) sensorConfig.getParameter(SensorConfig.NUMBER_OF_SENSE_CYCLES);
				applicationContext.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
				wifiManager.startScan();
				return true;
			}
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
		}
		return false;
	}

	// Called when a scan is finished
	protected void stopSensing()
	{
		applicationContext.unregisterReceiver(wifiReceiver);
	}

}

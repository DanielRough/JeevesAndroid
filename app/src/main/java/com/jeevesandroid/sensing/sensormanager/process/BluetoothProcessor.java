package com.jeevesandroid.sensing.sensormanager.process;

import android.content.Context;

import com.jeevesandroid.sensing.sensormanager.config.SensorConfig;
import com.jeevesandroid.sensing.sensormanager.data.BluetoothData;
import com.jeevesandroid.sensing.sensormanager.data.ESBluetoothDevice;

import java.util.ArrayList;

public class BluetoothProcessor extends AbstractProcessor
{
	public BluetoothProcessor(final Context c, boolean rw, boolean sp)
	{
		super(c, rw, sp);
	}

	public BluetoothData process(long pullSenseStartTimestamp, ArrayList<ESBluetoothDevice> btDevices, SensorConfig sensorConfig)
	{
		BluetoothData bluetoothData = new BluetoothData(pullSenseStartTimestamp, sensorConfig);
		if (setRawData)
		{
			bluetoothData.setBluetoothDevices(btDevices);
		}
		return bluetoothData;
	}

}

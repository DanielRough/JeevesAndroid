package com.jeeves.sensing.sensormanager.process;

import android.content.Context;

import com.jeeves.sensing.sensormanager.config.SensorConfig;
import com.jeeves.sensing.sensormanager.data.BluetoothData;
import com.jeeves.sensing.sensormanager.data.ESBluetoothDevice;

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

package com.jeevesx.sensing.sensormanager.process;

import android.content.Context;

import com.jeevesx.sensing.sensormanager.config.SensorConfig;
import com.jeevesx.sensing.sensormanager.data.BluetoothData;
import com.jeevesx.sensing.sensormanager.data.ESBluetoothDevice;

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

package com.jeevesx.sensing.sensormanager.process;

import android.content.Context;

import com.jeevesx.sensing.sensormanager.ESException;
import com.jeevesx.sensing.sensormanager.sensors.SensorUtils;

public abstract class AbstractProcessor
{
	public static AbstractProcessor getProcessor(Context c, int sensorType, boolean setRawData, boolean setProcessedData) throws ESException
	{
		if (!setRawData && !setProcessedData)
		{
			throw new ESException(ESException.INVALID_STATE, "No data (raw/processed) requested from the processor");
		}

		switch (sensorType)
		{
		case SensorUtils.SENSOR_TYPE_BLUETOOTH:
			return new BluetoothProcessor(c, setRawData, setProcessedData);
		case SensorUtils.SENSOR_TYPE_LOCATION:
			return new LocationProcessor(c, setRawData, setProcessedData);
		case SensorUtils.SENSOR_TYPE_MICROPHONE:
			return new MicrophoneProcessor(c, setRawData, setProcessedData);
		case SensorUtils.SENSOR_TYPE_WIFI:
			return new WifiProcessor(c, setRawData, setProcessedData);
		default:
			throw new ESException(ESException.UNKNOWN_SENSOR_TYPE, "No processor defined for this sensor id ("+sensorType+").");
		}
	}

	protected final boolean setRawData,setProcessedData;
	protected final Context appContext;

	public AbstractProcessor(final Context context, final boolean rw, final boolean sp)
	{
		this.appContext = context;
		this.setRawData = rw;
		this.setProcessedData = sp;
	}
}

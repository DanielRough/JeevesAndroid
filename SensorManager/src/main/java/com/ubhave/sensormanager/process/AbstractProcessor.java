package com.ubhave.sensormanager.process;

import android.content.Context;

import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.process.pull.AccelerometerProcessor;
import com.ubhave.sensormanager.process.pull.BluetoothProcessor;
import com.ubhave.sensormanager.process.pull.LocationProcessor;
import com.ubhave.sensormanager.process.pull.StepCounterProcessor;
import com.ubhave.sensormanager.process.pull.WifiProcessor;
import com.ubhave.sensormanager.process.push.BatteryProcessor;
import com.ubhave.sensormanager.process.push.ConnectionStateProcessor;
import com.ubhave.sensormanager.process.push.ConnectionStrengthProcessor;
import com.ubhave.sensormanager.process.push.PhoneStateProcessor;
import com.ubhave.sensormanager.process.push.SMSProcessor;
import com.ubhave.sensormanager.process.push.ScreenProcessor;
import com.ubhave.sensormanager.sensors.SensorUtils;

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
		case SensorUtils.SENSOR_TYPE_ACCELEROMETER:
			return new AccelerometerProcessor(c, setRawData, setProcessedData);
		case SensorUtils.SENSOR_TYPE_BLUETOOTH:
			return new BluetoothProcessor(c, setRawData, setProcessedData);
		case SensorUtils.SENSOR_TYPE_LOCATION:
			return new LocationProcessor(c, setRawData, setProcessedData);
		case SensorUtils.SENSOR_TYPE_WIFI:
			return new WifiProcessor(c, setRawData, setProcessedData);
		case SensorUtils.SENSOR_TYPE_BATTERY:
			return new BatteryProcessor(c, setRawData, setProcessedData);
		case SensorUtils.SENSOR_TYPE_CONNECTION_STATE:
			return new ConnectionStateProcessor(c, setRawData, setProcessedData);
		case SensorUtils.SENSOR_TYPE_PHONE_STATE:
			return new PhoneStateProcessor(c, setRawData, setProcessedData);
		case SensorUtils.SENSOR_TYPE_SCREEN:
			return new ScreenProcessor(c, setRawData, setProcessedData);
		case SensorUtils.SENSOR_TYPE_SMS:
			return new SMSProcessor(c, setRawData, setProcessedData);
		case SensorUtils.SENSOR_TYPE_CONNECTION_STRENGTH:
			return new ConnectionStrengthProcessor(c, setRawData, setProcessedData);
		case SensorUtils.SENSOR_TYPE_STEP_COUNTER:
			return new StepCounterProcessor(c, setRawData, setProcessedData);
		default:
			throw new ESException(ESException.UNKNOWN_SENSOR_TYPE, "No processor defined for this sensor id ("+sensorType+").");
		}
	}

	protected final boolean setRawData, setProcessedData;
	protected final Context appContext;

	public AbstractProcessor(final Context context, final boolean rw, final boolean sp)
	{
		this.appContext = context;
		this.setRawData = rw;
		this.setProcessedData = sp;
	}
}

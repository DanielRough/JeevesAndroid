package com.jeevesx.sensing.sensormanager.config;

public class WifiConfig
{
	public static final int DEFAULT_SAMPLING_CYCLES = 1;
	public static final int DEFAULT_SAMPLING_WINDOW_SIZE_PER_CYCLE_MILLIS = 5000;
	public static final long DEFAULT_SLEEP_INTERVAL = 15 * 60 * 1000;	
	
	public static SensorConfig getDefault()
	{
		SensorConfig sensorConfig = new SensorConfig();
		sensorConfig.setParameter(SensorConfig.POST_SENSE_SLEEP_LENGTH_MILLIS, DEFAULT_SLEEP_INTERVAL);
		sensorConfig.setParameter(SensorConfig.NUMBER_OF_SENSE_CYCLES, DEFAULT_SAMPLING_CYCLES);
		sensorConfig.setParameter(SensorConfig.SENSE_WINDOW_LENGTH_PER_CYCLE_MILLIS, DEFAULT_SAMPLING_WINDOW_SIZE_PER_CYCLE_MILLIS);
		return sensorConfig;
	}
}

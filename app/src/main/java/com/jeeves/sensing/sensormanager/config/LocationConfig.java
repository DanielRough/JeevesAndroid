package com.jeeves.sensing.sensormanager.config;

public class LocationConfig
{
	public final static String ACCURACY_TYPE = "LOCATION_ACCURACY";
	public final static String LOCATION_ACCURACY_COARSE = "LOCATION_ACCURACY_COARSE";
	public final static String LOCATION_ACCURACY_FINE = "LOCATION_ACCURACY_FINE";
	
	public static final long DEFAULT_SAMPLING_WINDOW_SIZE_MILLIS = 60000;
	public static final long DEFAULT_SLEEP_INTERVAL = 2*60000;
	public final static int LOCATION_CHANGE_DISTANCE_THRESHOLD = 100;
	
	public static SensorConfig getDefault()
	{
		SensorConfig sensorConfig = new SensorConfig();
		sensorConfig.setParameter(SensorConfig.POST_SENSE_SLEEP_LENGTH_MILLIS, DEFAULT_SLEEP_INTERVAL);
		sensorConfig.setParameter(SensorConfig.SENSE_WINDOW_LENGTH_MILLIS, DEFAULT_SAMPLING_WINDOW_SIZE_MILLIS);
		sensorConfig.setParameter(LocationConfig.ACCURACY_TYPE, LOCATION_ACCURACY_COARSE);
		return sensorConfig;
	}
}

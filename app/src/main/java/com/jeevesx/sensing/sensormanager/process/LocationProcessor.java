package com.jeevesx.sensing.sensormanager.process;

import android.content.Context;
import android.location.Location;

import com.jeevesx.sensing.sensormanager.config.SensorConfig;
import com.jeevesx.sensing.sensormanager.data.LocationData;

import java.util.List;

public class LocationProcessor extends AbstractProcessor
{
	public LocationProcessor(final Context c, boolean rw, boolean sp)
	{
		super(c, rw, sp);
	}

// --Commented out by Inspection START (5/8/2019 4:26 PM):
	public LocationData process(long pullSenseStartTimestamp, final List<Location> lastLocation, final SensorConfig sensorConfig)
	{
		LocationData locationData = new LocationData(pullSenseStartTimestamp, sensorConfig);
		if (setRawData)
		{
			locationData.setLocations(lastLocation);
		}
		return locationData;
	}
// --Commented out by Inspection STOP (5/8/2019 4:26 PM)

}

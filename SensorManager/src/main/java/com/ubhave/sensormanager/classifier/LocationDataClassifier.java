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

package com.ubhave.sensormanager.classifier;

import android.location.Location;
import android.util.Log;

import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.config.pull.LocationConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pull.LocationData;

public class LocationDataClassifier implements SensorDataClassifier
{
	private static boolean wasSameLastTime = false;
	@Override
	public boolean isInteresting(final SensorData sensorData, final SensorConfig sensorConfig, String value, boolean isTrigger)
	{
		//First, we need to find the lat/long that this location name corresponds to
		String[] locationInfo = value.split(";");
		if(locationInfo.length == 0)return false; //There was nothing here!
		if(locationInfo[0].length() == 0)return false;
		double latitude = Double.parseDouble(locationInfo[0]);
		double longitude = Double.parseDouble(locationInfo[1]);
		Location targetLocation = new Location("");//provider name is unecessary
		targetLocation.setLatitude(latitude);//your coords of course
		targetLocation.setLongitude(longitude);

		LocationData data = (LocationData) sensorData;
		//LocationData prevData = (LocationData) sensorData.getPrevSensorData();

		Location currLoc = null;
		//Location prevLoc = null;

		if (data != null)
		{
			currLoc = data.getLastLocation();
		}

	//	if (prevData != null)
	//	{
	//		prevLoc = prevData.getLastLocation();
	//	}

		// Interesting = SAME locations FOR GOODNESS SAKE
		return areSameLocations(currLoc, targetLocation);
	}

	private boolean areSameLocations(Location loc1, Location loc2)
	{
		if ((loc1 != null) && (loc2 != null))
		{
			Log.d("DISTANCE","distnace from " + loc1.getLatitude()+";"+loc1.getLongitude() + " and " + loc2.getLatitude() + ";" + loc2.getLatitude() + " is " + loc1.distanceTo(loc2));
			Log.d("THRESHOLD","Threshold is " + LocationConfig.LOCATION_CHANGE_DISTANCE_THRESHOLD);
			if ((loc1.distanceTo(loc2) < LocationConfig.LOCATION_CHANGE_DISTANCE_THRESHOLD) && wasSameLastTime == false)
			{
//				Log.d("LOC","current loc " + loc1.getLatitude()+";"+loc1.getLongitude() + " and taret loc is " + loc2.getLatitude() + ";" + loc2.getLongitude());
				wasSameLastTime = true;
				return true;
			}
		}
		else if ((loc1 == null) && (loc2 == null) && wasSameLastTime == false)
		{
			wasSameLastTime = false;
			return false;
		}
		wasSameLastTime = false;
		return false;
	}
	private String stringStatus;

	@Override
	public String getClassification(SensorData sensorData, SensorConfig sensorConfig) {
		isInteresting(sensorData,sensorConfig,"",false);
		return stringStatus;
	}
}

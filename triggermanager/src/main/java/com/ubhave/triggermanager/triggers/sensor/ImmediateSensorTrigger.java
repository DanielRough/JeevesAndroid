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

package com.ubhave.triggermanager.triggers.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.ESSensorManagerInterface;
import com.ubhave.sensormanager.SensorDataListener;
import com.ubhave.sensormanager.classifier.SensorClassifiers;
import com.ubhave.sensormanager.classifier.SensorDataClassifier;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;
import com.ubhave.triggermanager.TriggerException;
import com.ubhave.triggermanager.TriggerReceiver;
import com.ubhave.triggermanager.config.TriggerConfig;
import com.ubhave.triggermanager.config.TriggerManagerConstants;
import com.ubhave.triggermanager.triggers.Trigger;
import com.ubhave.triggermanager.triggers.TriggerUtils;

public class ImmediateSensorTrigger extends Trigger implements SensorDataListener/*, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener */{
	/*
	 * WARNING -- Obsolete
	 */

	protected ESSensorManagerInterface sensorManager;
	protected SensorDataClassifier classifier;
	private int subscriptionId;
//	GoogleApiClient mGoogleApiClient;
//	LocationRequest mLocationRequest;
//	Location currentLocation;
//	String lastUpdateTime;
	int id;
	protected static List<Geofence> geofenceList = new ArrayList<Geofence>();
	public ImmediateSensorTrigger(Context context, int id, TriggerReceiver listener, TriggerConfig params) throws TriggerException, ESException {
		super(context, id, listener, params);
		this.id = id; //Also used to identify the geofence
	}

	@Override
	public String getActionName() {
		return TriggerManagerConstants.ACTION_NAME_SENSOR_TRIGGER_IMMEDIATE;
	}

	@Override
	public void start() throws TriggerException {

		super.start();
		int sensorType = getSensorType();
		try {
//			if (getSensorType() == SensorUtils.SENSOR_TYPE_LOCATION) {
//				createLocationRequest();
//				mGoogleApiClient = new GoogleApiClient.Builder(context)
//						.addConnectionCallbacks(this)
//						.addOnConnectionFailedListener(this)
//						.addApi(LocationServices.API)
//						.build();
//				mGoogleApiClient.connect();
//
//				if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//					return;
//				}
//				if (params.containsKey(TriggerConfig.INTERESTING_VALUE)) {
//					String value = "";
//					value = params.getParameter(TriggerConfig.INTERESTING_VALUE).toString();
//					SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//					value = preferences.getString(value, ""); //If it's a location, we need to find the coordinates it corresponds to in userprefs
//					if(value.isEmpty())return;
//					String[] latlong = value.split(";");
//					geofenceList.add(new Geofence.Builder()
//						// Set the request ID of the geofence. This is a string to identify this
//						// geofence.
//						.setRequestId(Integer.toString(id))
//
//						.setCircularRegion(
//								latlong[-],
//								entry.getValue().longitude,
//								Constants.GEOFENCE_RADIUS_IN_METERS
//						)
//						.setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
//						.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
//								Geofence.GEOFENCE_TRANSITION_EXIT)
//						.build());
//			}
//
//
//			else {
				this.sensorManager = ESSensorManager.getSensorManager(context);
			//	setupParams(getSensorType(), true);
				Log.d("DOIGETHERE", "Do I get here?");
				this.classifier = SensorClassifiers.getSensorClassifier(sensorType);
				Log.d("HOWBOUTHERE", "And how bout here?");
				this.subscriptionId = sensorManager.subscribeToSensorData(sensorType, this);
				Log.d("HEEEEERE", "hmmmm");
		//	}
		} catch (ESException e) {
			throw new TriggerException(TriggerException.UNKNOWN_TRIGGER, "No classifier available for sensor type: " + sensorType);
		}
	}

	protected int getSensorType() throws TriggerException {
		if (params.containsKey(TriggerConfig.SENSOR_TYPE)) {
			return TriggerUtils.getSensorType(params.getParameter(TriggerConfig.SENSOR_TYPE).toString());
		} else {
			throw new TriggerException(TriggerException.MISSING_PARAMETERS, "Sensor Type not specified in parameters.");
		}
	}

	private double notificationProbability() {
		if (params.containsKey(TriggerConfig.NOTIFICATION_PROBABILITY)) {
			return (Double) params.getParameter(TriggerConfig.NOTIFICATION_PROBABILITY);
		} else {
			return TriggerManagerConstants.DEFAULT_NOTIFICATION_PROBABILITY;
		}
	}

	@Override
	public void onDataSensed(SensorData sensorData) {
		if (TriggerManagerConstants.LOG_MESSAGES) {
			Log.d("ImmediateSensorTrigger", "onDataSensed() " + sensorData.toString());
		}
		String value = "";
		if (params.containsKey(TriggerConfig.INTERESTING_VALUE)) {
			value = params.getParameter(TriggerConfig.INTERESTING_VALUE).toString();

			if (sensorData.getSensorType() == SensorUtils.SENSOR_TYPE_LOCATION) {
				//SharedPreferences prefs =
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

				value = preferences.getString(value, ""); //If it's a location, we need to find the coordinates it corresponds to in userprefs
				Log.d("VALUE", "VALUE IS " + value);
			}
		}
		if (classifier.isInteresting(sensorData, sensorData.getSensorConfig(), value)) {
			sendNotification();
		} else {
			Log.d("NAH", "Nah wasn't very interesting at all");
		}
	}

	@Override
	protected void sendNotification() {
		double notificationProbability = notificationProbability();
		double p = (new Random()).nextDouble();
		if (p <= notificationProbability) {
			super.sendNotification();
		}
	}

	@Override
	public void stop() throws TriggerException {
		super.stop();
		try {
		//	setupParams(getSensorType(), false);
			if (sensorManager != null) {
				sensorManager.unsubscribeFromSensorData(subscriptionId);
			}
		} catch (ESException e) {
			throw new TriggerException(TriggerException.INVALID_STATE, "Cannot unsubscribe from sensor subscription.");
		}
	}

	@Override
	public void onCrossingLowBatteryThreshold(boolean isBelowThreshold) {

	}

	@Override
	protected String getTriggerTag() {
		try {
			return "ImmediateSensorTrigger" + SensorUtils.getSensorName(getSensorType());
		} catch (Exception e) {
			e.printStackTrace();
			return "ImmediateSensorTrigger";
		}
	}

	@Override
	protected PendingIntent getPendingIntent() {
		int requestCode = TriggerUtils.TYPE_CLOCK_TRIGGER_ONCE;
		Intent intent = new Intent(TriggerManagerConstants.ACTION_NAME_ONE_TIME_TRIGGER);
		return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	protected void startAlarm() throws TriggerException {
		// TODO Auto-generated method stub

	}
//
//	@Override
//	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//	}
//
//	@Override
//	public void onConnected(@Nullable Bundle bundle) {
//		startLocationUpdates();
//		Log.d("COnnected", "Google API thing is connected");
//		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//			// TODO: Consider calling
//			return;
//		}
//		Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
//				mGoogleApiClient);
//		if (mLastLocation != null) {
//			Log.d("LOCATION", "Last location is " + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
//		} else {
//			Log.d("LOCATION", "SHIT IT WAS NULL");
//		}
//	}
//
//	protected void startLocationUpdates() {
//		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//			return;
//		}
//		LocationServices.FusedLocationApi.requestLocationUpdates(
//				mGoogleApiClient, mLocationRequest, this);
//		Log.d("Reuested","Now reuestling location updates");
//	}
//
//	protected void createLocationRequest() {
//		mLocationRequest = new LocationRequest();
//		mLocationRequest.setInterval(10000);
//		mLocationRequest.setFastestInterval(5000);
//		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//	}
//	@Override
//	public void onConnectionSuspended(int i) {
//
//	}
//
//	@Override
//	public void onLocationChanged(Location location) {
//		currentLocation = location;
//		lastUpdateTime = DateFormat.getTimeInstance().format(new Date());
//		Log.d("UPDATE","location is " + currentLocation.getLatitude()+","+currentLocation.getLongitude());
//		Log.d("TIMEYTIME","Updated at " + lastUpdateTime);
//	}

}

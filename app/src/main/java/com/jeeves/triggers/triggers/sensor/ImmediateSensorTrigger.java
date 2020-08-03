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

package com.jeeves.triggers.triggers.sensor;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jeeves.sensing.sensormanager.ESException;
import com.jeeves.sensing.sensormanager.ESSensorManager;
import com.jeeves.sensing.sensormanager.ESSensorManagerInterface;
import com.jeeves.sensing.sensormanager.SensorDataListener;
import com.jeeves.sensing.sensormanager.classifier.SensorClassifiers;
import com.jeeves.sensing.sensormanager.classifier.SensorDataClassifier;
import com.jeeves.sensing.sensormanager.data.SensorData;
import com.jeeves.sensing.sensormanager.sensors.SensorUtils;
import com.jeeves.triggers.TriggerException;
import com.jeeves.triggers.TriggerReceiver;
import com.jeeves.triggers.config.TriggerConfig;
import com.jeeves.triggers.config.TriggerConstants;
import com.jeeves.triggers.triggers.Trigger;
import com.jeeves.triggers.triggers.TriggerUtils;


import java.util.Random;

/**
 * Class representing the Jeeves 'Sensor Trigger'. Parameters consist of a sensor
 * type and the classification considered 'interesting' to fire the trigger
 */
public class ImmediateSensorTrigger extends Trigger implements SensorDataListener{

	private ESSensorManagerInterface sensorManager;
	private SensorDataClassifier classifier;
	private int subscriptionId;

	public ImmediateSensorTrigger(Context context, int id,
	TriggerReceiver listener, TriggerConfig params){
		super(context, id, listener, params);
	}

	@Override
	protected void startAlarm(){

	}

	@Override
	public String getActionName() {
		return TriggerConstants.ACTION_NAME_SENSOR_TRIGGER_IMMEDIATE;
	}

	@Override
	public void start() throws TriggerException {

		super.start();
		int sensorType = getSensorType();
		try {
			this.sensorManager = ESSensorManager.getSensorManager(context);
			this.classifier = SensorClassifiers.getSensorClassifier(sensorType);
			this.subscriptionId = sensorManager.subscribeToSensorData(sensorType, this);
		} catch (ESException e) {
			throw new TriggerException("No classifier available for sensor type: " + sensorType);
		}
	}

	private int getSensorType() throws TriggerException {
		if (params.containsKey(TriggerConfig.SENSOR_TYPE)) {
			return TriggerUtils.getSensorType(params.getParameter(TriggerConfig.SENSOR_TYPE).toString());
		} else {
			throw new TriggerException("Sensor Type not specified in parameters.");
		}
	}

	/**
	 * Always returns default right now but can be used for firing this trigger
	 * with a given probability
	 * @return double representing trigger probability (currently always 100%)
	 */
	private double notificationProbability() {
		if (params.containsKey(TriggerConfig.NOTIFICATION_PROBABILITY)) {
			return (Double) params.getParameter(TriggerConfig.NOTIFICATION_PROBABILITY);
		} else {
			return TriggerConstants.DEFAULT_NOTIFICATION_PROBABILITY;
		}
	}

	@Override
	public void onDataSensed(SensorData sensorData) {
		Log.d("ImmediateSensorTrigger", sensorData.toString());
		String value = "";
		if (params.containsKey(TriggerConfig.INTERESTING_VALUE)) {
			value = params.getParameter(TriggerConfig.INTERESTING_VALUE).toString();
			if (sensorData.getSensorType() == SensorUtils.SENSOR_TYPE_BLUETOOTH ||
				sensorData.getSensorType() == SensorUtils.SENSOR_TYPE_WIFI) {
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
				value = preferences.getString(value, "");
			}
		}
		if (classifier.isInteresting(sensorData, sensorData.getSensorConfig(), value, true)) {
			sendNotification();
		}
	}

	@Override
	public void onCrossingLowBatteryThreshold(boolean isBelowThreshold) {
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
			if (sensorManager != null) {
				sensorManager.unsubscribeFromSensorData(subscriptionId);
			}
		} catch (ESException e) {
			throw new TriggerException("Cannot unsubscribe from sensor subscription.");
		}
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
		Intent intent = new Intent(TriggerConstants.ACTION_NAME_ONE_TIME_TRIGGER);
		return PendingIntent.getBroadcast(context, requestCode,
			intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}


}

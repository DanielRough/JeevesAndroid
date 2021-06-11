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

package com.jeevesandroid.sensing.sensormanager;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.util.SparseArray;

import com.jeevesandroid.sensing.sensormanager.data.SensorData;
import com.jeevesandroid.sensing.sensormanager.sensors.SensorInterface;
import com.jeevesandroid.sensing.sensormanager.sensors.SensorUtils;
import com.jeevesandroid.sensing.sensormanager.tasks.PullSensorTask;
import com.jeevesandroid.sensing.sensormanager.tasks.Subscription;
import com.jeevesandroid.sensing.sensormanager.tasks.SubscriptionList;

import java.util.ArrayList;
import java.util.List;

public class ESSensorManager implements ESSensorManagerInterface, SensorDataListener
{
	private static final String TAG = "ESSensorManager";

	private static ESSensorManager sensorManager;
	private static Object lock = new Object();

	private final Context applicationContext;
	private PowerManager.WakeLock wakeLock;

	private boolean isSubscribedToBattery;
	private int batterySubscriptionId;

	private final SparseArray<PullSensorTask> sensorTaskMap;
	private final SubscriptionList subscriptionList;

	public static ESSensorManager getSensorManager(final Context context) throws ESException
	{
		if (context == null)
		{
			throw new ESException(ESException.INVALID_PARAMETER, " Invalid parameter, context object passed is null");
		}
		if (sensorManager == null)
		{
			synchronized (lock)
			{
				if (sensorManager == null)
				{
					sensorManager = new ESSensorManager(context);
				}
			}
		}
		return sensorManager;
	}

	private ESSensorManager(final Context appContext)
	{
		applicationContext = appContext;
		sensorTaskMap = new SparseArray<PullSensorTask>();
		subscriptionList = new SubscriptionList();
		isSubscribedToBattery = false;

		ArrayList<SensorInterface> sensors = SensorUtils.getAllSensors(appContext);
		for (SensorInterface aSensor : sensors)
		{

				int sensorType = aSensor.getSensorType();
			PullSensorTask sensorTask;
				sensorTask = new PullSensorTask(aSensor);


				sensorTask.start();
				Log.d("PUTTING", "PUTTING SENSOR " + sensorType);
				sensorTaskMap.put(sensorType, sensorTask);

		}
	}

	public Context getApplicationContext()
	{
		return applicationContext;
	}

	public synchronized int subscribeToSensorData(int sensorId, SensorDataListener listener) throws ESException
	{
		PullSensorTask task = sensorTaskMap.get(sensorId);
		if (task != null)
		{
			if (!isSubscribedToBattery)
			{
					Log.d(TAG, "Registering battery subscription.");
				isSubscribedToBattery = true;
				batterySubscriptionId = subscribeToSensorData(SensorUtils.SENSOR_TYPE_BATTERY, this);
			}

				Log.d(TAG, "subscribeToSensorData() subscribing listener to sensor: " + SensorUtils.getSensorName(sensorId));

			Subscription subscription = new Subscription(task, listener);
			int subscriptionId = subscriptionList.registerSubscription(subscription);
			return subscriptionId;
		}
		else
		{
			Log.d("ooo","eeeeeeeeeeeeeeeeee");

			throw new ESException(ESException.UNKNOWN_SENSOR_TYPE, "Invalid sensor type: " + SensorUtils.getSensorName(sensorId) + " (Check permissions? Sensor missing from device?)");
		}
	}

	public synchronized void unsubscribeFromSensorData(int subscriptionId) throws ESException
	{
		Subscription subscription = subscriptionList.removeSubscription(subscriptionId);
		if (subscription != null)
		{
			subscription.unregister();
			if (subscriptionList.getAllSubscriptions().size() == 1)
			{
					Log.d(TAG, "Removing battery subscription.");
				unsubscribeFromSensorData(batterySubscriptionId);
				isSubscribedToBattery = false;
			}
		}
		else
		{
			throw new ESException(ESException.INVALID_STATE, "Un-Mapped subscription id: " + subscriptionId);
		}
	}

	private PullSensorTask getSensorTask(int sensorId) throws ESException
	{
		PullSensorTask sensorTask = sensorTaskMap.get(sensorId);
		if (sensorTask == null)
		{
			try
			{
				String sensorName = SensorUtils.getSensorName(sensorId);
				throw new ESException(ESException.UNKNOWN_SENSOR_TYPE, sensorName + "sensor unavailable. Have you put the required permissions into your manifest?");
			}
			catch (ESException e)
			{
				e.printStackTrace();
				throw new ESException(ESException.UNKNOWN_SENSOR_TYPE, "Unknown sensor type: " + sensorId);
			}
		}
		return sensorTask;
	}

	public SensorData getDataFromSensor(int sensorId) throws ESException
	{
		SensorData sensorData = null;
		PullSensorTask sensorTask = getSensorTask(sensorId);
		if (sensorTask.isRunning())
		{
			SensorData recentlySensed = sensorTask.recentlySensed;
			return recentlySensed; //This is possibly bad practice but we'll see how it goes
			//throw new ESException(ESException.OPERATION_NOT_SUPPORTED,
			//		"This method is supported only for sensors tasks that are not currently running. Please unregister all your listeners to the sensor to call this method.");
		}
		else
		{
			sensorData = sensorTask.getCurrentSensorData(true);
		}
		return sensorData;
	}

	public void setSensorConfig(int sensorId, String configKey, Object configValue) throws ESException
	{
		PullSensorTask sensorTask = getSensorTask(sensorId);
		SensorInterface sensor = sensorTask.getSensor();
		sensor.setSensorConfig(configKey, configValue);
	}

	public Object getSensorConfigValue(int sensorId, String configKey) throws ESException
	{
		PullSensorTask sensorTask = getSensorTask(sensorId);
		SensorInterface sensor = sensorTask.getSensor();
		return sensor.getSensorConfig(configKey);
	}

// --Commented out by Inspection START (5/8/2019 4:26 PM):
//	private void acquireWakeLock()
//	{
//		if ((wakeLock != null) && (wakeLock.isHeld()))
//		{
//			return;
//		}
//		else
//		{
// --Commented out by Inspection START (5/8/2019 4:26 PM):
////			PowerManager pm = (PowerManager) applicationContext.getSystemService(Context.POWER_SERVICE);
////			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Wakelock_" + System.currentTimeMillis());
////			wakeLock.acquire(10*60*1000L /*10 minutes*/);
////		}
////	}
//// --Commented out by Inspection STOP (5/8/2019 4:26 PM)
// --Commented out by Inspection STOP (5/8/2019 4:26 PM)

	private void releaseWakeLock()
	{
		try
		{
			if (wakeLock != null)
			{
				if (wakeLock.isHeld())
				{
					wakeLock.release();
				}
			}
		}
		catch (Throwable thr)
		{
			// ignore, perhaps wake lock has already been released
		}
	}

	public void onDataSensed(SensorData data)
	{
		// ignore
	}

	public void onCrossingLowBatteryThreshold(boolean isBelowThreshold)
	{
		List<Subscription> subscribers = subscriptionList.getAllSubscriptions();
		for (Subscription sub : subscribers)
		{
			if (!(sub.getListener() instanceof ESSensorManager))
			{
				sub.getListener().onCrossingLowBatteryThreshold(isBelowThreshold);
			}
		}
	}

	public void pauseSubscription(int subscriptionId) {
		Subscription s = subscriptionList.getSubscription(subscriptionId);
		s.pause();
	}

	public void unPauseSubscription(int subscriptionId) {
		Subscription s = subscriptionList.getSubscription(subscriptionId);
		s.unpause();
	}

}
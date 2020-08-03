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

package com.jeeves.sensing.sensormanager.tasks;

import android.util.Log;

import com.jeeves.sensing.sensormanager.ESException;
import com.jeeves.sensing.sensormanager.SensorDataListener;
import com.jeeves.sensing.sensormanager.config.SensorConfig;
import com.jeeves.sensing.sensormanager.data.SensorData;
import com.jeeves.sensing.sensormanager.sensors.SensorInterface;
import com.jeeves.sensing.sensormanager.sensors.SensorUtils;

import java.util.ArrayList;

public class PullSensorTask extends Thread
{
	private class StopTask extends Thread
	{
		@Override
		public void run()
		{
			Log.d("StopTask", "Stopping sensor task...");
			stopTask();
		}
	}


	protected SensorInterface sensor;
	protected Object syncObject = new Object();

	protected int state;
	protected long pauseTime;

	protected ArrayList<SensorDataListener> listenerList;

	public static final int RUNNING = 6123;
	public static final int PAUSED = 6124;
	public static final int STOPPED = 6125;


	public SensorInterface getSensor()
	{
		return sensor;
	}

	@Override
	public void start()
	{
		state = STOPPED;
		super.start();
	}

	protected String getLogTag()
	{
		try
		{
			String sensorName = SensorUtils.getSensorName(sensor.getSensorType());
			return "SensorTask:" + sensorName;
		}
		catch (ESException exp)
		{
			exp.printStackTrace();
			return null;
		}
	}

	public int getSensorType()
	{
		return sensor.getSensorType();
	}

	public boolean registerSensorDataListener(SensorDataListener listener)
	{
		Log.d(getLogTag(), "registerSensorDataListener() listener: " + listener);
		synchronized (listenerList)
		{
			for (int i = 0; i < listenerList.size(); i++)
			{
				if (listenerList.get(i) == listener)
				{
					return false;
				}
			}
			listenerList.add(listener);
			startTask();
			return true;
		}
	}

	protected void publishData(SensorData sensorData)
	{
		synchronized (listenerList)
		{
			for (SensorDataListener listener : listenerList)
			{
				if (sensorData != null)
				{
					listener.onDataSensed(sensorData);
				}
				Log.d(getLogTag(), "sensorData is null");
			}
		}
	}



	public void unregisterSensorDataListener(SensorDataListener listener)
	{
		synchronized (listenerList)
		{
			listenerList.remove(listener);
			if (listenerList.isEmpty())
			{
				new StopTask().start();
			}
		}
	}

	public void startTask()
	{
		if (state == STOPPED)
		{
			synchronized (syncObject)
			{
				syncObject.notify();
			}
		}
		else
		{
			// ignore
		}
	}

	private void stopTask()
	{
		if (state == STOPPED)
		{
			// ignore
		}
		else
		{
			synchronized (syncObject)
			{
				synchronized (listenerList)
				{
					if (listenerList.isEmpty())
					{
						state = STOPPED;
						this.interrupt();
					}
				}
			}
		}
	}

	public boolean isRunning()
	{
		return state == RUNNING;
	}
	public SensorData recentlySensed;
	public PullSensorTask(SensorInterface sensor)
	{
		this.sensor = sensor;
		listenerList = new ArrayList<SensorDataListener>();
	}

	public SensorData getCurrentSensorData(boolean oneOffSensing) throws ESException
	{
		SensorData sensorData = (sensor).sense();
		// since this is a one-off query for sensor data, sleep interval
		// is not relevant in this case
		if (sensorData != null)
		{
			// remove sleep length value for the case of one-off sensing
			if (oneOffSensing)
			{
				SensorConfig sensorConfig = sensorData.getSensorConfig();
				sensorConfig.removeParameter(SensorConfig.POST_SENSE_SLEEP_LENGTH_MILLIS);
			}
		}
		return sensorData;
	}

	public void run()
	{
		synchronized (syncObject)
		{
			while (true)
			{
				try
				{
					try
					{

						if ((state == PAUSED) || (state == STOPPED))
						{
							if (state == PAUSED)
							{
								syncObject.wait(pauseTime);
							}
							else if (state == STOPPED)
							{
								syncObject.wait();
							}
							state = RUNNING;
							continue;
						}

						// SENSE
						// sense() is a blocking call and returns when
						// the sensing is complete, the sensorConfig object
						// will have the sampling window, cycle information
							Log.d(getLogTag(), "Pulling from: " + SensorUtils.getSensorName(sensor.getSensorType()));
						SensorData sensorData = getCurrentSensorData(false);
						recentlySensed = sensorData;
						// publish sensed data
						publishData(sensorData);

						// SLEEP
						long samplingInterval = (Long) sensor.getSensorConfig(SensorConfig.POST_SENSE_SLEEP_LENGTH_MILLIS);
						syncObject.wait(samplingInterval);
					}
					catch (InterruptedException exp)
					{
						// ignore
					}
				}
				catch (ESException e)
				{
					e.printStackTrace();
					try
					{
						Thread.sleep(30000);
					}
					catch (Exception exp)
					{
						exp.printStackTrace();
					}
				}
			}
		}
	}
}

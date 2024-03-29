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

package com.jeevesandroid.sensing.sensormanager.tasks;

import com.jeevesandroid.sensing.sensormanager.SensorDataListener;

public class Subscription
{
	private final PullSensorTask task;
	private final SensorDataListener listener;
	private boolean isPaused = false;
	
	public Subscription(PullSensorTask task, SensorDataListener listener)
	{
		this.task = task;
		this.listener = listener;
		this.isPaused = false;
	}

	public PullSensorTask getTask()
	{
		return task;
	}

	public SensorDataListener getListener()
	{
		return listener;
	}

	public boolean register()
	{
		return task.registerSensorDataListener(listener);
	}

	public void unregister()
	{
		task.unregisterSensorDataListener(listener);
	}
	
	public void pause()
	{
		isPaused = true;
		task.unregisterSensorDataListener(listener);
	}
	
	public void unpause()
	{
		isPaused = false;
		task.registerSensorDataListener(listener);
	}
	
// --Commented out by Inspection START (5/8/2019 4:26 PM):
	public boolean isPaused()
	{
		return isPaused;
	}
// --Commented out by Inspection STOP (5/8/2019 4:26 PM)

	public boolean equals(Subscription s)
	{
		return (this.task == s.getTask() && this.listener == s.getListener());
	}
}

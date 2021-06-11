/* **************************************************
 Copyright (c) 2014, University of Cambridge
 Neal Lathia, neal.lathia@cl.cam.ac.uk

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

package com.jeevesandroid.sensing.sensormanager.sensors;


public enum SensorEnum
{
	BLUETOOTH (SensorUtils.SENSOR_NAME_BLUETOOTH, SensorUtils.SENSOR_TYPE_BLUETOOTH),
	LOCATION (SensorUtils.SENSOR_NAME_LOCATION, SensorUtils.SENSOR_TYPE_LOCATION),
	MICROPHONE (SensorUtils.SENSOR_NAME_MICROPHONE, SensorUtils.SENSOR_TYPE_MICROPHONE),
	WIFI (SensorUtils.SENSOR_NAME_WIFI, SensorUtils.SENSOR_TYPE_WIFI);
	private final String name;
	private final int type;

	SensorEnum(String name, final int type)
	{
		this.name = name;
		this.type = type;
	}

	public String getName()
	{
		return name;
	}
	
	public int getType()
	{
		return type;
	}
}

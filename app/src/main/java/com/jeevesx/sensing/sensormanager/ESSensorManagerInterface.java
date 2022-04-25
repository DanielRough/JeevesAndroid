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

package com.jeevesx.sensing.sensormanager;

import com.jeevesx.sensing.sensormanager.data.SensorData;

public interface ESSensorManagerInterface
{
	/*
	 * Getting data from sensors
	 */
    int subscribeToSensorData(int sensorId, SensorDataListener listener) throws ESException;

	void unsubscribeFromSensorData(int subscriptionId) throws ESException;

// --Commented out by Inspection START (5/8/2019 4:26 PM):
// --Commented out by Inspection START (5/8/2019 4:26 PM):
    SensorData getDataFromSensor(int sensorId) throws ESException;
////
////	/*
////	 * Pause / Unpause
////	 */
////
 	void pauseSubscription(int subscriptionId) throws ESException;
// --Commented out by Inspection STOP (5/8/2019 4:26 PM)
// --Commented out by Inspection STOP (5/8/2019 4:26 PM)

	void unPauseSubscription(int subscriptionId) throws ESException;
	
	/*
	 * Getting/setting configuration parameters
	 */
    void setSensorConfig(int sensorId, String configKey, Object configValue) throws ESException;

	Object getSensorConfigValue(int sensorId, String configKey) throws ESException;
// --Commented out by Inspection STOP (5/8/2019 4:26 PM)
}

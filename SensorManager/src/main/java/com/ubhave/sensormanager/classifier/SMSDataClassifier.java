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

import android.provider.Telephony;
import android.util.Log;

import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.push.SmsData;

public class SMSDataClassifier implements SensorDataClassifier
{
	@Override
	public boolean isInteresting(final SensorData sensorData, final SensorConfig sensorConfig, String value)
	{
		SmsData sms = (SmsData) sensorData;
		Log.d("WOO MESSAGE TYPE", sms.getMessageType());
		if (sms.wasReceived() && value.equals("SMS received"))
		{
			Log.d("WOO HA","SMS RECEIVED");
			return true;
		}
		else if(sms.getMessageType().equals(Integer.toString(Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT)) && value.equals("SMS sent")) {
			Log.d("WOO HA","SMS SENT");
			Log.d("WOO ", sms.getContent());
			return true;

		}
		else
			return false;
	}

}

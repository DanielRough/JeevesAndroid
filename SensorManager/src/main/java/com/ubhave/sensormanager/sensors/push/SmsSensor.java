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

package com.ubhave.sensormanager.sensors.push;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.config.pull.ContentReaderConfig;
import com.ubhave.sensormanager.data.push.SmsData;
import com.ubhave.sensormanager.process.push.SMSProcessor;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class SmsSensor extends AbstractPushSensor
{
	private static final String TAG = "SmsSensor";
	private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS};

	private static SmsSensor smsSensor;
	private static Object lock = new Object();
	
	private ContentObserver observer;
	private String prevMessageId;

	public static SmsSensor getSensor(final Context context) throws ESException
	{
		if (smsSensor == null)
		{
			synchronized (lock)
			{
				if (smsSensor == null)
				{
					if (allPermissionsGranted(context, REQUIRED_PERMISSIONS))
					{
						smsSensor = new SmsSensor(context);
					}
					else
					{
						throw new ESException(ESException.PERMISSION_DENIED, SensorUtils.SENSOR_NAME_SMS);
					}
				}
			}
		}
		return smsSensor;
	}

	//From http://stackoverflow.com/questions/5808577/listen-to-outgoing-sms-or-sent-box
	private static final int MESSAGE_TYPE_SENT = 2;
	private SmsSensor(final Context context)
	{
		super(context);
//		ContentResolver contentResolver = context.getContentResolver();
//		contentResolver.registerContentObserver(Uri.parse("content:// sms"),true, observer);
		// Create a content observer for sms
		//I have no idea if this will work
		observer = new ContentObserver(new Handler())
		{

			public void onChange(boolean selfChange)
			{
				if (isSensing)
				{
					try
					{
						// check last sent message
						Uri smsUri = Uri.parse("content://sms/");
						ContentResolver resolver = applicationContext.getContentResolver();
						if (resolver != null)
						{
							Log.d("WOO HA URI",Telephony.Sms.Sent.CONTENT_URI.toString());
							Cursor cursor = resolver.query(smsUri, null, null, null, null);
							if (cursor != null)
							{
								// last sms sent is the fist in the list
								cursor.moveToFirst();
								if (!cursor.isAfterLast())
								{
									int type = cursor.getInt(cursor.getColumnIndex("type"));
									//	Log.d("WOO HA","how often is this happening?");
									String content = cursor.getString(cursor.getColumnIndex("body"));
							//		Log.d("WOO HA",content);
									String sentTo = cursor.getString(cursor.getColumnIndex("address"));
									SharedPreferences prefs = context.getSharedPreferences("userprefs",Context.MODE_PRIVATE);
									SharedPreferences.Editor editor = prefs.edit();
									editor.putString("lastSender",sentTo);
									editor.commit();
									Log.d("LAST SENDER","Last sender is now " + sentTo);

									String messageId = cursor.getString(cursor.getColumnIndex("_id"));
									// messageType - sent / received / draft etc.
									String messageType = cursor.getString(cursor.getColumnIndex(ContentReaderConfig.SMS_CONTENT_TYPE_KEY));

	//DJR: From what I can see here, it is ignored when a message is SENT, thus the messages received don't change. Removing the 'ignore' bit means it's logged whether sent or received

									if ((prevMessageId != null) && (prevMessageId.length() > 0)
											&& (prevMessageId.equals(messageId)))
									{
									//	Log.d("WOO HA", "Does this ever happen I wonder?");
									}
									else
									{

										prevMessageId = messageId;
										logDataSensed(System.currentTimeMillis(), content, sentTo, messageType,
												SmsData.SMS_CONTENT_CHANGED);
									}
								}
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		};

	}

	private void logDataSensed(long timestamp, String content, String addr, String messageType, String eventType)
	{
		SMSProcessor processor = (SMSProcessor) getProcessor();
		if (processor != null)
		{
			SmsData data = (SmsData) processor.process(timestamp, sensorConfig.clone(), content, addr, messageType, eventType);
			onDataSensed(data);
		}
	}

	public String getLogTag()
	{
		return TAG;
	}

	public int getSensorType()
	{
		return SensorUtils.SENSOR_TYPE_SMS;
	}

	protected void onBroadcastReceived(Context context, Intent intent)
	{
		if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
		{
			Bundle bundle = intent.getExtras();
			SmsMessage[] smsMessagesArray = null;
			if (bundle != null)
			{
				// read the sms received
				try
				{
					Object[] pdusArray = (Object[]) bundle.get("pdus");
					smsMessagesArray = new SmsMessage[pdusArray.length];
					for (int i = 0; i < smsMessagesArray.length; i++)
					{
						smsMessagesArray[i] = SmsMessage.createFromPdu((byte[]) pdusArray[i]);
						String address = smsMessagesArray[i].getOriginatingAddress();
						String content = smsMessagesArray[i].getMessageBody();

						// mesgType is null here as the last field (SmsData.SMS_RECEIVED) indicates
						// that this is for a received SMS. 
						logDataSensed(System.currentTimeMillis(), content, address, "", SmsData.SMS_RECEIVED);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		else{

		}
	}

	protected IntentFilter[] getIntentFilters()
	{
		IntentFilter[] filters = new IntentFilter[1];
		filters[0] = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
		return filters;
	}

	@Override
	protected boolean startSensing()
	{
		prevMessageId = "";

		// register content observer
		ContentResolver contentResolver = applicationContext.getContentResolver();
		contentResolver.registerContentObserver(Uri.parse("content://sms"), true, observer);
		return true;
	}

	@Override
	protected void stopSensing()
	{
		applicationContext.getContentResolver().unregisterContentObserver(observer);
	}
}

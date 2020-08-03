package com.jeeves.sensing.sensormanager.process;

import android.content.Context;

import com.jeeves.sensing.sensormanager.config.SensorConfig;
import com.jeeves.sensing.sensormanager.data.MicrophoneData;

public class MicrophoneProcessor extends AbstractProcessor
{
	public MicrophoneProcessor(final Context c, boolean rw, boolean sp)
	{
		super(c, rw, sp);
	}

	public MicrophoneData process(long pullSenseStartTimestamp, int[] maxAmpArray, long[] timestampArray, String mediaFilePath, SensorConfig sensorConfig)
	{
		MicrophoneData micData = new MicrophoneData(pullSenseStartTimestamp, sensorConfig);
		if (setRawData)
		{
			micData.setMaxAmplitudeArray(maxAmpArray);
			micData.setTimestampArray(timestampArray);
			if (mediaFilePath != null)
			{
				micData.setMediaFilePath(mediaFilePath);
			}
		}
		return micData;

	}

}

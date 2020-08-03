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

package com.jeeves.triggers.config;

public class TriggerConstants
{
	public final static boolean LOG_MESSAGES = true;

	public static final int DEFAULT_DO_NOT_DISTURB_BEFORE_MINUTES = 8 * 60;
	public static final int DEFAULT_DO_NOT_DISTURB_AFTER_MINUTES = 22 * 60;
	public static final int DEFAULT_MIN_TRIGGER_INTERVAL_MINUTES = 120;
	public static final int DEFAULT_NOTIFICATION_PROBABILITY = 1;

	private final static String ACTION_NAME_ROOT = "com.jeevesandroid.triggermanager.triggers";
	public final static String ACTION_NAME_ONE_TIME_TRIGGER = ACTION_NAME_ROOT+".clockbased.ONE_TIME_TRIGGER";
	public final static String ACTION_NAME_INTERVAL_TRIGGER = ACTION_NAME_ROOT+".clockbased.INTERVAL_TRIGGER";
	public final static String ACTION_NAME_RANDOM_DAY_TRIGGER = ACTION_NAME_ROOT+".clockbased.RANDOM_TRIGGER";
	public final static String ACTION_NAME_DAY_INTERVAL_TRIGGER = ACTION_NAME_ROOT+".clockbased.DAY_INTERVAL_TRIGGER";

	public final static String ACTION_NAME_BUTTON_TRIGGER = ACTION_NAME_ROOT + ".sensorbased.BUTTON_TRIGGER";
	public final static String ACTION_NAME_SURVEY_TRIGGER = ACTION_NAME_ROOT + ".sensorbased.SURVEY_TRIGGER";

	public final static String ACTION_NAME_SENSOR_TRIGGER_IMMEDIATE = ACTION_NAME_ROOT+".sensorbased.SENSOR_IMMEDIATE";
	public final static String ACTION_NAME_SENSOR_TRIGGER_DELAYED = ACTION_NAME_ROOT+".sensorbased.SENSOR_DELAYED";

}

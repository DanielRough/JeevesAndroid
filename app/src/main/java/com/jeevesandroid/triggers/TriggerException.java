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

package com.jeevesandroid.triggers;

public class TriggerException extends Exception
{
	private static final long serialVersionUID = -6952859423645368705L;

	public static final int NO_CONTEXT = 8000;
	public static final int INVALID_STATE = 8001;
	public static final int DATE_IN_PAST = 8003;
	public static final int UNABLE_TO_ALLOCATE = 8004;
	public static final int MISSING_PARAMETERS = 8005;
	public static final int UNKNOWN_TRIGGER = 8006;

	private int errorCode;
	private String message;

	public TriggerException(int errorCode, String message)
	{
		super(message);
		this.errorCode = errorCode;
		this.message = message;
	}

// --Commented out by Inspection START (5/8/2019 4:26 PM):
	public int getErrorCode()
	{
		return errorCode;
	}
// --Commented out by Inspection STOP (5/8/2019 4:26 PM)

	public String getMessage()
	{
		return message;
	}

}

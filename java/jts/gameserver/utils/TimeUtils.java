package jts.gameserver.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtils
{
	public static final long MINUTE_IN_MILLIS = 60000L;
	private static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("HH:mm dd.MM.yyyy");

	public static String toSimpleFormat(Calendar cal)
	{
		return SIMPLE_FORMAT.format(cal.getTime());
	}

	public static String toSimpleFormat(long cal)
	{
		return SIMPLE_FORMAT.format(cal);
	}
}
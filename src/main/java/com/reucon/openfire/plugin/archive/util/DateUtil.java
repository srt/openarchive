package com.reucon.openfire.plugin.archive.util;

import org.jivesoftware.util.JiveConstants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.TimeZone;
import java.util.Date;

/**
 * Utility class to parse and format dates in UTC that adhere to the DateTime format specified
 * in Jabber Date and Time Profiles.
 */
public class DateUtil
{
    private static final DateFormat dateFormat;

    static
    {
        dateFormat =  new SimpleDateFormat(JiveConstants.XMPP_DATETIME_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private DateUtil()
    {

    }

    public static Date parseDate(String dateString)
    {
        if (dateString == null)
        {
            return null;
        }

        synchronized(dateFormat)
        {
            try
            {
                return dateFormat.parse(dateString);
            }
            catch (ParseException e)
            {
                return null;
            }
        }
    }

    public static String formatDate(Date date)
    {
        if (date == null)
        {
            return null;
        }

        synchronized(dateFormat)
        {
            return dateFormat.format(date);
        }
    }
}

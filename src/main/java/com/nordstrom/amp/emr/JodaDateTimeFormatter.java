package com.nordstrom.amp.emr;

import java.util.Locale;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.tz.FixedDateTimeZone;

/**
 * Collection of premade date time formats
 */
public class JodaDateTimeFormatter {
    /**
     * This is the format to use when serializing to/from our storage systems.
     */
    public static final DateTimeFormatter ISO8651 = ISODateTimeFormat.dateTime()
            .withZone(new FixedDateTimeZone("GMT", "GMT", 0, 0));

    /**
     * This is the format to use with http headers
     */
    public static final DateTimeFormatter RFC1123 = DateTimeFormat
            .forPattern("EEE, dd MMM yyyy HH:mm:ss zzz")
            .withZoneUTC().withLocale(Locale.US);
}

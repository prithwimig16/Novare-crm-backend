package com.medwiz.novare_crm.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateTimeUtil {
    // Always English month abbreviation
    private static final DateTimeFormatter NICE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

    public static String formatNiceDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(NICE_DATE_FORMATTER);
    }
}

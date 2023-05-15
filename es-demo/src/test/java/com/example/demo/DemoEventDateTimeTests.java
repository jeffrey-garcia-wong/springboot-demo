package com.example.demo;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

public class DemoEventDateTimeTests {
    @Test
    public void convertUtcDateString_withInstantFormatAndZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        final String inputDateStringUtc = "2022-12-22T17:13:12.1234Z";
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

        try {
            ZonedDateTime extractedDateTime = ZonedDateTime.parse(inputDateStringUtc, dateTimeFormatter);
            assertEquals(extractedDateTime.getZone().getId(), ZoneOffset.UTC.getId());
            String outputDateStringInUtc = extractedDateTime
                    .toLocalDateTime()
                    .toString()
                    .replaceAll("0+$","");
            assertEquals(inputDateStringUtc, outputDateStringInUtc + 'Z');
        } catch (DateTimeParseException e) {
            fail(e.getMessage());
        }
    }
}

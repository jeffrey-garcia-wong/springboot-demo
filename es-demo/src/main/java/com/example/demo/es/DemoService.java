package com.example.demo.es;

import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class DemoService {
    private final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_INSTANT)
            .parseLenient()
            .toFormatter()
            .withZone(ZoneOffset.UTC);

    public String simpleDateFormat(String inputDateTimeStr) throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        simpleDateFormat.setLenient(false);
        try {
            Date convertedDate = simpleDateFormat.parse(inputDateTimeStr);
            return simpleDateFormat.format(convertedDate);
        } catch (Exception e) {
            throw e;
        }
    }

    public String dateTimeFormat(String inputDateTimeStr) throws Exception {
        try {
            ZonedDateTime convertedDate = ZonedDateTime.parse(inputDateTimeStr, dateTimeFormatter);
            return convertedDate.truncatedTo(ChronoUnit.SECONDS).toLocalDateTime().toString();
        } catch (Exception e) {
            throw e;
        }
    }


}

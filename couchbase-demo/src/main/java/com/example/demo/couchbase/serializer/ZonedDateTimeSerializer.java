package com.example.demo.couchbase.serializer;

import com.fasterxml.jackson.databind.JsonSerializer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeSerializer extends JsonSerializer<ZonedDateTime> {
    @Override
    public void serialize(
            ZonedDateTime zonedDateTime,
            com.fasterxml.jackson.core.JsonGenerator jsonGenerator,
            com.fasterxml.jackson.databind.SerializerProvider serializerProvider
    ) throws IOException {
        jsonGenerator.writeString(DateTimeFormatter.ISO_INSTANT.format(zonedDateTime));
    }
}

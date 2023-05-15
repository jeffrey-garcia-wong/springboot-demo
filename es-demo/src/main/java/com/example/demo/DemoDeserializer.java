package com.example.demo;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class DemoDeserializer extends StdDeserializer<DemoEvent> {
    public DemoDeserializer() {
        this(null);
    }

    protected DemoDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public DemoEvent deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        final JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);

        String id = rootNode.get("id").asText();
        String detail = rootNode.get("detail").asText();

        DemoEvent event = new DemoEvent();
        event.setId(id);
        event.setDetail(detail);

        return event;
    }
}

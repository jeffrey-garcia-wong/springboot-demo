package com.example.demo.couchbase.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Configuration(proxyBeanMethods = false)
public class DemoAppConfig {

//    @Bean
//    public Jackson2ObjectMapperBuilderCustomizer jacksonConfigCustomizer() {
//        return builder -> {
//            builder.modules(new JavaTimeModule());
//            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        };
//    }

//    @Bean
//    public ObjectMapper objectMapper() {
//        final JavaTimeModule javaTimeModule = new JavaTimeModule();
//        javaTimeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(DateTimeFormatter.ISO_INSTANT));
//        javaTimeModule.addDeserializer(ZonedDateTime.class, InstantDeserializer.ZONED_DATE_TIME);
//
//        final ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(javaTimeModule);
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        return objectMapper;
//    }

}

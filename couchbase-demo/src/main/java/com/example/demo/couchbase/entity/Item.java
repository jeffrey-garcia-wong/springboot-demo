package com.example.demo.couchbase.entity;

import com.example.demo.couchbase.serializer.ZonedDateTimeDeserializer;
import com.example.demo.couchbase.serializer.ZonedDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Item {

    @Id
    @Field("item_id")
    private String itemId;

    @Field("source_system")
    private String sourceSystem;

    @Field("source_id")
    private String sourceId;

    @Field("start_date_time")
    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime zonedDateTime;
}

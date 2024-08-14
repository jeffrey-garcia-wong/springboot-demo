package com.example.demo.couchbase.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;

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

}

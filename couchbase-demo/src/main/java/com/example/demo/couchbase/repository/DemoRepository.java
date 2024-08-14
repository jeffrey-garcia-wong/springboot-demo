package com.example.demo.couchbase.repository;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.query.QueryResult;
import com.example.demo.couchbase.config.CouchbaseConfig;
import com.example.demo.couchbase.entity.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Repository
public class DemoRepository {

    private final Cluster cluster;
    private final Collection itemCollection;
    private final CouchbaseConfig couchbaseConfig;

    public DemoRepository(
            @Autowired
            Cluster cluster,
            @Autowired
            Bucket bucket,
            @Autowired
            CouchbaseConfig couchbaseConfig
    ) {
        this.cluster = cluster;
        this.itemCollection = bucket.scope("_default").collection("item");
        this.couchbaseConfig = couchbaseConfig;
    }

    public Pair<MutationResult, Item> upsert(Item item) {
        final MutationResult result = itemCollection.upsert(item.getItemId(), item);
        final Pair<MutationResult, Item> mutationResultItemPair = Pair.of(result, item);
        return mutationResultItemPair;
    }

    public Item findByItemId(String itemId) {
        return itemCollection.get(itemId).contentAs(Item.class);
    }

    public List<Item> findAllItems() {
        final String query = "select item.* from `demo`.`_default`.`item` item";
        final QueryResult result = cluster.query(query);

        final List<Item> items = new LinkedList<>();
        for (Item item : result.rowsAs(Item.class)) {
            log.info("Found row: {}", item);
            items.add(item);
        }
        return items;
    }

    public void deleteAllItems() {
        final String query = "delete from `demo`.`_default`.`item`";
        final QueryResult result = cluster.query(query);
        log.info("Deleted item count: {}", result.rowsAsObject().size());
    }

}

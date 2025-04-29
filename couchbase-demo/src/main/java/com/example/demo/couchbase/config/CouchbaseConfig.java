package com.example.demo.couchbase.config;

import com.couchbase.client.core.error.*;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.codec.JacksonJsonSerializer;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.manager.collection.CollectionSpec;
import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;

import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {

    @Value("#{systemEnvironment['DB_CONN_STR'] ?: '${spring.couchbase.bootstrap-hosts:localhost}'}")
    private String host;

    @Value("#{systemEnvironment['DB_USERNAME'] ?: '${spring.couchbase.bucket.user:admin}'}")
    private String username;

    @Value("#{systemEnvironment['DB_PASSWORD'] ?: '${spring.couchbase.bucket.password:password}'}")
    private String password;

    @Value("${spring.couchbase.bucket.name:demo}")
    private String bucketName;

    @Override
    public String getConnectionString() {
        return host;
    }

    @Override
    public String getUserName() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getBucketName() {
        return bucketName;
    }

    /**
     * NOTE: If connecting to Couchbase Capella, you must enable TLS.
     * <p>
     * The simplest way to enable TLS is to edit {@code application.properties}
     * and make sure the {@code spring.couchbase.bootstrap-hosts} config property
     * starts with "couchbases://" (note the final 's'), like this:
     *
     * <pre>
     * spring.couchbase.bootstrap-hosts=couchbases://my-cluster.cloud.couchbase.com
     * </pre>
     *
     * Alternatively, you can enable TLS by writing code to configure the cluster
     * environment;
     * see the commented-out code in this method for an example.
     */
    // FIXME: fail-fast implementation upon startup
    @Override
    @Bean(destroyMethod = "disconnect")
    public Cluster couchbaseCluster(ClusterEnvironment couchbaseClusterEnvironment) {
        try {
            log.debug("Connecting to Couchbase cluster at " + host);
            Cluster cluster = Cluster.connect(host, username, password);
            cluster.waitUntilReady(Duration.ofSeconds(15));
            return cluster;
        } catch (UnambiguousTimeoutException e) {
            log.error("Connection to Couchbase cluster at " + host + " timed out", e);
            throw e;
        } catch (Exception e) {
            log.error(e.getClass().getName());
            log.error("Could not connect to Couchbase cluster at " + host, e);
            throw e;
        }
    }

    // FIXME: fail-fast implementation upon startup
    @Bean
    Bucket couchbaseBucket(Cluster cluster) {
        try {
            if (!cluster.buckets().getAllBuckets().containsKey(bucketName)) {
                throw new BucketNotFoundException("Bucket " + bucketName + " does not exist");
            }
            Bucket bucket = cluster.bucket(bucketName);
            bucket.waitUntilReady(Duration.ofSeconds(15));
            createScopeAndCollection(bucket);
            return bucket;
        } catch (UnambiguousTimeoutException e) {
            log.error("Connection to bucket " + bucketName + " timed out", e);
            throw e;
        } catch (BucketNotFoundException e) {
            log.error("Bucket " + bucketName + " does not exist", e);
            throw e;
        } catch (Exception e) {
            log.error(e.getClass().getName());
            log.error("Could not connect to bucket " + bucketName, e);
            throw e;
        }
    }

    private void createScopeAndCollection(Bucket bucket) {
        final String scope = "rights-handler";
        try {
            bucket.collections().createScope(scope);
        } catch (ScopeExistsException e) {
            log.info("Scope already exists, skipping");
        }

        bucket.collections()
                .getAllScopes()
                .stream()
                .filter(scopeSpec -> scopeSpec.name().equals(scope))
                .findFirst().map(scopeSpec -> {
                    for (String collectionName : List.of("item")) {
                        if (scopeSpec.collections().stream().noneMatch(_collection -> _collection.name().equals(collectionName))) {
                            // collection creation is asynchronous
                            log.info("Creating collection {}", collectionName);
                            bucket.collections().createCollection(CollectionSpec.create(collectionName, scope));
                            log.info("Collection {} created", collectionName);
                        } else {
                            log.info("Collection {} already exists, skipping", collectionName);
                        }
                        createPrimaryIndex(scope, collectionName, bucket);
                    }
                    return scopeSpec;
                }).orElseGet(() -> {
                    log.error("Scope {} does not exist", scope);
                    throw ScopeNotFoundException.forScope(scope);
                });
    }

    private void createPrimaryIndex(String scope, String collectionName, Bucket bucket) {
        // reliably suspend index creation until collection is ready
        final int maxAttempts = 5;
        final long waitInterval = 1000L;
        for (int i=1; i<=maxAttempts; i++) {
            try {
                // primary index
                log.info("Creating primary index on collection {}", collectionName);
                bucket.scope(scope).collection(collectionName).queryIndexes().createPrimaryIndex(CreatePrimaryQueryIndexOptions.createPrimaryQueryIndexOptions().ignoreIfExists(true));
                log.info("Primary index created on collection {}", collectionName);
                break;
            } catch (IndexFailureException e) {
                if (i>=maxAttempts)
                    throw e;
                else {
                    try {
                        Thread.sleep(waitInterval);
                    } catch (InterruptedException ex) {}
                }
            }
        }
    }

//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Override
//    public ObjectMapper couchbaseObjectMapper(){
//        return objectMapper;
//    }
//
//    @Override
//    protected void configureEnvironment(final ClusterEnvironment.Builder builder) {
//        builder.jsonSerializer(JacksonJsonSerializer.create(objectMapper));
//    }
}

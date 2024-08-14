package com.example.demo.couchbase.config;

import com.couchbase.client.core.error.BucketNotFoundException;
import com.couchbase.client.core.error.UnambiguousTimeoutException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class CouchbaseConfig {

    @Value("#{systemEnvironment['DB_CONN_STR'] ?: '${spring.couchbase.bootstrap-hosts:localhost}'}")
    private String host;

    @Value("#{systemEnvironment['DB_USERNAME'] ?: '${spring.couchbase.bucket.user:admin}'}")
    private String username;

    @Value("#{systemEnvironment['DB_PASSWORD'] ?: '${spring.couchbase.bucket.password:password}'}")
    private String password;

    @Value("${spring.couchbase.bucket.name:demo}")
    private String bucketName;

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
    @Bean(destroyMethod = "disconnect")
    Cluster getCouchbaseCluster() {
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
    Bucket getCouchbaseBucket(Cluster cluster) {
        try {
            if (!cluster.buckets().getAllBuckets().containsKey(bucketName)) {
                throw new BucketNotFoundException("Bucket " + bucketName + " does not exist");
            }
            Bucket bucket = cluster.bucket(bucketName);
            bucket.waitUntilReady(Duration.ofSeconds(15));
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
}

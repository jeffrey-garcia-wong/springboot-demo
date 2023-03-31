package com.example.demo;

import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ReactiveElasticsearchClientAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Objects;

@Configuration(proxyBeanMethods = false)
public class DemoConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoConfig.class);

    @Configuration(proxyBeanMethods = false)
    @EnableElasticsearchRepositories
    @ConditionalOnProperty(prefix = "spring.elasticsearch", name = "enabled", matchIfMissing = true, havingValue="true")
    public static class ElasticSearchConfigEnabler extends ElasticsearchConfiguration {
        public ElasticSearchConfigEnabler() {
            LOGGER.debug("{}", ElasticSearchConfigEnabler.class.getName());
        }

        @Value("${elasticsearch.url}")
        private String url;
        @Value("${elasticsearch.username}")
        private String username;
        @Value("${elasticsearch.password}")
        private String password;

        @Value("${elasticsearch.certFilename}")
        private String certFilename;

        @Override
        public ClientConfiguration clientConfiguration() {
            SSLContext sslContext = createSslContext();
            Objects.requireNonNull(sslContext);

            ClientConfiguration clientConfiguration = ClientConfiguration
                    .builder()
                    .connectedTo(url)
                    .usingSsl(sslContext)
                    .withBasicAuth(username, password)
                    .withClientConfigurer(ElasticsearchClients.ElasticsearchRestClientConfigurationCallback.from(restClientBuilder -> {
                        // configure the Elasticsearch RestClient
                        return restClientBuilder;
                    }))
                    .build();

            return clientConfiguration;
        }

        private SSLContext createSslContext() {
            try {
                KeyStore keyStore = loadKeyStore();
                SSLContextBuilder sslContextBuilder = SSLContexts.custom().loadTrustMaterial(keyStore, null);
                return sslContextBuilder.build();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            return null;
        }

        private KeyStore loadKeyStore() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(certFilename)) {
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                Certificate certificate = factory.generateCertificate(inputStream);
                KeyStore keyStore = KeyStore.getInstance("pkcs12");
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", certificate);
                return keyStore;
            }
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = {
            ReactiveElasticsearchClientAutoConfiguration.class,
            ElasticsearchRestClientAutoConfiguration.class,
            ElasticsearchClientAutoConfiguration.class,
            ElasticsearchDataAutoConfiguration.class,
            ElasticsearchRepositoriesAutoConfiguration.class
    })
    @ConditionalOnProperty(prefix = "spring.elasticsearch", name = "enabled", havingValue="false")
    public static class ElasticSearchConfigDisabler {
        public ElasticSearchConfigDisabler() {
            LOGGER.debug("{}", ElasticSearchConfigDisabler.class.getName());
        }
    }
}

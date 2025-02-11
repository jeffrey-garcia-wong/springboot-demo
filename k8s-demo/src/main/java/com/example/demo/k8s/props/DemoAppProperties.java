package com.example.demo.k8s.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.config")
public class DemoAppProperties {
    private final Api api;

    public DemoAppProperties(Api api) {
        this.api = api;
    }

    public Api getApi() {
        return this.api;
    }

    public static class Api {
        private final String baseUrl;

        public Api(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getBaseUrl() {
            return this.baseUrl;
        }
    }
}

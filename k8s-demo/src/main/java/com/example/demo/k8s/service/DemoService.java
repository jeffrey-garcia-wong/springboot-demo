package com.example.demo.k8s.service;

import com.example.demo.k8s.model.HttpBinData;
import com.example.demo.k8s.repository.DemoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.sql.rowset.serial.SerialClob;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Optional;

@Service
public class DemoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoService.class);

    private final RestTemplate restTemplate;
    private final DemoRepository repository;

    public DemoService(
            @Autowired RestTemplate restTemplate,
            @Autowired DemoRepository repository
    ) {
        this.restTemplate = restTemplate;
        this.repository = repository;
    }

    @Caching(
            evict = @CacheEvict(cacheNames = "httpbin", key = "#id", beforeInvocation = false)
    )
    public String deleteNetworkThenCache(String id) {
        ResponseEntity<String> response = restTemplate.exchange(
                id,
                HttpMethod.DELETE,
                null,
                String.class
        );
        final String output = response.getBody();
        LOGGER.debug("output: {}", output);
        repository.deleteById(id);
        return output;
    }

    @Caching(
            evict = @CacheEvict(cacheNames = "httpbin", key = "#id"),
            put = @CachePut(cacheNames = "httpbin", key = "#id")
    )
    public String writeNetworkThenCache(String id, String jsonString) throws Exception {
        final ResponseEntity<String> response = restTemplate.exchange(
                id,
                HttpMethod.POST,
                new HttpEntity<>(jsonString),
                String.class
        );
        final String output = response.getBody();
        LOGGER.debug("output: {}", output);
        return writeToDb(id, output);
    }

    @Caching(
            evict = @CacheEvict(cacheNames = "httpbin"),
            put = @CachePut(cacheNames = "httpbin", key = "#id")
    )
    public String writeNetworkThenCache(String id, MultiValueMap<String,String> formData) throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        final ResponseEntity<String> response = restTemplate.exchange(
                id,
                HttpMethod.POST,
                new HttpEntity<>(formData, headers),
                String.class
        );
        final String output = response.getBody();
        LOGGER.debug("output: {}", output);
        return writeToDb(id, output);
    }

    private String writeToDb(String id, String output) throws SQLException {
        final Clob clob = new SerialClob(output.toCharArray());
        repository.save(
                HttpBinData.builder()
                        .id(id)
                        .jsonString(clob)
                        .build()
        );
        return output;
    }


    @Caching(
            cacheable = @Cacheable(cacheNames = "httpbin", key = "#id", sync = true)
    )
    public Optional<String> readCacheThenNetwork(String id) throws IOException, SQLException {
        final Optional<String> resultFromDb = readFromDb(id);
        if (resultFromDb.isPresent()) return resultFromDb;
        final ResponseEntity<String> response = restTemplate.exchange(
                id,
                HttpMethod.GET,
                null,
                String.class
        );
        final String output = response.getBody();
        LOGGER.debug("output: {}", output);
        return Optional.of(writeToDb(id, output));
    }

    private Optional<String> readFromDb(String id) throws IOException, SQLException {
        final Optional<HttpBinData> httpBinData = repository.findById(id);
        if (httpBinData.isPresent()) {
            final Clob clob = httpBinData.get().getJsonString();
            return Optional.of(convertClobToString(clob));
        } else {
            return Optional.empty();
        }
    }

    private String convertClobToString(Clob clob) throws IOException, SQLException {
        long clobLength = clob.length();
        if (clobLength <= Integer.MAX_VALUE) {
            return clob.getSubString(1, (int) clobLength);
        } else {
            final StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(clob.getCharacterStream())) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
            }
            return stringBuilder.toString();
        }
    }

}

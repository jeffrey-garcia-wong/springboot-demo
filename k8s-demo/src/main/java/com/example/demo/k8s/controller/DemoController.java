package com.example.demo.k8s.controller;

import com.example.demo.k8s.props.DemoAppProperties;
import com.example.demo.k8s.service.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@RestController
@RequestMapping(path = "api")
public class DemoController {
    private final static Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

    private final DemoAppProperties appProperties;
    private final RestTemplate restTemplate;
    private final DemoService service;

//    @Value("${app.config.api.baseUrl:https://httpbin.org/}")
    private final String baseUrl;

    public DemoController(
            @Autowired
            DemoAppProperties appProperties,
            @Autowired
            RestTemplate restTemplate,
            @Autowired
            DemoService service
    ) {
        this.appProperties = appProperties;
        this.baseUrl = this.appProperties.getApi().getBaseUrl();
        this.restTemplate = restTemplate;
        this.service = service;
    }

    // GET with param
    // curl -i -X GET "http://localhost:8080/api/delay/1"
    @GetMapping(path = "/delay/{waitInSec}")
    public ResponseEntity<String> getWithPathVariable(
            @PathVariable int waitInSec
    ) {
        try {
//            final ResponseEntity<String> response = restTemplate.exchange(
//                    baseUrl + "/delay/" + waitInSec,
//                    HttpMethod.GET,
//                    null,
//                    String.class
//            );
//            final String output = response.getBody();
//            LOGGER.debug("output: {}", output);
            final Optional<String> output = service.readCacheThenNetwork(baseUrl + "/delay/" + waitInSec);
            return ResponseEntity.ok(output.orElse(""));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET with request param
    // curl -i -X GET "http://localhost:8080/api/delay?waitInSec=1"
    @GetMapping(path = "/delay")
    public ResponseEntity<String> getWithRequestParam(
            @RequestParam("waitInSec") int waitInSec
    ) {
        try {
//            final ResponseEntity<String> response = restTemplate.exchange(
//                    baseUrl + "/delay/" + waitInSec,
//                    HttpMethod.GET,
//                    null,
//                    String.class
//            );
//            final String output = response.getBody();
//            LOGGER.debug("output: {}", output);
            final Optional<String> output = service.readCacheThenNetwork(baseUrl + "/delay/" + waitInSec);
            return ResponseEntity.ok(output.orElse(""));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // POST with body
    // curl -i -X POST "http://localhost:8080/api/delay/1" -H "Content-Type: application/json" -d '{"key1":"value1"}'
    @PostMapping(path = "/delay/{waitInSec}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postWithJsonBody(
            @PathVariable int waitInSec,
            @RequestBody String jsonString
    ) {
        try {
//            final ResponseEntity<String> response = restTemplate.exchange(
//                    baseUrl + "/delay/" + waitInSec,
//                    HttpMethod.POST,
//                    new HttpEntity<>(jsonString),
//                    String.class
//            );
//            final String output = response.getBody();
//            LOGGER.debug("output: {}", output);
            final String output = service.writeNetworkThenCache(baseUrl + "/delay/" + waitInSec, jsonString);
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // POST with form
    @PostMapping(
            path = "/delay/{waitInSec}",
            consumes = {
                    MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_JSON_VALUE
            },
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> postWithFormData(
            @PathVariable int waitInSec,
            @RequestParam MultiValueMap<String,String> formData
    ) {
        try {
//            final HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//            final ResponseEntity<String> response = restTemplate.exchange(
//                    baseUrl + "/delay/" + waitInSec,
//                    HttpMethod.POST,
//                    new HttpEntity<>(formData, headers),
//                    String.class
//            );
//            final String output = response.getBody();
//            LOGGER.debug("output: {}", output);
            final String output = service.writeNetworkThenCache(baseUrl + "/delay/" + waitInSec, formData);
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping(path = "/delay/{waitInSec}")
    public ResponseEntity<String> deleteWithPathVariable(@PathVariable int waitInSec) {
        try {
//            ResponseEntity<String> response = restTemplate.exchange(
//                    baseUrl + "/delay/" + waitInSec,
//                    HttpMethod.DELETE,
//                    null,
//                    String.class
//            );
//            final String output = response.getBody();
//            LOGGER.debug("output: {}", output);
            final String output = service.deleteNetworkThenCache(baseUrl + "/delay/" + waitInSec);
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

//    @PutMapping(path = "/delay/{waitInSec}")
//    public ResponseEntity<String> putWithPathVariable(@PathVariable int waitInSec) {
//        try {
//            final ResponseEntity<String> response = restTemplate.exchange(
//                    baseUrl + "/delay/" + waitInSec,
//                    HttpMethod.PUT,
//                    null,
//                    String.class
//            );
//            final String output = response.getBody();
//            LOGGER.debug("output: {}", output);
//            service.writeToDb(output);
//            return response;
//        } catch (Exception e) {
//            LOGGER.error(e.getMessage(), e);
//            return ResponseEntity.internalServerError().build();
//        }
//    }

}

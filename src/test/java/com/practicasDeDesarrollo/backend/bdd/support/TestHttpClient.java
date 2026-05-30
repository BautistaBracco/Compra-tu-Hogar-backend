package com.practicasDeDesarrollo.backend.bdd.support;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class TestHttpClient {

    private final RestTemplate restTemplate;
    private final Environment env;

    public TestHttpClient(Environment env) {
        this.env = env;
        this.restTemplate = new RestTemplate();
        // We want to assert on 4xx/5xx responses, not throw exceptions.
        this.restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }
        });
    }

    private String baseUrl() {
        Integer port = env.getProperty("local.server.port", Integer.class);
        if (port == null) {
            throw new IllegalStateException("local.server.port not available yet");
        }
        return "http://localhost:" + port;
    }

    public ResponseEntity<String> postJson(String path, String bearerToken, Map<String, ?> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (bearerToken != null) {
            headers.setBearerAuth(bearerToken);
        }

        return restTemplate.exchange(baseUrl() + path, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    }

    public ResponseEntity<String> get(String path, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        if (bearerToken != null) {
            headers.setBearerAuth(bearerToken);
        }

        return restTemplate.exchange(baseUrl() + path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    public ResponseEntity<String> putJson(String path, String bearerToken, Map<String, ?> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (bearerToken != null) {
            headers.setBearerAuth(bearerToken);
        }

        return restTemplate.exchange(baseUrl() + path, HttpMethod.PUT, new HttpEntity<>(body, headers), String.class);
    }

}

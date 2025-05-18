package com.reliaquest.api.remote;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Component
@EnableRetry
public class RemoteClient {

    private RestTemplate restTemplate;
    private String clientUrl;

    @Autowired
    public RemoteClient(@Value("${external.api.base-url:http://localhost:8080}") String externalApiBaseUrl) {
        clientUrl = externalApiBaseUrl;
        restTemplate = new RestTemplate();
    }

    @Retryable(
            retryFor = { HttpServerErrorException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public <T> ResponseEntity<T> get(String endPoint, Map<String, String> queryParams, ParameterizedTypeReference<T> responseType) {
        String uri = createUrl(endPoint,queryParams);
        log.info("Sending GET request to URL: {}", uri);
        return restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                responseType
        );
    }

    @Retryable(
            retryFor = { HttpServerErrorException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public <T> ResponseEntity<T> getList(String endPoint, Map<String, String> queryParams, ParameterizedTypeReference<T> responseType) {
        String uri = createUrl(endPoint,queryParams);
        log.info("Sending GET request to URL: {}", uri);
        return restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                responseType
        );
    }

    @Retryable(
            retryFor = { HttpServerErrorException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public <T, R> ResponseEntity<T> post(String endPoint, R body, ParameterizedTypeReference<T> responseType) {
        HttpEntity<R> request = new HttpEntity<>(body, defaultHeaders());
        String uri = createUrl(endPoint,null);

        log.info("Sending POST request to URL: {}, body: {}", uri, body);

        return restTemplate.exchange(
                uri,
                HttpMethod.POST,
                request,
                responseType
        );
    }

    @Retryable(
            retryFor = { HttpServerErrorException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public <T,R> ResponseEntity<T> delete(String endpoint, R body, ParameterizedTypeReference<T> responseType) {
        HttpEntity<R> request = new HttpEntity<>(body, defaultHeaders());
        String url = createUrl(endpoint,null);
        log.info("Sending DELETE request to URL: {}", url);

        return restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                request,
                responseType
        );
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String createUrl(String endPoint, Map<String, String> queryParams){
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(clientUrl);
        if(!endPoint.isBlank()){
            builder.pathSegment(endPoint);
        }
        if (queryParams != null) {
            queryParams.forEach(builder::queryParam);
        }
        return builder.toUriString();
    }

}

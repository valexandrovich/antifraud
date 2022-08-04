package ua.com.solidity.enricher.service;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@CustomLog
@Component
@AllArgsConstructor
public class HttpClient {

    public <T, R> T post(String url, Class<? extends T> clazz, List<R> parameters) {
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<R>> request = new HttpEntity<>(parameters, headers);
        T result = null;
        while (result == null) {
            try {
                result = restTemplate.postForObject(url, request, clazz);
            } catch (ResourceAccessException e) {
                log.warn(e.getMessage());
                log.info("Need to run the Dispatcher");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public <T> T get(String url, Class<? extends T> clazz) {
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        headers.setContentType(MediaType.APPLICATION_JSON);
        T result = null;
        while (result == null) {
            try {
                result = restTemplate.getForObject(url, clazz);
            } catch (ResourceAccessException e) {
                log.warn(e.getMessage());
                log.info("Need to run the Dispatcher");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}

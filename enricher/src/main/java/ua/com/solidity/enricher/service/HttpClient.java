package ua.com.solidity.enricher.service;

import java.util.List;
import lombok.CustomLog;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ua.com.solidity.common.Utils;


@CustomLog
@Component
public class HttpClient {

    @Value("${enricher.sleepTimeDispatcher}")
    private int sleepTime;
    private boolean weHaveProblem = false;

    @SneakyThrows
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
                if (!weHaveProblem) {
                    log.error("Dispatcher not available");
                    weHaveProblem = true;
                }
            }
            if (result == null) Utils.waitMs(sleepTime);
            else if (weHaveProblem) {
                weHaveProblem = false;
                log.info("Dispatcher connection has been restored");
            }
        }
        return result;
    }

    @SneakyThrows
    public <T> T get(String url, Class<? extends T> clazz) {
        RestTemplate restTemplate = new RestTemplate();
        T result = null;
        while (result == null) {
            try {
                result = restTemplate.getForObject(url, clazz);
            } catch (ResourceAccessException e) {
                if (!weHaveProblem) {
                    log.error("Dispatcher not available");
                    weHaveProblem = true;
                }
            }
            if (result == null) Utils.waitMs(sleepTime);
            else if (weHaveProblem) {
                weHaveProblem = false;
                log.info("Dispatcher connection has been restored");
            }
        }
        return result;
    }
}

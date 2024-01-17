package ua.com.valexa.downloaderismc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.StepRequestDto;
import ua.com.valexa.common.dto.TestMessage;

@Service
@Slf4j
public class QueueListener {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DownloaderService downloaderService;

    @RabbitListener(queues = "ismc-downloader")
    public void receiveMessage(String message) {
        try {
            StepRequestDto requestDto = objectMapper.readValue(message, StepRequestDto.class);
            downloaderService.handleDownload(requestDto);
        } catch (JsonProcessingException e) {
            log.error("Cant serialize queue message: " + message);
        }

    }
}

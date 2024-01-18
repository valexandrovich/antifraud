package ua.com.valexa.downloaderismc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.StepRequestDto;
import ua.com.valexa.common.dto.StepResponseDto;
import ua.com.valexa.common.dto.TestMessage;

import java.io.IOException;
import java.util.concurrent.*;


@Service
@Slf4j
public class QueueListener {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DownloaderService downloaderService;

    @Autowired
    ApplicationContext appContext;

    @Autowired
    RabbitTemplate rabbitTemplate;



    @RabbitListener(queues = "ismc-downloader",  errorHandler = "queueListenerErrorHandler")
    public void receiveMessage(StepRequestDto requestDto) {




        CompletableFuture<StepResponseDto> cfuture = downloaderService.handleDownload(requestDto);
        cfuture.thenAcceptAsync(this::sendResponse);
    }


    public void sendResponse(StepResponseDto stepResponseDto){
        rabbitTemplate.convertAndSend("ismc-scheduler-response", stepResponseDto);
    }


//    @RabbitListener(queues = "ismc-downloader-manage", errorHandler = "queueListenerErrorHandler")
//    public void receiveManageMessage(String message, Channel channel) {
//        switch (message) {
//            case "clear":
//                try {
//                    log.info("Cleaning downloader queue");
//                    channel.queuePurge("ismc-downloader");
//                } catch (IOException e) {
//                    log.error("Error in main queue");
//                }
//                break;
//
//        }
//

//    }
}

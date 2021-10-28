package ua.com.solidity.otp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ua.com.solidity.otp.model.ReserveCopyMessage;

@Service
@Slf4j
public class ReserveCopyMessageListener {
    final ObjectMapper mapper;
    final ReserveCopyService reserveCopyService;

    public ReserveCopyMessageListener(ObjectMapper mapper, ReserveCopyService reserveCopyService) {
        this.mapper = mapper;
        this.reserveCopyService = reserveCopyService;
    }

    @RabbitListener(queues = "${spring.rabbitmq.queue}")
    public void handleMessage(String message) {
        try {
            ReserveCopyMessage reserveCopyMessage = mapper.readValue(message, ReserveCopyMessage.class);
            reserveCopyService.makeReserveCopy(reserveCopyMessage);
        } catch (JsonProcessingException e) {

            log.error("Error while parsing message " + message);
        }
    }
}

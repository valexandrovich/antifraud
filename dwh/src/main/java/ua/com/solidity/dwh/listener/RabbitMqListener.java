package ua.com.solidity.dwh.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ua.com.solidity.dwh.model.UpdateDWHRequest;
import ua.com.solidity.dwh.service.DWHService;

@Slf4j
@EnableRabbit
@RequiredArgsConstructor
@Component
@PropertySource({"classpath:dwh.properties", "classpath:application.properties"})
public class RabbitMqListener {

	@Value("${dwh.rabbitmq.name}")
	private String dwhQueue;

	private final DWHService dwhService;

	@RabbitListener(queues = "${dwh.rabbitmq.name}")
	public void processMyQueue(String message) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			UpdateDWHRequest updateDWHRequest = objectMapper.readValue(message, UpdateDWHRequest.class);
			log.debug("Received message from {}: {}", dwhQueue, message);
			dwhService.update(updateDWHRequest);
		} catch (JsonProcessingException e) {
			log.error("Can't understand object from queue!", e);
			log.debug("The message was: {}", message);
		}
	}
}

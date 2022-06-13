package ua.com.solidity.notification.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.notification.exception.JsonNotConvertedException;
import ua.com.solidity.notification.model.SendEmailRequest;


@Slf4j
@RequiredArgsConstructor
@RestController
public class RabbitController {

	@Value("${notification.rabbitmq.name}")
	private String queueName;
	private final AmqpTemplate template;

	@PostMapping("/emit")
	public ResponseEntity<String> emit(@RequestBody SendEmailRequest request) {
		String jo;
		try {
			jo = new ObjectMapper().writeValueAsString(request);
		} catch (JsonProcessingException e) {
			log.error("Couldn't convert request fields.");
			throw new JsonNotConvertedException();
		}
		log.info("Sending task to {}", queueName);
		template.convertAndSend(queueName, jo);
		return ResponseEntity.ok("Success emit to queue");
	}
}

package ua.com.solidity.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.com.solidity.common.Utils;

@Slf4j
@RequiredArgsConstructor
@Service
public class RabbitService {

	@Value("${scheduler.rabbitmq.name}")
	private String schedulerQueue;
	@Value("${downloader.rabbitmq.name}")
	private String downloaderQueue;
	@Value("${dwh.rabbitmq.name}")
	private String dwhQueue;
	@Value("${report.rabbitmq.name}")
	private String reportQueue;
	@Value("${importer.rabbitmq.name}")
	private String importerQueue;
	@Value("${notification.rabbitmq.name}")
	private String notificationQueue;
	@Value("${statuslogger.rabbitmq.name}")
	private String statusloggerQueue;
	@Value("${enricher.rabbitmq.name}")
	private String enricherQueue;

	private final AmqpTemplate template;

	public void send(String queue, String message) {
		log.info("Sending task to {}", queue);
		Utils.sendRabbitMQMessage(queue, message);
	}
}

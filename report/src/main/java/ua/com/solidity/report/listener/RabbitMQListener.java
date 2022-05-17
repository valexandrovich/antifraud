package ua.com.solidity.report.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.MonitoringNotification;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.repositories.MonitoringNotificationRepository;
import ua.com.solidity.db.repositories.UserRepository;
import ua.com.solidity.report.model.SendEmailRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@EnableRabbit
@Component
@RequiredArgsConstructor
@EntityScan(basePackages = {"ua.com.solidity.db.entities"})
@EnableJpaRepositories(basePackages = {"ua.com.solidity.db.repositories"})
public class RabbitMQListener {

	@Value("${report.rabbitmq.name}")
	private String reportQueue;
	@Value("${notification.rabbitmq.name}")
	private String notificationQueue;
	private final AmqpTemplate template;

	private final UserRepository userRepository;
	private final MonitoringNotificationRepository mnRepository;

	@RabbitListener(queues = "${report.rabbitmq.name}")
	public void processMyQueue() {
		log.debug("Receive task from " + reportQueue);

		List<User> userList = userRepository.findAll();
//		List<SendEmailRequest> sendEmailRequests = new ArrayList<>();

		userList.forEach(user -> {
			List<MonitoringNotification> monitoringNotificationList = new ArrayList<>();
			Set<YPerson> people = user.getPeople();
			people.forEach(yPerson -> monitoringNotificationList.addAll(mnRepository.findByYpersonIdAndSent(yPerson.getId(), false)));

			StringBuilder messageBuilder = new StringBuilder();
			for (MonitoringNotification monitoringNotification : monitoringNotificationList) {
				messageBuilder.append(monitoringNotification.getMessage()).append("\n");
			}

			if (messageBuilder.length() != 0) {
				SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
						.to(user.getEmail())
						.subject("Notification about monitoring person's changes.")
						.body(messageBuilder.toString())
						.retries(2)
						.build();

				String jo;
				try {
					jo = new ObjectMapper().writeValueAsString(sendEmailRequest);
					log.info("Emit to " + notificationQueue);
					template.convertAndSend(notificationQueue, jo);
				} catch (JsonProcessingException e) {
					log.error("Couldn't convert json: {}", e.getMessage());
				}

				monitoringNotificationList.forEach(monitoringNotification -> monitoringNotification.setSent(true));
				mnRepository.saveAll(monitoringNotificationList);

//				sendEmailRequests.add(sendEmailRequest);
			}
		});
//
//		sendEmailRequests.forEach(sendEmailRequest -> {
//			String jo;
//			try {
//				jo = new ObjectMapper().writeValueAsString(sendEmailRequest);
//				log.info("Emit to " + notificationQueue);
//				template.convertAndSend(notificationQueue, jo);
//			} catch (JsonProcessingException e) {
//				log.error("Couldn't convert json: {}", e.getMessage());
//			}
//		});

	}
}

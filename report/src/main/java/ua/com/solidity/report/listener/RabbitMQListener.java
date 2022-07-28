package ua.com.solidity.report.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyMonitoringNotification;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPersonMonitoringNotification;
import ua.com.solidity.db.repositories.UserRepository;
import ua.com.solidity.db.repositories.YCompanyMonitoringNotificationRepository;
import ua.com.solidity.db.repositories.YPersonMonitoringNotificationRepository;
import ua.com.solidity.report.model.SendEmailRequest;

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
	private final YPersonMonitoringNotificationRepository ypmnRepository;
	private final YCompanyMonitoringNotificationRepository ycmnRepository;

	@RabbitListener(queues = "${report.rabbitmq.name}")
	public void processMyQueue() {
		log.debug("Receive task from " + reportQueue);

		List<User> userList = userRepository.findAll();

		userList.forEach(user -> {
			List<YPersonMonitoringNotification> ypersonMonitoringNotificationList = new ArrayList<>();
			List<YCompanyMonitoringNotification> ycompanyMonitoringNotificationList = new ArrayList<>();
			Set<YPerson> people = user.getPersonSubscriptions();
			Set<YCompany> companies = user.getCompanies();
			people.forEach(yperson -> ypersonMonitoringNotificationList
					.addAll(ypmnRepository.findByYpersonIdAndSent(yperson.getId(), false)));
			companies.forEach(ycompany -> ycompanyMonitoringNotificationList
					.addAll(ycmnRepository.findByYcompanyIdAndSent(ycompany.getId(), false)));

			StringBuilder messageBuilder = new StringBuilder();
			for (YPersonMonitoringNotification ypersonMonitoringNotification : ypersonMonitoringNotificationList) {
				messageBuilder.append(ypersonMonitoringNotification.getMessage()).append("\n");
			}

			for (YCompanyMonitoringNotification ycompanyMonitoringNotification : ycompanyMonitoringNotificationList) {
				messageBuilder.append(ycompanyMonitoringNotification.getMessage()).append("\n");
			}

			if (messageBuilder.length() != 0) {
				SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
						.to(user.getEmail())
						.subject("Notification about monitoring people and companies changes.")
						.body(messageBuilder.toString())
						.retries(2)
						.build();

				String jo;
				try {
					jo = new ObjectMapper().writeValueAsString(sendEmailRequest);
					log.info("Sending task to {}", notificationQueue);
					template.convertAndSend(notificationQueue, jo);
				} catch (JsonProcessingException e) {
					log.error("Couldn't convert json: {}", e.getMessage());
				}

				ypersonMonitoringNotificationList
						.forEach(ypersonMonitoringNotification -> ypersonMonitoringNotification.setSent(true));
				ycompanyMonitoringNotificationList
						.forEach(ycompanyMonitoringNotification -> ycompanyMonitoringNotification.setSent(true));
				if (!ypersonMonitoringNotificationList.isEmpty())ypmnRepository.saveAll(ypersonMonitoringNotificationList);
				if (!ycompanyMonitoringNotificationList.isEmpty())ycmnRepository.saveAll(ycompanyMonitoringNotificationList);
			}
		});
	}
}

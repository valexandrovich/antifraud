package ua.com.solidity.report.listener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import ua.com.solidity.db.entities.NotificationPhysicalTagCondition;
import ua.com.solidity.db.entities.NotificationPhysicalTagMatching;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YCompanyMonitoringNotification;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPersonMonitoringNotification;
import ua.com.solidity.db.entities.YPersonPackageMonitoringNotification;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.NotificationPhysicalTagMatchingRepository;
import ua.com.solidity.db.repositories.UserRepository;
import ua.com.solidity.db.repositories.YCompanyMonitoringNotificationRepository;
import ua.com.solidity.db.repositories.YPersonMonitoringNotificationRepository;
import ua.com.solidity.db.repositories.YPersonPackageMonitoringNotificationRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
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
    private final YPersonMonitoringNotificationRepository personMonitoringNotificationRepository;
    private final YPersonPackageMonitoringNotificationRepository personPackageMonitoringNotificationRepository;
    private final YCompanyMonitoringNotificationRepository companyMonitoringNotificationRepository;
    private final NotificationPhysicalTagMatchingRepository physicalTagMatchingRepository;
    private final YPersonRepository personRepository;

    private final static String FILE_HEADER = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\" \"http://www.w3.org/TR/REC-html40/loose.dtd\">" +
            "<html>" +
            "<head>" +
            "</head>" +
            "<body>";

    @RabbitListener(queues = "${report.rabbitmq.name}")
    public void processMyQueue() {
        log.debug("Receive task from " + reportQueue);

        notifySubscribers();
        notifyPackageMonitoringSubscribers();

    }

    private void notifySubscribers() {
        List<User> userList = userRepository.findAll();

        userList.forEach(user -> {
            List<YPersonMonitoringNotification> ypersonMonitoringNotificationList =
                    personMonitoringNotificationRepository.findByUserAndSent(user, false);
            List<YCompanyMonitoringNotification> ycompanyMonitoringNotificationList =
                    companyMonitoringNotificationRepository.findByUserAndSent(user, false);

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
                        .subject("Сповіщення про зміни по моніторингу людей і компаній")
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
                if (!ypersonMonitoringNotificationList.isEmpty())
                    personMonitoringNotificationRepository.saveAll(ypersonMonitoringNotificationList);
                if (!ycompanyMonitoringNotificationList.isEmpty())
                    companyMonitoringNotificationRepository.saveAll(ycompanyMonitoringNotificationList);
            }
        });
    }

    private void notifyPackageMonitoringSubscribers() {
        List<NotificationPhysicalTagMatching> matchings = physicalTagMatchingRepository.findAll();

        matchings.forEach(matching -> {

            List<YPersonPackageMonitoringNotification> personPackageMonitoringNotifications =
                    personPackageMonitoringNotificationRepository.findByEmailAndSent(matching.getEmail(), false);

            Map<NotificationPhysicalTagCondition, List<YPerson>> conditionMap = new LinkedHashMap<>();

            personPackageMonitoringNotifications.forEach(notification -> {
                YPerson yPerson = personRepository.findWithInnsAndTagsById(notification.getYpersonId())
                        .orElse(null);

                conditionMap.computeIfAbsent(notification.getCondition(), k -> new ArrayList<>());
                if (yPerson != null) conditionMap.get(notification.getCondition()).add(yPerson);
            });

            StringBuilder messageBuilder = new StringBuilder();
            boolean built = false;
            messageBuilder.append(FILE_HEADER);

            for (Map.Entry<NotificationPhysicalTagCondition, List<YPerson>> entry : conditionMap.entrySet()) {
                NotificationPhysicalTagCondition condition = entry.getKey();
                List<YPerson> personList = entry.getValue();

                if (!personList.isEmpty()) {
                    built = true;
                    List<String> codeList = condition.getTagTypes().stream()
                            .map(TagType::getCode)
                            .collect(Collectors.toList());
                    StringBuilder codesInString = new StringBuilder("[");
                    codesInString.append(codeList.get(0));
                    for (int i = 1; i < codeList.size(); i++) {
                        codesInString.append(" &amp; ").append(codeList.get(i));
                    }
                    codesInString.append("]");

                    String rowspan = codeList.size() > 1 ? " rowspan=\"" + codeList.size() + "\"" : "";

                    messageBuilder.append(tableCaption(codesInString.toString()));

                    Map<YPerson, String> personNamesMap = new HashMap<>();
                    personList.forEach(person -> {
                        StringBuilder personName = new StringBuilder();
                        Stream.of(person.getLastName(), person.getFirstName(), person.getPatName())
                                .forEach(name -> {
                                    if (personName.length() > 0 && name != null) personName.append(" ");
                                    if (name != null) personName.append(name);
                                });
                        personNamesMap.put(person, personName.toString());
                    });
                    Stream<Map.Entry<YPerson, String>> personNamesStreamSorted = personNamesMap.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByValue());

                    personNamesStreamSorted.forEach(entryPersonName -> {
                        YPerson person = entryPersonName.getKey();
                        String personName = entryPersonName.getValue();

                        Map<String, String> tagAsOfDatesMap = new HashMap<>();
                        codeList.forEach(code -> {
                            List<String> tagAsOfDatesList = new ArrayList<>();
                            List<YTag> collect = person.getTags()
                                    .stream()
                                    .filter(tag -> tag.getTagType().getCode().equals(code))
                                    .sorted(Comparator.comparing(YTag::getAsOf))
                                    .collect(Collectors.toList());
                            collect.forEach(tag -> {
                                if (tag.getAsOf() != null) {
                                    tagAsOfDatesList.add(tag.getAsOf().toString());
                                }
                            });
                            if (!tagAsOfDatesList.isEmpty()) {
                                String tagAsOfDatesListInString = tagAsOfDatesList.toString();
                                tagAsOfDatesMap.put(code, tagAsOfDatesListInString.substring(1, tagAsOfDatesListInString.length() - 1));
                            } else {
                                tagAsOfDatesMap.put(code, "");
                            }

                        });

                        messageBuilder.append("<tr>");
                        messageBuilder.append("<td style=\"border:1px solid rgb(190, 190, 190);padding:5px 10px;text-align:center;\"").append(rowspan).append(">");
                        if (!person.getInns().isEmpty()) {
                            YINN yinn = person.getInns().iterator().next();
                            messageBuilder.append(yinn.getInn());
                        }
                        messageBuilder.append("</td>");

                        messageBuilder.append("<td style=\"border:1px solid rgb(190, 190, 190);padding:5px 10px;text-align:center;\"").append(rowspan).append(">");
                        if (personName.length() > 0) {
                            messageBuilder.append(personName);
                        }
                        messageBuilder.append("</td>");

                        if (!codeList.isEmpty()) {
                            messageBuilder.append("<td style=\"border:1px solid rgb(190, 190, 190);padding:5px 10px\">");
                            StringBuilder tagTypeAsOfBuilder = new StringBuilder(codeList.get(0));
                            if (!tagAsOfDatesMap.get(codeList.get(0)).isBlank()) {
                                tagTypeAsOfBuilder.append(" з ").append(tagAsOfDatesMap.get(codeList.get(0)));
                            }
                            messageBuilder.append(tagTypeAsOfBuilder);
                            messageBuilder.append("</td>");
                        }
                        messageBuilder.append("</tr>");

                        for (int i = 1; i < codeList.size(); i++) {
                            messageBuilder.append("<tr>");

                            messageBuilder.append("<td style=\"border:1px solid rgb(190, 190, 190);padding:5px 10px\">");
                            StringBuilder tagTypeAsOfBuilder = new StringBuilder(codeList.get(i));
                            if (!tagAsOfDatesMap.get(codeList.get(i)).isBlank()) {
                                tagTypeAsOfBuilder.append(" з ").append(tagAsOfDatesMap.get(codeList.get(i)));
                            }
                            messageBuilder.append(tagTypeAsOfBuilder);
                            messageBuilder.append("</td>");

                            messageBuilder.append("</tr>");
                        }

                    });

                    messageBuilder.append("</tbody>");
                    messageBuilder.append("</table>");

                }
            }
            messageBuilder.append("</body>");
            messageBuilder.append("</html>");

            if (built) {
                SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                        .to(matching.getEmail())
                        .subject("Сповіщення моніторингу пакетів про зміни тегів на основі умови моніторингу тегів")
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

                personPackageMonitoringNotifications
                        .forEach(personPackageMonitoringNotification -> personPackageMonitoringNotification.setSent(true));
                if (!personPackageMonitoringNotifications.isEmpty())
                    personPackageMonitoringNotificationRepository.saveAll(personPackageMonitoringNotifications);
            }
        });
    }

    private String tableCaption(String caption) {
        return "<table style=\"border-collapse:collapse;border:2px solid rgb(200, 200, 200);letter-spacing:1px;font-family:sans-serif;font-size:.8rem;\">" +
                "<caption style=\"padding:10px;caption-side:top;font-weight:bold;\">" + caption + "</caption>" +
                "<thead style=\"background-color:#3f87a6;color:#fff;\">" +
                "<tr>" +
                "<th scope=\"col\" style=\"border:1px solid rgb(190, 190, 190);padding:5px 10px;\">ІНН</th>" +
                "<th scope=\"col\" style=\"border:1px solid rgb(190, 190, 190);padding:5px 10px;\">Прізвище, Ім'я, по-батькові</th>" +
                "<th scope=\"col\" style=\"border:1px solid rgb(190, 190, 190);padding:5px 10px;\">Мітки</th>" +
                "</tr>" +
                "</thead>" +
                "<tbody style=\"background-color:#e4f0f5;\">";
    }
}
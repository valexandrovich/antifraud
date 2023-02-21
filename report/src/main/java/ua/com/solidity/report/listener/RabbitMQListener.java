package ua.com.solidity.report.listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.NotificationJuridicalTagCondition;
import ua.com.solidity.db.entities.NotificationJuridicalTagMatching;
import ua.com.solidity.db.entities.NotificationPhysicalTagCondition;
import ua.com.solidity.db.entities.NotificationPhysicalTagMatching;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YCTag;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyMonitoringNotification;
import ua.com.solidity.db.entities.YCompanyPackageMonitoringNotification;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPersonMonitoringNotification;
import ua.com.solidity.db.entities.YPersonPackageMonitoringNotification;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.NotificationJuridicalTagMatchingRepository;
import ua.com.solidity.db.repositories.NotificationPhysicalTagMatchingRepository;
import ua.com.solidity.db.repositories.UserRepository;
import ua.com.solidity.db.repositories.YCompanyMonitoringNotificationRepository;
import ua.com.solidity.db.repositories.YCompanyPackageMonitoringNotificationRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YPersonMonitoringNotificationRepository;
import ua.com.solidity.db.repositories.YPersonPackageMonitoringNotificationRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.report.model.DocumentData;
import ua.com.solidity.report.model.SendEmailRequest;
import ua.com.solidity.report.service.DocumentService;

import javax.transaction.Transactional;

import static ua.com.solidity.report.utils.Utils.randomPath;

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
    @Value("${otp.nfs.folder}")
    private String mountPoint;
    private final AmqpTemplate template;

    private final UserRepository userRepository;
    private final YPersonMonitoringNotificationRepository personMonitoringNotificationRepository;
    private final YCompanyMonitoringNotificationRepository companyMonitoringNotificationRepository;

    private final YPersonPackageMonitoringNotificationRepository personPackageMonitoringNotificationRepository;
    private final YCompanyPackageMonitoringNotificationRepository companyPackageMonitoringNotificationRepository;

    private final NotificationPhysicalTagMatchingRepository physicalTagMatchingRepository;
    private final NotificationJuridicalTagMatchingRepository juridicalTagMatchingRepository;

    private final YPersonRepository personRepository;
    private final YCompanyRepository companyRepository;

    private final DocumentService documentService;

    private static final String PHYSICAL_MESSAGE_SUBJ = "Сповіщення пакетного моніторингу фізичних осіб про зміни тегів на основі умови моніторингу тегів";
    private static final String JURIDICAL_MESSAGE_SUBJ = "Сповіщення пакетного моніторингу юридичних осіб про зміни тегів на основі умови моніторингу тегів";
    private static final String FILE_HEADER = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\" \"http://www.w3.org/TR/REC-html40/loose.dtd\">" +
            "<html>" +
            "<head><meta charset=\"utf-8\"></head>" +
            "<body>";
    private static final String SENDING_LOG = "Sending task to {}";
    private static final String COULD_NOT_CONVERT_LOG = "Couldn't convert json: {}";
    private static final String TD_CENTER_OPEN_HTML = "<td style=\"border:1px solid rgb(190, 190, 190);padding:5px 10px;text-align:center;\"";
    private static final String TD_OPEN_HTML = "<td style=\"border:1px solid rgb(190, 190, 190);padding:5px 10px\">";
    private static final String TD_CLOSE_HTML = "</td>";
    private static final String TR_OPEN_HTML = "<tr>";
    private static final String TR_CLOSE_HTML = "</tr>";
    private static final String BODY_CLOSE_HTML = "</body>";
    private static final String HTML_CLOSE_HTML = "</html>";
    private static final String TBODY_CLOSE_HTML = "</tbody>";
    private static final String TABLE_CLOSE_HTML = "</table>";

    @RabbitListener(queues = "${report.rabbitmq.name}")
    @Transactional
    public void processMyQueue() {
        log.info("Received task from {}", reportQueue);

        notifySubscribers();
        log.info("Subscribers notified");

//        physicalPackageMonitoringReport();
        physicalPackageMonitoringReportExcel();
        log.info("Package monitoring finished: Physical");

//        juridicalPackageMonitoringReport();
        juridicalPackageMonitoringReportExcel();
        log.info("Package monitoring finished: Juridical");
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
                    log.info(SENDING_LOG, notificationQueue);
                    template.convertAndSend(notificationQueue, jo);
                } catch (JsonProcessingException e) {
                    log.error(COULD_NOT_CONVERT_LOG, e.getMessage());
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

    @Transactional
    public void physicalPackageMonitoringReportExcel() {
        List<NotificationPhysicalTagMatching> tagMatchingList = physicalTagMatchingRepository.findAll();
        for (NotificationPhysicalTagMatching tagMatching : tagMatchingList) {

            List<DocumentData> documentDataList = new ArrayList<>();
            try (Stream<YPersonPackageMonitoringNotification> notificationListToSent =
                         personPackageMonitoringNotificationRepository.findByEmailAndSent(tagMatching.getEmail(), false)) {

                notificationListToSent.forEach(notification -> {
                    YPerson person = personRepository.findWithInnsAndTagsById(notification.getYpersonId())
                            .orElseThrow(null);

                    if (person != null) {
                        Set<TagType> notificationTagTypes = notification.getCondition().getTagTypes();
                        Set<YTag> requiredTags = person.getTags().stream()
                                .filter(yTag -> notificationTagTypes.contains(yTag.getTagType()))
                                .collect(Collectors.toSet());

                        DocumentData documentData = buildDocumentDataForPhysical(person, requiredTags);

                        documentDataList.add(documentData);
                    }
                    notification.setSent(true);
                });
            }
//            if (!documentDataList.isEmpty()) //TODO що робити у випадку пустих даних
            String filePath = documentService.createDocument(documentDataList);

            SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                    .to(tagMatching.getEmail())
                    .subject(PHYSICAL_MESSAGE_SUBJ)
                    .body("See attached")
                    .filePath(filePath)
                    .retries(2)
                    .build();

            doSend(sendEmailRequest);
        }
    }

    private DocumentData buildDocumentDataForPhysical(YPerson person, Set<YTag> requiredTags) {
        return DocumentData.builder()
                .name(person.getLastName() + " " + person.getFirstName() + " " + person.getPatName())
                .uniqueIdentifier(person.getInns().stream()
                        .map(yinn -> yinn.getInn().toString())
                        .collect(Collectors.joining(", ")))
                .link("https://antifraud.otpbank.com.ua/#/yperson/" + person.getId()) //TODO extract to prop file
                .tagInformationList(requiredTags.stream()
                        .map(yTag -> DocumentData.TagInformation.builder()
                                .tagTypeCode(yTag.getTagType().getCode())
                                .eventDate(yTag.getEventDate().toString())
                                .startDate(yTag.getAsOf().toString())
                                .endDate(yTag.getUntil().toString())
                                .numberValue(yTag.getNumberValue())
                                .textValue(yTag.getTextValue())
                                .description(yTag.getDescription())
                                .source(yTag.getImportSources().stream()
                                        .map(ImportSource::getName)
                                        .collect(Collectors.joining(", ")))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public void juridicalPackageMonitoringReportExcel() {
        List<NotificationJuridicalTagMatching> tagMatchingList = juridicalTagMatchingRepository.findAll();
        for (NotificationJuridicalTagMatching tagMatching : tagMatchingList) {

            List<DocumentData> documentDataList = new ArrayList<>();
            try (Stream<YCompanyPackageMonitoringNotification> notificationListToSent =
                         companyPackageMonitoringNotificationRepository.findByEmailAndSent(tagMatching.getEmail(), false)) {

                notificationListToSent.forEach(notification -> {
                    YCompany company = companyRepository.findWithTagsById(notification.getYcompanyId())
                            .orElseThrow(null);

                    if (company != null) {
                        Set<TagType> notificationTagTypes = notification.getCondition().getTagTypes();
                        Set<YCTag> requiredTags = company.getTags().stream()
                                .filter(yTag -> notificationTagTypes.contains(yTag.getTagType()))
                                .collect(Collectors.toSet());

                        DocumentData documentData = buildDocumentDataForJuridical(company, requiredTags);

                        documentDataList.add(documentData);
                    }
                    notification.setSent(true); //TODO на якому етапі проставляти sent
                });
            }
//            if (!documentDataList.isEmpty()) //TODO що робити у випадку пустих даних
            String filePath = documentService.createDocument(documentDataList);

            SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                    .to(tagMatching.getEmail())
                    .subject(PHYSICAL_MESSAGE_SUBJ)
                    .body("See attached")
                    .filePath(filePath)
                    .retries(2)
                    .build();

            doSend(sendEmailRequest);
        }
    }

    private DocumentData buildDocumentDataForJuridical(YCompany company, Set<YCTag> requiredTags) {
        return DocumentData.builder()
                .name(company.getName())
                .uniqueIdentifier(String.valueOf(company.getEdrpou()))
                .link("https://antifraud.otpbank.com.ua/#/ycompany/" + company.getId()) //TODO extract to prop file
                .tagInformationList(requiredTags.stream()
                        .map(yTag -> DocumentData.TagInformation.builder()
                                .tagTypeCode(yTag.getTagType().getCode())
                                .eventDate(yTag.getEventDate().toString())
                                .startDate(yTag.getAsOf().toString())
                                .endDate(yTag.getUntil().toString())
                                .numberValue(yTag.getNumberValue())
                                .textValue(yTag.getTextValue())
                                .description(yTag.getDescription())
                                .source(yTag.getImportSources().stream()
                                        .map(ImportSource::getName)
                                        .collect(Collectors.joining(", ")))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public void physicalPackageMonitoringReport() {
        log.debug("[physicalPackageMonitoringReport] Getting matchings");
        try (Stream<NotificationPhysicalTagMatching> notificationPhysicalTagMatchingStream = physicalTagMatchingRepository.streamAllBy()) {

            log.debug("[physicalPackageMonitoringReport] Iterating through matchings");
            notificationPhysicalTagMatchingStream.forEach(tagMatching -> {
                log.debug("[physicalPackageMonitoringReport] Get list of people to be notified");
                Stream<YPersonPackageMonitoringNotification> personPackageMonitoringNotifications =
                        personPackageMonitoringNotificationRepository.findByEmailAndSent(tagMatching.getEmail(), false);

                log.debug("[physicalPackageMonitoringReport] Create condition map");
                Map<NotificationPhysicalTagCondition, List<YPerson>> conditionMap = new LinkedHashMap<>();

                log.debug("[physicalPackageMonitoringReport] Mapping conditions to people to be notified");
                personPackageMonitoringNotifications.forEach(notification -> {
                    YPerson yPerson = personRepository.findWithInnsAndTagsById(notification.getYpersonId())
                            .orElse(null);

                    conditionMap.computeIfAbsent(notification.getCondition(), k -> new ArrayList<>());
                    if (yPerson != null) conditionMap.get(notification.getCondition()).add(yPerson);

                    notification.setSent(true);
                    personPackageMonitoringNotificationRepository.save(notification);
                });

                log.debug("[physicalPackageMonitoringReport] Preparing report file");
                String reportPath = randomPath() + ".html";
                log.debug("[physicalPackageMonitoringReport] Report file path to be used: {}", reportPath);
                File file = new File(mountPoint, reportPath);
                if (file.getParentFile().mkdirs()) {
                    log.debug("[physicalPackageMonitoringReport] Folder(s) created");
                }
                boolean built;
                try (FileWriter writer = new FileWriter(file)) {
                    built = processPhysicalReport(writer, conditionMap);

                    if (built) {
                        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                                .to(tagMatching.getEmail())
                                .subject(PHYSICAL_MESSAGE_SUBJ)
                                .body("See attached")
                                .filePath(file.getAbsolutePath())
                                .retries(2)
                                .build();

                        doSend(sendEmailRequest);
                    }
                } catch (IOException e) {
                    log.error("[physicalPackageMonitoringReport-a]", e);
                }
            });
        }
    }

    @Transactional
    public void doSend(SendEmailRequest sendEmailRequest) {
        String jo;
        try {
            jo = new ObjectMapper().writeValueAsString(sendEmailRequest);
            log.info(SENDING_LOG, notificationQueue);
            template.convertAndSend(notificationQueue, jo);
        } catch (JsonProcessingException e) {
            log.error(COULD_NOT_CONVERT_LOG, e.getMessage());
        }
    }

    @Transactional
    public boolean processPhysicalReport(FileWriter writer, Map<NotificationPhysicalTagCondition, List<YPerson>> conditionMap) {
        boolean built = false;
        try {
            writer.write(FILE_HEADER);

            for (Map.Entry<NotificationPhysicalTagCondition, List<YPerson>> entry : conditionMap.entrySet()) {
                NotificationPhysicalTagCondition condition = entry.getKey();
                List<YPerson> personList = entry.getValue();

                if (!personList.isEmpty()) {
                    built = true;
                    List<String> codeList = condition.getTagTypes().stream()
                            .map(TagType::getCode)
                            .collect(Collectors.toList());
                    StringBuilder codesInString = new StringBuilder(condition.getDescription());
                    codesInString.append(" [");
                    codesInString.append(codeList.get(0));
                    for (int i = 1; i < codeList.size(); i++) {
                        codesInString.append(" &amp; ").append(codeList.get(i));
                    }
                    codesInString.append("]");

                    String rowspan = codeList.size() > 1 ? " rowspan=\"" + codeList.size() + "\"" : "";

                    writer.write(tableCaption(codesInString.toString(), Entity.PERSON));

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

                        try {
                            writer.write(TR_OPEN_HTML);
                            writer.write(TD_CENTER_OPEN_HTML + rowspan + ">");
                            if (!person.getInns().isEmpty()) {
                                YINN yinn = person.getInns().iterator().next();
                                writer.write(String.valueOf(yinn.getInn()));
                            }
                            writer.write(TD_CLOSE_HTML);

                            writer.write(TD_CENTER_OPEN_HTML + rowspan + ">");
                            if (personName.length() > 0) {
                                writer.write(personName);
                            }
                            writer.write(TD_CLOSE_HTML);

                            if (!codeList.isEmpty()) {
                                writer.write(TD_OPEN_HTML);
                                StringBuilder tagTypeAsOfBuilder = new StringBuilder(codeList.get(0));
                                if (!tagAsOfDatesMap.get(codeList.get(0)).isBlank()) {
                                    tagTypeAsOfBuilder.append(" з ").append(tagAsOfDatesMap.get(codeList.get(0)));
                                }
                                writer.write(String.valueOf(tagTypeAsOfBuilder));
                                writer.write(TD_CLOSE_HTML);
                            }
                            writer.write(TR_CLOSE_HTML);

                            for (int i = 1; i < codeList.size(); i++) {
                                writer.write(TR_OPEN_HTML);
                                writer.write(TD_OPEN_HTML);
                                StringBuilder tagTypeAsOfBuilder = new StringBuilder(codeList.get(i));
                                if (!tagAsOfDatesMap.get(codeList.get(i)).isBlank()) {
                                    tagTypeAsOfBuilder.append(" з ").append(tagAsOfDatesMap.get(codeList.get(i)));
                                }
                                writer.write(tagTypeAsOfBuilder.toString());
                                writer.write(TD_CLOSE_HTML);
                                writer.write(TR_CLOSE_HTML);
                            }
                        } catch (IOException e) {
                            log.error("[physicalPackageMonitoringReport-c]", e);
                        }
                    });

                    writer.write(TBODY_CLOSE_HTML);
                    writer.write(TABLE_CLOSE_HTML);
                }
            }
            writer.write(BODY_CLOSE_HTML);
            writer.write(HTML_CLOSE_HTML);
        } catch (IOException e) {
            log.error("[physicalPackageMonitoringReport-b]", e);
        }
        return built;
    }

    private void juridicalPackageMonitoringReport() {
        List<NotificationJuridicalTagMatching> matchings = juridicalTagMatchingRepository.findAll();

        matchings.forEach(matching -> {

            Stream<YCompanyPackageMonitoringNotification> companyPackageMonitoringNotifications =
                    companyPackageMonitoringNotificationRepository.findByEmailAndSent(matching.getEmail(), false);

            Map<NotificationJuridicalTagCondition, List<YCompany>> conditionMap = new LinkedHashMap<>();

            companyPackageMonitoringNotifications.forEach(notification -> {
                YCompany yCompany = companyRepository.findWithTagsById(notification.getYcompanyId())
                        .orElse(null);

                conditionMap.computeIfAbsent(notification.getCondition(), k -> new ArrayList<>());
                if (yCompany != null) conditionMap.get(notification.getCondition()).add(yCompany);
            });

            StringBuilder messageBuilder = new StringBuilder();
            boolean built = false;
            messageBuilder.append(FILE_HEADER);

            for (Map.Entry<NotificationJuridicalTagCondition, List<YCompany>> entry : conditionMap.entrySet()) {
                NotificationJuridicalTagCondition condition = entry.getKey();
                List<YCompany> companyList = entry.getValue();

                if (!companyList.isEmpty()) {
                    built = true;
                    List<String> codeList = condition.getTagTypes().stream()
                            .map(TagType::getCode)
                            .collect(Collectors.toList());
                    StringBuilder codesInString = new StringBuilder(condition.getDescription());
                    codesInString.append(" [");
                    codesInString.append(codeList.get(0));
                    for (int i = 1; i < codeList.size(); i++) {
                        codesInString.append(" &amp; ").append(codeList.get(i));
                    }
                    codesInString.append("]");

                    String rowspan = codeList.size() > 1 ? " rowspan=\"" + codeList.size() + "\"" : "";

                    messageBuilder.append(tableCaption(codesInString.toString(), Entity.COMPANY));

                    Map<YCompany, String> companyNamesMap = new HashMap<>();
                    companyList.forEach(company -> companyNamesMap.put(company, StringUtils.defaultString(company.getName(), "")));
                    Stream<Map.Entry<YCompany, String>> companyNamesStreamSorted = companyNamesMap.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByValue());

                    companyNamesStreamSorted.forEach(entryCompanyName -> {
                        YCompany company = entryCompanyName.getKey();
                        String companyName = entryCompanyName.getValue();

                        Map<String, String> tagAsOfDatesMap = new HashMap<>();
                        codeList.forEach(code -> {
                            List<String> tagAsOfDatesList = new ArrayList<>();
                            List<YCTag> collect = company.getTags()
                                    .stream()
                                    .filter(tag -> tag.getTagType().getCode().equals(code))
                                    .sorted(Comparator.comparing(YCTag::getAsOf))
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

                        messageBuilder.append(TR_OPEN_HTML);
                        messageBuilder.append(TD_CENTER_OPEN_HTML).append(rowspan).append(">");
                        if (company.getEdrpou() != null) {
                            messageBuilder.append(company.getEdrpou());
                        }
                        messageBuilder.append(TD_CLOSE_HTML);

                        messageBuilder.append(TD_CENTER_OPEN_HTML).append(rowspan).append(">");
                        if (companyName.length() > 0) {
                            messageBuilder.append(companyName);
                        }
                        messageBuilder.append(TD_CLOSE_HTML);

                        if (!codeList.isEmpty()) {
                            messageBuilder.append(TD_OPEN_HTML);
                            StringBuilder tagTypeAsOfBuilder = new StringBuilder(codeList.get(0));
                            if (!tagAsOfDatesMap.get(codeList.get(0)).isBlank()) {
                                tagTypeAsOfBuilder.append(" з ").append(tagAsOfDatesMap.get(codeList.get(0)));
                            }
                            messageBuilder.append(tagTypeAsOfBuilder);
                            messageBuilder.append(TD_CLOSE_HTML);
                        }
                        messageBuilder.append(TR_CLOSE_HTML);

                        for (int i = 1; i < codeList.size(); i++) {
                            messageBuilder.append(TR_OPEN_HTML);

                            messageBuilder.append(TD_OPEN_HTML);
                            StringBuilder tagTypeAsOfBuilder = new StringBuilder(codeList.get(i));
                            if (!tagAsOfDatesMap.get(codeList.get(i)).isBlank()) {
                                tagTypeAsOfBuilder.append(" з ").append(tagAsOfDatesMap.get(codeList.get(i)));
                            }
                            messageBuilder.append(tagTypeAsOfBuilder);
                            messageBuilder.append(TD_CLOSE_HTML);

                            messageBuilder.append(TR_CLOSE_HTML);
                        }

                    });

                    messageBuilder.append(TBODY_CLOSE_HTML);
                    messageBuilder.append(TABLE_CLOSE_HTML);

                }
            }
            messageBuilder.append(BODY_CLOSE_HTML);
            messageBuilder.append(HTML_CLOSE_HTML);

            if (built) {
                SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                        .to(matching.getEmail())
                        .subject(JURIDICAL_MESSAGE_SUBJ)
                        .body(messageBuilder.toString())
                        .retries(2)
                        .build();

                String jo;
                try {
                    jo = new ObjectMapper().writeValueAsString(sendEmailRequest);
                    log.info(SENDING_LOG, notificationQueue);
                    template.convertAndSend(notificationQueue, jo);
                } catch (JsonProcessingException e) {
                    log.error(COULD_NOT_CONVERT_LOG, e.getMessage());
                }

//                companyPackageMonitoringNotifications
//                        .forEach(personPackageMonitoringNotification -> personPackageMonitoringNotification.setSent(true));
//                if (!companyPackageMonitoringNotifications.isEmpty())
//                    companyPackageMonitoringNotificationRepository.saveAll(companyPackageMonitoringNotifications);
            }
        });
    }

    private String tableCaption(String caption, Entity entity) {
        return "<table style=\"border-collapse:collapse;border:2px solid rgb(200, 200, 200);letter-spacing:1px;font-family:sans-serif;font-size:.8rem;\">" +
                "<caption style=\"padding:10px;caption-side:top;font-weight:bold;\">" + caption + "</caption>" +
                "<thead style=\"background-color:#3f87a6;color:#fff;\">" +
                "<tr>" +
                "<th scope=\"col\" style=\"border:1px solid rgb(190, 190, 190);padding:5px 10px;\">" + entity.identifier + "</th>" +
                "<th scope=\"col\" style=\"border:1px solid rgb(190, 190, 190);padding:5px 10px;\">" + entity.name + "</th>" +
                "<th scope=\"col\" style=\"border:1px solid rgb(190, 190, 190);padding:5px 10px;\">Мітки</th>" +
                TR_CLOSE_HTML +
                "</thead>" +
                "<tbody style=\"background-color:#e4f0f5;\">";
    }

    private enum Entity {

        PERSON("ІНН", "Прізвище, Ім'я, по-батькові"),
        COMPANY("ЄДРПОУ", "Назва компанії");

        private final String identifier;
        private final String name;

        Entity(String identifier, String name) {
            this.identifier = identifier;
            this.name = name;
        }
    }
}
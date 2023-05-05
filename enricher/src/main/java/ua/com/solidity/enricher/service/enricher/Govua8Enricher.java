package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.GOVUA8;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_INFO_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.TAG_TYPE_NBW;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.com.solidity.common.DefaultErrorLogger;
import ua.com.solidity.common.StatusChanger;
import ua.com.solidity.common.UtilString;
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.Govua8;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.repository.Govua8Repository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class Govua8Enricher implements Enricher {
    private static final String SOURCE_NAME = "govua8";
    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final Govua8Repository govua8Repository;
    private final YPersonRepository ypr;
    private final MonitoringNotificationService emnService;

    private final TagTypeRepository tagTypeRepository;
    private final ImportSourceRepository isr;
    private final HttpClient httpClient;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${dispatcher.url}")
    private String urlPost;
    @Value("${dispatcher.url.delete}")
    private String urlDelete;
    private List<EntityProcessing> resp = new ArrayList<>();

    @SneakyThrows
    @Override
    public void enrich(UUID portion) {

        logStart(GOVUA8);

        StatusChanger statusChanger = new StatusChanger(portion, GOVUA8, ENRICHER);

        long[] counter = new long[1];

        UUID newPortion = UUID.randomUUID();

        try {
            Pageable pageRequest = PageRequest.of(0, pageSize);
            Page<Govua8> onePage = govua8Repository.findAllByPortionId(portion, pageRequest);
            if (onePage.isEmpty()) return;
            long count = govua8Repository.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, GOVUA8, portion));

            ImportSource source = isr.findImportSourceByName(SOURCE_NAME);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();

                List<EntityProcessing> entityProcessings = onePage.stream().map(p -> {
                    EntityProcessing entityProcessing = new EntityProcessing();
                    entityProcessing.setUuid(p.getId());
                    entityProcessing.setInn(Long.parseLong(p.getCode()));
                    entityProcessing.setPersonHash(Objects.hash(UtilString.toUpperCase(p.getLastNameUa()),
                            UtilString.toUpperCase(p.getFirstNameUa()), UtilString.toUpperCase(p.getMiddleNameUa())));
                    return entityProcessing;
                }).collect(Collectors.toList());

                UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                log.info("Passing {}, count: {}", portion, entityProcessings.size());
                String url = urlPost + "?id=" + portion;
                DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                resp = new ArrayList<>(response.getResp());
                List<UUID> respId = response.getRespId();

                if (respId.isEmpty()) {
                    extender.sendMessageToQueue(GOVUA8, portion);
                    statusChanger.newStage(null, "All data is being processed. Portions sent to the queue.", count, null);
                    return;
                }

                log.info(ENRICHER_INFO_MESSAGE, resp.size());
                statusChanger.setStatus(Utils.messageFormat(ENRICHER_INFO_MESSAGE, resp.size()));

                List<Govua8> temp = new ArrayList<>();
                List<Govua8> finalWorkPortion = new ArrayList<>();
                onePage.stream().forEach(p -> {
                    if (respId.contains(p.getId())) finalWorkPortion.add(p);
                    else {
                        p.setPortionId(newPortion);
                        temp.add(p);
                    }
                });

                List<Govua8> workPortion = finalWorkPortion.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());

                Set<YPerson> people = new HashSet<>();
                Optional<TagType> tagType = tagTypeRepository.findByCode(TAG_TYPE_NBW);

                workPortion.forEach(r -> {
                    String lastName = UtilString.toUpperCase(r.getLastNameUa());
                    String firstName = UtilString.toUpperCase(r.getFirstNameUa());
                    String patName = UtilString.toUpperCase(r.getMiddleNameUa());
                    String lastNameRu = UtilString.toUpperCase(r.getLastNameRu());
                    String firstNameRu = UtilString.toUpperCase(r.getFirstNameRu());
                    String patNameRu = UtilString.toUpperCase(r.getMiddleNameRu());
                    String lastNameEn = UtilString.toUpperCase(r.getLastNameEn());
                    String firstNameEn = UtilString.toUpperCase(r.getFirstNameEn());
                    String patNameEn = UtilString.toUpperCase(r.getMiddleNameEn());
                    LocalDate birthDay = r.getBirthDay();
                    String sex = r.getSex().equals("ЧОЛОВІЧА") ? "Ч" : "Ж";


                    YPerson person = new YPerson();
                    person.setLastName(lastName);
                    person.setFirstName(firstName);
                    person.setPatName(patName);
                    person.setBirthdate(birthDay);
                    person.setSex(sex);

                    YTag tag = new YTag();
                    tagType.ifPresent(tag::setTagType);
                    tag.setEventDate(r.getLostDate());
                    tag.setTextValue(r.getArticleCrim());
                    tag.setDescription(r.getRestraint());
                    tag.setSource(GOVUA8);
                    tag.setUntil(LocalDate.of(3500, 1, 1));

                    if (people.stream().noneMatch(p -> Objects.equals(lastName, p.getLastName())
                            && Objects.equals(firstName, p.getFirstName()) && Objects.equals(patName, p.getPatName())
                            && Objects.equals(birthDay, p.getBirthdate()))) {

                        Set<YPerson> savedPersonSet = new HashSet<>();
                        if (!lastName.isBlank() && !firstName.isBlank())
                            savedPersonSet.addAll(ypr.findByLastNameAndFirstNameAndPatName(lastName, firstName, patName));
                        else if (!lastNameRu.isBlank() && !firstNameRu.isBlank())
                            savedPersonSet.addAll(ypr.findByAltPeople(lastNameRu, firstNameRu, patNameRu));

                        Set<YPerson> peopleWithEqualBirthday = new HashSet<>();
                        Set<YPerson> peopleWithoutBirthday = new HashSet<>();
                        savedPersonSet.forEach(savedPerson -> {
                            if (birthDay.equals(savedPerson.getBirthdate())) peopleWithEqualBirthday.add(savedPerson);
                            else if (savedPerson.getBirthdate() == null) peopleWithoutBirthday.add(savedPerson);
                        });

                        if (peopleWithEqualBirthday.isEmpty() && peopleWithoutBirthday.size() == 1) {
                            peopleWithoutBirthday.forEach(findPerson -> findPerson.setBirthdate(birthDay));
                            peopleWithEqualBirthday.addAll(peopleWithoutBirthday);
                        }

                        if (peopleWithEqualBirthday.isEmpty()) {
                            person.setId(UUID.randomUUID());
                            if (StringUtils.isNotBlank(lastNameEn) || StringUtils.isNotBlank(firstNameEn) || StringUtils.isNotBlank(patNameEn))
                                extender.addAltPerson(person, lastNameEn, firstNameEn, patNameEn, "EN", source);

                            if (StringUtils.isNotBlank(lastNameRu) || StringUtils.isNotBlank(firstNameRu) || StringUtils.isNotBlank(patNameRu))
                                extender.addAltPerson(person, lastNameRu, firstNameRu, patNameRu, "RU", source);

                            Set<YTag> tags = new HashSet<>();
                            tags.add(tag);
                            extender.addTags(person, tags, source);

                            people.add(person);
                        } else {
                            peopleWithEqualBirthday.forEach(findPerson -> {
                                findPerson.setSex(sex);
                                if (StringUtils.isNotBlank(lastNameRu) || StringUtils.isNotBlank(firstNameRu) || StringUtils.isNotBlank(patNameRu))
                                    extender.addAltPerson(findPerson, lastNameRu, firstNameRu, patNameRu, "RU", source);

                                if (StringUtils.isNotBlank(lastNameEn) || StringUtils.isNotBlank(firstNameEn) || StringUtils.isNotBlank(patNameEn))
                                    extender.addAltPerson(findPerson, lastNameEn, firstNameEn, patNameEn, "EN", source);

                                Set<YTag> tags = new HashSet<>();
                                tags.add(tag);
                                extender.addTags(findPerson, tags, source);

                                people.add(findPerson);
                            });
                        }
                    }
                    counter[0]++;
                    statusChanger.addProcessedVolume(1);
                });

                UUID dispatcherIdFinish = httpClient.get(urlPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {

                    emnService.enrichYPersonPackageMonitoringNotification(people);

                    if (!people.isEmpty()) {
                        log.info("Saving people");
                        ypr.saveAll(people);
                        emnService.enrichYPersonMonitoringNotification(people);
                        statusChanger.setStatus(Utils.messageFormat("Enriched {} rows", statusChanger.getProcessedVolume()));
                    }

                    deleteResp();
                } else {
                    counter[0] = 0L;
                    statusChanger.newStage(null, "Restoring from dispatcher restart", count, null);
                    statusChanger.addProcessedVolume(0);
                }

                onePage = govua8Repository.findAllByPortionId(portion, pageRequest);

                if (!temp.isEmpty()) {
                    govua8Repository.saveAll(temp);
                    extender.sendMessageToQueue(GOVUA8, newPortion);
                    log.info("Send message with uuid: {}, count: {}", newPortion, temp.size());
                }

                logFinish(GOVUA8, counter[0]);
                logger.finish();

                statusChanger.complete(importedRecords(counter[0]));
            }
        } catch (Exception e) {
            statusChanger.error(Utils.messageFormat("ERROR: {}", Utils.getExceptionString(e, ";")));
            log.error("$$Enrichment error.", e);
            extender.sendMessageToQueue(GOVUA8, portion);
        } finally {
            deleteResp();
        }
    }

    @Override
    @PreDestroy
    public void deleteResp() {
        if (!resp.isEmpty()) {
            log.info("Going to remove, count: {}", resp.size());
            httpClient.post(urlDelete, Boolean.class, resp);
            resp.clear();
            log.info("Removed");
        }
    }
}

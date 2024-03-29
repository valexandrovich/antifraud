package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.GOVUA18;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_INFO_MESSAGE;
import static ua.com.solidity.util.validator.Validator.isAllZeroChar;
import static ua.com.solidity.util.validator.Validator.isValidInn;

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
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.com.solidity.common.DefaultErrorLogger;
import ua.com.solidity.common.StatusChanger;
import ua.com.solidity.common.UtilString;
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.Govua18;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.repository.Govua18Repository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class Govua18Enricher implements Enricher {
    private static final String SOURCE_NAME = "govua18";
    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final HttpClient httpClient;
    private final Govua18Repository govua18Repository;
    private final YINNRepository yinnRepository;
    private final YPersonRepository ypr;
    private final TagTypeRepository tagTypeRepository;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${dispatcher.url}")
    private String urlPost;
    @Value("${dispatcher.url.delete}")
    private String urlDelete;
    private List<EntityProcessing> resp = new ArrayList<>();

    @Override
    public void enrich(UUID portion) {
        logStart(GOVUA18);

        StatusChanger statusChanger = new StatusChanger(portion, GOVUA18, ENRICHER);

        long[] counter = new long[1];

        UUID newPortion = UUID.randomUUID();

        try {
            Pageable pageRequest = PageRequest.of(0, pageSize);
            Page<Govua18> onePage = govua18Repository.findAllByPortionId(portion, pageRequest);
            if (onePage.isEmpty()) return;
            long count = govua18Repository.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, GOVUA18, portion));

            ImportSource source = isr.findImportSourceByName(SOURCE_NAME);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();

                List<EntityProcessing> entityProcessings = onePage.stream().map(c -> {
                    EntityProcessing entityProcessing = new EntityProcessing();
                    entityProcessing.setUuid(c.getId());
                    if (UtilString.matches(c.getPdv(), CONTAINS_NUMERAL_REGEX)) {
                        String inn = c.getPdv().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        entityProcessing.setInn(Long.parseLong(inn));
                    }
                    return entityProcessing;
                }).collect(Collectors.toList());

                UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                log.info("Passing {}, count: {}", portion, entityProcessings.size());
                String url = urlPost + "?id=" + portion;
                DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                resp = new ArrayList<>(response.getResp());
                List<UUID> respId = response.getRespId();

                if (respId.isEmpty()) {
                    extender.sendMessageToQueue(GOVUA18, portion);
                    statusChanger.newStage(null, "All data is being processed. Portions sent to the queue.", count, null);
                    return;
                }

                log.info(ENRICHER_INFO_MESSAGE, resp.size());
                statusChanger.setStatus(Utils.messageFormat(ENRICHER_INFO_MESSAGE, resp.size()));

                List<Govua18> finalWorkPortion = new ArrayList<>();
                List<Govua18> temp = new ArrayList<>();
                onePage.stream().parallel().forEach(p -> {
                    if (respId.contains(p.getId())) finalWorkPortion.add(p);
                    else {
                        p.setPortionId(newPortion);
                        temp.add(p);
                    }
                });

                List<Govua18> workPortion = finalWorkPortion.parallelStream()
                        .filter(govua -> govua != null && !isAllZeroChar(govua.getPdv())).collect(Collectors.toList());

                Set<Long> codes = new HashSet<>();
                Set<YINN> inns = new HashSet<>();
                Set<YPerson> people = new HashSet<>();

                Set<YPerson> savedPeople = new HashSet<>();
                workPortion.forEach(r -> {
                    if (UtilString.matches(r.getPdv(), CONTAINS_NUMERAL_REGEX)) {
                        String inn = r.getPdv().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        codes.add(Long.parseLong(inn));
                    }
                });

                if (!codes.isEmpty()) {
                    inns.addAll(yinnRepository.findInns(codes));
                    savedPeople.addAll(ypr.findPeopleWithInns(codes));
                }

                workPortion.forEach(r -> {

                    if (UtilString.matches(r.getPdv(), CONTAINS_NUMERAL_REGEX)) {
                        String inn = r.getPdv().replaceAll(ALL_NOT_NUMBER_REGEX, "");

                        if (isValidInn(inn, null, null) && r.getNameAnul().contains("смерть")) {
                            YPerson person = new YPerson();
                            String[] fio = null;
                            String firstName = "";
                            String lastName = "";
                            String patName = "";
                            if (!StringUtils.isBlank(r.getName())) fio = r.getName().split(" ");
                            if (fio != null && fio.length >= 1) lastName = UtilString.toUpperCase(fio[0]);
                            if (fio != null && fio.length >= 2) firstName = UtilString.toUpperCase(fio[1]);
                            if (fio != null && fio.length >= 3) patName = UtilString.toUpperCase(fio[2]);
                            person.setFirstName(firstName);
                            person.setLastName(lastName);
                            person.setPatName(patName);

                            person = extender.addInn(Long.parseLong(inn), people, source, person, inns, savedPeople);

                            person = extender.addPerson(people, person, source, false);

                            Optional<TagType> tagType = tagTypeRepository.findByCode("NBDA");

                            if (tagType.isPresent()) {
                                Set<YTag> tagSet = new HashSet<>();
                                YTag yTag = new YTag();
                                yTag.setEventDate(r.getDateAnul());
                                yTag.setTagType(tagType.get());
                                yTag.setSource(GOVUA18);
                                yTag.setUntil(LocalDate.of(3500, 1, 1));
                                tagSet.add(yTag);
                                extender.addTags(person, tagSet, source);
                            }
                        } else if (r.getNameAnul().contains("смерть")) {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getPdv()), "Wrong INN");
                        }
                    }
                    counter[0]++;
                    statusChanger.addProcessedVolume(1);
                });
                UUID dispatcherIdFinish = httpClient.get(urlPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {

                    if (!people.isEmpty()) {
                        ypr.saveAll(people);
                        log.info("Saving people");
                        emnService.enrichYPersonMonitoringNotification(people);
                        statusChanger.setStatus(Utils.messageFormat("Enriched {} rows", statusChanger.getProcessedVolume()));
                    }

                    deleteResp();
                } else {
                    counter[0] = 0L;
                    statusChanger.newStage(null, "Restoring from dispatcher restart", count, null);
                    statusChanger.addProcessedVolume(0);
                }

                onePage = govua18Repository.findAllByPortionId(portion, pageRequest);

                if (!temp.isEmpty()) {
                    govua18Repository.saveAll(temp);
                    extender.sendMessageToQueue(GOVUA18, newPortion);
                    log.info("Send message with uuid: {}, count: {}", newPortion, temp.size());
                }

                logFinish(GOVUA18, counter[0]);
                logger.finish();

                statusChanger.complete(importedRecords(counter[0]));
            }
        } catch (Exception e) {
            statusChanger.error(Utils.messageFormat("ERROR: {}", Utils.getExceptionString(e, ";")));
            log.error("$$Enrichment error.", e);
            extender.sendMessageToQueue(GOVUA18, portion);
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

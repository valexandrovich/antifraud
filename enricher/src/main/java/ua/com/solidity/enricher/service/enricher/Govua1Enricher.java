package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.GOVUA1;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.util.validator.Validator.isValidEdrpou;
import static ua.com.solidity.util.validator.Validator.isValidInn;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
import ua.com.solidity.db.entities.Govua1;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.YCTag;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.repository.Govua1Repository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class Govua1Enricher implements Enricher {

    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final YPersonRepository ypr;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final Govua1Repository govua1Repository;
    private final TagTypeRepository tagTypeRepository;
    private final YINNRepository yinnRepository;
    private final YCompanyRepository companyRepository;
    private final HttpClient httpClient;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${enricher.timeOutTime}")
    private Integer timeOutTime;
    @Value("${enricher.sleepTime}")
    private int sleepTime;
    @Value("${dispatcher.url}")
    private String urlPost;
    @Value("${dispatcher.url.delete}")
    private String urlDelete;
    private List<EntityProcessing> resp = new ArrayList<>();

    @SneakyThrows
    @Override
    public void enrich(UUID portion) {
        LocalDateTime startTime = LocalDateTime.now();

        logStart(GOVUA1);

        StatusChanger statusChanger = new StatusChanger(portion, GOVUA1, ENRICHER);

        long[] counter = new long[1];

        try {
            Pageable pageRequest = PageRequest.of(0, pageSize);
            Page<Govua1> onePage = govua1Repository.findAllByPortionId(portion, pageRequest);
            long count = govua1Repository.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, GOVUA1, portion));

            ImportSource source = isr.findImportSourceByName(GOVUA1);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();
                List<Govua1> page = onePage.toList();

                while (!page.isEmpty()) {
                    Duration duration = Duration.between(startTime, LocalDateTime.now());
                    if (duration.getSeconds() > timeOutTime) {
                        statusChanger.setStatus(" Timeout after 25 minutes. Task has been rescheduled.");
                        extender.sendMessageToQueue(GOVUA1, portion);

                        throw new TimeoutException("Time ran out for portion: " + portion);
                    }
                    List<EntityProcessing> entityProcessings = page.parallelStream().map(p -> {
                        EntityProcessing entityProcessing = new EntityProcessing();
                        entityProcessing.setUuid(p.getId());
                        if (StringUtils.isNotBlank(p.getEdrpou()) && p.getEdrpou().matches(CONTAINS_NUMERAL_REGEX)) {
                            String code = p.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                            entityProcessing.setInn(Long.parseLong(code));
                            entityProcessing.setEdrpou(Long.parseLong(code));
                        }
                        if (StringUtils.isNotBlank(p.getName()))
                            entityProcessing.setCompanyHash(Objects.hashCode(UtilString.toUpperCase(p.getName().trim())));
                        return entityProcessing;
                    }).collect(Collectors.toList());

                    UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                    log.info("Passing {}, count: {}", portion, entityProcessings.size());
                    String url = urlPost + "?id=" + portion;
                    DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                    resp = new ArrayList<>(response.getResp());
                    List<UUID> respId = response.getRespId();
                    List<UUID> temp = response.getTemp();
                    log.info("To be processed: {}, waiting: {}", resp.size(), temp.size());
                    statusChanger.setStatus(Utils.messageFormat("Enriched: {}, to be processed: {}, waiting: {}", statusChanger.getProcessedVolume(), resp.size(), temp.size()));

                    List<Govua1> workPortion = page.parallelStream().filter(p -> respId.contains(p.getId()))
                            .collect(Collectors.toList());

                    if (workPortion.isEmpty()) Utils.waitMs(sleepTime);

                    Set<Long> codes = new HashSet<>();
                    Set<YINN> inns = new HashSet<>();
                    Set<YPerson> people = new HashSet<>();
                    Set<YCompany> companies = new HashSet<>();
                    Set<YPerson> savedPersonSet = new HashSet<>();

                    Set<YCompany> savedCompanies = new HashSet<>();

                    workPortion.forEach(r -> {
                        if (StringUtils.isNotBlank(r.getEdrpou()) && r.getEdrpou().matches(CONTAINS_NUMERAL_REGEX)) {
                            String code = r.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                            codes.add(Long.parseLong(code));
                        }
                    });

                    if (!codes.isEmpty()) {
                        inns.addAll(yinnRepository.findInns(codes));
                        savedPersonSet.addAll(ypr.findPeopleWithInns(codes));
                        savedCompanies.addAll(companyRepository.findByEdrpous(codes));
                    }

                    workPortion.forEach(r -> {

                        if (StringUtils.isNotBlank(r.getEdrpou()) && r.getEdrpou().matches(CONTAINS_NUMERAL_REGEX)) {
                            String code = r.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");

                            if (isValidEdrpou(code)) {
                                YCompany company = new YCompany();
                                company.setEdrpou(Long.parseLong(code));
                                company.setName(UtilString.toUpperCase(r.getName()));
                                company = extender.addCompany(companies, source, company, savedCompanies);
                                if (StringUtils.isNotBlank(r.getCaseNumber()) && StringUtils.isNotBlank(r.getRecordType())) {
                                    Optional<TagType> tagType = tagTypeRepository.findByCode("NBB2");

                                    if (tagType.isPresent()) {
                                        Set<YCTag> tagSet = new HashSet<>();
                                        YCTag ycTag = new YCTag();
                                        ycTag.setAsOf(r.getRecordDate());
                                        ycTag.setTagType(tagType.get());
                                        ycTag.setSource(GOVUA1);
                                        ycTag.setUntil(LocalDate.of(3500, 1, 1));
                                        tagSet.add(ycTag);
                                        extender.addTags(company, tagSet, source);
                                    }
                                }
                                counter[0]++;
                                statusChanger.addProcessedVolume(1);
                            } else if (isValidInn(code, null)) {
                                YPerson person = new YPerson();

                                String lastName = null;
                                String firstName = null;
                                String patName = null;

                                List<String> splitedList;
                                String[] fio = new String[3];
                                Integer commaIndex = null;

                                if (StringUtils.isNotBlank(r.getName())) {
                                    splitedList = Arrays.stream(r.getName().split("[ .]+"))
                                            .filter(s -> StringUtils.isNotBlank(s) && !s.contains("»") && !s.contains("«"))
                                            .collect(Collectors.toList());
                                    int splitedSize = splitedList.size();

                                    if (splitedList.size() > 3) {
                                        for (int i = 0; i < splitedSize; i++) {
                                            if (splitedList.get(i).contains(",")) {
                                                splitedList.add(i, splitedList.get(i).replaceAll(",", ""));
                                                commaIndex = i;
                                                break;
                                            }
                                        }

                                        if (commaIndex != null && commaIndex >= 2) {
                                            fio[0] = splitedList.get(commaIndex - 2);
                                            fio[1] = splitedList.get(commaIndex - 1);
                                            fio[2] = splitedList.get(commaIndex);
                                        } else {
                                            fio[0] = splitedList.get(splitedSize - 3);
                                            fio[1] = splitedList.get(splitedSize - 2);
                                            fio[2] = splitedList.get(splitedSize - 1);
                                        }
                                    } else {
                                        for (int i = 0; i < splitedSize; i++) {
                                            fio[i] = splitedList.get(i);
                                        }
                                    }
                                    lastName = UtilString.toUpperCase(fio[0]);
                                    firstName = UtilString.toUpperCase(fio[1]);
                                    patName = UtilString.toUpperCase(fio[2]);
                                }
                                person.setLastName(lastName);
                                person.setFirstName(firstName);
                                person.setPatName(patName);

                                person = extender.addInn(Long.parseLong(code), people, source, person, inns, savedPersonSet);
                                person = extender.addPerson(people, person, source, true);

                                Optional<TagType> tagType = tagTypeRepository.findByCode("NBB2");

                                if (tagType.isPresent()) {
                                    Set<YTag> tagSet = new HashSet<>();
                                    YTag yTag = new YTag();
                                    yTag.setAsOf(r.getRecordDate());
                                    yTag.setTagType(tagType.get());
                                    yTag.setSource(GOVUA1);
                                    yTag.setUntil(LocalDate.of(3500, 1, 1));
                                    tagSet.add(yTag);
                                    extender.addTags(person, tagSet, source);
                                }
                                counter[0]++;
                                statusChanger.addProcessedVolume(1);
                            } else {
                                logError(logger, (counter[0] + 1L), Utils.messageFormat("EDRPOU: {}", r.getEdrpou()), "Wrong EDRPOU");
                            }
                        }
                    });

                    UUID dispatcherIdFinish = httpClient.get(urlPost, UUID.class);
                    if (Objects.equals(dispatcherId, dispatcherIdFinish)) {

                        if (!people.isEmpty()) {
                            emnService.enrichYPersonPackageMonitoringNotification(people);
                            log.info("Saving people");
                            ypr.saveAll(people);
                            emnService.enrichYPersonMonitoringNotification(people);
                            statusChanger.setStatus(Utils.messageFormat("Enriched {} rows", statusChanger.getProcessedVolume()));
                        }

                        if (!companies.isEmpty()) {
                            emnService.enrichYCompanyPackageMonitoringNotification(companies);
                            log.info("Saving companies");
                            companyRepository.saveAll(companies);
                            emnService.enrichYCompanyMonitoringNotification(companies);
                        }

                        deleteResp();

                        page = page.parallelStream().filter(p -> temp.contains(p.getId())).collect(Collectors.toList());
                    } else {
                        counter[0] -= resp.size();
                        statusChanger.newStage(null, "Restoring from dispatcher restart", count, null);
                        statusChanger.addProcessedVolume(-resp.size());
                    }
                }

                onePage = govua1Repository.findAllByPortionId(portion, pageRequest);
            }

            logFinish(GOVUA1, counter[0]);
            logger.finish();

            statusChanger.complete(importedRecords(counter[0]));
        } catch (Exception e) {
            statusChanger.error(Utils.messageFormat("ERROR: {}", e.getMessage()));
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

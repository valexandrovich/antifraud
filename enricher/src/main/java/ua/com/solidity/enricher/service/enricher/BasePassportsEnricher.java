package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.service.validator.Validator.isValidLocalPassport;
import static ua.com.solidity.enricher.util.Base.BASE_PASSPORTS;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringFormatUtil.transliterationToCyrillicLetters;
import static ua.com.solidity.enricher.util.StringStorage.DOMESTIC_PASSPORT;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_INFO_MESSAGE;
import static ua.com.solidity.util.validator.Validator.isValidInn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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
import ua.com.solidity.db.entities.BasePassports;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPassportRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.repository.BasePassportsRepository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class BasePassportsEnricher implements Enricher {

    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final BasePassportsRepository bpr;
    private final YPersonRepository ypr;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final YPassportRepository passportRepository;
    private final YINNRepository yinnRepository;
    private final HttpClient httpClient;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
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

        logStart(BASE_PASSPORTS);

        StatusChanger statusChanger = new StatusChanger(portion, BASE_PASSPORTS, ENRICHER);

        long[] counter = new long[1];

        UUID newPortion = UUID.randomUUID();

        try {
            Pageable pageRequest = PageRequest.of(0, pageSize);
            Page<BasePassports> onePage = bpr.findAllByPortionId(portion, pageRequest);
            if (onePage.isEmpty()) return;
            long count = bpr.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_PASSPORTS, portion));

            ImportSource source = isr.findImportSourceByName(BASE_PASSPORTS);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();

                List<EntityProcessing> entityProcessings = onePage.stream().parallel().map(p -> {
                    EntityProcessing entityProcessing = new EntityProcessing();
                    entityProcessing.setUuid(p.getId());
                    if (!StringUtils.isBlank(p.getInn()) && p.getInn().matches(CONTAINS_NUMERAL_REGEX)) {
                        String inn = p.getInn().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        entityProcessing.setInn(Long.parseLong(inn));
                    }
                    if (!StringUtils.isBlank(p.getPassId()) && p.getPassId().matches(CONTAINS_NUMERAL_REGEX)) {
                        String passportNo = String.format("%06d", Integer.parseInt(p.getPassId().replaceAll(ALL_NOT_NUMBER_REGEX, "")));
                        String passportSerial = p.getSerial();
                        entityProcessing.setPassHash(Objects.hash(transliterationToCyrillicLetters(passportSerial), Integer.valueOf(passportNo)));
                    }
                    entityProcessing.setPersonHash(Objects.hash(UtilString.toUpperCase(p.getLastName()), UtilString.toUpperCase(p.getFirstName()), UtilString.toUpperCase(p.getMiddleName()),
                            p.getBirthdate()));
                    return entityProcessing;
                }).collect(Collectors.toList());

                UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                log.info("Passing {}, count: {}", portion, entityProcessings.size());
                String url = urlPost + "?id=" + portion;
                DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                resp = new ArrayList<>(response.getResp());
                List<UUID> respId = response.getRespId();

                if (respId.isEmpty()) {
                    extender.sendMessageToQueue(BASE_PASSPORTS, portion);
                    statusChanger.newStage(null, "All data is being processed. Portions sent to the queue.", count, null);
                    return;
                }

                log.info(ENRICHER_INFO_MESSAGE, resp.size());
                statusChanger.setStatus(Utils.messageFormat(ENRICHER_INFO_MESSAGE, resp.size()));

                List<BasePassports> finalWorkPortion = new ArrayList<>();
                List<BasePassports> temp = new ArrayList<>();
                onePage.stream().parallel().forEach(p -> {
                    if (respId.contains(p.getId())) finalWorkPortion.add(p);
                    else {
                        p.setPortionId(newPortion);
                        temp.add(p);
                    }
                });

                List<BasePassports> workPortion = finalWorkPortion.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());

                Set<Long> codes = new HashSet<>();
                Set<YPassport> passportSeriesWithNumber = new HashSet<>();
                Set<YPerson> people = new HashSet<>();

                Set<YINN> inns = new HashSet<>();
                Set<YPassport> passports = new HashSet<>();
                Set<YPerson> savedPersonSet = new HashSet<>();

                workPortion.forEach(r -> {
                    if (!StringUtils.isBlank(r.getPassId()) && r.getPassId().matches(CONTAINS_NUMERAL_REGEX)) {
                        String passportNo = String.format("%06d", Integer.parseInt(r.getPassId().replaceAll(ALL_NOT_NUMBER_REGEX, "")));
                        String passportSerial = r.getSerial();
                        YPassport pass = new YPassport();
                        pass.setNumber(Integer.valueOf(passportNo));
                        pass.setSeries(passportSerial);
                        passportSeriesWithNumber.add(pass);
                    }
                    if (!StringUtils.isBlank(r.getInn()) && r.getInn().matches(CONTAINS_NUMERAL_REGEX)) {
                        String inn = r.getInn().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        codes.add(Long.valueOf(inn));
                    }
                });

                if (!codes.isEmpty()) {
                    inns.addAll(yinnRepository.findInns(codes));
                    savedPersonSet.addAll(ypr.findPeopleInnsForBaseEnricher(codes));
                }
                if (!passportSeriesWithNumber.isEmpty()) {
                    for (YPassport passport : passportSeriesWithNumber) {
                        List<YPassport> passportList = passportRepository.findPassportsByNumberAndSeries(passport.getNumber(), passport.getSeries());
                        passports.addAll(passportList);
                    }
                }

                if (!passports.isEmpty())
                    savedPersonSet.addAll(ypr.findPeoplePassportsForBaseEnricher(passports.parallelStream().map(YPassport::getId).collect(Collectors.toList())));

                workPortion.forEach(r -> {
                    String lastName = UtilString.toUpperCase(r.getLastName());
                    String firstName = UtilString.toUpperCase(r.getFirstName());
                    String patName = UtilString.toUpperCase(r.getMiddleName());

                    YPerson person = new YPerson();
                    person.setLastName(lastName);
                    person.setFirstName(firstName);
                    person.setPatName(patName);
                    person.setBirthdate(r.getBirthdate());

                    if (!StringUtils.isBlank(r.getInn()) && r.getInn().matches(CONTAINS_NUMERAL_REGEX)) {
                        String inn = r.getInn().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        if (isValidInn(inn, r.getBirthdate(), null)) {
                            person = extender.addInn(Long.parseLong(inn), people, source, person, inns, savedPersonSet);
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getInn()), "Wrong INN");
                        }
                    }

                    if (!StringUtils.isBlank(r.getPassId()) && r.getPassId().matches(CONTAINS_NUMERAL_REGEX)) {
                        String passportNo = String.format("%06d", Integer.parseInt(r.getPassId().replaceAll(ALL_NOT_NUMBER_REGEX, "")));
                        String passportSerial = r.getSerial();
                        if (isValidLocalPassport(passportNo, passportSerial, counter, logger)) {
                            passportSerial = transliterationToCyrillicLetters(passportSerial);
                            int number = Integer.parseInt(passportNo);
                            YPassport passport = new YPassport();
                            passport.setSeries(passportSerial);
                            passport.setNumber(number);
                            passport.setAuthority(null);
                            passport.setIssued(null);
                            passport.setEndDate(null);
                            passport.setRecordNumber(null);
                            passport.setValidity(true);
                            passport.setType(DOMESTIC_PASSPORT);
                            person = extender.addPassport(passport, people, source, person, savedPersonSet, passports);
                        }
                    }
                    extender.addPerson(people, person, source, false);

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

                onePage = bpr.findAllByPortionId(portion, pageRequest);

                if (!temp.isEmpty()) {
                    bpr.saveAll(temp);
                    extender.sendMessageToQueue(BASE_PASSPORTS, newPortion);
                    log.info("Send message with uuid: {}, count: {}", newPortion, temp.size());
                }

                logFinish(BASE_PASSPORTS, counter[0]);
                logger.finish();

                statusChanger.complete(importedRecords(statusChanger.getProcessedVolume()));
            }
        } catch (Exception e) {
            statusChanger.error(Utils.messageFormat("ERROR: {}", Utils.getExceptionString(e, ";")));
            log.error("$$Enrichment error.", e);
            extender.sendMessageToQueue(BASE_PASSPORTS, portion);
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

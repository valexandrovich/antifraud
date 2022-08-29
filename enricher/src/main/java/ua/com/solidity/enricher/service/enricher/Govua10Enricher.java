package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.service.validator.Validator.isValidIdPassport;
import static ua.com.solidity.enricher.service.validator.Validator.isValidLocalPassport;
import static ua.com.solidity.enricher.util.Base.GOVUA10;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.transliterationToCyrillicLetters;
import static ua.com.solidity.enricher.util.StringStorage.DOMESTIC_PASSPORT;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.IDCARD_PASSPORT;
import static ua.com.solidity.enricher.util.StringStorage.TAG_TYPE_NAL;

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
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.com.solidity.common.DefaultErrorLogger;
import ua.com.solidity.common.StatusChanger;
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.Govua10;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YPassportRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.repository.Govua10Repository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class Govua10Enricher implements Enricher {

    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final YPersonRepository ypr;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final Govua10Repository govua10Repository;
    private final YPassportRepository passportRepository;
    private final HttpClient httpClient;
    private final TagTypeRepository tagTypeRepository;

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

        logStart(GOVUA10);

        StatusChanger statusChanger = new StatusChanger(portion, GOVUA10, ENRICHER);

        long[] counter = new long[1];

        UUID newPortion = UUID.randomUUID();

        try {
            Pageable pageRequest = PageRequest.of(0, pageSize);
            log.info("before PageRequest.");
            Page<Govua10> onePage = govua10Repository.findAllByPortionId(portion, pageRequest);
            log.info("after pageReqest : {}", !onePage.isEmpty());
            long count = govua10Repository.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, GOVUA10, portion));

            ImportSource source = isr.findImportSourceByName(GOVUA10);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();

                List<EntityProcessing> entityProcessings = onePage.stream().parallel().map(p -> {
                    EntityProcessing entityProcessing = new EntityProcessing();
                    entityProcessing.setUuid(p.getId());
                    if (StringUtils.isNotBlank(p.getNumber()) && p.getNumber().matches(ALL_NUMBER_REGEX))
                        entityProcessing.setPassHash(Objects.hash(transliterationToCyrillicLetters(p.getSeries()),
                                Long.parseLong(p.getNumber())));
                    return entityProcessing;
                }).collect(Collectors.toList());

                UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                log.info("Passing {}, count: {}", portion, entityProcessings.size());
                String url = urlPost + "?id=" + portion;
                DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                resp = new ArrayList<>(response.getResp());
                List<UUID> respId = response.getRespId();
                log.info("To be processed: {}", resp.size());
                statusChanger.setStatus(Utils.messageFormat("To be processed: {}", resp.size()));


                if (respId.isEmpty()) {
                    extender.sendMessageToQueue(GOVUA10, portion);
                    return;
                }

                List<Govua10> workPortion = new ArrayList<>();
                List<Govua10> temp = new ArrayList<>();
                onePage.stream().parallel().forEach(p -> {
                    if (respId.contains(p.getId())) workPortion.add(p);
                    else {
                        p.setPortionId(newPortion);
                        temp.add(p);
                    }
                });

                Set<YPassport> passports = new HashSet<>();
                Set<YPerson> people = new HashSet<>();
                Set<YPassport> passportSeriesWithNumber = new HashSet<>();

                workPortion.forEach(r -> {
                    if (StringUtils.isNotBlank(r.getNumber()) && r.getNumber().matches(ALL_NUMBER_REGEX)) {
                        YPassport pass = new YPassport();
                        pass.setNumber(Integer.valueOf(r.getNumber()));
                        pass.setSeries(r.getSeries());
                        passportSeriesWithNumber.add(pass);
                    }
                });

                if (!passportSeriesWithNumber.isEmpty()) {
                    for (YPassport passport : passportSeriesWithNumber) {
                        Optional<YPassport> newPass = passportRepository.findPassportsByNumberAndSeries(passport.getNumber(), passport.getSeries());
                        newPass.ifPresent(passports::add);
                    }
                }

                Set<YPerson> savedPersonSet = new HashSet<>();
                if (!passports.isEmpty())
                    savedPersonSet.addAll(new HashSet<>(ypr.findPeoplePassportsForBaseEnricher(passports.parallelStream().map(YPassport::getId).collect(Collectors.toList()))));

                Optional<TagType> tagType = tagTypeRepository.findByCode(TAG_TYPE_NAL);
                workPortion.forEach(r -> {
                    YPerson person = new YPerson();

                    if (StringUtils.isNotBlank(r.getSeries())) {
                        String passportNo = r.getNumber();
                        String passportSerial = r.getSeries();
                        if (isValidLocalPassport(passportNo, passportSerial, counter, logger)) {
                            passportSerial = transliterationToCyrillicLetters(passportSerial);
                            int number = Integer.parseInt(passportNo);
                            YPassport passport = new YPassport();
                            passport.setSeries(passportSerial);
                            passport.setNumber(number);
                            passport.setAuthority(null);
                            passport.setIssued(null);
                            passport.setEndDate(r.getModified());
                            passport.setRecordNumber(null);
                            passport.setType(DOMESTIC_PASSPORT);
                            passport.setValidity(false);
                            person = extender.addPassport(passport, people, source, person, savedPersonSet, passports);
                        }
                    } else {
                        String passportNo = r.getNumber();
                        if (isValidIdPassport(passportNo, null, counter, logger)) {
                            int number = Integer.parseInt(passportNo);
                            YPassport passport = new YPassport();
                            passport.setSeries(null);
                            passport.setNumber(number);
                            passport.setAuthority(null);
                            passport.setIssued(null);
                            passport.setEndDate(r.getModified());
                            passport.setRecordNumber(null);
                            passport.setType(IDCARD_PASSPORT);
                            passport.setValidity(false);
                            person = extender.addPassport(passport, people, source, person, savedPersonSet, passports);
                        }
                    }

                    person = extender.addPerson(people, person, source, false);

                    Set<YTag> tags = new HashSet<>();
                    YTag tag = new YTag();
                    tagType.ifPresent(tag::setTagType);
                    tag.setSource(GOVUA10);
                    tag.setUntil(LocalDate.of(3500, 1, 1));
                    tags.add(tag);

                    extender.addTags(person, tags, source);

                    if (!resp.isEmpty()) {
                        counter[0]++;
                        statusChanger.addProcessedVolume(1);
                    }
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
                    statusChanger.setProcessedVolume(0);
                }

                onePage = govua10Repository.findAllByPortionId(portion, pageRequest);

                if (!temp.isEmpty()) {
                    govua10Repository.saveAll(temp);
                    extender.sendMessageToQueue(GOVUA10, newPortion);
                    log.info("Send message with uuid: {}, count: {}", newPortion, temp.size());
                }

                logFinish(GOVUA10, counter[0]);
                logger.finish();

                statusChanger.addProcessedVolume(-resp.size());
            }
        } catch (Exception e) {
            statusChanger.error(Utils.messageFormat("ERROR: {}", e.getMessage()));
            extender.sendMessageToQueue(GOVUA10, portion);
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

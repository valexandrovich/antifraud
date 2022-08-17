package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.service.validator.Validator.isValidForeignPassport;
import static ua.com.solidity.enricher.util.Base.GOVUA11;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringFormatUtil.transliterationToCyrillicLetters;
import static ua.com.solidity.enricher.util.StringFormatUtil.transliterationToLatinLetters;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.FOREIGN_PASSPORT;
import static ua.com.solidity.enricher.util.StringStorage.TAG_TYPE_NAL;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.Govua11;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YPassportRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.repository.Govua11Repository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class Govua11Enricher implements Enricher {
    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final YPersonRepository ypr;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final Govua11Repository govua11Repository;
    private final YPassportRepository passportRepository;
    private final HttpClient httpClient;
    private final TagTypeRepository tagTypeRepository;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${enricher.timeOutTime}")
    private Integer timeOutTime;
    @Value("${enricher.sleepTime}")
    private Long sleepTime;
    @Value("${dispatcher.url}")
    private String urlPost;
    @Value("${dispatcher.url.delete}")
    private String urlDelete;
    private List<EntityProcessing> resp = new ArrayList<>();

    @SneakyThrows
    @Override
    public void enrich(UUID portion) {
        LocalDateTime startTime = LocalDateTime.now();
        try {
            logStart(GOVUA11);

            StatusChanger statusChanger = new StatusChanger(portion, GOVUA11, ENRICHER);

            long[] counter = new long[1];
            long[] wrongCounter = new long[1];

            Pageable pageRequest = PageRequest.of(0, pageSize);
            Page<Govua11> onePage = govua11Repository.findAllByPortionId(portion, pageRequest);
            long count = govua11Repository.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, GOVUA11, portion));

            ImportSource source = isr.findImportSourceByName(GOVUA11);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();

                List<Govua11> page = onePage.toList();

                while (!page.isEmpty()) {
                    Duration duration = Duration.between(startTime, LocalDateTime.now());
                    if (duration.getSeconds() > timeOutTime)
                        throw new TimeoutException("Time ran out for portion: " + portion);
                    List<EntityProcessing> entityProcessings = page.parallelStream().map(p -> {
                        EntityProcessing entityProcessing = new EntityProcessing();
                        entityProcessing.setUuid(p.getId());
                        if (StringUtils.isNotBlank(p.getNumber()) && p.getNumber().matches(ALL_NUMBER_REGEX))
                            entityProcessing.setPassHash(Objects.hash(transliterationToCyrillicLetters(p.getSeries()), Integer.valueOf(p.getNumber())));
                        return entityProcessing;
                    }).collect(Collectors.toList());

                    UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                    String url = urlPost + "?id=" + portion;
                    DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                    resp = new ArrayList<>(response.getResp());
                    List<UUID> respId = response.getRespId();
                    List<UUID> temp = response.getTemp();

                    List<Govua11> workPortion = page.stream().parallel().filter(p -> respId.contains(p.getId()))
                            .collect(Collectors.toList());

                    if (workPortion.isEmpty()) Thread.sleep(sleepTime);

                    Set<YPassport> passports = new HashSet<>();
                    Set<YPassport> passportSeriesWithNumber = new HashSet<>();
                    Set<Integer> passportNumbers = new HashSet<>();
                    Set<YPerson> people = new HashSet<>();

                    workPortion.forEach(r -> {
                        if (StringUtils.isNotBlank(r.getNumber()) && r.getNumber().matches(ALL_NUMBER_REGEX)) {
                            YPassport pass = new YPassport();
                            pass.setNumber(Integer.valueOf(r.getNumber()));
                            pass.setSeries(r.getSeries());
                            passportSeriesWithNumber.add(pass);
                            passportNumbers.add(Integer.parseInt(r.getNumber()));
                        }
                    });

                    if (!passportNumbers.isEmpty() && !passportSeriesWithNumber.isEmpty()) {
                        passports = passportRepository.findPassportsByNumber(passportNumbers);
                        passports = passports.parallelStream().filter(passportSeriesWithNumber::contains).collect(Collectors.toSet());
                    }

                    Set<YPerson> savedPersonSet = new HashSet<>();
                    if (!passports.isEmpty())
                        savedPersonSet.addAll(new HashSet<>(ypr.findPeoplePassportsForBaseEnricher(passports.parallelStream().map(YPassport::getId).collect(Collectors.toList()))));

                    Set<YPassport> finalPassports = passports;
                    Optional<TagType> tagType = tagTypeRepository.findByCode(TAG_TYPE_NAL);
                    workPortion.forEach(r -> {
                        YPerson person = new YPerson();


                        String passportNo = r.getNumber();
                        String passportSerial = r.getSeries();

                        if (isValidForeignPassport(passportNo, passportSerial, null, wrongCounter, counter, logger)) {
                            passportSerial = transliterationToLatinLetters(passportSerial);
                            int number = Integer.parseInt(passportNo);
                            YPassport passport = new YPassport();
                            passport.setSeries(passportSerial);
                            passport.setNumber(number);
                            passport.setAuthority(null);
                            passport.setIssued(null);
                            passport.setEndDate(r.getModified());
                            passport.setRecordNumber(null);
                            passport.setType(FOREIGN_PASSPORT);
                            passport.setValidity(false);
                            person = extender.addPassport(passport, people, source, person, savedPersonSet, finalPassports);

                            person = extender.addPerson(people, person, source, false);

                            Set<YTag> tags = new HashSet<>();
                            YTag tag = new YTag();
                            tagType.ifPresent(tag::setTagType);
                            tag.setSource(GOVUA11);
                            tag.setUntil(LocalDate.of(3500, 1, 1));
                            tags.add(tag);

                            extender.addTags(person, tags, source);

                            if (!resp.isEmpty()) {
                                counter[0]++;
                                statusChanger.addProcessedVolume(1);
                            }
                        }
                    });
                    UUID dispatcherIdFinish = httpClient.get(urlPost, UUID.class);
                    if (Objects.equals(dispatcherId, dispatcherIdFinish)) {

                        emnService.enrichYPersonPackageMonitoringNotification(people);

                        if (!people.isEmpty())
                            ypr.saveAll(people);

                        emnService.enrichYPersonMonitoringNotification(people);

                        if (!resp.isEmpty()) {
                            httpClient.post(urlDelete, Boolean.class, resp);
                            resp.clear();
                        }

                        page = page.parallelStream().filter(p -> temp.contains(p.getId())).collect(Collectors.toList());
                    } else {
                        counter[0] -= resp.size();
                        statusChanger.addProcessedVolume(-resp.size());
                    }
                }

                onePage = govua11Repository.findAllByPortionId(portion, pageRequest);

            }

            logFinish(GOVUA11, counter[0]);
            logger.finish();

            statusChanger.complete(importedRecords(counter[0]));
        } finally {
            deleteResp();
        }
    }

    @Override
    @PreDestroy
    public void deleteResp() {
        if (!resp.isEmpty()) {
            httpClient.post(urlDelete, Boolean.class, resp);
            resp.clear();
        }
    }
}

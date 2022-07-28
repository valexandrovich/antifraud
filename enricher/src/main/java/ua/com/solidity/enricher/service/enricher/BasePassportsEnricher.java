package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.service.validator.Validator.isValidInn;
import static ua.com.solidity.enricher.service.validator.Validator.isValidLocalPassport;
import static ua.com.solidity.enricher.util.Base.BASE_PASSPORTS;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.ALL_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringFormatUtil.transliterationToCyrillicLetters;
import static ua.com.solidity.enricher.util.StringStorage.DOMESTIC_PASSPORT;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
import ua.com.solidity.db.entities.BasePassports;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPassportRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.model.YPersonProcessing;
import ua.com.solidity.enricher.model.response.YPersonDispatcherResponse;
import ua.com.solidity.enricher.repository.BasePassportsRepository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;

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
    @Value("${dispatcher.url.person}")
    private String urlPersonPost;
    @Value("${dispatcher.url.person.delete}")
    private String urlPersonDelete;

    @Override
    public void enrich(UUID portion) {
        logStart(BASE_PASSPORTS);

        StatusChanger statusChanger = new StatusChanger(portion, BASE_PASSPORTS, ENRICHER);

        long[] counter = new long[1];
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BasePassports> onePage = bpr.findAllByPortionId(portion, pageRequest);
        long count = bpr.countAllByPortionId(portion);
        statusChanger.newStage(null, "enriching", count, null);
        String fileName = fileFormatUtil.getLogFileName(portion.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_PASSPORTS, portion));

        ImportSource source = isr.findImportSourceByName(BASE_PASSPORTS);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            Set<YPerson> personSet = new HashSet<>();
            List<BasePassports> page = onePage.toList();

            while (!page.isEmpty()) {
                List<YPersonProcessing> peopleProcessing = page.parallelStream().map(p -> {
                    YPersonProcessing personProcessing = new YPersonProcessing();
                    personProcessing.setUuid(p.getId());
                    if (StringUtils.isNotBlank(p.getInn()) && p.getInn().matches(ALL_NUMBER_REGEX))
                        personProcessing.setInn(Long.valueOf(p.getInn()));
                    if (StringUtils.isNotBlank(p.getPassId()) && p.getPassId().matches(ALL_NUMBER_REGEX))
                        personProcessing.setPassHash(Objects.hash(transliterationToCyrillicLetters(p.getSerial()), Integer.valueOf(p.getPassId())));
                    personProcessing.setPersonHash(Objects.hash(UtilString.toUpperCase(p.getLastName()), UtilString.toUpperCase(p.getFirstName()), UtilString.toUpperCase(p.getMiddleName()),
                            p.getBirthdate()));
                    return personProcessing;
                }).collect(Collectors.toList());

                UUID dispatcherId = httpClient.get(urlPersonPost, UUID.class);

                YPersonDispatcherResponse response = httpClient.post(urlPersonPost, YPersonDispatcherResponse.class, peopleProcessing);
                List<UUID> resp = response.getResp();
                List<UUID> temp = response.getTemp();

                page = onePage.stream().parallel().filter(p -> resp.contains(p.getId()))
                        .collect(Collectors.toList());

                Set<Long> codes = new HashSet<>();
                Set<String> passportSeries = new HashSet<>();
                Set<Integer> passportNumbers = new HashSet<>();
                Set<YPerson> people = new HashSet<>();

                Set<YINN> inns = new HashSet<>();
                Set<YPassport> passports = new HashSet<>();
                page.forEach(r -> {
                    if (StringUtils.isNotBlank(r.getPassId()) && r.getPassId().matches(ALL_NUMBER_REGEX)) {
                        passportSeries.add(transliterationToCyrillicLetters(r.getSerial()));
                        passportNumbers.add(Integer.parseInt(r.getPassId()));
                    }
                    if (StringUtils.isNotBlank(r.getInn()) && r.getInn().matches(ALL_NUMBER_REGEX)) {
                        codes.add(Long.parseLong(r.getInn()));
                    }
                });

                if (!codes.isEmpty())
                    inns = yinnRepository.findInns(codes);
                if (!passportNumbers.isEmpty() && !passportSeries.isEmpty())
                    passports = passportRepository.findPassports(passportSeries, passportNumbers);

                Set<YPerson> savedPersonSet = new HashSet<>();

                if (!codes.isEmpty())
                    savedPersonSet = ypr.findPeopleInnsForBaseEnricher(codes);
                if (!passports.isEmpty())
                    savedPersonSet.addAll(ypr.findPeoplePassportsForBaseEnricher(passports.parallelStream().map(YPassport::getId).collect(Collectors.toList())));
                Set<YPerson> savedPeople = savedPersonSet;

                Set<YINN> finalInns = inns;
                Set<YPassport> finalPassports = passports;
                page.forEach(r -> {
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
                        if (isValidInn(inn, r.getBirthdate())) {
                            person = extender.addInn(Long.parseLong(inn), people, source, person, finalInns, savedPeople);
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getInn()), "Wrong INN");
                            wrongCounter[0]++;
                        }
                    }

                    if (!StringUtils.isBlank(r.getPassId()) && r.getPassId().matches(CONTAINS_NUMERAL_REGEX)) {
                        String passportNo = String.format("%06d", Integer.parseInt(r.getPassId().replaceAll(ALL_NOT_NUMBER_REGEX, "")));
                        String passportSerial = r.getSerial();
                        if (isValidLocalPassport(passportNo, passportSerial, wrongCounter, counter, logger)) {
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
                            extender.addPassport(passport, people, source, person, savedPeople, finalPassports);
                        }
                    }
                    extender.addPerson(people, person, source, false);
                    counter[0]++;
                    statusChanger.addProcessedVolume(1);
                });
                UUID dispatcherIdFinish = httpClient.get(urlPersonPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {
                    ypr.saveAll(people);
                    personSet.addAll((people));

                    httpClient.post(urlPersonDelete, Boolean.class, resp);

                    page = onePage.stream().parallel().filter(p -> temp.contains(p.getId())).collect(Collectors.toList());
                } else {
                    counter[0] -= resp.size();
                    statusChanger.setProcessedVolume(counter[0]);
                }
            }
            emnService.enrichYPersonMonitoringNotification(personSet);

            onePage = bpr.findAllByPortionId(portion, pageRequest);
        }

        logFinish(BASE_PASSPORTS, counter[0]);
        logger.finish();

        statusChanger.complete(importedRecords(counter[0]));
    }
}

package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.common.UtilString.stringToDate;
import static ua.com.solidity.enricher.service.validator.Validator.isValidForeignPassport;
import static ua.com.solidity.enricher.service.validator.Validator.isValidIdPassport;
import static ua.com.solidity.enricher.service.validator.Validator.isValidLocalPassport;
import static ua.com.solidity.enricher.util.Base.MANUAL_PERSON;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.ALL_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringFormatUtil.transliterationToCyrillicLetters;
import static ua.com.solidity.enricher.util.StringFormatUtil.transliterationToLatinLetters;
import static ua.com.solidity.enricher.util.StringStorage.DOMESTIC_PASSPORT;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_INFO_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.FOREIGN_PASSPORT;
import static ua.com.solidity.enricher.util.StringStorage.IDCARD_PASSPORT;
import static ua.com.solidity.enricher.util.StringStorage.TAG_TYPE_NA;
import static ua.com.solidity.enricher.util.StringStorage.TAG_TYPE_NAL;
import static ua.com.solidity.util.validator.Validator.isValidInn;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.ManualPerson;
import ua.com.solidity.db.entities.ManualTag;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.db.entities.YEmail;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPhone;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.FileDescriptionRepository;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.ManualPersonRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPassportRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class ManualPersonEnricher implements Enricher {
    private static final String SOURCE_NAME = "manual";
    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final YPersonRepository ypr;
    private final ManualPersonRepository manualPersonRepository;
    private final FileDescriptionRepository fileDescriptionRepository;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final YPassportRepository passportRepository;
    private final YINNRepository yinnRepository;
    private final TagTypeRepository tagTypeRepository;
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
    public void enrich(UUID revision) {

        logStart(MANUAL_PERSON);

        StatusChanger statusChanger = new StatusChanger(revision, MANUAL_PERSON, ENRICHER);

        long[] counter = new long[1];

        UUID newPortion = UUID.randomUUID();

        try {
            Pageable pageRequest = PageRequest.of(0, pageSize);
            FileDescription file = fileDescriptionRepository.findByUuid(revision).orElseThrow(() ->
                    new RuntimeException("Can't find file with id = " + revision));
            Page<ManualPerson> onePage = manualPersonRepository.findAllByUuid(file, pageRequest);
            if (onePage.isEmpty()) return;
            long count = manualPersonRepository.countAllByUuid(file);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(revision.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, MANUAL_PERSON, revision));

            ImportSource source = isr.findImportSourceByName(SOURCE_NAME);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();
                Map<Long, UUID> uuidMap = new HashMap<>();

                onePage.forEach(p -> {
                    UUID uuid = UUID.randomUUID();
                    uuidMap.put(p.getId(), uuid);
                });

                List<EntityProcessing> entityProcessings = onePage.stream().map(p -> {
                    EntityProcessing entityProcessing = new EntityProcessing();
                    entityProcessing.setUuid(uuidMap.get(p.getId()));
                    if (!StringUtils.isBlank(p.getOkpo()) && p.getOkpo().matches(CONTAINS_NUMERAL_REGEX)) {
                        String inn = p.getOkpo().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        entityProcessing.setInn(Long.parseLong(inn));
                    }
                    if (StringUtils.isNotBlank(p.getPassLocalNum()) && p.getPassLocalNum().matches(ALL_NUMBER_REGEX))
                        entityProcessing.setPassHash(Objects.hash(transliterationToCyrillicLetters(p.getPassLocalSerial()), Integer.valueOf(p.getPassLocalNum())));
                    if (StringUtils.isNotBlank(p.getLnameUk()) || StringUtils.isNotBlank(p.getFnameUk()) || StringUtils.isNotBlank(p.getPnameUk())
                            || p.getBirthday() != null)
                        entityProcessing.setPersonHash(Objects.hash(UtilString.toUpperCase(p.getLnameUk()), UtilString.toUpperCase(p.getFnameUk()), UtilString.toUpperCase(p.getPnameUk()),
                                p.getBirthday()));
                    return entityProcessing;
                }).collect(Collectors.toList());
                entityProcessings.addAll(onePage.stream().parallel().map(p -> {
                    EntityProcessing entityProcessing = new EntityProcessing();
                    entityProcessing.setUuid(uuidMap.get(p.getId()));
                    if (StringUtils.isNotBlank(p.getPassIdNum()) && p.getPassIdNum().matches(ALL_NUMBER_REGEX))
                        entityProcessing.setPassHash(Objects.hash(Integer.valueOf(p.getPassIdNum())));
                    return entityProcessing;
                }).collect(Collectors.toList()));
                entityProcessings.addAll(onePage.stream().parallel().map(p -> {
                    EntityProcessing entityProcessing = new EntityProcessing();
                    entityProcessing.setUuid(uuidMap.get(p.getId()));
                    if (StringUtils.isNotBlank(p.getPassIntNum())) {
                        String passportNo = p.getPassIntNum().substring(2);
                        String passportSerial = p.getPassIntNum().substring(0, 2);
                        entityProcessing.setPassHash(Objects.hash(transliterationToLatinLetters(passportSerial), Integer.valueOf(passportNo)));
                    }
                    return entityProcessing;
                }).collect(Collectors.toList()));

                UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                log.info("Passing {}, count: {}", revision, entityProcessings.size());
                String url = urlPost + "?id=" + revision;
                DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                resp = new ArrayList<>(response.getResp());
                List<UUID> respId = response.getRespId();

                if (respId.isEmpty()) {
                    extender.sendMessageToQueue(MANUAL_PERSON, revision);
                    statusChanger.newStage(null, "All data is being processed. Portions sent to the queue.", count, null);
                    return;
                }

                log.info(ENRICHER_INFO_MESSAGE, resp.size());
                statusChanger.setStatus(Utils.messageFormat(ENRICHER_INFO_MESSAGE, resp.size()));

                List<ManualPerson> finalWorkPortion = new ArrayList<>();
                List<ManualPerson> temp = new ArrayList<>();
                FileDescription newFileDescription = new FileDescription();
                newFileDescription.setUuid(newPortion);
                onePage.stream().forEach(p -> {
                    if (respId.contains(uuidMap.get(p.getId()))) finalWorkPortion.add(p);
                    else {
                        p.setUuid(newFileDescription);
                        temp.add(p);
                    }
                });

                List<ManualPerson> workPortion = finalWorkPortion.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());

                Set<YPerson> personSet = new HashSet<>();
                Set<YPassport> passportSeriesWithNumber = new HashSet<>();
                Set<Long> codes = new HashSet<>();

                workPortion.forEach(r -> {
                    if (StringUtils.isNotBlank(r.getPassLocalNum())) {
                        YPassport pass = new YPassport();
                        pass.setNumber(Integer.valueOf(r.getPassLocalNum()));
                        pass.setSeries(transliterationToCyrillicLetters(r.getPassLocalSerial()));
                        passportSeriesWithNumber.add(pass);
                    }
                    if (StringUtils.isNotBlank(r.getPassIntNum())) {
                        String passportNo = r.getPassIntNum().substring(2);
                        String passportSerial = r.getPassIntNum().substring(0, 2);
                        YPassport pass = new YPassport();
                        pass.setNumber(Integer.parseInt(passportNo));
                        pass.setSeries(transliterationToLatinLetters(passportSerial));
                        passportSeriesWithNumber.add(pass);
                    }
                    if (StringUtils.isNotBlank(r.getPassIdNum())) {
                        YPassport pass = new YPassport();
                        pass.setNumber(Integer.valueOf(r.getPassIdNum()));
                        pass.setSeries(null);
                        passportSeriesWithNumber.add(pass);
                    }
                    if (!StringUtils.isBlank(r.getOkpo()) && r.getOkpo().matches(CONTAINS_NUMERAL_REGEX)) {
                        String inn = r.getOkpo().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        codes.add(Long.parseLong(inn));
                    }
                });

                Set<YINN> inns = new HashSet<>();
                Set<YPassport> passports = new HashSet<>();
                Set<YPerson> savedPersonSet = new HashSet<>();

                if (!codes.isEmpty()) {
                    inns.addAll(yinnRepository.findInns(codes));
                    savedPersonSet.addAll(ypr.findPeopleWithInns(codes));
                }

                if (!passportSeriesWithNumber.isEmpty()) {
                    for (YPassport passport : passportSeriesWithNumber) {
                        List<YPassport> passportList = passportRepository.findPassportsByNumberAndSeries(passport.getNumber(), passport.getSeries());
                        passports.addAll(passportList);
                    }
                }

                if (!passports.isEmpty())
                    savedPersonSet.addAll(ypr.findPeoplePassports(passports.parallelStream().map(YPassport::getId).collect(Collectors.toList())));

                workPortion.forEach(r -> {
                    String lastName = UtilString.toUpperCase(r.getLnameUk());
                    String firstName = UtilString.toUpperCase(r.getFnameUk());
                    String patName = UtilString.toUpperCase(r.getPnameUk());
                    LocalDate birthday = stringToDate(r.getBirthday());
                    String sex = UtilString.toUpperCase(r.getSex());
                    if (StringUtils.isNotBlank(sex)) sex = sex.trim();

                    YPerson person = new YPerson();
                    person.setLastName(lastName);
                    person.setFirstName(firstName);
                    person.setPatName(patName);
                    person.setBirthdate(birthday);

                    if (!StringUtils.isBlank(r.getOkpo()) && r.getOkpo().matches(CONTAINS_NUMERAL_REGEX)) {
                        String inn = r.getOkpo().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        if (isValidInn(inn, stringToDate(r.getBirthday()), sex)) {
                            person = extender.addInn(Long.parseLong(inn), personSet, source, person, inns, savedPersonSet);
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getOkpo()), "Wrong INN");
                        }
                    }

                    String passportNo = r.getPassLocalNum();
                    String passportSerial = r.getPassLocalSerial();
                    if (isValidLocalPassport(passportNo, passportSerial, counter, logger)) {
                        passportSerial = transliterationToCyrillicLetters(passportSerial);
                        int number = Integer.parseInt(passportNo);
                        YPassport passport = new YPassport();
                        passport.setSeries(passportSerial);
                        passport.setNumber(number);
                        passport.setAuthority(r.getPassLocalIssuer());
                        passport.setIssued(stringToDate(r.getPassLocalIssueDate()));
                        passport.setEndDate(null);
                        passport.setRecordNumber(null);
                        passport.setType(DOMESTIC_PASSPORT);
                        passport.setValidity(r.getTags().isEmpty() || !r.getTags().stream().map(ManualTag::getMkId).collect(Collectors.toSet()).contains(TAG_TYPE_NAL));
                        person = extender.addPassport(passport, personSet, source, person, savedPersonSet, passports);
                    }

                    String passportRecNo;
                    if (StringUtils.isNotBlank(r.getPassIntNum())) {
                        passportNo = r.getPassIntNum().substring(2);
                        passportSerial = r.getPassIntNum().substring(0, 2);
                        passportRecNo = r.getPassIntRecNum();
                        if (isValidForeignPassport(passportNo, passportSerial, passportRecNo,
                                counter, logger)) {
                            passportSerial = transliterationToLatinLetters(passportSerial);
                            int number = Integer.parseInt(passportNo);
                            YPassport passport = new YPassport();
                            passport.setSeries(passportSerial);
                            passport.setNumber(number);
                            passport.setAuthority(r.getPassIntIssuer());
                            passport.setIssued(stringToDate(r.getPassIntIssueDate()));
                            passport.setEndDate(null);
                            passport.setRecordNumber(r.getPassIntRecNum());
                            passport.setType(FOREIGN_PASSPORT);
                            passport.setValidity(r.getTags().isEmpty() || !r.getTags().stream().map(ManualTag::getMkId).collect(Collectors.toSet()).contains(TAG_TYPE_NA));
                            person = extender.addPassport(passport, personSet, source, person, savedPersonSet, passports);
                        }
                    }

                    passportNo = r.getPassIdNum();
                    passportRecNo = r.getPassIdRecNum();
                    if (isValidIdPassport(passportNo, passportRecNo, counter, logger)) {
                        int number = Integer.parseInt(passportNo);
                        YPassport passport = new YPassport();
                        passport.setSeries(null);
                        passport.setNumber(number);
                        passport.setAuthority(r.getPassIdIssuer());
                        passport.setIssued(stringToDate(r.getPassIdIssueDate()));
                        passport.setEndDate(null);
                        passport.setRecordNumber(r.getPassIdRecNum());
                        passport.setType(IDCARD_PASSPORT);
                        passport.setValidity(true);
                        person = extender.addPassport(passport, personSet, source, person, savedPersonSet, passports);
                    }

                    person = extender.addPerson(personSet, person, source, true);

                    if (StringUtils.isNotBlank(r.getSex())) {
                        person.setSex(r.getSex().trim().toUpperCase());
                    }
                    if (StringUtils.isNotBlank(r.getCountry())) {
                        person.setCountry(r.getCountry().trim().toUpperCase());
                    }
                    if (StringUtils.isNotBlank(r.getComment())) {
                        person.setComment(r.getComment().trim().toUpperCase());
                    }

                    if (StringUtils.isNotBlank(r.getBirthPlace())) {
                        person.setBirthPlace(r.getBirthPlace().trim().toUpperCase());
                    }

                    Set<YAddress> addresses = new HashSet<>();
                    if (StringUtils.isNotBlank(r.getAddress())) {
                        YAddress address = new YAddress();
                        address.setAddress(r.getAddress().toUpperCase());
                        addresses.add(address);
                    }

                    extender.addAddresses(person, addresses, source);

                    Set<YPhone> phones = new HashSet<>();
                    if (StringUtils.isNotBlank(r.getPhone())) {
                        YPhone phone = new YPhone();
                        phone.setPhone(r.getPhone().toUpperCase());
                        phones.add(phone);
                    }
                    extender.addPhones(person, phones, source);

                    Set<YEmail> emails = new HashSet<>();
                    if (StringUtils.isNotBlank(r.getEmail())) {
                        YEmail email = new YEmail();
                        email.setEmail(r.getEmail().toUpperCase());
                        emails.add(email);
                    }
                    extender.addEmails(person, emails, source);

                    Set<YTag> tags = new HashSet<>();
                    r.getTags().forEach(t -> {
                        YTag tag = new YTag();
                        tag.setTagType(tagTypeRepository.findByCode(t.getMkId().toUpperCase()).orElseThrow(() ->
                                new RuntimeException("Not found tag with code: " + t.getMkId())));
                        tag.setAsOf(extender.stringToDate(t.getMkStart()));
                        tag.setUntil(extender.stringToDate(t.getMkExpire()));
                        tag.setEventDate(extender.stringToDate(t.getMkEventDate()));
                        tag.setNumberValue(t.getMkNumberValue());
                        tag.setTextValue(t.getMkTextValue());
                        tag.setDescription(t.getMkDescription());
                        tag.setSource(t.getMkSource());
                        tags.add(tag);
                    });
                    extender.addTags(person, tags, source);

                    if (StringUtils.isNotBlank(r.getLnameRu()) || StringUtils.isNotBlank(r.getFnameRu()) || StringUtils.isNotBlank(r.getPnameRu()))
                        extender.addAltPerson(person, UtilString.toUpperCase(r.getLnameRu()),
                                UtilString.toUpperCase(r.getFnameRu()),
                                UtilString.toUpperCase(r.getPnameRu()), "RU", source);
                    if (StringUtils.isNotBlank(r.getLnameEn()) || StringUtils.isNotBlank(r.getFnameEn()) || StringUtils.isNotBlank(r.getPnameEn()))
                        extender.addAltPerson(person, UtilString.toUpperCase(r.getLnameEn()),
                                UtilString.toUpperCase(r.getFnameEn()),
                                UtilString.toUpperCase(r.getPnameEn()), "EN", source);

                    if (!resp.isEmpty()) {
                        counter[0]++;
                        statusChanger.addProcessedVolume(1);
                    }
                });

                UUID dispatcherIdFinish = httpClient.get(urlPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {

                    if (!personSet.isEmpty()) {
                        emnService.enrichYPersonPackageMonitoringNotification(personSet);
                        log.info("Saving people");
                        ypr.saveAll(personSet);
                        emnService.enrichYPersonMonitoringNotification(personSet);
                        statusChanger.setStatus(Utils.messageFormat("Enriched {} rows", statusChanger.getProcessedVolume()));
                    }

                    deleteResp();
                } else {
                    counter[0] = 0L;
                    statusChanger.newStage(null, "Restoring from dispatcher restart", count, null);
                    statusChanger.addProcessedVolume(0);
                }

                onePage = manualPersonRepository.findAllByUuid(file, pageRequest);

                if (!temp.isEmpty()) {
                    manualPersonRepository.saveAll(temp);
                    extender.sendMessageToQueue(MANUAL_PERSON, newPortion);
                    log.info("Send message with uuid: {}, count: {}", newPortion, temp.size());
                }

                logFinish(MANUAL_PERSON, counter[0]);
                logger.finish();

                statusChanger.complete(importedRecords(counter[0]));
            }
        } catch (Exception e) {
            statusChanger.error(Utils.messageFormat("ERROR: {}", Utils.getExceptionString(e, ";")));
            log.error("$$Enrichment error.", e);
            extender.sendMessageToQueue(MANUAL_PERSON, revision);
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

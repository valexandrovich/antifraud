package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.service.validator.Validator.isValidForeignPassport;
import static ua.com.solidity.enricher.service.validator.Validator.isValidIdPassport;
import static ua.com.solidity.enricher.service.validator.Validator.isValidLocalPassport;
import static ua.com.solidity.enricher.util.Base.BASE_PASSPORTS;
import static ua.com.solidity.enricher.util.Base.CONTRAGENT;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
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
import static ua.com.solidity.enricher.util.StringStorage.TAG_TYPE_RC;
import static ua.com.solidity.util.validator.Validator.isValidEdrpou;
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
import java.util.stream.Stream;
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
import ua.com.solidity.db.entities.Contragent;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.db.entities.YCAddress;
import ua.com.solidity.db.entities.YCTag;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YEmail;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPhone;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.ContragentRepository;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
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
public class ContragentEnricher implements Enricher {

    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final YPersonRepository ypr;
    private final ContragentRepository cr;
    private final YINNRepository yir;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final YPassportRepository yPassportRepository;
    private final TagTypeRepository tagTypeRepository;
    private final HttpClient httpClient;
    private final YCompanyRepository companyRepository;

    private static final String PHYSICAL_RESIDENT = "5";
    private static final String JURIDICAL_RESIDENT = "3";
    public static final long B2_FOREIGN_PASSPORT_1 = 2L;
    public static final long B2_FOREIGN_PASSPORT_2 = 112L;
    public static final long B2_IDCARD_PASSPORT = 29L;
    public static final long B2_LOCAL_PASSPORT = 1L;

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

        logStart(CONTRAGENT);

        StatusChanger statusChanger = new StatusChanger(portion, CONTRAGENT, ENRICHER);
        long[] counter = new long[1];

        UUID newPortion = UUID.randomUUID();

        try {
            Pageable pageRequest = PageRequest.of(0, pageSize);
            Page<Contragent> onePage = cr.findAllByPortionId(portion, pageRequest);
            if (onePage.isEmpty()) return;
            long count = cr.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            ImportSource source = isr.findImportSourceByName("dwh");
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_PASSPORTS, portion));

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();

                List<EntityProcessing> entityProcessings = onePage.stream()
                        .map(p -> {
                            String firstName = UtilString.toUpperCase(p.getClientName());
                            String patName = UtilString.toUpperCase(p.getClientPatronymicName());
                            String lastName = UtilString.toUpperCase(p.getClientLastName());

                            EntityProcessing entityProcessing = new EntityProcessing();
                            entityProcessing.setUuid(p.getUuid());
                            if (!StringUtils.isBlank(p.getIdentifyCode()) && p.getIdentifyCode().matches(CONTAINS_NUMERAL_REGEX)) {
                                String inn = p.getIdentifyCode().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                                entityProcessing.setInn(Long.parseLong(inn));
                                entityProcessing.setEdrpou(Long.parseLong(inn));
                            }

                            if (!StringUtils.isBlank(p.getPassportNo()) && p.getPassportNo().matches(CONTAINS_NUMERAL_REGEX)) {
                                String passportNo = p.getPassportNo().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                                String passportSerial = null;
                                if (passportNo.length() < 10) {
                                    int number = Integer.parseInt(passportNo);
                                    if (p.getPassportType() == B2_LOCAL_PASSPORT)
                                        passportSerial = transliterationToCyrillicLetters(p.getPassportSerial());
                                    else if (p.getPassportType() == B2_FOREIGN_PASSPORT_1 || p.getPassportType() == B2_FOREIGN_PASSPORT_2) {
                                        passportSerial = transliterationToLatinLetters(p.getPassportSerial());
                                    }
                                    entityProcessing.setPassHash(Objects.hash(passportSerial, number));
                                } else log.warn("Passport type " + p.getPassportType()
                                        + " has incorrect format: Series: " + p.getPassportSerial()
                                        + ", Number: " + p.getPassportNo() + " - ignored");
                            }

                            entityProcessing.setPersonHash(Objects.hash(lastName, firstName, patName, p.getClientBirthday()));

                            if (StringUtils.isNotBlank(p.getName()))
                                entityProcessing.setCompanyHash(Objects.hash(UtilString.toUpperCase(p.getName())));
                            return entityProcessing;
                        }).collect(Collectors.toList());

                UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                log.info("Passing {}, count: {}", portion, entityProcessings.size());
                String url = urlPost + "?id=" + portion;
                DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                resp = new ArrayList<>(response.getResp());
                List<UUID> respId = response.getRespId();

                if (respId.isEmpty()) {
                    extender.sendMessageToQueue(CONTRAGENT, portion);
                    statusChanger.newStage(null, "All data is being processed. Portions sent to the queue.", count, null);
                    return;
                }

                log.info(ENRICHER_INFO_MESSAGE, resp.size());
                statusChanger.setStatus(Utils.messageFormat(ENRICHER_INFO_MESSAGE, resp.size()));

                List<Contragent> temp = new ArrayList<>();
                List<Contragent> finalWorkPortion = new ArrayList<>();
                onePage.stream().forEach(p -> {
                    if (respId.contains(p.getUuid())) finalWorkPortion.add(p);
                    else {
                        p.setPortionId(newPortion);
                        temp.add(p);
                    }
                });

                List<Contragent> workPortion = finalWorkPortion.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());

                Set<Long> codes = new HashSet<>();
                Set<YPerson> people = new HashSet<>();
                Set<YCompany> companies = new HashSet<>();
                Set<YINN> inns = new HashSet<>();
                Set<YPassport> passports = new HashSet<>();
                Set<YPassport> passportSeriesWithNumber = new HashSet<>();
                Set<YPerson> savedPersonSet = new HashSet<>();
                Set<YCompany> savedCompanySet = new HashSet<>();

                workPortion.forEach(r -> {

                    if (!StringUtils.isBlank(r.getPassportNo()) && r.getPassportNo().matches(CONTAINS_NUMERAL_REGEX)) {
                        String passportNo = r.getPassportNo().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        String passportSerial = null;
                        if (passportNo.length() < 10) {
                            int number = Integer.parseInt(passportNo);
                            if (r.getPassportType() == B2_LOCAL_PASSPORT)
                                passportSerial = transliterationToCyrillicLetters(r.getPassportSerial());
                            else if (r.getPassportType() == B2_FOREIGN_PASSPORT_1 || r.getPassportType() == B2_FOREIGN_PASSPORT_2)
                                passportSerial = transliterationToLatinLetters(r.getPassportSerial());
                            YPassport pass = new YPassport();
                            pass.setNumber(number);
                            pass.setSeries(passportSerial);
                            passportSeriesWithNumber.add(pass);
                        }
                    }

                    if (!StringUtils.isBlank(r.getIdentifyCode()) && r.getIdentifyCode().matches(CONTAINS_NUMERAL_REGEX)) {
                        String code = r.getIdentifyCode().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        codes.add(Long.valueOf(code));
                    }
                });

                if (!codes.isEmpty()) {
                    inns.addAll(yir.findInns(codes));
                    savedPersonSet.addAll(ypr.findPeopleWithInns(codes));
                    savedCompanySet.addAll(companyRepository.findByEdrpous(codes));
                }
                if (!passportSeriesWithNumber.isEmpty()) {
                    for (YPassport passport : passportSeriesWithNumber) {
                        List<YPassport> passportList = yPassportRepository.findPassportsByNumberAndSeries(passport.getNumber(), passport.getSeries());
                        passports.addAll(passportList);
                    }
                }

                if (!passports.isEmpty())
                    savedPersonSet.addAll(ypr.findPeoplePassports(passports.parallelStream().map(YPassport::getId).collect(Collectors.toList())));

                Optional<TagType> tagType = tagTypeRepository.findByCode(TAG_TYPE_RC);
                workPortion.forEach(r -> {
                    if (StringUtils.isNotBlank(r.getContragentTypeId()) &&
                            Objects.equals(r.getContragentTypeId().trim(), PHYSICAL_RESIDENT)) {
                        String firstName = UtilString.toUpperCase(r.getClientName());
                        String patName = UtilString.toUpperCase(r.getClientPatronymicName());
                        String lastName = UtilString.toUpperCase(r.getClientLastName());

                        YPerson person = new YPerson();
                        person.setLastName(lastName);
                        person.setFirstName(firstName);
                        person.setPatName(patName);
                        person.setBirthdate(r.getClientBirthday());

                        if (!StringUtils.isBlank(r.getIdentifyCode()) && r.getIdentifyCode().matches(CONTAINS_NUMERAL_REGEX)) {
                            String inn = r.getIdentifyCode().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                            if (isValidInn(inn, r.getClientBirthday(), null)) {
                                person = extender.addInn(Long.parseLong(inn), people, source, person, inns, savedPersonSet);
                            } else {
                                logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getIdentifyCode()), "Wrong INN");
                            }
                        }

                        if (!StringUtils.isBlank(r.getPassportNo()) && r.getPassportNo().matches(CONTAINS_NUMERAL_REGEX)) {
                            String passportSerial = null;
                            String passportType = null;
                            String passportNo = r.getPassportNo().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                            boolean isValidPassport = false;
                            if (passportNo.length() < 10) {
                                int number = Integer.parseInt(passportNo);
                                if (r.getPassportType() == B2_LOCAL_PASSPORT) {
                                    isValidPassport = isValidLocalPassport(String.valueOf(number), passportSerial, counter, logger);
                                    passportSerial = transliterationToCyrillicLetters(passportSerial);
                                    passportType = DOMESTIC_PASSPORT;
                                } else if (r.getPassportType() == B2_FOREIGN_PASSPORT_1 || r.getPassportType() == B2_FOREIGN_PASSPORT_2) {
                                    isValidPassport = isValidForeignPassport(String.valueOf(number), passportSerial, null, counter, logger);
                                    passportSerial = transliterationToLatinLetters(passportSerial);
                                    passportType = FOREIGN_PASSPORT;
                                } else if (r.getPassportType() == B2_IDCARD_PASSPORT) {
                                    isValidPassport = isValidIdPassport(String.valueOf(number), null, counter, logger);
                                    passportType = IDCARD_PASSPORT;
                                } else log.warn("Identification document type " + r.getPassportType()
                                        + ": Series: " + r.getPassportSerial()
                                        + ", Number: " + r.getPassportNo() + " - ignored");
                                if (isValidPassport) {
                                    YPassport passport = new YPassport();
                                    passport.setSeries(passportSerial);
                                    passport.setNumber(number);
                                    passport.setAuthority(r.getPassportIssuePlace());
                                    passport.setIssued(r.getPassportIssueDate());
                                    passport.setEndDate(r.getPassportEndDate());
                                    passport.setRecordNumber(null);
                                    passport.setValidity(true);
                                    passport.setType(passportType);
                                    person = extender.addPassport(passport, people, source, person, savedPersonSet, passports);
                                }
                            }
                        }
                        person = extender.addPerson(people, person, source, true);

                        Set<YAddress> addresses = new HashSet<>();
                        Stream.of(r.getAddress(), r.getBirthplace()).forEach(a -> {
                            if (StringUtils.isNotBlank(a)) {
                                YAddress address = new YAddress();
                                address.setAddress(a.toUpperCase());
                                addresses.add(address);
                            }
                        });
                        extender.addAddresses(person, addresses, source);

                        Set<YPhone> phones = new HashSet<>();
                        Stream.of(r.getPhones(), r.getMobilePhone(), r.getPhoneHome()).forEach(p -> {
                            if (p != null) {
                                String phoneCleaned = p.replaceAll(ALL_NOT_NUMBER_REGEX, "");
                                if (StringUtils.isNotBlank(phoneCleaned)) {
                                    YPhone phone = new YPhone();
                                    phone.setPhone(p.toUpperCase());
                                    phones.add(phone);
                                }
                            }
                        });
                        extender.addPhones(person, phones, source);

                        Set<YEmail> emails = new HashSet<>();
                        if (StringUtils.isNotBlank(r.getEmail())) {
                            YEmail email = new YEmail();
                            email.setEmail(r.getEmail().toUpperCase());
                            emails.add(email);
                        }
                        extender.addEmails(person, emails, source);

                        Set<YTag> tags = new HashSet<>();
                        YTag tag = new YTag();
                        tagType.ifPresent(tag::setTagType);
                        tag.setSource(CONTRAGENT);
                        tag.setUntil(LocalDate.of(3500, 1, 1));
                        tags.add(tag);
                        extender.addTags(person, tags, source);

                        if (StringUtils.isNotBlank(r.getFirstNameLat()) || StringUtils.isNotBlank(r.getLastNameLat()))
                            extender.addAltPerson(person, UtilString.toUpperCase(r.getLastNameLat()),
                                    UtilString.toUpperCase(r.getFirstNameLat()), null, "EN", source);

                    } else if (StringUtils.isNotBlank(r.getContragentTypeId()) &&
                            Objects.equals(r.getContragentTypeId().trim(), JURIDICAL_RESIDENT)) {
                        YCompany company;

                        if (UtilString.matches(r.getIdentifyCode(), CONTAINS_NUMERAL_REGEX)) {
                            String code = r.getIdentifyCode().replaceAll(ALL_NOT_NUMBER_REGEX, "");

                            if (isValidEdrpou(code)) {
                                company = new YCompany();
                                company.setEdrpou(Long.parseLong(code));
                                company.setName(UtilString.toUpperCase(r.getName()));

                                company = extender.addCompany(companies, source, company, savedCompanySet);

                                Set<YCAddress> cAddresses = new HashSet<>();
                                Stream.of(r.getJuridicalAddress()).forEach(a -> {
                                    if (StringUtils.isNotBlank(a)) {
                                        YCAddress cAddress = new YCAddress();
                                        cAddress.setAddress(a.toUpperCase());
                                        cAddresses.add(cAddress);
                                    }
                                });
                                extender.addCAddresses(company, cAddresses, source);

                                if (StringUtils.isNotBlank(r.getAlternateName()))
                                    extender.addAltCompany(company, UtilString.toUpperCase(r.getAlternateName().trim()), "UA", source);

                                Set<YCTag> cTags = new HashSet<>();
                                YCTag cTag = new YCTag();
                                tagType.ifPresent(cTag::setTagType);
                                cTag.setSource(CONTRAGENT);
                                cTag.setUntil(LocalDate.of(3500, 1, 1));
                                cTags.add(cTag);
                                extender.addTags(company, cTags, source);
                            } else {
                                logError(logger, (counter[0] + 1L), Utils.messageFormat("EDRPOU: {}", r.getIdentifyCode()), "Wrong EDRPOU");
                            }
                        }
                    }
                    counter[0]++;
                    statusChanger.addProcessedVolume(1);
                });

                UUID dispatcherIdFinish = httpClient.get(urlPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {

                    if (!people.isEmpty()) {
                        emnService.enrichYPersonPackageMonitoringNotification(people);
                        log.info("Saving people");
                        ypr.saveAll(people);
                        emnService.enrichYPersonMonitoringNotification(people);
                    }

                    if (!companies.isEmpty()) {
                        emnService.enrichYCompanyPackageMonitoringNotification(companies);
                        log.info("Saving companies");
                        companyRepository.saveAll(companies);
                        emnService.enrichYCompanyMonitoringNotification(companies);
                    }

                    statusChanger.setStatus(Utils.messageFormat("Enriched {} rows", statusChanger.getProcessedVolume()));

                    deleteResp();
                } else {
                    counter[0] = 0L;
                    statusChanger.newStage(null, "Restoring from dispatcher restart", count, null);
                    statusChanger.addProcessedVolume(0);
                }

                onePage = cr.findAllByPortionId(portion, pageRequest);

                if (!temp.isEmpty()) {
                    cr.saveAll(temp);
                    extender.sendMessageToQueue(CONTRAGENT, newPortion);
                    log.info("Send message with uuid: {}, count: {}", newPortion, temp.size());
                }

                logFinish(CONTRAGENT, counter[0]);
                statusChanger.complete(importedRecords(counter[0]));
            }
        } catch (Exception e) {
            statusChanger.error(Utils.messageFormat("ERROR: {}", Utils.getExceptionString(e, ";")));
            log.debug("$$Enrichment error.", e);
            extender.sendMessageToQueue(CONTRAGENT, portion);
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

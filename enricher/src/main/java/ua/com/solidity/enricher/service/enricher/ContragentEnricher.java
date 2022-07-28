package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.service.validator.Validator.isValidInn;
import static ua.com.solidity.enricher.service.validator.Validator.isValidLocalPassport;
import static ua.com.solidity.enricher.util.Base.BASE_PASSPORTS;
import static ua.com.solidity.enricher.util.Base.CONTRAGENT;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import ua.com.solidity.db.entities.Contragent;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.db.entities.YEmail;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPhone;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.ContragentRepository;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPassportRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.model.YPersonProcessing;
import ua.com.solidity.enricher.model.response.YPersonDispatcherResponse;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;

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

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${dispatcher.url.person}")
    private String urlPersonPost;
    @Value("${dispatcher.url.person.delete}")
    private String urlPersonDelete;

    @Override
    public void enrich(UUID portion) {
        logStart(CONTRAGENT);

        StatusChanger statusChanger = new StatusChanger(portion, CONTRAGENT, ENRICHER);
        long[] counter = new long[1];
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<Contragent> onePage = cr.findAllByPortionId(portion, pageRequest);
        long count = cr.countAllByPortionId(portion);
        statusChanger.newStage(null, "enriching", count, null);
        ImportSource source = isr.findImportSourceByName("dwh");
        String fileName = fileFormatUtil.getLogFileName(portion.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_PASSPORTS, portion));

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            Set<YPerson> personSet = new HashSet<>();
            List<Contragent> page = onePage.toList();

            while (!page.isEmpty()) {
                List<YPersonProcessing> peopleProcessing = page.parallelStream().map(p -> {
                    String firstName = null;
                    String patName;
                    String lastName = UtilString.toUpperCase(p.getClientLastName());

                    List<String> splitPersonNames = Arrays.asList(p.getName().split("[ ]+"));
                    String splitPatronymic = splitPersonNames.size() == 3 ? splitPersonNames.get(2) : null;

                    if (!StringUtils.isBlank(p.getClientPatronymicName()))
                        patName = UtilString.toUpperCase(p.getClientPatronymicName());
                    else
                        patName = UtilString.toUpperCase(splitPatronymic);

                    if (!StringUtils.isBlank(p.getClientName()))
                        firstName = UtilString.toUpperCase(p.getClientName());
                    else if (splitPersonNames.size() == 3) {
                        firstName = splitPersonNames.get(1).equalsIgnoreCase(p.getClientLastName()) ? splitPersonNames.get(0) : splitPersonNames.get(1);
                        firstName = UtilString.toUpperCase(firstName);
                    }

                    YPersonProcessing personProcessing = new YPersonProcessing();
                    personProcessing.setUuid(p.getUuid());
                    if (StringUtils.isNotBlank(p.getIdentifyCode()) && p.getIdentifyCode().matches(ALL_NUMBER_REGEX))
                        personProcessing.setInn(Long.valueOf(p.getIdentifyCode()));
                    if (StringUtils.isNotBlank(p.getPassportNo()) && p.getPassportNo().matches(ALL_NUMBER_REGEX))
                        personProcessing.setPassHash(Objects.hash(transliterationToCyrillicLetters(p.getPassportSerial()), Integer.valueOf(p.getPassportNo())));
                    personProcessing.setPersonHash(Objects.hash(lastName, firstName, patName, p.getClientBirthday()));
                    return personProcessing;
                }).collect(Collectors.toList());

                UUID dispatcherId = httpClient.get(urlPersonPost, UUID.class);

                YPersonDispatcherResponse response = httpClient.post(urlPersonPost, YPersonDispatcherResponse.class, peopleProcessing);
                List<UUID> resp = response.getResp();
                List<UUID> temp = response.getTemp();

                page = onePage.stream().parallel().filter(p -> resp.contains(p.getUuid()))
                        .collect(Collectors.toList());

                Set<Long> codes = new HashSet<>();
                Set<String> passportSeries = new HashSet<>();
                Set<Integer> passportNumbers = new HashSet<>();
                Set<YPerson> people = new HashSet<>();
                Set<YINN> inns = new HashSet<>();
                Set<YPassport> passports = new HashSet<>();

                page.forEach(r -> {
                    if (StringUtils.isNotBlank(r.getPassportNo()) && r.getPassportNo().matches(ALL_NUMBER_REGEX)) {
                        passportSeries.add(transliterationToCyrillicLetters(r.getPassportSerial()));
                        passportNumbers.add(Integer.parseInt(r.getPassportNo()));
                    }
                    if (StringUtils.isNotBlank(r.getIdentifyCode()) && r.getIdentifyCode().matches(ALL_NUMBER_REGEX)) {
                        codes.add(Long.parseLong(r.getIdentifyCode()));
                    }
                });

                if (!codes.isEmpty())
                    inns = yir.findInns(codes);
                if (!passportNumbers.isEmpty() && !passportSeries.isEmpty())
                    passports = yPassportRepository.findPassports(passportSeries, passportNumbers);

                Set<YPerson> savedPersonSet = new HashSet<>();

                if (!codes.isEmpty())
                    savedPersonSet = ypr.findPeopleInns(codes);
                if (!passports.isEmpty())
                    savedPersonSet.addAll(ypr.findPeoplePassports(passports.parallelStream().map(YPassport::getId).collect(Collectors.toList())));
                Set<YPerson> savedPeople = savedPersonSet;

                Set<YINN> finalInns = inns;
                Set<YPassport> finalPassports = passports;
                page.forEach(r -> {
                    String firstName = null;
                    String patName;
                    String lastName = UtilString.toUpperCase(r.getClientLastName());

                    List<String> splitPersonNames = Arrays.asList(r.getName().split("[ ]+"));
                    String splitPatronymic = splitPersonNames.size() == 3 ? splitPersonNames.get(2) : null;

                    if (!StringUtils.isBlank(r.getClientPatronymicName()))
                        patName = UtilString.toUpperCase(r.getClientPatronymicName());
                    else
                        patName = UtilString.toUpperCase(splitPatronymic);

                    if (!StringUtils.isBlank(r.getClientName()))
                        firstName = UtilString.toUpperCase(r.getClientName());
                    else if (splitPersonNames.size() == 3) {
                        firstName = splitPersonNames.get(1).equalsIgnoreCase(r.getClientLastName()) ? splitPersonNames.get(0) : splitPersonNames.get(1);
                        firstName = UtilString.toUpperCase(firstName);
                    }

                    YPerson person = new YPerson();
                    person.setLastName(lastName);
                    person.setFirstName(firstName);
                    person.setPatName(patName);
                    person.setBirthdate(r.getClientBirthday());

                    if (!StringUtils.isBlank(r.getIdentifyCode()) && r.getIdentifyCode().matches(CONTAINS_NUMERAL_REGEX)) {
                        String inn = r.getIdentifyCode().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        if (isValidInn(inn, r.getClientBirthday())) {
                            person = extender.addInn(Long.parseLong(inn), people, source, person, finalInns, savedPeople);
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getIdentifyCode()), "Wrong INN");
                            wrongCounter[0]++;
                        }
                    }

                    if (!StringUtils.isBlank(r.getPassportNo()) && r.getPassportNo().matches(CONTAINS_NUMERAL_REGEX)) {
                        String passportNo = String.format("%06d", Integer.parseInt(r.getPassportNo().replaceAll(ALL_NOT_NUMBER_REGEX, "")));
                        String passportSerial = r.getPassportSerial();
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
                            String phoneCleaned = p.replaceAll("[^0-9]+", "");
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
                    tag.setTagType(tagTypeRepository.findByCode("RC")
                            .orElseThrow(() -> new RuntimeException("Not found tag with code: " + "RC")));
                    tags.add(tag);
                    extender.addTags(person, tags, source);

                    if (!StringUtils.isBlank(r.getAlternateName())) {
                        List<String> splitAltPersonNames = Arrays.asList(r.getAlternateName().split("[ ]+"));

                        String[] names = new String[3];
                        for (int i = 0; i < splitAltPersonNames.size(); i++) {
                            if (i == 2) {
                                StringBuilder sb = new StringBuilder(splitAltPersonNames.get(2));
                                for (int j = 3; j < splitAltPersonNames.size(); j++) {
                                    sb.append(' ').append(splitAltPersonNames.get(j));
                                }
                                names[2] = sb.toString();
                                break;
                            }
                            names[i] = splitAltPersonNames.get(i);
                        }
                        if (StringUtils.isNotBlank(names[0]) || StringUtils.isNotBlank(names[1]) || StringUtils.isNotBlank(names[2]))
                            extender.addAltPerson(person, UtilString.toUpperCase(names[1]),
                                    UtilString.toUpperCase(names[0]),
                                    UtilString.toUpperCase(names[2]), "EN", source);
                    }
                    counter[0]++;
                    statusChanger.addProcessedVolume(1);
                });

                UUID dispatcherIdFinish = httpClient.get(urlPersonPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {
                    ypr.saveAll(people);
                    personSet.addAll((people));

                    httpClient.post(urlPersonDelete, Boolean.class, resp);

                    page = onePage.stream().parallel().filter(p -> temp.contains(p.getUuid())).collect(Collectors.toList());
                } else {
                    counter[0] -= resp.size();
                    statusChanger.setProcessedVolume(counter[0]);
                }
            }
            emnService.enrichYPersonMonitoringNotification(personSet);

            onePage = cr.findAllByPortionId(portion, pageRequest);
        }

        logFinish(CONTRAGENT, counter[0]);
        statusChanger.complete(importedRecords(counter[0]));
    }
}

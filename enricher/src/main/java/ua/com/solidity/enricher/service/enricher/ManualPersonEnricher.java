package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.service.validator.Validator.isValidForeignPassport;
import static ua.com.solidity.enricher.service.validator.Validator.isValidIdPassport;
import static ua.com.solidity.enricher.service.validator.Validator.isValidInn;
import static ua.com.solidity.enricher.service.validator.Validator.isValidLocalPassport;
import static ua.com.solidity.enricher.util.Base.MANUAL_PERSON;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringFormatUtil.stringToDate;
import static ua.com.solidity.enricher.util.StringFormatUtil.transliterationToCyrillicLetters;
import static ua.com.solidity.enricher.util.StringFormatUtil.transliterationToLatinLetters;
import static ua.com.solidity.enricher.util.StringStorage.DOMESTIC_PASSPORT;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.FOREIGN_PASSPORT;
import static ua.com.solidity.enricher.util.StringStorage.IDCARD_PASSPORT;

import java.time.LocalDate;
import java.util.HashSet;
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
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.ManualPerson;
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
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;

@CustomLog
@Service
@RequiredArgsConstructor
public class ManualPersonEnricher implements Enricher {

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

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;

    @Override
    public void enrich(UUID revision) {
        logStart(MANUAL_PERSON);

        StatusChanger statusChanger = new StatusChanger(revision, MANUAL_PERSON, ENRICHER);

        long[] counter = new long[1];
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        FileDescription file = fileDescriptionRepository.findByUuid(revision).orElseThrow(() ->
                new RuntimeException("Can't find file with id = " + revision));
        Page<ManualPerson> onePage = manualPersonRepository.findAllByUuid(file, pageRequest);
        long count = manualPersonRepository.countAllByUuid(file);
        statusChanger.newStage(null, "enriching", count, null);
        String fileName = fileFormatUtil.getLogFileName(revision.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, MANUAL_PERSON, revision));

        ImportSource source = isr.findImportSourceByName("manual");

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            Set<YPerson> personSet = new HashSet<>();
            Set<String> passportSeries = new HashSet<>();
            Set<Integer> passportNumbers = new HashSet<>();
            Set<Long> codes = new HashSet<>();

            onePage.forEach(r -> {
                if (StringUtils.isNotBlank(r.getPassLocalNum())) {
                    passportSeries.add(r.getPassLocalSerial());
                    passportNumbers.add(Integer.parseInt(r.getPassLocalNum()));
                }
                if (StringUtils.isNotBlank(r.getPassIntNum())) {
                    String passportNo = r.getPassIntNum().substring(2);
                    String passportSerial = r.getPassIntNum().substring(0, 2);
                    passportSeries.add(passportSerial);
                    passportNumbers.add(Integer.parseInt(passportNo));
                }
                if (StringUtils.isNotBlank(r.getPassIdNum())) {
                    passportSeries.add(null);
                    passportNumbers.add(Integer.parseInt(r.getPassIdNum()));
                }
                if (StringUtils.isNotBlank(r.getOkpo())) {
                    codes.add(Long.parseLong(r.getOkpo()));
                }
            });

            Set<YINN> inns = yinnRepository.findInns(codes);
            Set<YPassport> passports = passportRepository.findPassports(passportSeries, passportNumbers);

            Set<YPerson> savedPersonSet = new HashSet<>();

            if (!inns.isEmpty())
                savedPersonSet = ypr.findPeopleInns(codes);
            if (!passports.isEmpty())
                savedPersonSet.addAll(ypr.findPeoplePassports(passports.parallelStream().map(YPassport::getId).collect(Collectors.toList())));
            Set<YPerson> savedPeople = savedPersonSet;

            onePage.forEach(r -> {
                String lastName = UtilString.toUpperCase(r.getLnameUk());
                String firstName = UtilString.toUpperCase(r.getFnameUk());
                String patName = UtilString.toUpperCase(r.getPnameUk());
                LocalDate birthday = stringToDate(r.getBirthday());

                YPerson person = new YPerson();
                person.setLastName(lastName);
                person.setFirstName(firstName);
                person.setPatName(patName);
                person.setBirthdate(birthday);

                if (!StringUtils.isBlank(r.getOkpo()) && r.getOkpo().matches(CONTAINS_NUMERAL_REGEX)) {
                    String inn = r.getOkpo().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                    if (isValidInn(inn, stringToDate(r.getBirthday()))) {
                        person = extender.addInn(Long.parseLong(inn), personSet, source, person, inns, savedPeople);
                    } else {
                        logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getOkpo()), "Wrong INN");
                        wrongCounter[0]++;
                    }
                }

                String passportNo = r.getPassLocalNum();
                String passportSerial = r.getPassLocalSerial();
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
                    passport.setType(DOMESTIC_PASSPORT);
                    passport.setValidity(true);
                    person = extender.addPassport(passport, personSet, source, person, savedPeople, passports);
                }

                String passportRecNo;
                if (StringUtils.isNotBlank(r.getPassIntNum())) {
                    passportNo = r.getPassIntNum().substring(2);
                    passportSerial = r.getPassIntNum().substring(0, 2);
                    passportRecNo = r.getPassIntRecNum();
                    if (isValidForeignPassport(passportNo, passportSerial, passportRecNo,
                            wrongCounter, counter, logger)) {
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
                        passport.setValidity(true);
                        person = extender.addPassport(passport, personSet, source, person, savedPeople, passports);
                    }
                }

                passportNo = r.getPassIdNum();
                passportRecNo = r.getPassIdRecNum();
                if (isValidIdPassport(passportNo, passportRecNo, wrongCounter, counter, logger)) {
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
                    person = extender.addPassport(passport, personSet, source, person, savedPeople, passports);
                }

                person = extender.addPerson(personSet, person, source, true);

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
                counter[0]++;
                statusChanger.addProcessedVolume(1);
            });

            ypr.saveAll(personSet);
            emnService.enrichYPersonMonitoringNotification(personSet);

            onePage = manualPersonRepository.findAllByUuid(file, pageRequest);
        }

        logFinish(MANUAL_PERSON, counter[0]);
        logger.finish();

        statusChanger.complete(importedRecords(counter[0]));
    }
}

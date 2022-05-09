package ua.com.solidity.enricher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.BaseDrfo;
import ua.com.solidity.db.entities.BaseElections;
import ua.com.solidity.db.entities.BaseFodb;
import ua.com.solidity.db.entities.BasePassports;
import ua.com.solidity.db.entities.StatusLogger;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.db.entities.YAltPerson;
import ua.com.solidity.db.entities.YEmail;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPhone;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.entities.Contragent;
import ua.com.solidity.enricher.model.EnricherRequest;
import ua.com.solidity.enricher.repository.BaseDrfoRepository;
import ua.com.solidity.enricher.repository.BaseElectionsRepository;
import ua.com.solidity.enricher.repository.BaseFodbRepository;
import ua.com.solidity.enricher.repository.BasePassportsRepository;
import ua.com.solidity.enricher.repository.ContragentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
@EntityScan(basePackages = {"ua.com.solidity.db.entities", "ua.com.solidity.enricher.entities"})
@EnableJpaRepositories(basePackages = {"ua.com.solidity.db.repositories", "ua.com.solidity.enricher.repository"})
public class EnricherServiceImpl implements EnricherService {
    private final BaseDrfoRepository bdr;
    private final BaseElectionsRepository ber;
    private final BaseFodbRepository bfr;
    private final BasePassportsRepository bpr;
    private final YPersonRepository ypr;
    private final ContragentRepository cr;
    private final YINNRepository yir;

    private static final String BASE_DRFO = "base_drfo";
    private static final String BASE_ELECTIONS = "base_elections";
    private static final String BASE_FODB = "base_fodb";
    private static final String BASE_PASSPORTS = "base_passports";
    private static final String CONTRAGENT = "contragent";
    private static final String ENRICHER = "ENRICHER";
    private static final String RECORDS = "records";

    private static final String NUMBER_REGEX = "^[0-9]+$";

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${statuslogger.rabbitmq.name}")
    private String queueName;

    private String importedRecords(long num) {
        return String.format("Imported %d records", num);
    }

    private void logStart(String table) {
        log.info("Data from {} are being transferred to OLAP zone", table);
    }

    private void logFinish(String table, Long rows) {
        log.info("Imported {} records from {}", rows, table);
    }

    @Override
    public void enrich(EnricherRequest er) {
        log.info("Received request to enrich {} revision {}", er.getTable(), er.getRevision());
        switch (er.getTable()) {
            case BASE_DRFO:
                baseDrfoEnrich(er.getRevision());
                break;
            case BASE_ELECTIONS:
                baseElectionsEnrich(er.getRevision());
                break;
            case BASE_FODB:
                baseFodbEnrich(er.getRevision());
                break;
            case BASE_PASSPORTS:
                basePassportsEnrich(er.getRevision());
                break;
            case CONTRAGENT:
                contragentEnrich(er.getRevision());
                break;
//            case "physical_person":
//                physicalPersonEnrich(er.getRevision());
//                break;
            default:
                log.warn("Ignoring unsupported {} enrichment", er.getTable());
        }
    }

    public void baseDrfoEnrich(UUID revision) {
        logStart(BASE_DRFO);

        LocalDateTime startTime = LocalDateTime.now();
        StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
                BASE_DRFO, ENRICHER, startTime, null, null);
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));

        long[] counter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BaseDrfo> onePage = bdr.findAllByRevision(revision, pageRequest);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<YPerson> personList = new ArrayList<>();

            onePage.forEach(r -> {
                List<YINN> yinns = yir.findByInn(r.getInn());
                if (yinns.isEmpty()) {
                    YPerson person = new YPerson();
                    person.setId(UUID.randomUUID());
                    person.setFirstName(r.getFirstName().toUpperCase());
                    person.setLastName(r.getLastName().toUpperCase());
                    person.setPatName(r.getPatName().toUpperCase());
                    person.setBirthdate(r.getBirthdate());

                    Set<YINN> inns = new HashSet<>();
                    YINN inn = new YINN();
                    inn.setInn(r.getInn());
                    inn.setPerson(person);
                    inns.add(inn);
                    person.setInns(inns);

                    Set<YAddress> addresses = new HashSet<>();

                    YAddress residenceAddress = new YAddress();
                    residenceAddress.setAddress(r.getResidenceAddress().substring(11).toUpperCase());//11=прописка
                    residenceAddress.setPerson(person);
                    addresses.add(residenceAddress);

                    YAddress address = new YAddress();
                    address.setAddress(r.getAddress().toUpperCase());
                    address.setPerson(person);
                    addresses.add(address);

                    YAddress address2 = new YAddress();
                    address2.setAddress(r.getAddress2().toUpperCase());
                    address2.setPerson(person);
                    addresses.add(address2);

                    Arrays.stream(r.getAllAddresses().split(" Адрес ")).forEach(a -> {
                        YAddress ya = new YAddress();
                        ya.setAddress(a.toUpperCase());
                        ya.setPerson(person);
                        addresses.add(ya);
                    });

                    person.setAddresses(addresses);

                    personList.add(person);
                } else {
                    YPerson yPerson = ypr.findById(yinns.get(0).getPerson().getId()).get(); //TODO: isPresent() check
                    if (!(yPerson.getFirstName().equals(r.getFirstName().toUpperCase()) &&
                    yPerson.getLastName().equals(r.getLastName().toUpperCase()) &&
                    yPerson.getPatName().equals(r.getPatName().toUpperCase()))) {
                        Set<YAltPerson> yAltPeople = yPerson.getAltPeople();
                        YAltPerson yAltPerson = new YAltPerson();
                        yAltPerson.setPerson(yPerson);
                        yAltPerson.setFirstName(r.getFirstName().toUpperCase());
                        yAltPerson.setLastName(r.getLastName().toUpperCase());
                        yAltPerson.setPatName(r.getPatName().toUpperCase());
                        yAltPerson.setLanguage("UA");
                        yAltPeople.add(yAltPerson);
                        ypr.save(yPerson);
                    }
                }
                counter[0]++;
            });

            ypr.saveAll(personList);
            onePage = bdr.findAllByRevision(revision, pageRequest);

            statusLogger = new StatusLogger(revision, counter[0], RECORDS,
                    BASE_DRFO, ENRICHER, startTime, null, null);
            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
        }

        logFinish(BASE_DRFO, counter[0]);

        statusLogger = new StatusLogger(revision, 100L, "%",
                BASE_DRFO, ENRICHER, startTime,
                LocalDateTime.now(),
                importedRecords(counter[0]));
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
    }

    public void baseElectionsEnrich(UUID revision) {
        logStart(BASE_ELECTIONS);

        LocalDateTime startTime = LocalDateTime.now();
        StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
                BASE_ELECTIONS, ENRICHER, startTime, null, null);
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));

        long[] counter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BaseElections> onePage = ber.findAllByRevision(revision, pageRequest);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<YPerson> personList = new ArrayList<>();

            onePage.forEach(r -> {
                YPerson person = new YPerson();
                person.setId(UUID.randomUUID());
                final String[] fio = r.getFio().split(" ");
                person.setLastName(fio[0]);
                person.setFirstName(fio[1]);
                person.setPatName(fio[2]);
                person.setBirthdate(r.getBirthdate());

                Set<YAddress> addresses = new HashSet<>();

                YAddress address = new YAddress();
                address.setAddress(r.getAddress());
                address.setPerson(person);
                addresses.add(address);

                person.setAddresses(addresses);

                personList.add(person);
                counter[0]++;
            });

            ypr.saveAll(personList);
            onePage = ber.findAllByRevision(revision, pageRequest);

            statusLogger = new StatusLogger(revision, counter[0], RECORDS,
                    BASE_ELECTIONS, ENRICHER, startTime, null, null);
            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
        }

        logFinish(BASE_ELECTIONS, counter[0]);

        statusLogger = new StatusLogger(revision, 100L, "%",
                BASE_ELECTIONS, ENRICHER, startTime,
                LocalDateTime.now(),
                importedRecords(counter[0]));
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
    }

    public void baseFodbEnrich(UUID revision) {
        logStart(BASE_FODB);

        LocalDateTime startTime = LocalDateTime.now();
        StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
                BASE_FODB, ENRICHER, startTime, null, null);
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));

        long[] counter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BaseFodb> onePage = bfr.findAllByRevision(revision, pageRequest);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<YPerson> personList = new ArrayList<>();

            onePage.forEach(r -> {
                YPerson person = new YPerson();
                person.setId(UUID.randomUUID());
                person.setFirstName(r.getFirstNameUa());
                person.setLastName(r.getLastNameUa());
                person.setPatName(r.getMiddleNameUa());
                person.setBirthdate(r.getBirthdate());

                Set<YINN> inns = new HashSet<>();
                YINN inn = new YINN();
                inn.setInn(Long.parseLong(r.getInn()));
                inn.setPerson(person);
                inns.add(inn);
                person.setInns(inns);

                Set<YAddress> addresses = new HashSet<>();

                YAddress birthAddress = new YAddress();
                StringBuilder baString = new StringBuilder();
                baString.append(r.getBirthCountry());
                if (r.getBirthRegion() != null) {
                    baString.append(", ").append(r.getBirthRegion()).append(" ОБЛ.");
                }
                if (r.getBirthCounty() != null) {
                    baString.append(", ").append(r.getBirthCounty()).append(" Р-Н");
                }
                if (r.getBirthCityUa() != null) {
                    baString.append(", ").append(r.getBirthCityType()).append(". ").append(r.getBirthCityUa());
                }
                birthAddress.setAddress(baString.toString());
                birthAddress.setPerson(person);
                addresses.add(birthAddress);

                YAddress liveAddress = new YAddress();
                StringBuilder laString = new StringBuilder();
                laString.append(r.getLiveCountry());
                if (r.getLiveRegion() != null) {
                    laString.append(", ").append(r.getLiveRegion()).append(" ОБЛ.");
                }
                if (r.getLiveCounty() != null) {
                    laString.append(", ").append(r.getLiveCounty()).append(" Р-Н");
                }
                laString.append(", ").append(r.getLiveCityType()).append(". ").append(r.getLiveCityUa());
                laString.append(", ").append(r.getLiveStreetType()).append(r.getLiveStreet());
                if (r.getLiveBuildingNumber() != null && !r.getLiveBuildingNumber().equals("0")) {
                    laString.append(" ").append(r.getLiveBuildingNumber());
                }
                if (r.getLiveBuildingApartment() != null && !r.getLiveBuildingApartment().equals("0")) {
                    laString.append(", КВ. ").append(r.getLiveBuildingApartment());
                }
                liveAddress.setAddress(laString.toString());
                liveAddress.setPerson(person);
                addresses.add(liveAddress);

                person.setAddresses(addresses);

                personList.add(person);
                counter[0]++;
            });

            ypr.saveAll(personList);
            onePage = bfr.findAllByRevision(revision, pageRequest);

            statusLogger = new StatusLogger(revision, counter[0], RECORDS,
                    BASE_FODB, ENRICHER, startTime, null, null);
            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
        }

        logFinish(BASE_FODB, counter[0]);

        statusLogger = new StatusLogger(revision, 100L, "%",
                BASE_FODB, ENRICHER, startTime, LocalDateTime.now(),
                importedRecords(counter[0]));
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
    }

    public void basePassportsEnrich(UUID revision) {
        logStart(BASE_PASSPORTS);

        LocalDateTime startTime = LocalDateTime.now();
        StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
                BASE_PASSPORTS, ENRICHER, startTime, null, null);
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));

        long[] counter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BasePassports> onePage = bpr.findAllByRevision(revision, pageRequest);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<YPerson> personList = new ArrayList<>();

            onePage.forEach(r -> {

                if (r.getLastName() != null) { // We skip person without last name
                    YPerson person = new YPerson();
                    person.setId(UUID.randomUUID());
                    person.setLastName(r.getLastName());
                    person.setFirstName(r.getFirstName());
                    person.setPatName(r.getMiddleName());
                    person.setBirthdate(r.getBirthdate());

                    if (r.getInn() != null) {
                        Set<YINN> inns = new HashSet<>();
                        YINN inn = new YINN();
                        inn.setInn(Long.parseLong(r.getInn()));
                        inn.setPerson(person);
                        inns.add(inn);
                        person.setInns(inns);
                    }

                    if (r.getSerial() != null) {
                        Set<YPassport> passports = new HashSet<>();
                        YPassport passport = new YPassport();
                        passport.setSeries(r.getSerial());
                        passport.setNumber(Integer.parseInt(r.getPassId()));
                        passport.setValidity(true);
                        passport.setType("UA_DOMESTIC");

                        passport.setPerson(person);
                        passports.add(passport);
                        person.setPassports(passports);
                    }

                    personList.add(person);
                }
                counter[0]++;
            });

            ypr.saveAll(personList);
            onePage = bpr.findAllByRevision(revision, pageRequest);

            statusLogger = new StatusLogger(revision, counter[0], RECORDS,
                    BASE_PASSPORTS, ENRICHER, startTime, null, null);
            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
        }

        logFinish(BASE_PASSPORTS, counter[0]);

        statusLogger = new StatusLogger(revision, 100L, "%",
                BASE_PASSPORTS, ENRICHER, startTime, LocalDateTime.now(),
                importedRecords(counter[0]));
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
    }

    public void contragentEnrich(UUID revision) {
        logStart(CONTRAGENT);

        LocalDateTime startTime = LocalDateTime.now();
        StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
                CONTRAGENT, ENRICHER, startTime, null, null);
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));

        long[] counter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<Contragent> onePage = cr.findAllByRevision(revision, pageRequest);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<YPerson> personList = new ArrayList<>();

            onePage.forEach(r -> {
                if (!StringUtils.isBlank(r.getClientLastName())) { // We skip person without last name
                    YPerson person = new YPerson();
                    person.setId(UUID.randomUUID());
                    List<String> splitPersonNames = Arrays.asList(r.getName().split("[ ]+"));
                    String splitPatronymic = splitPersonNames.size() == 3 ? splitPersonNames.get(2) : null;

                    if (!StringUtils.isBlank(r.getClientPatronymicName()))
                        person.setPatName(r.getClientPatronymicName());
                    else person.setPatName(splitPatronymic);
                    person.setLastName(r.getClientLastName());

                    if (!StringUtils.isBlank(r.getClientName())) person.setFirstName(r.getClientName());
                    else if (splitPersonNames.size() == 3) {
                        person.setFirstName(splitPersonNames.get(1).equals(r.getClientLastName()) ? splitPersonNames.get(0) : splitPersonNames.get(1));
                    }

                    person.setBirthdate(r.getClientBirthday());

                    String identifyCode = r.getIdentifyCode();
                    if (!StringUtils.isBlank(identifyCode) && identifyCode.matches(NUMBER_REGEX)) {
                        Set<YINN> inns = new HashSet<>();
                        YINN inn = new YINN();
                        inn.setInn(Long.parseLong(identifyCode));
                        inn.setPerson(person);
                        inns.add(inn);
                        person.setInns(inns);
                    }

                    String passportNo = r.getPassportNo();
                    if (!StringUtils.isBlank(passportNo) && passportNo.matches(NUMBER_REGEX)) {
                        Set<YPassport> passports = new HashSet<>();
                        YPassport passport = new YPassport();
                        passport.setSeries(r.getPassportSerial());
                        passport.setNumber(Integer.parseInt(passportNo));
                        passport.setAuthority(r.getPassportIssuePlace());
                        passport.setIssued(r.getPassportIssueDate());
                        passport.setEndDate(r.getPassportEndDate());
                        passport.setRecordNumber(null);
                        passport.setValidity(true);
                        passport.setType("UA_DOMESTIC");

                        passport.setPerson(person);
                        passports.add(passport);
                        person.setPassports(passports);
                    }

                    person.setTags(new HashSet<>());
                    if (r.getWorkplace() != null) {
                        YTag tag = new YTag();
                        tag.setName(r.getWorkplace());
                        tag.setAsOf(LocalDate.now());
                        tag.setSource(CONTRAGENT);

                        tag.setPerson(person);
                        person.getTags().add(tag);
                    }

                    if (r.getWorkPosition() != null) {
                        YTag tag = new YTag();
                        tag.setName(r.getWorkPosition());
                        tag.setAsOf(LocalDate.now());
                        tag.setSource(CONTRAGENT);

                        tag.setPerson(person);
                        person.getTags().add(tag);
                    }

                    person.setPhones(new HashSet<>());
                    Stream.of(r.getPhones(), r.getMobilePhone(), r.getPhoneHome()).forEach(c -> {
                        if (c != null) {
                            String phoneCleaned = c.replaceAll("[^0-9]+", "");
                            if (!StringUtils.isBlank(phoneCleaned)) {
                                YPhone phone = new YPhone();
                                phone.setPhone(phoneCleaned);

                                phone.setPerson(person);
                                person.getPhones().add(phone);
                            }
                        }
                    });

                    person.setAddresses(new HashSet<>());
                    Stream.of(r.getAddress(), r.getBirthplace()).forEach(c -> {
                        if (!StringUtils.isBlank(c)) {
                            YAddress address = new YAddress();
                            address.setAddress(c);

                            address.setPerson(person);
                            person.getAddresses().add(address);
                        }
                    });

                    if (!StringUtils.isBlank(r.getAlternateName())) {
                        Set<YAltPerson> altPeople = new HashSet<>();
                        YAltPerson altPerson = new YAltPerson();
                        List<String> splitAltPersonNames = Arrays.asList(r.getAlternateName().split("[ ]+"));

                        List<Consumer<String>> consumers = Arrays.asList(altPerson::setFirstName,
                                altPerson::setLastName,
                                altPerson::setPatName);

                        for (int i = 0; i < splitAltPersonNames.size(); i++) {
                            if (i == 2) {
                                StringBuilder sb = new StringBuilder(splitAltPersonNames.get(2));
                                for (int j = 3; j < splitAltPersonNames.size(); j++) {
                                    sb.append(' ').append(splitAltPersonNames.get(j));
                                }
                                consumers.get(2).accept(sb.toString());
                                break;
                            }
                            consumers.get(i).accept(splitAltPersonNames.get(i));
                        }
                        altPerson.setLanguage("UA");

                        altPerson.setPerson(person);
                        altPeople.add(altPerson);
                        person.setAltPeople(altPeople);
                    }

                    if (!StringUtils.isBlank(r.getEmail())) {
                        Set<YEmail> emails = new HashSet<>();
                        YEmail email = new YEmail();
                        email.setEmail(r.getEmail());

                        email.setPerson(person);
                        emails.add(email);
                        person.setEmails(emails);
                    }
                    personList.add(person);
                }
                counter[0]++;
            });

            ypr.saveAll(personList);
            onePage = cr.findAllByRevision(revision, pageRequest);

            statusLogger = new StatusLogger(revision, counter[0], RECORDS,
                    CONTRAGENT, ENRICHER, startTime, null, null);
            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
        }

        logFinish(CONTRAGENT, counter[0]);

        statusLogger = new StatusLogger(revision, 100L, "%",
                CONTRAGENT, ENRICHER, startTime, LocalDateTime.now(),
                importedRecords(counter[0]));
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
    }
}

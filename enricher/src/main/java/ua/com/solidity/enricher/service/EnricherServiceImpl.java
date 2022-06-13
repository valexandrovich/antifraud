package ua.com.solidity.enricher.service;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import ua.com.solidity.common.DefaultErrorLogger;
import ua.com.solidity.common.ErrorReport;
import ua.com.solidity.common.UtilString;
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.BaseCreator;
import ua.com.solidity.db.entities.BaseDirector;
import ua.com.solidity.db.entities.BaseDrfo;
import ua.com.solidity.db.entities.BaseElections;
import ua.com.solidity.db.entities.BaseFodb;
import ua.com.solidity.db.entities.BasePassports;
import ua.com.solidity.db.entities.Contragent;
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.ManualPerson;
import ua.com.solidity.db.entities.ManualTag;
import ua.com.solidity.db.entities.StatusLogger;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.db.entities.YAltPerson;
import ua.com.solidity.db.entities.YEmail;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YManager;
import ua.com.solidity.db.entities.YManagerType;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPhone;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.ContragentRepository;
import ua.com.solidity.db.repositories.FileDescriptionRepository;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.ManualPersonRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YManagerRepository;
import ua.com.solidity.db.repositories.YManagerTypeRepository;
import ua.com.solidity.db.repositories.YPassportRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.model.EnricherRequest;
import ua.com.solidity.enricher.repository.BaseCreatorRepository;
import ua.com.solidity.enricher.repository.BaseDirectorRepository;
import ua.com.solidity.enricher.repository.BaseDrfoRepository;
import ua.com.solidity.enricher.repository.BaseElectionsRepository;
import ua.com.solidity.enricher.repository.BaseFodbRepository;
import ua.com.solidity.enricher.repository.BasePassportsRepository;
import ua.com.solidity.enricher.util.FileFormatUtil;

@Slf4j
@RequiredArgsConstructor
@Service
@EntityScan(basePackages = {"ua.com.solidity.db.entities"})
@EnableJpaRepositories(basePackages = {"ua.com.solidity.db.repositories", "ua.com.solidity.enricher.repository"})
public class EnricherServiceImpl implements EnricherService {
    private final BaseDrfoRepository bdr;
    private final BaseElectionsRepository ber;
    private final BaseFodbRepository bfr;
    private final BasePassportsRepository bpr;
    private final YPersonRepository ypr;
    private final ContragentRepository cr;
    private final BaseDirectorRepository baseDirectorRepository;
    private final YINNRepository yir;
    private final ManualPersonRepository manualPersonRepository;
    private final FileDescriptionRepository fileDescriptionRepository;
    private final EnricherMonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final YPassportRepository yPassportRepository;
    private final FileFormatUtil fileFormatUtil;
    private final YManagerRepository yManagerRepository;
    private final YManagerTypeRepository yManagerTypeRepository;
    private final BaseCreatorRepository baseCreatorRepository;

    private static final String BASE_DRFO = "base_drfo";
    private static final String BASE_ELECTIONS = "base_elections";
    private static final String BASE_FODB = "base_fodb";
    private static final String BASE_PASSPORTS = "base_passports";
    private static final String CONTRAGENT = "contragent";
    private static final String MANUAL_PERSON = "manual_person";
    private static final String BASE_DIRECTOR = "base_director";
    private static final String BASE_CREATOR = "base_creator";
    private static final String ENRICHER = "ENRICHER";
    private static final String RECORDS = "records";
    private static final String DOMESTIC_PASSPORT = "UA_DOMESTIC";
    private static final String IDCARD_PASSPORT = "UA_IDCARD";
    private static final String FOREIGN_PASSPORT = "UA_FOREIGN";
    private static final String DIRECTOR = "DIRECTOR";
    public static final LocalDate START_DATE = LocalDate.of(1900, 1, 1);
    private static final String CREATOR = "CREATOR";

    private static final String NUMBER_REGEX = "^[0-9]+$";
    private static final String ALL_NOT_NUMBER = "[^0-9]";
    private static final String INN_REGEX = "^[\\d]{10}$";
    private static final String OKPO_REGEX = "^[\\d]{8,9}$";
    private static final String PASS_NUMBER_REGEX = "^[\\d]{6}$";
    private static final String IDCARD_NUMBER_REGEX = "^[\\d]{9}$";
    private static final String FOREIGN_SERIES_REGEX = "^[A-Z]{2}$";
    private static final String DOMESTIC_SERIES_REGEX = "^[А-Я]{2}$";
    private static final String RECORD_NUMBER_REGEX = "^[\\d]{8}-[\\d]{5}$";
    private static final String INN_FORMAT_REGEX = "%010d";
    private static final String LATIN_LETTERS = "ABEKMHOPCTXY";
    private static final String CYRILLIC_LETTERS = "АВЕКМНОРСТХУ";
    private static final String ENRICHER_ERROR_REPORT_MESSAGE = "Enricher error report for table: {} with revision: {}";

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${statuslogger.rabbitmq.name}")
    private String queueName;
    private boolean cachedCopy;

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
            case MANUAL_PERSON:
                manualPersonEnrich(er.getRevision());
                break;
            case BASE_DIRECTOR:
                baseDirectorEnricher(er.getRevision());
                break;
            case BASE_CREATOR:
                baseCreatorEnricher(er.getRevision());
                break;
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
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BaseDrfo> onePage = bdr.findAllByRevision(revision, pageRequest);
        String fileName = fileFormatUtil.getLogFileName(revision.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_DRFO, revision));

        ImportSource source = isr.findImportSourceByName(BASE_DRFO);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<YPerson> personList = new ArrayList<>();
            Set<YINN> yinnList = new HashSet<>();

            onePage.forEach(r -> {
                String lastName = UtilString.toUpperCase(r.getLastName());
                String firstName = UtilString.toUpperCase(r.getFirstName());
                String patName = UtilString.toUpperCase(r.getPatName());

                Long yinn = null;
                if (r.getInn() != null) {
                    String inn = String.format(INN_FORMAT_REGEX, r.getInn());
                    if (isValidInn(inn, r.getBirthdate(), logger, wrongCounter, counter))
                        yinn = Long.parseLong(inn);
                }

                YPerson person = addPerson(yinnList, personList, yinn,
                        lastName, firstName, patName, r.getBirthdate(), source);

                Set<String> addresses = new HashSet<>();
                if (StringUtils.isBlank(r.getAllAddresses()))
                    addresses = Arrays.stream(r.getAllAddresses().split(" Адрес "))
                            .map(UtilString::toUpperCase).collect(Collectors.toSet());
                if (StringUtils.isBlank(r.getResidenceAddress()))
                    addresses.add(r.getResidenceAddress().substring(11).toUpperCase());
                if (!StringUtils.isBlank(r.getAddress())) addresses.add(r.getAddress().toUpperCase());
                if (!StringUtils.isBlank(r.getAddress2())) addresses.add(r.getAddress2().toUpperCase());

                addAddresses(person, addresses, source);

                addAltPerson(person, lastName, firstName, patName, "UK", source);

                if (!cachedCopy) {
                    personList.add(person);
                    counter[0]++;
                }
            });

            ypr.saveAll(personList);
            emnService.enrichMonitoringNotification(personList);

            onePage = bdr.findAllByRevision(revision, pageRequest);

            statusLogger = new StatusLogger(revision, counter[0], RECORDS,
                    BASE_DRFO, ENRICHER, startTime, null, null);
            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
        }

        logFinish(BASE_DRFO, counter[0]);

        statusLogger = new StatusLogger(revision, 100L, "%",
                BASE_DRFO, ENRICHER, startTime, LocalDateTime.now(),
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
        String fileName = fileFormatUtil.getLogFileName(revision.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_ELECTIONS, revision));

        ImportSource source = isr.findImportSourceByName(BASE_ELECTIONS);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<YPerson> personList = new ArrayList<>();

            onePage.forEach(r -> {
                String[] fio = null;
                String fistName = "";
                String lastName = "";
                String patName = "";
                if (!StringUtils.isBlank(r.getFio())) fio = r.getFio().split(" ");
                if (fio != null && fio.length >= 1) lastName = UtilString.toUpperCase(fio[0]);
                if (fio != null && fio.length >= 2) fistName = UtilString.toUpperCase(fio[1]);
                if (fio != null && fio.length >= 3) patName = UtilString.toUpperCase(fio[2]);

                if (StringUtils.isBlank(lastName))
                    logError(logger, (counter[0] + 1L), "LastName: " + lastName, "Empty last name");
                else {
                    YPerson person = addPerson(new HashSet<>(), personList, null,
                            lastName, fistName, patName, r.getBirthdate(), source);

                    if (!StringUtils.isBlank(r.getAddress())) {
                        String[] partAddress = r.getAddress().split(", ");
                        StringBuilder sbAddress = new StringBuilder();
                        for (int i = partAddress.length - 1; i > 0; i--) {
                            sbAddress.append(partAddress[i].toUpperCase()).append(", ");
                        }
                        sbAddress.append(partAddress[0].toUpperCase());

                        Set<String> addresses = new HashSet<>();
                        addresses.add(sbAddress.toString());

                        addAddresses(person, addresses, source);
                    }

                    if (!cachedCopy) {
                        personList.add(person);
                        counter[0]++;
                    }
                }
            });

            ypr.saveAll(personList);
            emnService.enrichMonitoringNotification(personList);

            onePage = ber.findAllByRevision(revision, pageRequest);

            statusLogger = new StatusLogger(revision, counter[0], RECORDS,
                    BASE_ELECTIONS, ENRICHER, startTime, null, null);
            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
        }

        logFinish(BASE_ELECTIONS, counter[0]);
        logger.finish();

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
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BaseFodb> onePage = bfr.findAllByRevision(revision, pageRequest);
        String fileName = fileFormatUtil.getLogFileName(revision.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_FODB, revision));

        ImportSource source = isr.findImportSourceByName(BASE_FODB);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<YPerson> personList = new ArrayList<>();
            Set<YINN> yinnList = new HashSet<>();

            onePage.forEach(r -> {

                String lastName = UtilString.toUpperCase(r.getLastNameUa());
                String firstName = UtilString.toUpperCase(r.getFirstNameUa());
                String patName = UtilString.toUpperCase(r.getMiddleNameUa());

                Long inn = null;
                if (!StringUtils.isBlank(r.getInn())) {
                    String code = String.format(INN_FORMAT_REGEX, Long.parseLong(r.getInn().replaceAll(ALL_NOT_NUMBER, "")));
                    if (isValidInn(code, r.getBirthdate(), logger, wrongCounter, counter))
                        inn = Long.parseLong(code);
                }

                YPerson person = addPerson(yinnList, personList, inn,
                        lastName, firstName, patName, r.getBirthdate(), source);

                Set<String> addresses = new HashSet<>();
                StringBuilder laString = new StringBuilder();
                if (!StringUtils.isBlank(r.getLiveRegion())) {
                    laString.append(r.getLiveRegion().toUpperCase()).append(" ОБЛАСТЬ");
                }
                if (!StringUtils.isBlank(r.getLiveCounty())) {
                    laString.append(", ").append(r.getLiveCounty().toUpperCase()).append(" Р-Н");
                }
                laString.append(", ").append(UtilString.toUpperCase(r.getLiveCityType()))
                        .append(". ").append(UtilString.toUpperCase(r.getLiveCityUa()));
                laString.append(", ").append(UtilString.toUpperCase(r.getLiveStreetType())).append(" ")
                        .append(UtilString.toUpperCase(r.getLiveStreet()));
                if (!StringUtils.isBlank(r.getLiveBuildingNumber()) && !r.getLiveBuildingNumber().equals("0")) {
                    laString.append(" ").append(r.getLiveBuildingNumber().toUpperCase());
                }
                if (!StringUtils.isBlank(r.getLiveBuildingApartment()) && !r.getLiveBuildingApartment().equals("0")) {
                    laString.append(", КВ. ").append(r.getLiveBuildingApartment().toUpperCase());
                }
                addresses.add(laString.toString());

                addAddresses(person, addresses, source);

                addAltPerson(person, lastName, firstName, patName, "UK", source);

                if (!cachedCopy) {
                    personList.add(person);
                    counter[0]++;
                }
            });

            ypr.saveAll(personList);
            emnService.enrichMonitoringNotification(personList);

            onePage = bfr.findAllByRevision(revision, pageRequest);

            statusLogger = new StatusLogger(revision, counter[0], RECORDS,
                    BASE_FODB, ENRICHER, startTime, null, null);
            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
        }

        logFinish(BASE_FODB, counter[0]);
        logger.finish();

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
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BasePassports> onePage = bpr.findAllByRevision(revision, pageRequest);
        String fileName = fileFormatUtil.getLogFileName(revision.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_PASSPORTS, revision));

        ImportSource source = isr.findImportSourceByName(BASE_PASSPORTS);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<YPerson> personList = new ArrayList<>();
            Set<YINN> yinnList = new HashSet<>();

            onePage.forEach(r -> {
                String lastName = UtilString.toUpperCase(r.getLastName());
                String firstName = UtilString.toUpperCase(r.getFirstName());
                String patName = UtilString.toUpperCase(r.getMiddleName());

                if (!StringUtils.isBlank(lastName)) {                          //skip person without lastName
                    Long inn = null;
                    if (!StringUtils.isBlank(r.getInn())) {
                        String code = String.format(INN_FORMAT_REGEX, Long.parseLong(r.getInn().replaceAll(ALL_NOT_NUMBER, "")));
                        if (isValidInn(code, r.getBirthdate(), logger, wrongCounter, counter))
                            inn = Long.parseLong(code);
                    }

                    YPerson person = addPerson(yinnList, personList, inn,
                            lastName, firstName, patName, r.getBirthdate(), source);

                    String passportNo = r.getPassId();
                    String passportSerial = r.getSerial();
                    int number;
                    if (isValidLocalPassport(passportNo, passportSerial, wrongCounter, counter, logger)) {
                        passportSerial = transliterationToCyrillicLetters(passportSerial);
                        number = Integer.parseInt(passportNo);
                        addPassport(person, passportSerial, number, null,
                                null, null, DOMESTIC_PASSPORT, personList,
                                wrongCounter, counter, logger, source);
                    }

                    addAltPerson(person, lastName, firstName, patName, "UK", source);

                    if (!cachedCopy) {
                        personList.add(person);
                        counter[0]++;
                    }
                }
            });

            ypr.saveAll(personList);
            emnService.enrichMonitoringNotification(personList);

            onePage = bpr.findAllByRevision(revision, pageRequest);

            statusLogger = new StatusLogger(revision, counter[0], RECORDS,
                    BASE_PASSPORTS, ENRICHER, startTime, null, null);
            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
        }

        logFinish(BASE_PASSPORTS, counter[0]);
        logger.finish();

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

        ImportSource source = isr.findImportSourceByName("dwh");

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<YPerson> personList = new ArrayList<>();
            Set<YINN> yinnList = new HashSet<>();

            onePage.forEach(r -> {
                Optional<YINN> yinnSavedOptional;
                Optional<YINN> yinnCachedOptional;
                String identifyCode = r.getIdentifyCode();

                if (StringUtils.isNotBlank(identifyCode) && identifyCode.matches(NUMBER_REGEX)) {// We skip person without inn
                    long inn = Long.parseLong(identifyCode);
                    yinnSavedOptional = yir.findByInn(inn);
                    yinnCachedOptional = yinnList.stream().filter(i -> i.getInn() == inn).findAny();
                    boolean cached = false;
                    boolean saved = false;

                    YPerson person = null;
                    if (yinnSavedOptional.isPresent()) {
                        person = yinnSavedOptional.get().getPerson();
                        saved = true;
                    }
                    if (yinnCachedOptional.isPresent()) {
                        person = yinnCachedOptional.get().getPerson();
                        cached = true;
                    }
                    if (person == null) person = new YPerson(UUID.randomUUID());
                    addSource(person.getImportSources(), source);

                    List<String> splitPersonNames = Arrays.asList(r.getName().split("[ ]+"));
                    String splitPatronymic = splitPersonNames.size() == 3 ? splitPersonNames.get(2) : null;

                    if (!StringUtils.isBlank(r.getClientPatronymicName()))
                        person.setPatName(UtilString.toUpperCase(chooseNotBlank(person.getPatName(), r.getClientPatronymicName())));
                    else
                        person.setPatName(UtilString.toUpperCase(chooseNotBlank(person.getPatName(), splitPatronymic)));
                    person.setLastName(UtilString.toUpperCase(chooseNotBlank(person.getLastName(), r.getClientLastName())));

                    if (!StringUtils.isBlank(r.getClientName()))
                        person.setFirstName(UtilString.toUpperCase(r.getClientName()));
                    else if (splitPersonNames.size() == 3) {
                        String firstName = splitPersonNames.get(1).equalsIgnoreCase(r.getClientLastName()) ? splitPersonNames.get(0) : splitPersonNames.get(1);
                        person.setFirstName(UtilString.toUpperCase(firstName));
                    }

                    person.setBirthdate(chooseNotNull(person.getBirthdate(), r.getClientBirthday()));

                    YINN yinn = null;
                    if (yinnSavedOptional.isPresent()) yinn = yinnSavedOptional.get();
                    if (yinnCachedOptional.isPresent()) yinn = yinnCachedOptional.get();
                    if (yinn == null) yinn = new YINN();
                    addSource(yinn.getImportSources(), source);
                    if (!cached && !saved) {
                        yinn.setInn(Long.parseLong(identifyCode));
                        yinn.setPerson(person);
                        yinn.getPerson().getInns().add(yinn);
                    }

                    String passportNo = r.getPassportNo();
                    String passportSerial = r.getPassportSerial();
                    Integer number;
                    if (StringUtils.isNotBlank(passportNo) && passportNo.matches(NUMBER_REGEX)) {
                        number = Integer.parseInt(passportNo);
                        Optional<YPassport> passportOptional = person.getPassports()
                                .stream()
                                .filter(p -> Objects.equals(p.getNumber(), number) && UtilString.equalsIgnoreCase(p.getSeries(), passportSerial))
                                .findAny();
                        YPassport passport = passportOptional.orElseGet(YPassport::new);
                        addSource(passport.getImportSources(), source);
                        passport.setSeries(UtilString.toUpperCase(passportSerial));
                        passport.setNumber(number);
                        passport.setAuthority(UtilString.toUpperCase(chooseNotBlank(passport.getAuthority(), r.getPassportIssuePlace())));
                        passport.setIssued(chooseNotNull(passport.getIssued(), r.getPassportIssueDate()));
                        passport.setEndDate(chooseNotNull(passport.getEndDate(), r.getPassportEndDate()));
                        passport.setRecordNumber(null);
                        passport.setValidity(true);
                        passport.setType(DOMESTIC_PASSPORT);

                        if (passportOptional.isEmpty()) {
                            passport.setPerson(person);
                            person.getPassports().add(passport);
                        }
                    }

                    Optional<YTag> tagPlaceOptional = person.getTags()
                            .stream()
                            .filter(t -> UtilString.equalsIgnoreCase(t.getName(), r.getWorkplace()))
                            .findAny();
                    if (StringUtils.isNotBlank(r.getWorkplace())) {
                        YTag tag = tagPlaceOptional.orElseGet(YTag::new);
                        addSource(tag.getImportSources(), source);
                        tag.setName(UtilString.toUpperCase(r.getWorkplace()));
                        if (tag.getAsOf() == null) tag.setAsOf(LocalDate.now());
                        tag.setSource(CONTRAGENT);

                        if (tagPlaceOptional.isEmpty()) {
                            tag.setPerson(person);
                            person.getTags().add(tag);
                        }
                    }

                    Optional<YTag> tagPositionOptional = person.getTags()
                            .stream()
                            .filter(t -> UtilString.equalsIgnoreCase(t.getName(), r.getWorkPosition()))
                            .findAny();
                    if (StringUtils.isNotBlank(r.getWorkPosition())) {
                        YTag tag = tagPositionOptional.orElseGet(YTag::new);
                        addSource(tag.getImportSources(), source);
                        tag.setName(UtilString.toUpperCase(r.getWorkPosition()));
                        if (tag.getAsOf() == null) tag.setAsOf(LocalDate.now());
                        tag.setSource(CONTRAGENT);

                        if (tagPositionOptional.isEmpty()) {
                            tag.setPerson(person);
                            person.getTags().add(tag);
                        }
                    }

                    YPerson finalPerson = person;
                    Stream.of(r.getPhones(), r.getMobilePhone(), r.getPhoneHome()).forEach(phone -> {
                        if (phone != null) {
                            String phoneCleaned = phone.replaceAll("[^0-9]+", "");
                            if (StringUtils.isNotBlank(phoneCleaned)) {
                                Optional<YPhone> yphoneOptional = finalPerson.getPhones()
                                        .stream()
                                        .filter(yphone -> yphone.getPhone().equals(phoneCleaned))
                                        .findFirst();

                                YPhone yPhone = yphoneOptional.orElseGet(YPhone::new);
                                addSource(yPhone.getImportSources(), source);
                                yPhone.setPhone(phoneCleaned);

                                if (yphoneOptional.isEmpty()) {
                                    yPhone.setPerson(finalPerson);
                                    finalPerson.getPhones().add(yPhone);
                                }
                            }
                        }
                    });

                    YPerson finalPerson1 = person;
                    Stream.of(r.getAddress(), r.getBirthplace()).forEach(address -> {
                        if (StringUtils.isNotBlank(address)) {
                            Optional<YAddress> yAddressOptional = finalPerson1.getAddresses()
                                    .stream()
                                    .filter(yAddress -> yAddress.getAddress().equalsIgnoreCase(address))
                                    .findAny();

                            YAddress yAddress = yAddressOptional.orElseGet(YAddress::new);
                            addSource(yAddress.getImportSources(), source);
                            yAddress.setAddress(UtilString.toUpperCase(address.toUpperCase()));

                            if (yAddressOptional.isEmpty()) {
                                yAddress.setPerson(finalPerson1);
                                finalPerson1.getAddresses().add(yAddress);
                            }
                        }
                    });

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
                        Optional<YAltPerson> altPersonOptional = person.getAltPeople().stream()
                                .filter(yAltPerson -> StringUtils.equalsIgnoreCase(yAltPerson.getFirstName(), names[0]) &&
                                        StringUtils.equalsIgnoreCase(yAltPerson.getLastName(), names[1]) &&
                                        StringUtils.equalsIgnoreCase(yAltPerson.getPatName(), names[2]))
                                .findFirst();

                        YAltPerson newAltPerson = altPersonOptional.orElseGet(YAltPerson::new);
                        addSource(newAltPerson.getImportSources(), source);
                        newAltPerson.setFirstName(UtilString.toUpperCase(names[0]));
                        newAltPerson.setLastName(UtilString.toUpperCase(names[1]));
                        newAltPerson.setPatName(UtilString.toUpperCase(names[2]));
                        newAltPerson.setLanguage("EN");

                        if (altPersonOptional.isEmpty()) {
                            newAltPerson.setPerson(person);
                            person.getAltPeople().add(newAltPerson);
                        }
                    }

                    if (!StringUtils.isBlank(r.getEmail())) {
                        Optional<YEmail> emailOptional = person.getEmails()
                                .stream()
                                .filter(e -> StringUtils.equalsIgnoreCase(r.getEmail(), e.getEmail()))
                                .findAny();
                        YEmail email = emailOptional.orElseGet(YEmail::new);
                        addSource(email.getImportSources(), source);
                        email.setEmail(UtilString.toLowerCase(r.getEmail()));

                        if (emailOptional.isEmpty()) {
                            email.setPerson(person);
                            person.getEmails().add(email);
                        }
                    }
                    if (!cached) {
                        personList.add(person);
                        yinnList.add(yinn);
                        counter[0]++;
                    }
                }

            });

            ypr.saveAll(personList);
            emnService.enrichMonitoringNotification(personList);


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

    private void baseDirectorEnricher(UUID revision) {
        logStart(BASE_DIRECTOR);

        LocalDateTime startTime = LocalDateTime.now();
        StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
                BASE_DIRECTOR, ENRICHER, startTime, null, null);
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));

        long[] counter = new long[1];
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BaseDirector> onePage = baseDirectorRepository.findAllByRevision(revision, pageRequest);
        String fileName = fileFormatUtil.getLogFileName(revision.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_DIRECTOR, revision));

        ImportSource source = isr.findImportSourceByName(BASE_DIRECTOR);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            Set<YManager> managerList = new HashSet<>();

            onePage.forEach(r -> {
                if (!StringUtils.isBlank(r.getInn()) && !StringUtils.isBlank(r.getOkpo())) {
                    String okpo = String.format("%08d", Long.parseLong(r.getOkpo().replaceAll(ALL_NOT_NUMBER, "")));
                    String inn = String.format(INN_FORMAT_REGEX, Long.parseLong(r.getInn().replaceAll(ALL_NOT_NUMBER, "")));
                    YManager manager;
                    if (isValidInn(inn, null, logger, wrongCounter, counter)
                            && isValidOkpo(okpo, logger, wrongCounter, counter)) {
                        manager = addManager(okpo, inn, DIRECTOR, source, managerList);
                        managerList.add(manager);
                    }
                } else {
                    logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {} and OKPO: {}", r.getInn(), r.getOkpo()), "Empty INN or OKPO");
                    wrongCounter[0]++;
                }
                counter[0]++;
            });

            yManagerRepository.saveAll(managerList);

            onePage = baseDirectorRepository.findAllByRevision(revision, pageRequest);

            statusLogger = new StatusLogger(revision, counter[0], RECORDS,
                    BASE_DIRECTOR, ENRICHER, startTime, null, null);
            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
        }

        logFinish(BASE_DIRECTOR, counter[0]);
        logger.finish();

        statusLogger = new StatusLogger(revision, 100L, "%",
                BASE_DIRECTOR, ENRICHER, startTime,
                LocalDateTime.now(),
                importedRecords(counter[0]));
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
    }

    private void baseCreatorEnricher(UUID revision) {
        logStart(BASE_CREATOR);

        LocalDateTime startTime = LocalDateTime.now();
        StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
                BASE_CREATOR, ENRICHER, startTime, null, null);
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));

        long[] counter = new long[1];
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BaseCreator> onePage = baseCreatorRepository.findAllByRevision(revision, pageRequest);
        String fileName = fileFormatUtil.getLogFileName(revision.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_CREATOR, revision));

        ImportSource source = isr.findImportSourceByName(BASE_CREATOR);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            Set<YManager> managerList = new HashSet<>();

            onePage.forEach(r -> {
                if (!StringUtils.isBlank(r.getInn()) && !StringUtils.isBlank(r.getOkpo())) {
                    String okpo = String.format("%08d", Long.parseLong(r.getOkpo().replaceAll(ALL_NOT_NUMBER, "")));
                    String inn = String.format(INN_FORMAT_REGEX, Long.parseLong(r.getInn().replaceAll(ALL_NOT_NUMBER, "")));
                    YManager manager;
                    if (isValidInn(inn, null, logger, wrongCounter, counter)
                            && isValidOkpo(okpo, logger, wrongCounter, counter)) {
                        manager = addManager(okpo, inn, CREATOR, source, managerList);
                        managerList.add(manager);
                    }
                } else {
                    logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {} and OKPO: {}", r.getInn(), r.getOkpo()), "Empty INN or OKPO");
                    wrongCounter[0]++;
                }
                counter[0]++;
            });

            yManagerRepository.saveAll(managerList);

            onePage = baseCreatorRepository.findAllByRevision(revision, pageRequest);

            statusLogger = new StatusLogger(revision, counter[0], RECORDS,
                    BASE_CREATOR, ENRICHER, startTime, null, null);
            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
        }

        logFinish(BASE_CREATOR, counter[0]);
        logger.finish();

        statusLogger = new StatusLogger(revision, 100L, "%",
                BASE_CREATOR, ENRICHER, startTime,
                LocalDateTime.now(),
                importedRecords(counter[0]));
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
    }

    private void manualPersonEnrich(UUID revision) {
        LocalDateTime startTime = LocalDateTime.now();
        StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
                MANUAL_PERSON, ENRICHER, startTime, null, null);
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));

        long[] counter = new long[1];
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        FileDescription file = fileDescriptionRepository.findByUuid(revision).orElseThrow(() ->
                new RuntimeException("Can't find file with id = " + revision));
        Page<ManualPerson> onePage = manualPersonRepository.findAllByUuid(file, pageRequest);
        String fileName = fileFormatUtil.getLogFileName(revision.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, MANUAL_PERSON, revision));

        ImportSource source = isr.findImportSourceByName("manual");

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<YPerson> personList = new ArrayList<>();
            Set<YINN> yinnList = new HashSet<>();

            onePage.forEach(r -> {
                String lastName = UtilString.toUpperCase(r.getLnameUk());
                String firstName = UtilString.toUpperCase(r.getFnameUk());
                String patName = UtilString.toUpperCase(r.getPnameUk());
                LocalDate birthday = stringToDate(r.getBirthday());

                if (!StringUtils.isBlank(lastName)) {
                    Long inn = null;
                    if (!StringUtils.isBlank(r.getOkpo())) {
                        String code = String.format(INN_FORMAT_REGEX, Long.parseLong(r.getOkpo().replaceAll(ALL_NOT_NUMBER, "")));
                        if (isValidInn(code, stringToDate(r.getBirthday()), logger, wrongCounter, counter))
                            inn = Long.parseLong(code);
                    }

                    YPerson person = addPerson(yinnList, personList, inn,
                            lastName, firstName, patName, birthday, source);

                    Set<String> addresses = new HashSet<>();
                    if (!StringUtils.isBlank(r.getAddress()))
                        addresses.add(r.getAddress().toUpperCase());

                    addAddresses(person, addresses, source);

                    String passportNo = r.getPassLocalNum();
                    String passportSerial = r.getPassLocalSerial();
                    int number;
                    if (isValidLocalPassport(passportNo, passportSerial, wrongCounter, counter, logger)) {
                        passportSerial = transliterationToCyrillicLetters(passportSerial);
                        number = Integer.parseInt(passportNo);
                        addPassport(person, passportSerial, number, r.getPassLocalIssuer(),
                                stringToDate(r.getPassLocalIssueDate()), null, DOMESTIC_PASSPORT, personList,
                                wrongCounter, counter, logger, source);
                    }

                    if (!StringUtils.isBlank(r.getPassIntNum())) {
                        passportNo = r.getPassIntNum().substring(2);
                        passportSerial = r.getPassIntNum().substring(0, 2);
                    }
                    String passportRecNo = r.getPassIntRecNum();
                    if (isValidForeignPassport(passportNo, passportSerial, passportRecNo,
                            wrongCounter, counter, logger)) {
                        passportSerial = transliterationToLatinLetters(passportSerial);
                        number = Integer.parseInt(passportNo);
                        addPassport(person, passportSerial, number, r.getPassIntIssuer(),
                                stringToDate(r.getPassIntIssueDate()), r.getPassIntRecNum(), FOREIGN_PASSPORT, personList,
                                wrongCounter, counter, logger, source);
                    }

                    passportNo = r.getPassIdNum();
                    passportRecNo = r.getPassIdRecNum();
                    if (isValidIdPassport(passportNo, passportRecNo, wrongCounter, counter, logger)) {
                        number = Integer.parseInt(passportNo);
                        addPassport(person, null, number, r.getPassIdIssuer(),
                                stringToDate(r.getPassIdIssueDate()), r.getPassIdRecNum(), IDCARD_PASSPORT, personList,
                                wrongCounter, counter, logger, source);
                    }

                    Set<String> phones = new HashSet<>();
                    if (!StringUtils.isBlank(r.getPhone()))
                        phones.add(r.getPhone().toUpperCase());

                    addPhones(person, phones, source);

                    Set<String> emails = new HashSet<>();
                    if (!StringUtils.isBlank(r.getEmail()))
                        emails.add(r.getEmail().toUpperCase());

                    addEmails(person, emails, source);

                    addTags(person, r.getTags(), source);

                    addAltPerson(person, lastName, firstName, patName, "UK", source);
                    if (!StringUtils.isBlank(r.getLnameRu()))
                        addAltPerson(person, UtilString.toUpperCase(r.getLnameRu()),
                                UtilString.toUpperCase(r.getFnameRu()),
                                UtilString.toUpperCase(r.getPnameRu()), "RU", source);
                    if (!StringUtils.isBlank(r.getLnameEn()))
                        addAltPerson(person, UtilString.toUpperCase(r.getLnameEn()),
                                UtilString.toUpperCase(r.getFnameEn()),
                                UtilString.toUpperCase(r.getPnameEn()), "EN", source);

                    if (!cachedCopy) {
                        personList.add(person);
                        counter[0]++;
                    }
                }
            });

            ypr.saveAll(personList);
            emnService.enrichMonitoringNotification(personList);

            onePage = manualPersonRepository.findAllByUuid(file, pageRequest);

            statusLogger = new StatusLogger(revision, counter[0], RECORDS,
                    MANUAL_PERSON, ENRICHER, startTime, null, null);
            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
        }

        logFinish(MANUAL_PERSON, counter[0]);
        logger.finish();

        statusLogger = new StatusLogger(revision, 100L, "%",
                MANUAL_PERSON, ENRICHER, startTime, LocalDateTime.now(),
                importedRecords(counter[0]));
        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
    }

    private <T> T chooseNotNull(T first, T second) {
        return second != null ? second : first;
    }

    private String chooseNotBlank(String first, String second) {
        return !StringUtils.isBlank(second) ? second : first;
    }

    private void addAltPerson(YPerson person, String lastName, String firstName,
                              String patName, String language,
                              ImportSource source) {
        if (!(Objects.equals(person.getLastName(), lastName)
                && Objects.equals(person.getFirstName(), firstName)
                && Objects.equals(person.getPatName(), patName))) {
            Optional<YAltPerson> altPersonOptional = person.getAltPeople()
                    .parallelStream()
                    .filter(p -> Objects.equals(p.getPerson().getLastName(), lastName)
                            && Objects.equals(p.getPerson().getFirstName(), firstName)
                            && Objects.equals(p.getPerson().getPatName(), patName))
                    .findAny();
            YAltPerson altPerson = altPersonOptional.orElseGet(YAltPerson::new);
            addSource(altPerson.getImportSources(), source);
            altPerson.setFirstName(firstName);
            altPerson.setLastName(lastName);
            altPerson.setPatName(patName);
            altPerson.setLanguage(language);

            if (altPersonOptional.isEmpty()) {
                altPerson.setPerson(person);
                person.getAltPeople().add(altPerson);
            }
        }
    }

    private void addPassport(YPerson person, String passportSeries,
                             Integer number, String authority,
                             LocalDate issued, String recordNumber,
                             String passportType, List<YPerson> personList, long[] wrongCounter,
                             long[] counter, DefaultErrorLogger logger,
                             ImportSource source) {
        YPerson findPerson = null;
        Optional<YPassport> passportFindOptional = findPassportDuplicate(passportSeries, number, passportType, personList);
        if (passportFindOptional.isPresent()) findPerson = passportFindOptional.get().getPerson();
        YPassport passport;
        if (findPerson != null && (!(Objects.equals(person.getLastName(), findPerson.getLastName())
                && Objects.equals(person.getFirstName(), findPerson.getFirstName())
                && Objects.equals(person.getPatName(), findPerson.getPatName()))
                || (person.getBirthdate() != null && findPerson.getBirthdate() != null
                && !person.getBirthdate().equals(findPerson.getBirthdate())))) {
            logError(logger, (counter[0] + 1L), "First Person: " + person
                            + System.lineSeparator() + "Second Person: " + findPerson,
                    "Duplicate passport with different owners");
            wrongCounter[0]++;
            addSource(passportFindOptional.get().getImportSources(), source);
        } else {
            Optional<YPassport> passportOptional = person.getPassports()
                    .parallelStream()
                    .filter(p -> Objects.equals(p.getType(), passportType)
                            && Objects.equals(p.getNumber(), number) && Objects.equals(p.getSeries(), passportSeries)).findAny();
            passport = passportOptional.orElseGet(YPassport::new);
            addSource(passport.getImportSources(), source);

            passport.setSeries(chooseNotBlank(passport.getSeries(), passportSeries));
            passport.setNumber(number);
            passport.setAuthority(chooseNotBlank(passport.getAuthority(), authority));
            passport.setIssued(chooseNotNull(passport.getIssued(), issued));
            passport.setEndDate(null);
            passport.setRecordNumber(chooseNotBlank(passport.getRecordNumber(), recordNumber));
            passport.setType(passportType);
            passport.setValidity(true);

            if (passportOptional.isEmpty()) {
                passport.setPerson(person);
                person.getPassports().add(passport);
            }
        }
    }

    private Optional<YPassport> findPassportDuplicate(String passportSeries,
                                                      Integer number,
                                                      String passportType, List<YPerson> personList) {
        Optional<YPassport> optionalYPassport = personList.stream()
                .flatMap(p -> p.getPassports().stream())
                .filter(p -> Objects.equals(p.getType(), passportType)
                        && Objects.equals(p.getSeries(), passportSeries)
                        && Objects.equals(p.getNumber(), number)).findAny();
        if (optionalYPassport.isEmpty())
            optionalYPassport = yPassportRepository.findByTypeAndNumberAndSeries(passportType, number, passportSeries);
        return optionalYPassport;
    }

    private void addAddresses(YPerson person, Set<String> addresses, ImportSource source) {
        addresses.forEach(a -> {
            Optional<YAddress> addressOptional = person.getAddresses()
                    .stream()
                    .filter(adr -> Objects.equals(adr.getAddress(), a))
                    .findAny();
            YAddress address = addressOptional.orElseGet(YAddress::new);
            addSource(address.getImportSources(), source);
            address.setAddress(chooseNotBlank(address.getAddress(), a));

            if (addressOptional.isEmpty()) {
                address.setPerson(person);
                person.getAddresses().add(address);
            }
        });
    }

    private void addPhones(YPerson person, Set<String> phones, ImportSource source) {
        phones.forEach(p -> {
            Optional<YPhone> phonesOptional = person.getPhones()
                    .stream()
                    .filter(ph -> Objects.equals(ph.getPhone(), p))
                    .findAny();
            YPhone phone = phonesOptional.orElseGet(YPhone::new);
            addSource(phone.getImportSources(), source);
            phone.setPhone(chooseNotBlank(phone.getPhone(), p));

            if (phonesOptional.isEmpty()) {
                phone.setPerson(person);
                person.getPhones().add(phone);
            }
        });
    }

    private void addEmails(YPerson person, Set<String> emails, ImportSource source) {
        emails.forEach(e -> {
            Optional<YEmail> emailOptional = person.getEmails()
                    .stream()
                    .filter(em -> Objects.equals(em.getEmail(), e))
                    .findAny();
            YEmail email = emailOptional.orElseGet(YEmail::new);
            addSource(email.getImportSources(), source);
            email.setEmail(chooseNotBlank(email.getEmail(), e));

            if (emailOptional.isEmpty()) {
                email.setPerson(person);
                person.getEmails().add(email);
            }
        });
    }

    private void addTags(YPerson person, Set<ManualTag> tags, ImportSource source) {
        tags.forEach(t -> {
            Optional<YTag> tagOptional = person.getTags()
                    .stream()
                    .filter(tg -> Objects.equals(tg.getName(), UtilString.toUpperCase(t.getMkId()))
                            && Objects.equals(tg.getAsOf(), stringToDate(t.getMkStart()))
                            && Objects.equals(tg.getUntil(), stringToDate(t.getMkExpire()))).findAny();
            YTag tag = tagOptional.orElseGet(YTag::new);
            addSource(tag.getImportSources(), source);

            if (!StringUtils.isBlank(t.getMkId())) {
                tag.setName(chooseNotBlank(tag.getName(), UtilString.toUpperCase(t.getMkId())));
                tag.setAsOf(chooseNotNull(tag.getAsOf(), stringToDate(t.getMkStart())));
                tag.setUntil(chooseNotNull(tag.getUntil(), stringToDate(t.getMkExpire())));
                tag.setSource(chooseNotBlank(tag.getSource(), UtilString.toUpperCase(t.getMkSource())));


                if (tagOptional.isEmpty()) {
                    tag.setPerson(person);
                    person.getTags().add(tag);
                }
            }
        });
    }

    private YPerson addPerson(Set<YINN> yinnList, List<YPerson> personList, Long inn,
                              String lastName, String firstName, String patName, LocalDate birthDate,
                              ImportSource source) {

        boolean find = false;
        cachedCopy = false;

        YPerson person = null;
        YINN yinn = null;

        if (inn != null) {
            Optional<YINN> yinnCachedOptional = yinnList.parallelStream()
                    .filter(i -> Objects.equals(i.getInn(), inn)).findAny();
            if (yinnCachedOptional.isPresent()) {
                yinn = yinnCachedOptional.get();
                addSource(yinn.getImportSources(), source);
                cachedCopy = true;
                if (yinnCachedOptional.get().getPerson() != null) {
                    person = yinnCachedOptional.get().getPerson();
                    find = true;
                }
            }

            if (!find) {
                Optional<YINN> yinnSavedOptional = yir.findByInn(inn);
                if (yinnSavedOptional.isPresent()) {
                    yinn = yinnSavedOptional.get();
                    addSource(yinn.getImportSources(), source);
                    if (yinnSavedOptional.get().getPerson() != null) {
                        person = yinnSavedOptional.get().getPerson();
                        find = true;
                    }
                }
            }
        }

        if (!find) {
            List<YPerson> yPersonCachedList = personList.parallelStream().filter(p -> Objects.equals(p.getLastName(), lastName)
                            && Objects.equals(p.getFirstName(), firstName)
                            && Objects.equals(p.getPatName(), patName)
                            && Objects.equals(birthDate, p.getBirthdate()))
                    .collect(Collectors.toList());
            if (yPersonCachedList.size() == 1) {
                person = yPersonCachedList.get(0);
                cachedCopy = true;
                find = true;
            }


            if (!find) {
                List<YPerson> yPersonSavedList = ypr.findByLastNameAndFirstNameAndPatNameAndBirthdate(lastName,
                        firstName, patName, birthDate);
                if (yPersonSavedList.size() == 1) {
                    person = yPersonSavedList.get(0);
                }
            }
        }

        if (person == null) {
            person = new YPerson(UUID.randomUUID());
            if (!StringUtils.isBlank(lastName)) person.setLastName(lastName.toUpperCase());
            if (!StringUtils.isBlank(firstName)) person.setFirstName(firstName.toUpperCase());
            if (!StringUtils.isBlank(patName)) person.setPatName(patName.toUpperCase());
        }

        if (yinn == null && inn != null) {
            yinn = new YINN();
            addSource(yinn.getImportSources(), source);
            yinn.setInn(inn);
            yinn.setPerson(person);
            yinn.getPerson().getInns().add(yinn);
            yinnList.add(yinn);
        }

        if (inn != null) yinn.setPerson(person);
        person.setBirthdate(chooseNotNull(person.getBirthdate(), birthDate));
        addSource(person.getImportSources(), source);
        return person;
    }

    private YManager addManager(String okpo, String inn, String type, ImportSource source,
                                Set<YManager> managersList) {
        Optional<YINN> yinnSavedOptional = yir.findByInn(Long.parseLong(inn));
        YINN yinn = yinnSavedOptional.orElseGet(YINN::new);
        yinn.setInn(Long.parseLong(inn));
        addSource(yinn.getImportSources(), source);
        yir.save(yinn);

        YManagerType managerType = yManagerTypeRepository.findByType(type);

        Optional<YManager> yManagerOptional = managersList.stream()
                .filter(m -> Objects.equals(m.getOkpo(), okpo)
                        && Objects.equals(m.getInn().getInn(), Long.parseLong(inn))
                        && Objects.equals(m.getType().getType(), type)).findAny();
        if (yManagerOptional.isEmpty())
            yManagerOptional = yManagerRepository.findByOkpoAndInnAndType(okpo, yinn, managerType);

        YManager manager = yManagerOptional.orElseGet(YManager::new);
        if (manager.getId() == null) manager.setId(UUID.randomUUID());
        manager.setOkpo(okpo);
        manager.setInn(yinn);
        manager.setType(managerType);
        addSource(manager.getImportSources(), source);
        return manager;
    }

    private LocalDate stringToDate(String date) {
        LocalDate localDate = null;
        if (!StringUtils.isBlank(date)) {
            localDate = LocalDate.of(Integer.parseInt(date.substring(6)),
                    Integer.parseInt(date.substring(3, 5)),
                    Integer.parseInt(date.substring(0, 2)));
        }
        return localDate;
    }

    private String transliterationToCyrillicLetters(String serial) {
        StringBuilder kirillSerial = new StringBuilder();
        if (!StringUtils.isBlank(serial))
            for (int i = 0; i < serial.length(); i++) {
                int index = LATIN_LETTERS.indexOf(serial.charAt(i));
                if (index > -1) kirillSerial.append(CYRILLIC_LETTERS.charAt(index));
                else kirillSerial.append(serial.charAt(i));
            }
        return kirillSerial.toString();
    }

    private String transliterationToLatinLetters(String serial) {
        StringBuilder latinSerial = new StringBuilder();
        for (int i = 0; i < serial.length(); i++) {
            int index = CYRILLIC_LETTERS.indexOf(serial.charAt(i));
            if (index > -1) latinSerial.append(LATIN_LETTERS.charAt(index));
            else latinSerial.append(serial.charAt(i));
        }
        return latinSerial.toString();
    }

    private boolean isValidInn(String id, LocalDate birthDay, DefaultErrorLogger logger,
                               long[] wrongCounter, long[] counter) {
        if (id.matches(INN_REGEX)) {
            boolean isValidBirthDateInn = birthDay == null || Objects.equals(String.valueOf(birthDay.toEpochDay()
                    - START_DATE.toEpochDay() + 1), id.substring(0, 5));
            int controlNumber = ((-1 * Integer.parseInt(String.valueOf(id.charAt(0)))
                    + 5 * Integer.parseInt(String.valueOf(id.charAt(1)))
                    + 7 * Integer.parseInt(String.valueOf(id.charAt(2)))
                    + 9 * Integer.parseInt(String.valueOf(id.charAt(3)))
                    + 4 * Integer.parseInt(String.valueOf(id.charAt(4)))
                    + 6 * Integer.parseInt(String.valueOf(id.charAt(5)))
                    + 10 * Integer.parseInt(String.valueOf(id.charAt(6)))
                    + 5 * Integer.parseInt(String.valueOf(id.charAt(7)))
                    + 7 * Integer.parseInt(String.valueOf(id.charAt(8)))) % 11) % 10;
            return Objects.equals(Integer.parseInt(String.valueOf(id.charAt(9))), controlNumber)
                    && isValidBirthDateInn;
        }
        logError(logger, (counter[0] + 1L), "INN: " + id, "Wrong INN");
        wrongCounter[0]++;
        return false;
    }

    private boolean isValidLocalPassport(String number, String serial,
                                         long[] wrongCounter, long[] counter, DefaultErrorLogger logger) {
        if (StringUtils.isBlank(number) || StringUtils.isBlank(serial)) {
            logError(logger, (counter[0] + 1L), "Passport: " + serial + number, "Empty serial or number");
            wrongCounter[0]++;
        } else if (!transliterationToCyrillicLetters(serial).matches(DOMESTIC_SERIES_REGEX)
                || !number.matches(PASS_NUMBER_REGEX)) {
            logError(logger, (counter[0] + 1L), "Passport: " + serial + number, "Wrong format passport serial or number");
            wrongCounter[0]++;
        } else return true;
        return false;
    }

    private boolean isValidForeignPassport(String number, String serial, String recordNumber,
                                           long[] wrongCounter, long[] counter, DefaultErrorLogger logger) {
        if (StringUtils.isBlank(number) || StringUtils.isBlank(serial)) {
            logError(logger, (counter[0] + 1L), "Passport: " + serial + number, "Empty serial or number");
            wrongCounter[0]++;
        } else if (!transliterationToLatinLetters(serial).matches(FOREIGN_SERIES_REGEX)
                || !number.matches(PASS_NUMBER_REGEX)) {
            logError(logger, (counter[0] + 1L), "Passport: " + serial + number, "Wrong format passport serial or number");
            wrongCounter[0]++;
        } else if (!StringUtils.isBlank(recordNumber) && !recordNumber.matches(RECORD_NUMBER_REGEX)) {
            logError(logger, (counter[0] + 1L), "Record number: " + recordNumber, "Wrong format passport record number");
            wrongCounter[0]++;
        } else return true;
        return false;
    }

    private boolean isValidIdPassport(String number, String recordNumber,
                                      long[] wrongCounter, long[] counter, DefaultErrorLogger logger) {
        if (StringUtils.isBlank(number)) {
            logError(logger, (counter[0] + 1L), "Passport number: " + number, "Empty number");
            wrongCounter[0]++;
        } else if (!number.matches(IDCARD_NUMBER_REGEX)) {
            logError(logger, (counter[0] + 1L), "Passport number: " + number, "Wrong format passport number");
            wrongCounter[0]++;
        } else if (!StringUtils.isBlank(recordNumber) && !recordNumber.matches(RECORD_NUMBER_REGEX)) {
            logError(logger, (counter[0] + 1L), "Record number: " + recordNumber, "Wrong format passport record number");
            wrongCounter[0]++;
        } else return true;
        return false;
    }

    private boolean isValidOkpo(String okpo, DefaultErrorLogger logger,
                                long[] wrongCounter, long[] counter) {
        if (!StringUtils.isBlank(okpo) && okpo.matches(OKPO_REGEX)) {
            int idk = Integer.parseInt(okpo);
            Integer lnCs;
            if (idk < 30000000 || idk > 60000000) {
                lnCs = (Integer.parseInt(String.valueOf(okpo.charAt(0)))
                        + Integer.parseInt(String.valueOf(okpo.charAt(1))) * 2
                        + Integer.parseInt(String.valueOf(okpo.charAt(2))) * 3
                        + Integer.parseInt(String.valueOf(okpo.charAt(3))) * 4
                        + Integer.parseInt(String.valueOf(okpo.charAt(4))) * 5
                        + Integer.parseInt(String.valueOf(okpo.charAt(5))) * 6
                        + Integer.parseInt(String.valueOf(okpo.charAt(6))) * 7) % 11;
            } else {
                lnCs = ((Integer.parseInt(String.valueOf(okpo.charAt(0))) * 7
                        + Integer.parseInt(String.valueOf(okpo.charAt(1)))
                        + Integer.parseInt(String.valueOf(okpo.charAt(2))) * 2
                        + Integer.parseInt(String.valueOf(okpo.charAt(3))) * 3
                        + Integer.parseInt(String.valueOf(okpo.charAt(4))) * 4
                        + Integer.parseInt(String.valueOf(okpo.charAt(5))) * 5
                        + Integer.parseInt(String.valueOf(okpo.charAt(6))) * 6) % 11);
            }
            if (lnCs == 10) {
                if (idk < 30000000 || idk > 60000000) {
                    lnCs = (Integer.parseInt(String.valueOf(okpo.charAt(0))) * 3
                            + Integer.parseInt(String.valueOf(okpo.charAt(1))) * 4
                            + Integer.parseInt(String.valueOf(okpo.charAt(2))) * 5
                            + Integer.parseInt(String.valueOf(okpo.charAt(3))) * 6
                            + Integer.parseInt(String.valueOf(okpo.charAt(4))) * 7
                            + Integer.parseInt(String.valueOf(okpo.charAt(5))) * 8
                            + Integer.parseInt(String.valueOf(okpo.charAt(6))) * 9) % 11;
                } else {
                    lnCs = (Integer.parseInt(String.valueOf(okpo.charAt(0))) * 9
                            + Integer.parseInt(String.valueOf(okpo.charAt(1))) * 3
                            + Integer.parseInt(String.valueOf(okpo.charAt(2))) * 4
                            + Integer.parseInt(String.valueOf(okpo.charAt(3))) * 5
                            + Integer.parseInt(String.valueOf(okpo.charAt(4))) * 6
                            + Integer.parseInt(String.valueOf(okpo.charAt(5))) * 7
                            + Integer.parseInt(String.valueOf(okpo.charAt(6))) * 8) % 11;
                }
            }
            return (String.valueOf(okpo.charAt(7)).equals(String.valueOf(lnCs)));
        }
        if (!StringUtils.isBlank(okpo))
            logError(logger, (counter[0] + 1L), "OKPO: " + okpo, "Wrong OKPO");
        wrongCounter[0]++;
        return false;
    }

    protected void logError(DefaultErrorLogger logger, long row, String info, String clarification) {
        if (logger != null)
            logger.logError(new ErrorReport(row, -1L, -1L, -1L, -1L, info, clarification));
    }

    private void addSource(Set<ImportSource> sources, ImportSource source) {
        if (source != null) sources.add(source);
    }
}

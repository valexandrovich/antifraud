package ua.com.solidity.enricher.service;
//
//import lombok.CustomLog;
//import lombok.RequiredArgsConstructor;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.stereotype.Service;
//import ua.com.solidity.common.DefaultErrorLogger;
//import ua.com.solidity.common.ErrorReport;
//import ua.com.solidity.common.UtilString;
//import ua.com.solidity.common.Utils;
//import ua.com.solidity.db.entities.BaseCreator;
//import ua.com.solidity.db.entities.BaseDirector;
//import ua.com.solidity.db.entities.BaseDrfo;
//import ua.com.solidity.db.entities.BaseElections;
//import ua.com.solidity.db.entities.BaseFodb;
//import ua.com.solidity.db.entities.BasePassports;
//import ua.com.solidity.db.entities.Contragent;
//import ua.com.solidity.db.entities.FileDescription;
//import ua.com.solidity.db.entities.Govua10;
//import ua.com.solidity.db.entities.ImportSource;
//import ua.com.solidity.db.entities.ManualPerson;
//import ua.com.solidity.db.entities.ManualTag;
//import ua.com.solidity.db.entities.StatusLogger;
//import ua.com.solidity.db.entities.YAddress;
//import ua.com.solidity.db.entities.YAltPerson;
//import ua.com.solidity.db.entities.YCompany;
//import ua.com.solidity.db.entities.YCompanyRelation;
//import ua.com.solidity.db.entities.YCompanyRole;
//import ua.com.solidity.db.entities.YEmail;
//import ua.com.solidity.db.entities.YINN;
//import ua.com.solidity.db.entities.YPassport;
//import ua.com.solidity.db.entities.YPerson;
//import ua.com.solidity.db.entities.YPhone;
//import ua.com.solidity.db.entities.YTag;
//import ua.com.solidity.db.repositories.ContragentRepository;
//import ua.com.solidity.db.repositories.FileDescriptionRepository;
//import ua.com.solidity.db.repositories.ImportSourceRepository;
//import ua.com.solidity.db.repositories.ManualPersonRepository;
//import ua.com.solidity.db.repositories.YAddressRepository;
//import ua.com.solidity.db.repositories.YAltPersonRepository;
//import ua.com.solidity.db.repositories.YCompanyRelationRepository;
//import ua.com.solidity.db.repositories.YCompanyRepository;
//import ua.com.solidity.db.repositories.YCompanyRoleRepository;
//import ua.com.solidity.db.repositories.YEmailRepository;
//import ua.com.solidity.db.repositories.YINNRepository;
//import ua.com.solidity.db.repositories.YPassportRepository;
//import ua.com.solidity.db.repositories.YPersonRepository;
//import ua.com.solidity.db.repositories.YPhoneRepository;
//import ua.com.solidity.db.repositories.YTagRepository;
//import ua.com.solidity.enricher.model.EnricherPortionRequest;
//import ua.com.solidity.enricher.repository.BaseCreatorRepository;
//import ua.com.solidity.enricher.repository.BaseDirectorRepository;
//import ua.com.solidity.enricher.repository.BaseDrfoRepository;
//import ua.com.solidity.enricher.repository.BaseElectionsRepository;
//import ua.com.solidity.enricher.repository.BaseFodbRepository;
//import ua.com.solidity.enricher.repository.BasePassportsRepository;
//import ua.com.solidity.enricher.repository.Govua10Repository;
//import ua.com.solidity.enricher.util.FileFormatUtil;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.Set;
//import java.util.UUID;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//@CustomLog
//@RequiredArgsConstructor
//@Service
//@EntityScan(basePackages = {"ua.com.solidity.db.entities"})
//@EnableJpaRepositories(basePackages = {"ua.com.solidity.db.repositories", "ua.com.solidity.enricher.repository"})
//@PropertySource({"classpath:enricher.properties", "classpath:application.properties"})
public class EnricherServiceImpl
////        implements Enricher
{
//    private final BaseDrfoRepository bdr;
//    private final BaseElectionsRepository ber;
//    private final BaseFodbRepository bfr;
//    private final BasePassportsRepository bpr;
//    private final YPersonRepository ypr;
//    private final ContragentRepository cr;
//    private final BaseDirectorRepository baseDirectorRepository;
//    private final YINNRepository yir;
//    private final ManualPersonRepository manualPersonRepository;
//    private final FileDescriptionRepository fileDescriptionRepository;
//    private final MonitoringNotificationService emnService;
//    private final ImportSourceRepository isr;
//    private final YPassportRepository yPassportRepository;
//    private final FileFormatUtil fileFormatUtil;
//    private final BaseCreatorRepository baseCreatorRepository;
//    private final Govua10Repository govua10Repository;
//    private final YAltPersonRepository yAltPersonRepository;
//    private final YAddressRepository yAddressRepository;
//    private final YPhoneRepository yPhoneRepository;
//    private final YEmailRepository yEmailRepository;
//    private final YTagRepository yTagRepository;
//    private final YCompanyRepository yCompanyRepository;
//    private final YCompanyRoleRepository yCompanyRoleRepository;
//    private final YCompanyRelationRepository yCompanyRelationRepository;
//    //DONE
//    private static final String BASE_DRFO = "base_drfo";
//    private static final String BASE_ELECTIONS = "base_elections";
//    private static final String BASE_FODB = "base_fodb";
//    private static final String BASE_PASSPORTS = "base_passports";
//    private static final String CONTRAGENT = "contragent";
//    private static final String MANUAL_PERSON = "manual_person";
//    private static final String BASE_DIRECTOR = "base_director";
//    private static final String BASE_CREATOR = "base_creator";
//    private static final String GOVUA10 = "govua_10";
//    //DONE
//    private static final String ALL_NOT_NUMBER = "[^0-9]";
//    private static final String INN_REGEX = "^[\\d]{10}$";
//    private static final String OKPO_REGEX = "^[\\d]{8,9}$";
//    private static final String PASS_NUMBER_REGEX = "^[\\d]{6}$";
//    private static final String IDCARD_NUMBER_REGEX = "^[\\d]{9}$";
//    private static final String FOREIGN_SERIES_REGEX = "^[A-Z]{2}$";
//    private static final String DOMESTIC_SERIES_REGEX = "^[А-Я]{2}$";
//    private static final String RECORD_NUMBER_REGEX = "^[\\d]{8}-[\\d]{5}$";
//    private static final String INN_FORMAT_REGEX = "%010d";
////DONE
//    private static final String ENRICHER = "ENRICHER";
//    private static final String PERCENT = "%";
//    private static final String DOMESTIC_PASSPORT = "UA_DOMESTIC";
//    private static final String IDCARD_PASSPORT = "UA_IDCARD";
//    private static final String LATIN_LETTERS = "ABEKMHOPCTXY";
//    private static final String CYRILLIC_LETTERS = "АВЕКМНОРСТХУ";
//    private static final String FOREIGN_PASSPORT = "UA_FOREIGN";
//    private static final String DIRECTOR = "DIRECTOR";
//    public static final LocalDate START_DATE = LocalDate.of(1900, 1, 1);
//    private static final String CREATOR = "CREATOR";
//    private static final String ENRICHER_ERROR_REPORT_MESSAGE = "Enricher error report for table: {} with revision: {}";
//
//    @Value("${otp.enricher.page-size}")
//    private Integer pageSize;
//    @Value("${statuslogger.rabbitmq.name}")
//    private String queueName;
//    private boolean findCopy;
//
//    private String importedRecords(long num) {
//        return String.format("Imported %d records", num);
//    }
//
//    private void logStart(String table) {
//        log.info("Data from {} are being transferred to OLAP zone", table);
//    }
//
//    private void logFinish(String table, Long rows) {
//        log.info("Imported {} records from {}", rows, table);
//    }
//
////    @Override
//    public void enrich(EnricherPortionRequest er) {
//        log.info("Received request to enrich {} revision {}", er.getTable(), er.getPortion());
//        switch (er.getTable()) {
//            case BASE_DRFO:
//                baseDrfoEnrich(er.getPortion());
//                break;
//            case BASE_ELECTIONS:
//                baseElectionsEnrich(er.getPortion());
//                break;
//            case BASE_FODB:
//                baseFodbEnrich(er.getPortion());
//                break;
//            case BASE_PASSPORTS:
//                basePassportsEnrich(er.getPortion());
//                break;
//            case CONTRAGENT:
//                contragentEnrich(er.getPortion());
//                break;
//            case MANUAL_PERSON:
//                manualPersonEnrich(er.getPortion());
//                break;
//            case BASE_DIRECTOR:
//                baseDirectorEnricher(er.getPortion());
//                break;
//            case BASE_CREATOR:
//                baseCreatorEnricher(er.getPortion());
//                break;
//            case GOVUA10:
//                govua10Enricher(er.getPortion());
//                break;
//            default:
//                log.warn("Ignoring unsupported {} enrichment", er.getTable());
//        }
//    }
//
//    public void baseDrfoEnrich(UUID portion) {
//        logStart(BASE_DRFO);
//
//        LocalDateTime startTime = LocalDateTime.now();
//        StatusLogger statusLogger = new StatusLogger(portion, 0L, "%",
//                BASE_DRFO, ENRICHER, startTime, null, null);
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//
//        long[] counter = new long[1];
//        long[] wrongCounter = new long[1];
//
//        Pageable pageRequest = PageRequest.of(0, pageSize);
//        Page<BaseDrfo> onePage = bdr.findAllByPortionId(portion, pageRequest);
//        long count = bdr.countAllByPortionId(portion);
//        String fileName = fileFormatUtil.getLogFileName(portion.toString());
//        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
//                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_DRFO, portion));
//
//        ImportSource source = isr.findImportSourceByName(BASE_DRFO);
//
//        while (!onePage.isEmpty()) {
//            pageRequest = pageRequest.next();
//            List<YPerson> personList = new ArrayList<>();
//
//            onePage.forEach(r -> {
//                findCopy = false;
//                String lastName = UtilString.toUpperCase(r.getLastName());
//                String firstName = UtilString.toUpperCase(r.getFirstName());
//                String patName = UtilString.toUpperCase(r.getPatName());
//
//                YPerson person = new YPerson();
//                person.setLastName(lastName);
//                person.setFirstName(firstName);
//                person.setPatName(patName);
//                person.setBirthdate(r.getBirthdate());
//
//                if (r.getInn() != null) {
//                    String code = String.format(INN_FORMAT_REGEX, r.getInn());
//                    if (isValidInn(code, r.getBirthdate(), logger, wrongCounter, counter)) {
//                        long inn = Long.parseLong(code);
//                        person = addInn(inn, personList, source, person);
//                    }
//                }
//
//                person = addPerson(personList, person, source);
//
//                Set<String> addresses = new HashSet<>();
//                if (StringUtils.isBlank(r.getAllAddresses()))
//                    addresses = Arrays.stream(r.getAllAddresses().split(" Адрес "))
//                            .map(UtilString::toUpperCase).collect(Collectors.toSet());
//                if (StringUtils.isBlank(r.getResidenceAddress()))
//                    addresses.add(r.getResidenceAddress().substring(11).toUpperCase());
//                if (!StringUtils.isBlank(r.getAddress())) addresses.add(r.getAddress().toUpperCase());
//                if (!StringUtils.isBlank(r.getAddress2())) addresses.add(r.getAddress2().toUpperCase());
//
//                addAddresses(person, addresses, source);
//
//                personList.add(person);
//                counter[0]++;
//            });
//
//            ypr.saveAll(personList);
//            emnService.enrichMonitoringNotification(personList);
//
//            onePage = bdr.findAllByPortionId(portion, pageRequest);
//
//            statusLogger = new StatusLogger(portion, counter[0] * 100L / count, PERCENT,
//                    BASE_DRFO, ENRICHER, startTime, null, null);
//            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//        }
//
//        logFinish(BASE_DRFO, counter[0]);
//
//        statusLogger = new StatusLogger(portion, 100L, "%",
//                BASE_DRFO, ENRICHER, startTime, LocalDateTime.now(),
//                importedRecords(counter[0]));
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//    }
//
//    public void baseElectionsEnrich(UUID portion) {
//        logStart(BASE_ELECTIONS);
//
//        LocalDateTime startTime = LocalDateTime.now();
//        StatusLogger statusLogger = new StatusLogger(portion, 0L, "%",
//                BASE_ELECTIONS, ENRICHER, startTime, null, null);
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//
//        long[] counter = new long[1];
//
//        Pageable pageRequest = PageRequest.of(0, pageSize);
//        Page<BaseElections> onePage = ber.findAllByPortionId(portion, pageRequest);
//        long count = ber.countAllByPortionId(portion);
//        String fileName = fileFormatUtil.getLogFileName(portion.toString());
//        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
//                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_ELECTIONS, portion));
//
//        ImportSource source = isr.findImportSourceByName(BASE_ELECTIONS);
//
//        while (!onePage.isEmpty()) {
//            pageRequest = pageRequest.next();
//            List<YPerson> personList = new ArrayList<>();
//
//            onePage.forEach(r -> {
//                findCopy = false;
//                String[] fio = null;
//                String firstName = "";
//                String lastName = "";
//                String patName = "";
//                if (!StringUtils.isBlank(r.getFio())) fio = r.getFio().split(" ");
//                if (fio != null && fio.length >= 1) lastName = UtilString.toUpperCase(fio[0]);
//                if (fio != null && fio.length >= 2) firstName = UtilString.toUpperCase(fio[1]);
//                if (fio != null && fio.length >= 3) patName = UtilString.toUpperCase(fio[2]);
//
//                if (StringUtils.isBlank(lastName))
//                    logError(logger, (counter[0] + 1L), "LastName: " + lastName, "Empty last name");
//
//                YPerson person = new YPerson();
//                person.setLastName(lastName);
//                person.setFirstName(firstName);
//                person.setPatName(patName);
//                person.setBirthdate(r.getBirthdate());
//
//                person = addPerson(personList, person, source);
//
//                if (!StringUtils.isBlank(r.getAddress())) {
//                    String[] partAddress = r.getAddress().split(", ");
//                    StringBuilder sbAddress = new StringBuilder();
//                    for (int i = partAddress.length - 1; i > 0; i--) {
//                        sbAddress.append(partAddress[i].toUpperCase()).append(", ");
//                    }
//                    sbAddress.append(partAddress[0].toUpperCase());
//
//                    Set<String> addresses = new HashSet<>();
//                    addresses.add(sbAddress.toString());
//
//                    addAddresses(person, addresses, source);
//                }
//                personList.add(person);
//                counter[0]++;
//            });
//
//            ypr.saveAll(personList);
//            emnService.enrichMonitoringNotification(personList);
//
//            onePage = ber.findAllByPortionId(portion, pageRequest);
//
//            statusLogger = new StatusLogger(portion, counter[0] * 100L / count, PERCENT,
//                    BASE_ELECTIONS, ENRICHER, startTime, null, null);
//            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//        }
//
//        logFinish(BASE_ELECTIONS, counter[0]);
//        logger.finish();
//
//        statusLogger = new StatusLogger(portion, 100L, "%",
//                BASE_ELECTIONS, ENRICHER, startTime,
//                LocalDateTime.now(),
//                importedRecords(counter[0]));
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//    }
//
//    public void baseFodbEnrich(UUID portion) {
//        logStart(BASE_FODB);
//
//        LocalDateTime startTime = LocalDateTime.now();
//        StatusLogger statusLogger = new StatusLogger(portion, 0L, "%",
//                BASE_FODB, ENRICHER, startTime, null, null);
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//
//        long[] counter = new long[1];
//        long[] wrongCounter = new long[1];
//
//        Pageable pageRequest = PageRequest.of(0, pageSize);
//        Page<BaseFodb> onePage = bfr.findAllByPortionId(portion, pageRequest);
//        long count = bfr.countAllByPortionId(portion);
//        String fileName = fileFormatUtil.getLogFileName(portion.toString());
//        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
//                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_FODB, portion));
//
//        ImportSource source = isr.findImportSourceByName(BASE_FODB);
//
//        while (!onePage.isEmpty()) {
//            pageRequest = pageRequest.next();
//            List<YPerson> personList = new ArrayList<>();
//
//            onePage.forEach(r -> {
//                findCopy = false;
//                String lastName = UtilString.toUpperCase(r.getLastNameUa());
//                String firstName = UtilString.toUpperCase(r.getFirstNameUa());
//                String patName = UtilString.toUpperCase(r.getMiddleNameUa());
//
//                YPerson person = new YPerson();
//                person.setLastName(lastName);
//                person.setFirstName(firstName);
//                person.setPatName(patName);
//                person.setBirthdate(r.getBirthdate());
//
//                if (!StringUtils.isBlank(r.getInn())) {
//                    String code = String.format(INN_FORMAT_REGEX, Long.parseLong(r.getInn().replaceAll(ALL_NOT_NUMBER, "")));
//                    if (isValidInn(code, r.getBirthdate(), logger, wrongCounter, counter)) {
//                        long inn = Long.parseLong(code);
//                        person = addInn(inn, personList, source, person);
//                    }
//                }
//
//                person = addPerson(personList, person, source);
//
//                Set<String> addresses = new HashSet<>();
//                StringBuilder laString = new StringBuilder();
//                if (!StringUtils.isBlank(r.getLiveRegion())) {
//                    laString.append(r.getLiveRegion().toUpperCase()).append(" ОБЛАСТЬ");
//                }
//                if (!StringUtils.isBlank(r.getLiveCounty())) {
//                    laString.append(", ").append(r.getLiveCounty().toUpperCase()).append(" Р-Н");
//                }
//                laString.append(", ").append(UtilString.toUpperCase(r.getLiveCityType()))
//                        .append(". ").append(UtilString.toUpperCase(r.getLiveCityUa()));
//                laString.append(", ").append(UtilString.toUpperCase(r.getLiveStreetType())).append(" ")
//                        .append(UtilString.toUpperCase(r.getLiveStreet()));
//                if (!StringUtils.isBlank(r.getLiveBuildingNumber()) && !r.getLiveBuildingNumber().equals("0")) {
//                    laString.append(" ").append(r.getLiveBuildingNumber().toUpperCase());
//                }
//                if (!StringUtils.isBlank(r.getLiveBuildingApartment()) && !r.getLiveBuildingApartment().equals("0")) {
//                    laString.append(", КВ. ").append(r.getLiveBuildingApartment().toUpperCase());
//                }
//                addresses.add(laString.toString());
//
//                addAddresses(person, addresses, source);
//
//                addAltPerson(person, r.getLastNameRu(), r.getFirstNameRu(), r.getMiddleNameRu(), "RU", source);
//
//                personList.add(person);
//                counter[0]++;
//            });
//
//            ypr.saveAll(personList);
//            emnService.enrichMonitoringNotification(personList);
//
//            onePage = bfr.findAllByPortionId(portion, pageRequest);
//
//            statusLogger = new StatusLogger(portion, counter[0] * 100L / count, PERCENT,
//                    BASE_FODB, ENRICHER, startTime, null, null);
//            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//        }
//
//        logFinish(BASE_FODB, counter[0]);
//        logger.finish();
//
//        statusLogger = new StatusLogger(portion, 100L, "%",
//                BASE_FODB, ENRICHER, startTime, LocalDateTime.now(),
//                importedRecords(counter[0]));
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//    }
//
//    public void basePassportsEnrich(UUID portion) {
//        logStart(BASE_PASSPORTS);
//
//        LocalDateTime startTime = LocalDateTime.now();
//        StatusLogger statusLogger = new StatusLogger(portion, 0L, "%",
//                BASE_PASSPORTS, ENRICHER, startTime, null, null);
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//
//        long[] counter = new long[1];
//        long[] wrongCounter = new long[1];
//
//        Pageable pageRequest = PageRequest.of(0, pageSize);
//        Page<BasePassports> onePage = bpr.findAllByPortionId(portion, pageRequest);
//        long count = bpr.countAllByPortionId(portion);
//        String fileName = fileFormatUtil.getLogFileName(portion.toString());
//        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
//                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_PASSPORTS, portion));
//
//        ImportSource source = isr.findImportSourceByName(BASE_PASSPORTS);
//
//        while (!onePage.isEmpty()) {
//            pageRequest = pageRequest.next();
//            List<YPerson> personList = new ArrayList<>();
//
//            onePage.forEach(r -> {
//                findCopy = false;
//                String lastName = UtilString.toUpperCase(r.getLastName());
//                String firstName = UtilString.toUpperCase(r.getFirstName());
//                String patName = UtilString.toUpperCase(r.getMiddleName());
//
//                YPerson person = new YPerson();
//                person.setLastName(lastName);
//                person.setFirstName(firstName);
//                person.setPatName(patName);
//                person.setBirthdate(r.getBirthdate());
//
//                if (!StringUtils.isBlank(r.getInn())) {
//                    String code = String.format(INN_FORMAT_REGEX, Long.parseLong(r.getInn().replaceAll(ALL_NOT_NUMBER, "")));
//                    if (isValidInn(code, r.getBirthdate(), logger, wrongCounter, counter)) {
//                        long inn = Long.parseLong(code);
//                        person = addInn(inn, personList, source, person);
//                    }
//                }
//
//                String passportNo = r.getPassId();
//                String passportSerial = r.getSerial();
//                if (isValidLocalPassport(passportNo, passportSerial, wrongCounter, counter, logger)) {
//                    passportSerial = transliterationToCyrillicLetters(passportSerial);
//                    int number = Integer.parseInt(passportNo);
//                    YPassport passport = new YPassport();
//                    passport.setSeries(passportSerial);
//                    passport.setNumber(number);
//                    passport.setAuthority(null);
//                    passport.setIssued(null);
//                    passport.setEndDate(null);
//                    passport.setRecordNumber(null);
//                    passport.setValidity(true);
//                    passport.setType(DOMESTIC_PASSPORT);
//                    person = addPassport(passport, personList, source, person);
//                }
//                person = addPerson(personList, person, source);
//                personList.add(person);
//                counter[0]++;
//
//            });
//
//            ypr.saveAll(personList);
//            emnService.enrichMonitoringNotification(personList);
//
//            onePage = bpr.findAllByPortionId(portion, pageRequest);
//
//            statusLogger = new StatusLogger(portion, counter[0] * 100L / count, PERCENT,
//                    BASE_PASSPORTS, ENRICHER, startTime, null, null);
//            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//        }
//
//        logFinish(BASE_PASSPORTS, counter[0]);
//        logger.finish();
//
//        statusLogger = new StatusLogger(portion, 100L, "%",
//                BASE_PASSPORTS, ENRICHER, startTime, LocalDateTime.now(),
//                importedRecords(counter[0]));
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//    }
//
//	public void contragentEnrich(UUID portion) {
//		logStart(CONTRAGENT);
//
//		LocalDateTime startTime = LocalDateTime.now();
//		StatusLogger statusLogger = new StatusLogger(portion, 0L, "%",
//		                                             CONTRAGENT, ENRICHER, startTime, null, null);
//		Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//
//		long[] counter = new long[1];
//
//		Pageable pageRequest = PageRequest.of(0, pageSize);
//		Page<Contragent> onePage = cr.findAllByPortionId(portion, pageRequest);
//		long count = cr.countAllByPortionId(portion);
//		ImportSource source = isr.findImportSourceByName("dwh");
//
//		while (!onePage.isEmpty()) {
//			pageRequest = pageRequest.next();
//			List<YPerson> personList = new ArrayList<>();
//			Set<YINN> yinnList = new HashSet<>();
//
//			onePage.forEach(r -> {
//				Optional<YINN> yinnSavedOptional;
//				Optional<YINN> yinnCachedOptional;
//				String identifyCode = r.getIdentifyCode();
//
//				if (StringUtils.isNotBlank(identifyCode) && identifyCode.matches(INN_REGEX)) {// We skip person without inn
//					long inn = Long.parseLong(identifyCode);
//					yinnSavedOptional = yir.findByInn(inn);
//					yinnCachedOptional = yinnList.stream().filter(i -> i.getInn() == inn).findAny();
//					boolean cached = false;
//					boolean saved = false;
//
//					YPerson person = null;
//					if (yinnSavedOptional.isPresent()) {
//						UUID personId = yinnSavedOptional.get().getPerson().getId();
//						Optional<YPerson> ypersonOptional = ypr.findWithInnsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSourcesById(personId);
//						person = ypersonOptional.orElseGet(() -> null);
//						saved = true;
//					}
//					if (yinnCachedOptional.isPresent()) {
//						if (saved) {
//							UUID personId = yinnSavedOptional.get().getPerson().getId();
//							Optional<YPerson> ypersonOptional = ypr.findWithInnsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSourcesById(personId);
//							person = ypersonOptional.orElseGet(() -> null);
//						} else {
//							person = yinnCachedOptional.get().getPerson();
//						}
//						cached = true;
//					}
//					if (person == null) person = new YPerson(UUID.randomUUID());
//					addSource(person.getImportSources(), source);
//
//					List<String> splitPersonNames = Arrays.asList(r.getName().split("[ ]+"));
//					String splitPatronymic = splitPersonNames.size() == 3 ? splitPersonNames.get(2) : null;
//
//					if (!StringUtils.isBlank(r.getClientPatronymicName()))
//						person.setPatName(UtilString.toUpperCase(chooseNotBlank(person.getPatName(), r.getClientPatronymicName())));
//					else
//						person.setPatName(UtilString.toUpperCase(chooseNotBlank(person.getPatName(), splitPatronymic)));
//					person.setLastName(UtilString.toUpperCase(chooseNotBlank(person.getLastName(), r.getClientLastName())));
//
//					if (!StringUtils.isBlank(r.getClientName()))
//						person.setFirstName(UtilString.toUpperCase(r.getClientName()));
//					else if (splitPersonNames.size() == 3) {
//						String firstName = splitPersonNames.get(1).equalsIgnoreCase(r.getClientLastName()) ? splitPersonNames.get(0) : splitPersonNames.get(1);
//						person.setFirstName(UtilString.toUpperCase(firstName));
//					}
//
//					person.setBirthdate(chooseNotNull(person.getBirthdate(), r.getClientBirthday()));
//
//					YINN yinn = null;
//					if (yinnSavedOptional.isPresent()) yinn = yinnSavedOptional.get();
//					if (yinnCachedOptional.isPresent()) yinn = yinnCachedOptional.get();
//					if (yinn == null) yinn = new YINN();
//					addSource(yinn.getImportSources(), source);
//					if (!cached && !saved) {
//						yinn.setInn(Long.parseLong(identifyCode));
//						yinn.setPerson(person);
//						yinn.getPerson().getInns().add(yinn);
//					}
//
//					String passportNo = r.getPassportNo();
//					String passportSerial = UtilString.toUpperCase(r.getPassportSerial());
//					Integer number;
//					if (StringUtils.isNotBlank(passportNo) && passportNo.matches(PASS_NUMBER_REGEX)) {
//						number = Integer.parseInt(passportNo);
//						List<YPerson> personListWithSamePassport = personList.stream()
//								.filter(yperson -> {
//									return yperson.getPassports()
//											.stream()
//											.anyMatch(passport -> {
//												return Objects.equals(passport.getNumber(), number) &&
//														Objects.equals(passport.getSeries(), passportSerial) &&
//														Objects.equals(passport.getType(), DOMESTIC_PASSPORT);
//											});
//								})
//								.collect(Collectors.toList());
//
//						Optional<YPassport> passportOptional = yPassportRepository.findByTypeAndNumberAndSeries(DOMESTIC_PASSPORT, number, passportSerial);
//						passportOptional.ifPresent(passport -> personListWithSamePassport.addAll(ypr.findByPassportsContains(passport)));
//
//                        if (passportOptional.isEmpty() && !personListWithSamePassport.isEmpty())
//                            passportOptional = personListWithSamePassport.get(0).getPassports().parallelStream().filter(pas -> Objects.equals(pas.getType(), DOMESTIC_PASSPORT)
//                                    && Objects.equals(pas.getSeries(), passportSerial)
//                                    && Objects.equals(pas.getNumber(), number)).findAny();
//
//						YPassport passport = passportOptional.orElseGet(YPassport::new);
//						addSource(passport.getImportSources(), source);
//						passport.setSeries(UtilString.toUpperCase(passportSerial));
//						passport.setNumber(number);
//						passport.setAuthority(UtilString.toUpperCase(chooseNotBlank(passport.getAuthority(), r.getPassportIssuePlace())));
//						passport.setIssued(chooseNotNull(passport.getIssued(), r.getPassportIssueDate()));
//						passport.setEndDate(chooseNotNull(passport.getEndDate(), r.getPassportEndDate()));
//						passport.setRecordNumber(null);
//						passport.setValidity(true);
//						passport.setType(DOMESTIC_PASSPORT);
//
//						Optional<YPerson> optionalYPerson = Optional.empty();
//						if (!personListWithSamePassport.isEmpty()) {
//							YPerson finalPerson = person;
//							optionalYPerson = personListWithSamePassport.parallelStream().filter(p -> isEqualsPerson(p, finalPerson)).findAny();
//						}
//
//						if (optionalYPerson.isPresent()) {
//							YPerson findPerson = optionalYPerson.get();
//							findPerson.setLastName(chooseNotNull(findPerson.getLastName(), person.getLastName()));
//							findPerson.setFirstName(chooseNotNull(findPerson.getFirstName(), person.getFirstName()));
//							findPerson.setPatName(chooseNotNull(findPerson.getPatName(), person.getPatName()));
//							findPerson.setBirthdate(chooseNotNull(findPerson.getBirthdate(), person.getBirthdate()));
//
//							person = findPerson;
//							findCopy = true;
//						} else if (passportOptional.isPresent() && passport.getId() == null) yPassportRepository.save(passport);
//						person.getPassports().add(passport);
//						addSource(passport.getImportSources(), source);
//
//                        if (person.getId() != null) {
//                            person = ypr.findWithInnsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSourcesById(person.getId()).orElse(person);
//                        }
//
//					}
//
//					Optional<YTag> tagPlaceOptional = person.getTags()
//							.parallelStream()
//							.filter(t -> UtilString.equalsIgnoreCase(t.getName(), r.getWorkplace()))
//							.findAny();
//					if (StringUtils.isNotBlank(r.getWorkplace())) {
//						YTag tag = tagPlaceOptional.orElseGet(YTag::new);
//						addSource(tag.getImportSources(), source);
//						tag.setName(UtilString.toUpperCase(r.getWorkplace()));
//						if (tag.getAsOf() == null) tag.setAsOf(LocalDate.now());
//						tag.setSource(CONTRAGENT);
//
//						if (tagPlaceOptional.isEmpty()) {
//							tag.setPerson(person);
//							person.getTags().add(tag);
//						}
//					}
//
//					Optional<YTag> tagPositionOptional = person.getTags()
//							.parallelStream()
//							.filter(t -> UtilString.equalsIgnoreCase(t.getName(), r.getWorkPosition()))
//							.findAny();
//					if (StringUtils.isNotBlank(r.getWorkPosition())) {
//						YTag tag = tagPositionOptional.orElseGet(YTag::new);
//						addSource(tag.getImportSources(), source);
//						tag.setName(UtilString.toUpperCase(r.getWorkPosition()));
//						if (tag.getAsOf() == null) tag.setAsOf(LocalDate.now());
//						tag.setSource(CONTRAGENT);
//
//						if (tagPositionOptional.isEmpty()) {
//							tag.setPerson(person);
//							person.getTags().add(tag);
//						}
//					}
//
//					YTag tag = new YTag();
//					tag.setName("RC");
//					tag.setSource(CONTRAGENT);
//					tag.setPerson(person);
//					addSource(tag.getImportSources(), source);
//					person.getTags().add(tag);
//
//					YPerson finalPerson = person;
//					Stream.of(r.getPhones(), r.getMobilePhone(), r.getPhoneHome()).forEach(phone -> {
//						if (phone != null) {
//							String phoneCleaned = phone.replaceAll("[^0-9]+", "");
//							if (StringUtils.isNotBlank(phoneCleaned)) {
//								Optional<YPhone> yphoneOptional = finalPerson.getPhones()
//										.parallelStream()
//										.filter(yphone -> yphone.getPhone().equals(phoneCleaned))
//										.findFirst();
//
//								YPhone yPhone = yphoneOptional.orElseGet(YPhone::new);
//								addSource(yPhone.getImportSources(), source);
//								yPhone.setPhone(phoneCleaned);
//
//								if (yphoneOptional.isEmpty()) {
//									yPhone.setPerson(finalPerson);
//									finalPerson.getPhones().add(yPhone);
//								}
//							}
//						}
//					});
//
//					YPerson finalPerson1 = person;
//					Stream.of(r.getAddress(), r.getBirthplace()).forEach(address -> {
//						if (StringUtils.isNotBlank(address)) {
//							Optional<YAddress> yAddressOptional = finalPerson1.getAddresses()
//									.parallelStream()
//									.filter(yAddress -> yAddress.getAddress().equalsIgnoreCase(address))
//									.findAny();
//
//							YAddress yAddress = yAddressOptional.orElseGet(YAddress::new);
//							addSource(yAddress.getImportSources(), source);
//							yAddress.setAddress(UtilString.toUpperCase(address.toUpperCase()));
//
//							if (yAddressOptional.isEmpty()) {
//								yAddress.setPerson(finalPerson1);
//								finalPerson1.getAddresses().add(yAddress);
//							}
//						}
//					});
//
//					if (!StringUtils.isBlank(r.getAlternateName())) {
//						List<String> splitAltPersonNames = Arrays.asList(r.getAlternateName().split("[ ]+"));
//
//						String[] names = new String[3];
//						for (int i = 0; i < splitAltPersonNames.size(); i++) {
//							if (i == 2) {
//								StringBuilder sb = new StringBuilder(splitAltPersonNames.get(2));
//								for (int j = 3; j < splitAltPersonNames.size(); j++) {
//									sb.append(' ').append(splitAltPersonNames.get(j));
//								}
//								names[2] = sb.toString();
//								break;
//							}
//							names[i] = splitAltPersonNames.get(i);
//						}
//						Optional<YAltPerson> altPersonOptional = person.getAltPeople().parallelStream()
//								.filter(yAltPerson -> StringUtils.equalsIgnoreCase(yAltPerson.getFirstName(), names[0]) &&
//										StringUtils.equalsIgnoreCase(yAltPerson.getLastName(), names[1]) &&
//										StringUtils.equalsIgnoreCase(yAltPerson.getPatName(), names[2]))
//								.findFirst();
//
//						YAltPerson newAltPerson = altPersonOptional.orElseGet(YAltPerson::new);
//						addSource(newAltPerson.getImportSources(), source);
//						newAltPerson.setFirstName(UtilString.toUpperCase(names[0]));
//						newAltPerson.setLastName(UtilString.toUpperCase(names[1]));
//						newAltPerson.setPatName(UtilString.toUpperCase(names[2]));
//						newAltPerson.setLanguage("EN");
//
//						if (altPersonOptional.isEmpty()) {
//							newAltPerson.setPerson(person);
//							person.getAltPeople().add(newAltPerson);
//						}
//					}
//
//					if (!StringUtils.isBlank(r.getEmail())) {
//						Optional<YEmail> emailOptional = person.getEmails()
//								.parallelStream()
//								.filter(e -> StringUtils.equalsIgnoreCase(r.getEmail(), e.getEmail()))
//								.findAny();
//						YEmail email = emailOptional.orElseGet(YEmail::new);
//						addSource(email.getImportSources(), source);
//						email.setEmail(UtilString.toLowerCase(r.getEmail()));
//
//						if (emailOptional.isEmpty()) {
//							email.setPerson(person);
//							person.getEmails().add(email);
//						}
//					}
//					if (!cached) {
//						personList.add(person);
//						yinnList.add(yinn);
//						counter[0]++;
//					}
//				}
//			});
//
//			ypr.saveAll(personList);
//			emnService.enrichMonitoringNotification(personList);
//
//
//			onePage = cr.findAllByPortionId(portion, pageRequest);
//
//			statusLogger = new StatusLogger(portion, counter[0] * 100L / count, PERCENT,
//			                                CONTRAGENT, ENRICHER, startTime, null, null);
//			Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//		}
//
//		logFinish(CONTRAGENT, counter[0]);
//
//		statusLogger = new StatusLogger(portion, 100L, "%",
//		                                CONTRAGENT, ENRICHER, startTime, LocalDateTime.now(),
//		                                importedRecords(counter[0]));
//		Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//	}
//
//    private void baseDirectorEnricher(UUID portion) {
//        logStart(BASE_DIRECTOR);
//
//        LocalDateTime startTime = LocalDateTime.now();
//        StatusLogger statusLogger = new StatusLogger(portion, 0L, "%",
//                BASE_DIRECTOR, ENRICHER, startTime, null, null);
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//
//        long[] counter = new long[1];
//        long[] wrongCounter = new long[1];
//
//        Pageable pageRequest = PageRequest.of(0, pageSize);
//        Page<BaseDirector> onePage = baseDirectorRepository.findAllByPortionId(portion, pageRequest);
//        long count = baseDirectorRepository.countAllByPortionId(portion);
//        String fileName = fileFormatUtil.getLogFileName(portion.toString());
//        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
//                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_DIRECTOR, portion));
//
//        ImportSource source = isr.findImportSourceByName(BASE_DIRECTOR);
//
//        while (!onePage.isEmpty()) {
//            pageRequest = pageRequest.next();
//            Set<YCompanyRelation> companyRelationSet = new HashSet<>();
//
//            onePage.forEach(r -> {
//                if (!StringUtils.isBlank(r.getInn()) && !StringUtils.isBlank(r.getOkpo())) {
//                    String okpo = String.format("%08d", Long.parseLong(r.getOkpo().replaceAll(ALL_NOT_NUMBER, "")));
//                    String inn = String.format(INN_FORMAT_REGEX, Long.parseLong(r.getInn().replaceAll(ALL_NOT_NUMBER, "")));
//                    YCompanyRelation yCompanyRelation;
//                    if (isValidInn(inn, null, logger, wrongCounter, counter)
//                            && isValidOkpo(okpo, logger, wrongCounter, counter)) {
//                        yCompanyRelation = addCompanyRelation(okpo, inn, DIRECTOR, source, companyRelationSet);
//                        companyRelationSet.add(yCompanyRelation);
//                    }
//                } else {
//                    logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {} and OKPO: {}", r.getInn(), r.getOkpo()), "Empty INN or OKPO");
//                    wrongCounter[0]++;
//                }
//                counter[0]++;
//            });
//
//            yCompanyRelationRepository.saveAll(companyRelationSet);
//
//            onePage = baseDirectorRepository.findAllByPortionId(portion, pageRequest);
//
//            statusLogger = new StatusLogger(portion, counter[0] * 100L / count, PERCENT,
//                    BASE_DIRECTOR, ENRICHER, startTime, null, null);
//            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//        }
//
//        logFinish(BASE_DIRECTOR, counter[0]);
//        logger.finish();
//
//        statusLogger = new StatusLogger(portion, 100L, "%",
//                BASE_DIRECTOR, ENRICHER, startTime,
//                LocalDateTime.now(),
//                importedRecords(counter[0]));
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//    }
//
//    private void baseCreatorEnricher(UUID portion) {
//        logStart(BASE_CREATOR);
//
//        LocalDateTime startTime = LocalDateTime.now();
//        StatusLogger statusLogger = new StatusLogger(portion, 0L, "%",
//                BASE_CREATOR, ENRICHER, startTime, null, null);
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//
//        long[] counter = new long[1];
//        long[] wrongCounter = new long[1];
//
//        Pageable pageRequest = PageRequest.of(0, pageSize);
//        Page<BaseCreator> onePage = baseCreatorRepository.findAllByPortionId(portion, pageRequest);
//        long count = baseCreatorRepository.countAllByPortionId(portion);
//        String fileName = fileFormatUtil.getLogFileName(portion.toString());
//        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
//                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_CREATOR, portion));
//
//        ImportSource source = isr.findImportSourceByName(BASE_CREATOR);
//
//        while (!onePage.isEmpty()) {
//            pageRequest = pageRequest.next();
//            Set<YCompanyRelation> yCompanyRelationSet = new HashSet<>();
//
//            onePage.forEach(r -> {
//                if (!StringUtils.isBlank(r.getInn()) && !StringUtils.isBlank(r.getOkpo())) {
//                    String okpo = String.format("%08d", Long.parseLong(r.getOkpo().replaceAll(ALL_NOT_NUMBER, "")));
//                    String inn = String.format(INN_FORMAT_REGEX, Long.parseLong(r.getInn().replaceAll(ALL_NOT_NUMBER, "")));
//                    YCompanyRelation yCompanyRelation;
//                    if (isValidInn(inn, null, logger, wrongCounter, counter)
//                            && isValidOkpo(okpo, logger, wrongCounter, counter)) {
//                        yCompanyRelation = addCompanyRelation(okpo, inn, CREATOR, source, yCompanyRelationSet);
//                        yCompanyRelationSet.add(yCompanyRelation);
//                    }
//                } else {
//                    logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {} and OKPO: {}", r.getInn(), r.getOkpo()), "Empty INN or OKPO");
//                    wrongCounter[0]++;
//                }
//                counter[0]++;
//            });
//
//            yCompanyRelationRepository.saveAll(yCompanyRelationSet);
//
//            onePage = baseCreatorRepository.findAllByPortionId(portion, pageRequest);
//
//            statusLogger = new StatusLogger(portion, counter[0] * 100L / count, PERCENT,
//                    BASE_CREATOR, ENRICHER, startTime, null, null);
//            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//        }
//
//        logFinish(BASE_CREATOR, counter[0]);
//        logger.finish();
//
//        statusLogger = new StatusLogger(portion, 100L, "%",
//                BASE_CREATOR, ENRICHER, startTime,
//                LocalDateTime.now(),
//                importedRecords(counter[0]));
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//    }
//
//    public void govua10Enricher(UUID portion) {
//        logStart(GOVUA10);
//
//        LocalDateTime startTime = LocalDateTime.now();
//        StatusLogger statusLogger = new StatusLogger(portion, 0L, "%",
//                GOVUA10, ENRICHER, startTime, null, null);
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//
//        long[] counter = new long[1];
//        long[] wrongCounter = new long[1];
//
//        Pageable pageRequest = PageRequest.of(0, pageSize);
//        log.info("before PageRequest.");
//        Page<Govua10> onePage = govua10Repository.findAllByPortionId(portion, pageRequest);
//        log.info("after pageReqest : {}", !onePage.isEmpty());
//        long count = govua10Repository.countAllByPortionId(portion);
//        String fileName = fileFormatUtil.getLogFileName(portion.toString());
//        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
//                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, GOVUA10, portion));
//
//        ImportSource source = isr.findImportSourceByName("govua10_t");
//
//        while (!onePage.isEmpty()) {
//            pageRequest = pageRequest.next();
//            List<YPerson> personList = new ArrayList<>();
//
//            onePage.forEach(r -> {
//                findCopy = false;
//                YPerson person = new YPerson();
//
//                if (r.getNumber().length() <= 6) {
//                    String passportNo = r.getNumber();
//                    String passportSerial = r.getSeries();
//                    if (isValidLocalPassport(passportNo, passportSerial, wrongCounter, counter, logger)) {
//                        passportSerial = transliterationToCyrillicLetters(passportSerial);
//                        int number = Integer.parseInt(passportNo);
//                        YPassport passport = new YPassport();
//                        passport.setSeries(passportSerial);
//                        passport.setNumber(number);
//                        passport.setAuthority(null);
//                        passport.setIssued(null);
//                        passport.setEndDate(r.getModified());
//                        passport.setRecordNumber(null);
//                        passport.setType(DOMESTIC_PASSPORT);
//                        passport.setValidity(false);
//                        person = addPassport(passport, personList, source, person);
//                    }
//                } else if (r.getNumber().length() > 6) {
//                    String passportNo = r.getNumber();
//                    if (isValidIdPassport(passportNo, null, wrongCounter, counter, logger)) {
//                        int number = Integer.parseInt(passportNo);
//                        YPassport passport = new YPassport();
//                        passport.setSeries(null);
//                        passport.setNumber(number);
//                        passport.setAuthority(null);
//                        passport.setIssued(null);
//                        passport.setEndDate(r.getModified());
//                        passport.setRecordNumber(null);
//                        passport.setType(IDCARD_PASSPORT);
//                        passport.setValidity(false);
//                        person = addPassport(passport, personList, source, person);
//                    }
//                }
//                person = addPerson(personList, person, source);
//                personList.add(person);
//                counter[0]++;
//            });
//            log.info("before save.");
//            ypr.saveAll(personList);
//            log.info("after save.");
//            emnService.enrichMonitoringNotification(personList);
//            log.info("before pageReqest.");
//            onePage = govua10Repository.findAllByPortionId(portion, pageRequest);
//            log.info("after pageReqest : {}", !onePage.isEmpty());
//
//            statusLogger = new StatusLogger(portion, counter[0] * 100L / count, PERCENT,
//                    GOVUA10, ENRICHER, startTime, null, null);
//            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//        }
//
//        logFinish(GOVUA10, counter[0]);
//        logger.finish();
//
//        statusLogger = new StatusLogger(portion, 100L, "%",
//                GOVUA10, ENRICHER, startTime, LocalDateTime.now(),
//                importedRecords(counter[0]));
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//    }
//
//    private void manualPersonEnrich(UUID revision) {
//        LocalDateTime startTime = LocalDateTime.now();
//        StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
//                MANUAL_PERSON, ENRICHER, startTime, null, null);
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//
//        long[] counter = new long[1];
//        long[] wrongCounter = new long[1];
//
//        Pageable pageRequest = PageRequest.of(0, pageSize);
//        FileDescription file = fileDescriptionRepository.findByUuid(revision).orElseThrow(() ->
//                new RuntimeException("Can't find file with id = " + revision));
//        Page<ManualPerson> onePage = manualPersonRepository.findAllByUuid(file, pageRequest);
//        long count = manualPersonRepository.countAllByUuid(file);
//        String fileName = fileFormatUtil.getLogFileName(revision.toString());
//        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
//                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, MANUAL_PERSON, revision));
//
//        ImportSource source = isr.findImportSourceByName("manual");
//
//        while (!onePage.isEmpty()) {
//            pageRequest = pageRequest.next();
//            List<YPerson> personList = new ArrayList<>();
//
//            onePage.forEach(r -> {
//                findCopy = false;
//                String lastName = UtilString.toUpperCase(r.getLnameUk());
//                String firstName = UtilString.toUpperCase(r.getFnameUk());
//                String patName = UtilString.toUpperCase(r.getPnameUk());
//                LocalDate birthday = stringToDate(r.getBirthday());
//
//                YPerson person = new YPerson();
//                person.setLastName(lastName);
//                person.setFirstName(firstName);
//                person.setPatName(patName);
//                person.setBirthdate(birthday);
//
//                if (!StringUtils.isBlank(r.getOkpo())) {
//                    String code = String.format(INN_FORMAT_REGEX, Long.parseLong(r.getOkpo().replaceAll(ALL_NOT_NUMBER, "")));
//                    if (isValidInn(code, stringToDate(r.getBirthday()), logger, wrongCounter, counter)) {
//                        long inn = Long.parseLong(code);
//                        person = addInn(inn, personList, source, person);
//                    }
//                }
//
//                String passportNo = r.getPassLocalNum();
//                String passportSerial = r.getPassLocalSerial();
//                if (isValidLocalPassport(passportNo, passportSerial, wrongCounter, counter, logger)) {
//                    passportSerial = transliterationToCyrillicLetters(passportSerial);
//                    int number = Integer.parseInt(passportNo);
//                    YPassport passport = new YPassport();
//                    passport.setSeries(passportSerial);
//                    passport.setNumber(number);
//                    passport.setAuthority(null);
//                    passport.setIssued(null);
//                    passport.setEndDate(null);
//                    passport.setRecordNumber(null);
//                    passport.setType(DOMESTIC_PASSPORT);
//                    passport.setValidity(true);
//                    person = addPassport(passport, personList, source, person);
//                }
//
//                String passportRecNo;
//                if (!StringUtils.isBlank(r.getPassIntNum())) {
//                    passportNo = r.getPassIntNum().substring(2);
//                    passportSerial = r.getPassIntNum().substring(0, 2);
//                    passportRecNo = r.getPassIntRecNum();
//                    if (isValidForeignPassport(passportNo, passportSerial, passportRecNo,
//                            wrongCounter, counter, logger)) {
//                        passportSerial = transliterationToLatinLetters(passportSerial);
//                        int number = Integer.parseInt(passportNo);
//                        YPassport passport = new YPassport();
//                        passport.setSeries(passportSerial);
//                        passport.setNumber(number);
//                        passport.setAuthority(r.getPassIntIssuer());
//                        passport.setIssued(stringToDate(r.getPassIntIssueDate()));
//                        passport.setEndDate(null);
//                        passport.setRecordNumber(r.getPassIntRecNum());
//                        passport.setType(FOREIGN_PASSPORT);
//                        passport.setValidity(true);
//                        person = addPassport(passport, personList, source, person);
//                    }
//                }
//
//                passportNo = r.getPassIdNum();
//                passportRecNo = r.getPassIdRecNum();
//                if (isValidIdPassport(passportNo, passportRecNo, wrongCounter, counter, logger)) {
//                    int number = Integer.parseInt(passportNo);
//                    YPassport passport = new YPassport();
//                    passport.setSeries(null);
//                    passport.setNumber(number);
//                    passport.setAuthority(r.getPassIdIssuer());
//                    passport.setIssued(stringToDate(r.getPassIdIssueDate()));
//                    passport.setEndDate(null);
//                    passport.setRecordNumber(r.getPassIdRecNum());
//                    passport.setType(IDCARD_PASSPORT);
//                    passport.setValidity(true);
//                    person = addPassport(passport, personList, source, person);
//                }
//
//                person = addPerson(personList, person, source);
//
//                Set<String> addresses = new HashSet<>();
//                if (!StringUtils.isBlank(r.getAddress()))
//                    addresses.add(r.getAddress().toUpperCase());
//
//                addAddresses(person, addresses, source);
//
//                Set<String> phones = new HashSet<>();
//                if (!StringUtils.isBlank(r.getPhone()))
//                    phones.add(r.getPhone().toUpperCase());
//
//                addPhones(person, phones, source);
//
//                Set<String> emails = new HashSet<>();
//                if (!StringUtils.isBlank(r.getEmail()))
//                    emails.add(r.getEmail().toUpperCase());
//
//                addEmails(person, emails, source);
//
//                addTags(person, r.getTags(), source);
//
//                if (!StringUtils.isBlank(r.getLnameRu()))
//                    addAltPerson(person, UtilString.toUpperCase(r.getLnameRu()),
//                            UtilString.toUpperCase(r.getFnameRu()),
//                            UtilString.toUpperCase(r.getPnameRu()), "RU", source);
//                if (!StringUtils.isBlank(r.getLnameEn()))
//                    addAltPerson(person, UtilString.toUpperCase(r.getLnameEn()),
//                            UtilString.toUpperCase(r.getFnameEn()),
//                            UtilString.toUpperCase(r.getPnameEn()), "EN", source);
//
//                personList.add(person);
//                counter[0]++;
//            });
//
//            ypr.saveAll(personList);
//            emnService.enrichMonitoringNotification(personList);
//
//            onePage = manualPersonRepository.findAllByUuid(file, pageRequest);
//
//            statusLogger = new StatusLogger(revision, counter[0] * 100L / count, PERCENT,
//                    MANUAL_PERSON, ENRICHER, startTime, null, null);
//            Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//        }
//
//        logFinish(MANUAL_PERSON, counter[0]);
//        logger.finish();
//
//        statusLogger = new StatusLogger(revision, 100L, "%",
//                MANUAL_PERSON, ENRICHER, startTime, LocalDateTime.now(),
//                importedRecords(counter[0]));
//        Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
//    }
//
//    private <T> T chooseNotNull(T first, T second) {
//        return second != null ? second : first;
//    }
//
//    private String chooseNotBlank(String first, String second) {
//        return !StringUtils.isBlank(second) ? second : first;
//    }
//
//    private void addAltPerson(YPerson person, String lastName, String firstName,
//                              String patName, String language,
//                              ImportSource source) {
//        Optional<YAltPerson> altPersonOptional;
//        if (person.getId() != null)
//            person = ypr.findWithAltPeopleById(person.getId()).orElse(person);
//
//        altPersonOptional = person.getAltPeople()
//                .parallelStream()
//                .filter(p -> (Objects.equals(p.getLastName(), lastName)
//                        && Objects.equals(p.getFirstName(), firstName)
//                        && Objects.equals(p.getPatName(), patName)))
//                .findAny();
//        YAltPerson altPerson = altPersonOptional.orElseGet(YAltPerson::new);
//        addSource(altPerson.getImportSources(), source);
//
//        altPerson.setFirstName(chooseNotNull(altPerson.getFirstName(), firstName));
//        altPerson.setLastName(chooseNotNull(altPerson.getLastName(), lastName));
//        altPerson.setPatName(chooseNotNull(altPerson.getPatName(), patName));
//        altPerson.setLanguage(chooseNotNull(altPerson.getLanguage(), language));
//
//        if (altPersonOptional.isEmpty()) {
//            altPerson.setPerson(person);
//            person.getAltPeople().add(altPerson);
//        }
//    }
//
//    private YPerson addPassport(YPassport ypassport, List<YPerson> personList,
//                                ImportSource source, YPerson person) {
//        YPassport passport;
//        if (person.getId() != null)
//            person = ypr.findWithPassportsById(person.getId()).orElse(person);
//
//        List<YPerson> yPersonList = personList.parallelStream()
//                .filter(p -> p.getPassports().contains(ypassport))
//                .collect(Collectors.toList());
//
//        Optional<YPassport> optionalYPassport = yPassportRepository.findByTypeAndNumberAndSeries(ypassport.getType(), ypassport.getNumber(), ypassport.getSeries());
//        optionalYPassport.ifPresent(pass -> yPersonList.addAll(ypr.findByPassportsContains(pass)));
//
//        if (optionalYPassport.isEmpty() && !yPersonList.isEmpty())
//            optionalYPassport = yPersonList.get(0).getPassports().parallelStream().filter(pas -> Objects.equals(pas.getType(), ypassport.getType())
//                    && Objects.equals(pas.getSeries(), ypassport.getSeries())
//                    && Objects.equals(pas.getNumber(), ypassport.getNumber())).findAny();
//        passport = optionalYPassport.orElseGet(YPassport::new);
//        if (passport.getId() != null)
//            passport = yPassportRepository.findById(passport.getId()).orElseGet(YPassport::new);
//
//        passport.setSeries(chooseNotBlank(passport.getSeries(), ypassport.getSeries()));
//        passport.setNumber(ypassport.getNumber());
//        passport.setAuthority(chooseNotBlank(passport.getAuthority(), ypassport.getAuthority()));
//        passport.setIssued(chooseNotNull(passport.getIssued(), ypassport.getIssued()));
//        passport.setEndDate(chooseNotNull(passport.getEndDate(), ypassport.getEndDate()));
//        passport.setRecordNumber(chooseNotBlank(passport.getRecordNumber(), ypassport.getRecordNumber()));
//        passport.setType(ypassport.getType());
//        passport.setValidity(ypassport.getValidity());
//
//        Optional<YPerson> optionalYPerson = Optional.empty();
//        if (!yPersonList.isEmpty()) {
//            YPerson finalPerson = person;
//            optionalYPerson = yPersonList.parallelStream().filter(p -> isEqualsPerson(p, finalPerson)).findAny();
//        }
//
//        if (optionalYPerson.isPresent()) {
//            YPerson findPerson = optionalYPerson.get();
//            findPerson.setLastName(chooseNotNull(findPerson.getLastName(), person.getLastName()));
//            findPerson.setFirstName(chooseNotNull(findPerson.getFirstName(), person.getFirstName()));
//            findPerson.setPatName(chooseNotNull(findPerson.getPatName(), person.getPatName()));
//            findPerson.setBirthdate(chooseNotNull(findPerson.getBirthdate(), person.getBirthdate()));
//
//            person = findPerson;
//            findCopy = true;
//            personList.remove(findPerson);
//        } else if (optionalYPassport.isPresent() && passport.getId() == null) yPassportRepository.save(passport);
//        person.getPassports().add(passport);
//        addSource(passport.getImportSources(), source);
//        return person;
//    }
//
//    private YPerson addInn(Long inn, List<YPerson> personList,
//                           ImportSource source, YPerson person) {
//        YINN yinn;
//        Optional<YINN> optionalYINN = personList.parallelStream()
//                .flatMap(p -> p.getInns().parallelStream())
//                .filter(p -> Objects.equals(p.getInn(), inn)).findAny();
//        if (optionalYINN.isEmpty())
//            optionalYINN = yir.findByInn(inn);
//
//        yinn = optionalYINN.orElseGet(YINN::new);
//        if (yinn.getId() != null) yinn = yir.findById(yinn.getId()).orElse(yinn);
//
//        addSource(yinn.getImportSources(), source);
//
//        yinn.setInn(chooseNotNull(yinn.getInn(), inn));
//
//        if (yinn.getPerson() != null && isEqualsPerson(yinn.getPerson(), person)) {
//            YPerson oldPerson = yinn.getPerson();
//            oldPerson.setLastName(chooseNotNull(oldPerson.getLastName(), person.getLastName()));
//            oldPerson.setFirstName(chooseNotNull(oldPerson.getFirstName(), person.getFirstName()));
//            oldPerson.setPatName(chooseNotNull(oldPerson.getPatName(), person.getPatName()));
//            oldPerson.setBirthdate(chooseNotNull(oldPerson.getBirthdate(), person.getBirthdate()));
//
//            person = oldPerson;
//            findCopy = true;
//            personList.remove(oldPerson);
//        } else if (yinn.getPerson() != null) {
//            addAltPerson(yinn.getPerson(), person.getLastName(), person.getFirstName(), person.getPatName(), "UA", source);
//            person = yinn.getPerson();
//        }
//
//        if (optionalYINN.isEmpty()) {
//            person.getInns().add(yinn);
//            yinn.setPerson(person);
//        }
//        return person;
//    }
//
//    private boolean isEqualsPerson(YPerson person, YPerson newPerson) {
//        return !(newPerson != null && person != null
//                && ((person.getLastName() != null && newPerson.getLastName() != null
//                && !person.getLastName().isBlank() && !newPerson.getLastName().isBlank() && !person.getLastName().equals(newPerson.getLastName()))
//                || (person.getFirstName() != null && newPerson.getFirstName() != null
//                && !person.getFirstName().isBlank() && !newPerson.getFirstName().isBlank() && !person.getFirstName().equals(newPerson.getFirstName()))
//                || (person.getPatName() != null && newPerson.getPatName() != null
//                && !person.getPatName().isBlank() && !newPerson.getPatName().isBlank() && !person.getPatName().equals(newPerson.getPatName()))
//                || (person.getBirthdate() != null && newPerson.getBirthdate() != null && !person.getBirthdate().equals(newPerson.getBirthdate()))));
//    }
//
//    private void addAddresses(YPerson person, Set<String> addresses, ImportSource source) {
//        addresses.forEach(a -> {
//            Optional<YAddress> addressOptional = person.getAddresses()
//                    .parallelStream()
//                    .filter(adr -> Objects.equals(adr.getAddress(), a))
//                    .findAny();
//            YAddress address = addressOptional.orElseGet(YAddress::new);
//            addSource(address.getImportSources(), source);
//            address.setAddress(chooseNotBlank(address.getAddress(), a));
//
//            address.setPerson(person);
//            person.getAddresses().add(address);
//        });
//    }
//
//    private void addPhones(YPerson person, Set<String> phones, ImportSource source) {
//        phones.forEach(p -> {
//            Optional<YPhone> phonesOptional = person.getPhones()
//                    .parallelStream()
//                    .filter(ph -> Objects.equals(ph.getPhone(), p))
//                    .findAny();
//            YPhone phone = phonesOptional.orElseGet(YPhone::new);
//            addSource(phone.getImportSources(), source);
//            phone.setPhone(chooseNotBlank(phone.getPhone(), p));
//
//            phone.setPerson(person);
//            person.getPhones().add(phone);
//        });
//    }
//
//    private void addEmails(YPerson person, Set<String> emails, ImportSource source) {
//        emails.forEach(e -> {
//            Optional<YEmail> emailOptional = person.getEmails()
//                    .parallelStream()
//                    .filter(em -> Objects.equals(em.getEmail(), e))
//                    .findAny();
//            YEmail email = emailOptional.orElseGet(YEmail::new);
//            addSource(email.getImportSources(), source);
//            email.setEmail(chooseNotBlank(email.getEmail(), e));
//
//            email.setPerson(person);
//            person.getEmails().add(email);
//        });
//    }
//
//    private void addTags(YPerson person, Set<ManualTag> tags, ImportSource source) {
//        tags.forEach(t -> {
//            Optional<YTag> tagOptional = person.getTags()
//                    .parallelStream()
//                    .filter(tg -> Objects.equals(tg.getName(), UtilString.toUpperCase(t.getMkId()))
//                            && Objects.equals(tg.getAsOf(), stringToDate(t.getMkStart()))
//                            && Objects.equals(tg.getUntil(), stringToDate(t.getMkExpire()))).findAny();
//            YTag tag = tagOptional.orElseGet(YTag::new);
//            addSource(tag.getImportSources(), source);
//
//            if (!StringUtils.isBlank(t.getMkId())) {
//                tag.setName(chooseNotBlank(tag.getName(), UtilString.toUpperCase(t.getMkId())));
//                tag.setAsOf(chooseNotNull(tag.getAsOf(), stringToDate(t.getMkStart())));
//                tag.setUntil(chooseNotNull(tag.getUntil(), stringToDate(t.getMkExpire())));
//                tag.setSource(chooseNotBlank(tag.getSource(), UtilString.toUpperCase(t.getMkSource())));
//
//                tag.setPerson(person);
//                person.getTags().add(tag);
//            }
//        });
//    }
//
//    private YPerson addPerson(List<YPerson> personList, YPerson yperson, ImportSource source) {
//        boolean find = false;
//        YPerson person = yperson;
//
//        if (!findCopy && yperson.getFirstName() != null && yperson.getLastName() != null && yperson.getPatName() != null
//                && !yperson.getFirstName().isBlank() && !yperson.getLastName().isBlank() && !yperson.getPatName().isBlank()) {
//            List<YPerson> yPersonCachedList = personList.parallelStream().filter(p ->
//                            Objects.equals(p.getLastName(), yperson.getLastName())
//                                    && Objects.equals(p.getFirstName(), yperson.getFirstName())
//                                    && Objects.equals(p.getPatName(), yperson.getPatName())
//                                    && Objects.equals(yperson.getBirthdate(), p.getBirthdate()))
//                    .collect(Collectors.toList());
//            if (yPersonCachedList.size() == 1) {
//                person = yPersonCachedList.get(0);
//                findCopy = true;
//                find = true;
//            }
//
//            if (!find) {
//                List<YPerson> yPersonSavedList = ypr.findByLastNameAndFirstNameAndPatNameAndBirthdate(yperson.getLastName(),
//                        yperson.getFirstName(), yperson.getPatName(), yperson.getBirthdate());
//                if (yPersonSavedList.size() == 1)
//                    person = yPersonSavedList.get(0);
//            }
//
//            if (!StringUtils.isBlank(yperson.getLastName()))
//                person.setLastName(chooseNotNull(person.getLastName(), yperson.getLastName().toUpperCase()));
//            if (!StringUtils.isBlank(yperson.getFirstName()))
//                person.setFirstName(chooseNotNull(person.getFirstName(), yperson.getFirstName().toUpperCase()));
//            if (!StringUtils.isBlank(yperson.getPatName()))
//                person.setPatName(chooseNotNull(person.getPatName(), yperson.getPatName().toUpperCase()));
//            person.setBirthdate(chooseNotNull(person.getBirthdate(), yperson.getBirthdate()));
//        }
//        if (person.getId() == null) person.setId(UUID.randomUUID());
//        else person = ypr.findWithInnsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSourcesById(person.getId()).orElse(person);
//
//        addSource(person.getImportSources(), source);
//        personList.remove(person);
//        return person;
//    }
//
//    private YCompanyRelation addCompanyRelation(String edrpou, String inn, String role, ImportSource source,
//                                                Set<YCompanyRelation> companyRelationSet) {
//        Optional<YINN> innSavedOptional = yir.findByInn(Long.parseLong(inn));
//        YINN yinn = innSavedOptional.orElseGet(YINN::new);
//        if (yinn.getId() != null)
//            yinn = yir.findById(yinn.getId()).orElse(yinn);
//        addSource(yinn.getImportSources(), source);
//        yinn.setInn(Long.parseLong(inn));
//
//        Optional<YPerson> personSavedOptional = Optional.empty();
//        if (yinn.getId() != null) personSavedOptional = ypr.findByInnsContains(yinn);
//        YPerson yPerson = personSavedOptional.orElseGet(YPerson::new);
//        yPerson.setId(UUID.randomUUID());
//        yPerson.getInns().add(yinn);
//        if (yPerson.getId() != null) {
//            yPerson = ypr.findWithSourcesById(yPerson.getId()).orElse(yPerson);
//        }
//        addSource(yPerson.getImportSources(), source);
//        ypr.save(yPerson);
//
//        Optional<YCompany> companySavedOptional = yCompanyRepository.findByEdrpou(Long.parseLong(edrpou));
//        YCompany yCompany = companySavedOptional.orElseGet(YCompany::new);
//        if (yCompany.getId() == null) yCompany.setId(UUID.randomUUID());
//        yCompany.setEdrpou(Long.parseLong(edrpou));
//        addSource(yCompany.getImportSources(), source);
//        yCompanyRepository.save(yCompany);
//
//        YCompanyRole companyRole = yCompanyRoleRepository.findByRole(role);
//
//        YPerson finalYPerson = yPerson;
//        Optional<YCompanyRelation> yCompanyRelationOptional = companyRelationSet.parallelStream()
//                .filter(mr -> Objects.equals(mr.getCompany(), yCompany)
//                        && Objects.equals(mr.getPerson(), finalYPerson)
//                        && Objects.equals(mr.getRole().getRole(), role)).findAny();
//        if (yCompanyRelationOptional.isEmpty())
//            yCompanyRelationOptional = yCompanyRelationRepository.findByCompanyAndPersonAndRole(yCompany, yPerson, companyRole);
//
//        YCompanyRelation yCompanyRelation = yCompanyRelationOptional.orElseGet(YCompanyRelation::new);
//        yCompanyRelation.setCompany(yCompany);
//        yCompanyRelation.setPerson(yPerson);
//        yCompanyRelation.setRole(companyRole);
//
//        return yCompanyRelation;
//    }
//
//    private LocalDate stringToDate(String date) {
//        LocalDate localDate = null;
//        if (!StringUtils.isBlank(date)) {
//            localDate = LocalDate.of(Integer.parseInt(date.substring(6)),
//                    Integer.parseInt(date.substring(3, 5)),
//                    Integer.parseInt(date.substring(0, 2)));
//        }
//        return localDate;
//    }
//
//    private String transliterationToCyrillicLetters(String serial) {
//        StringBuilder cyrillicSerial = new StringBuilder();
//        if (!StringUtils.isBlank(serial))
//            for (int i = 0; i < serial.length(); i++) {
//                int index = LATIN_LETTERS.indexOf(serial.charAt(i));
//                if (index > -1) cyrillicSerial.append(CYRILLIC_LETTERS.charAt(index));
//                else cyrillicSerial.append(serial.charAt(i));
//            }
//        return cyrillicSerial.toString();
//    }
//
//    private String transliterationToLatinLetters(String serial) {
//        StringBuilder latinSerial = new StringBuilder();
//        for (int i = 0; i < serial.length(); i++) {
//            int index = CYRILLIC_LETTERS.indexOf(serial.charAt(i));
//            if (index > -1) latinSerial.append(LATIN_LETTERS.charAt(index));
//            else latinSerial.append(serial.charAt(i));
//        }
//        return latinSerial.toString();
//    }
//
//    private boolean isValidInn(String inn, LocalDate birthDay, DefaultErrorLogger logger,
//                               long[] wrongCounter, long[] counter) {
//        if (inn.matches(INN_REGEX)) {
//            boolean isValidBirthDateInn = birthDay == null || Objects.equals(String.valueOf(birthDay.toEpochDay()
//                    - START_DATE.toEpochDay() + 1), inn.substring(0, 5));
//            int controlNumber = ((-1 * Integer.parseInt(String.valueOf(inn.charAt(0)))
//                    + 5 * Integer.parseInt(String.valueOf(inn.charAt(1)))
//                    + 7 * Integer.parseInt(String.valueOf(inn.charAt(2)))
//                    + 9 * Integer.parseInt(String.valueOf(inn.charAt(3)))
//                    + 4 * Integer.parseInt(String.valueOf(inn.charAt(4)))
//                    + 6 * Integer.parseInt(String.valueOf(inn.charAt(5)))
//                    + 10 * Integer.parseInt(String.valueOf(inn.charAt(6)))
//                    + 5 * Integer.parseInt(String.valueOf(inn.charAt(7)))
//                    + 7 * Integer.parseInt(String.valueOf(inn.charAt(8)))) % 11) % 10;
//            return Objects.equals(Integer.parseInt(String.valueOf(inn.charAt(9))), controlNumber)
//                    && isValidBirthDateInn;
//        }
//        logError(logger, (counter[0] + 1L), "INN: " + inn, "Wrong INN");
//        wrongCounter[0]++;
//        return false;
//    }
//
//    private boolean isValidLocalPassport(String number, String serial,
//                                         long[] wrongCounter, long[] counter, DefaultErrorLogger logger) {
//        if (StringUtils.isBlank(number) || StringUtils.isBlank(serial)) {
//            logError(logger, (counter[0] + 1L), "Passport: " + serial + number, "Empty serial or number");
//            wrongCounter[0]++;
//        } else if (!transliterationToCyrillicLetters(serial).matches(DOMESTIC_SERIES_REGEX)
//                || !number.matches(PASS_NUMBER_REGEX)) {
//            logError(logger, (counter[0] + 1L), "Passport: " + serial + number, "Wrong format passport serial or number");
//            wrongCounter[0]++;
//        } else return true;
//        return false;
//    }
//
//    private boolean isValidForeignPassport(String number, String serial, String recordNumber,
//                                           long[] wrongCounter, long[] counter, DefaultErrorLogger logger) {
//        if (StringUtils.isBlank(number) || StringUtils.isBlank(serial)) {
//            logError(logger, (counter[0] + 1L), "Passport: " + serial + number, "Empty serial or number");
//            wrongCounter[0]++;
//        } else if (!transliterationToLatinLetters(serial).matches(FOREIGN_SERIES_REGEX)
//                || !number.matches(PASS_NUMBER_REGEX)) {
//            logError(logger, (counter[0] + 1L), "Passport: " + serial + number, "Wrong format passport serial or number");
//            wrongCounter[0]++;
//        } else if (!StringUtils.isBlank(recordNumber) && !recordNumber.matches(RECORD_NUMBER_REGEX)) {
//            logError(logger, (counter[0] + 1L), "Record number: " + recordNumber, "Wrong format passport record number");
//            wrongCounter[0]++;
//        } else return true;
//        return false;
//    }
//
//    private boolean isValidIdPassport(String number, String recordNumber,
//                                      long[] wrongCounter, long[] counter, DefaultErrorLogger logger) {
//        if (StringUtils.isBlank(number)) {
//            logError(logger, (counter[0] + 1L), "Passport number: " + number, "Empty number");
//            wrongCounter[0]++;
//        } else if (!number.matches(IDCARD_NUMBER_REGEX)) {
//            logError(logger, (counter[0] + 1L), "Passport number: " + number, "Wrong format passport number");
//            wrongCounter[0]++;
//        } else if (!StringUtils.isBlank(recordNumber) && !recordNumber.matches(RECORD_NUMBER_REGEX)) {
//            logError(logger, (counter[0] + 1L), "Record number: " + recordNumber, "Wrong format passport record number");
//            wrongCounter[0]++;
//        } else return true;
//        return false;
//    }
//
//    private boolean isValidOkpo(String okpo, DefaultErrorLogger logger,
//                                long[] wrongCounter, long[] counter) {
//        if (!StringUtils.isBlank(okpo) && okpo.matches(OKPO_REGEX)) {
//            int lnCs;
//            int idk = Integer.parseInt(okpo);
//            int firstNum = Integer.parseInt(String.valueOf(okpo.charAt(0)));
//            int secondNum = Integer.parseInt(String.valueOf(okpo.charAt(1)));
//            int thirdNum = Integer.parseInt(String.valueOf(okpo.charAt(2)));
//            int fourthNum = Integer.parseInt(String.valueOf(okpo.charAt(3)));
//            int fifthNum = Integer.parseInt(String.valueOf(okpo.charAt(4)));
//            int sixthNum = Integer.parseInt(String.valueOf(okpo.charAt(5)));
//            int seventhNum = Integer.parseInt(String.valueOf(okpo.charAt(6)));
//            if (idk < 30000000 || idk > 60000000) lnCs = (firstNum + secondNum * 2 + thirdNum * 3 + fourthNum * 4
//                    + fifthNum * 5 + sixthNum * 6 + seventhNum * 7) % 11;
//            else lnCs = (firstNum * 7 + secondNum + thirdNum * 2 + fourthNum * 3
//                    + fifthNum * 4 + sixthNum * 5 + seventhNum * 6) % 11;
//            if (lnCs == 10) {
//                if (idk < 30000000 || idk > 60000000)
//                    lnCs = (firstNum * 3 + secondNum * 4 + thirdNum * 5 + fourthNum * 6
//                            + fifthNum * 7 + sixthNum * 8 + seventhNum * 9) % 11;
//                else lnCs = (firstNum * 9 + secondNum * 3 + thirdNum * 4 + fourthNum * 5
//                        + fifthNum * 6 + sixthNum * 7 + seventhNum * 8) % 11;
//            }
//            return (String.valueOf(okpo.charAt(7)).equals(String.valueOf(lnCs)));
//        }
//        if (!StringUtils.isBlank(okpo))
//            logError(logger, (counter[0] + 1L), "OKPO: " + okpo, "Wrong OKPO");
//        wrongCounter[0]++;
//        return false;
//    }
//
//    protected void logError(DefaultErrorLogger logger, long row, String info, String clarification) {
//        if (logger != null)
//            logger.logError(new ErrorReport(row, -1L, -1L, -1L, -1L, info, clarification));
//    }
//
//    private void addSource(Set<ImportSource> sources, ImportSource source) {
//        if (source != null) sources.add(source);
//    }
}

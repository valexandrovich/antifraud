package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.MANUAL_COMPANY;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.ALL_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.util.validator.Validator.isValidEdrpou;
import static ua.com.solidity.util.validator.Validator.isValidInn;
import static ua.com.solidity.util.validator.Validator.isValidPdv;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import ua.com.solidity.common.UtilString;
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.ManualCompany;
import ua.com.solidity.db.entities.YCAddress;
import ua.com.solidity.db.entities.YCTag;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyRelation;
import ua.com.solidity.db.entities.YCompanyRelationCompany;
import ua.com.solidity.db.entities.YCompanyRole;
import ua.com.solidity.db.entities.YCompanyState;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.repositories.FileDescriptionRepository;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.ManualCompanyRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YCompanyRelationCompanyRepository;
import ua.com.solidity.db.repositories.YCompanyRelationRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YCompanyRoleRepository;
import ua.com.solidity.db.repositories.YCompanyStateRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class ManualCompanyEnricher implements Enricher {
    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final YPersonRepository ypr;
    private final ManualCompanyRepository manualCompanyRepository;
    private final FileDescriptionRepository fileDescriptionRepository;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final YCompanyRepository companyRepository;
    private final YINNRepository yinnRepository;
    private final TagTypeRepository tagTypeRepository;
    private final YCompanyStateRepository companyStateRepository;
    private final YCompanyRoleRepository companyRoleRepository;
    private final YCompanyRelationRepository yCompanyRelationRepository;
    private final YCompanyRelationCompanyRepository yCompanyRelationCompanyRepository;
    private final HttpClient httpClient;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${enricher.searchPortion}")
    private Integer searchPortion;
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
    public void enrich(UUID revision) {
        LocalDateTime startTime = LocalDateTime.now();
        try {
            logStart(MANUAL_COMPANY);

            StatusChanger statusChanger = new StatusChanger(revision, MANUAL_COMPANY, ENRICHER);

            long[] counter = new long[1];
            long[] wrongCounter = new long[1];

            Pageable pageRequest = PageRequest.of(0, pageSize);
            FileDescription file = fileDescriptionRepository.findByUuid(revision).orElseThrow(() ->
                    new RuntimeException("Can't find file with id = " + revision));
            Page<ManualCompany> onePage = manualCompanyRepository.findAllByUuid(file, pageRequest);
            long count = manualCompanyRepository.countAllByUuid(file);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(revision.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, MANUAL_COMPANY, revision));

            ImportSource source = isr.findImportSourceByName(MANUAL_COMPANY);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();
                List<ManualCompany> page = onePage.toList();
                Map<Long, UUID> uuidMap = new HashMap<>();

                while (!page.isEmpty()) {
                    Duration duration = Duration.between(startTime, LocalDateTime.now());
                    if (duration.getSeconds() > timeOutTime)
                        throw new TimeoutException("Time ran out for portion: " + revision);
                    List<EntityProcessing> entityProcessings = page.parallelStream().map(p -> {
                        EntityProcessing entityProcessing = new EntityProcessing();
                        UUID uuid = UUID.randomUUID();
                        uuidMap.put(p.getId(), uuid);
                        entityProcessing.setUuid(uuid);
                        if (StringUtils.isNotBlank(p.getInn()) && p.getInn().matches(ALL_NUMBER_REGEX))
                            entityProcessing.setInn(Long.parseLong(p.getInn()));
                        if (StringUtils.isNotBlank(p.getEdrpou()) && p.getEdrpou().matches(ALL_NUMBER_REGEX))
                            entityProcessing.setEdrpou(Long.parseLong(p.getEdrpou()));
                        if (StringUtils.isNotBlank(p.getPdv()) && p.getPdv().matches(ALL_NUMBER_REGEX))
                            entityProcessing.setPdv(Long.parseLong(p.getPdv()));
                        if (StringUtils.isNotBlank(p.getName()))
                            entityProcessing.setCompanyHash(Objects.hash(p.getName()));
                        return entityProcessing;
                    }).collect(Collectors.toList());

                    entityProcessings.addAll(page.parallelStream().map(c -> {
                        EntityProcessing entityProcessing = new EntityProcessing();
                        UUID uuid = UUID.randomUUID();
                        uuidMap.put(c.getId(), uuid);
                        entityProcessing.setUuid(uuid);
                        entityProcessing.setUuid(uuid);
                        if (StringUtils.isNotBlank(c.getEdrpouRelationCompany()) && c.getEdrpouRelationCompany().matches(ALL_NUMBER_REGEX))
                            entityProcessing.setEdrpou(Long.parseLong(c.getEdrpouRelationCompany()));
                        if (StringUtils.isNotBlank(c.getCname()))
                            entityProcessing.setCompanyHash(Objects.hash(c.getCname()));
                        return entityProcessing;
                    }).collect(Collectors.toList()));

                    UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                    String url = urlPost + "?id=" + revision;
                    DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                    resp = new ArrayList<>(response.getResp());
                    List<UUID> respId = response.getRespId();
                    List<UUID> temp = response.getTemp();

                    List<ManualCompany> workPortion = page.parallelStream().filter(p -> respId.contains(uuidMap.get(p.getId())))
                            .collect(Collectors.toList());

                    if (workPortion.isEmpty()) Thread.sleep(sleepTime);

                    Set<YPerson> personSet = new HashSet<>();
                    Set<Long> innsSet = new HashSet<>();
                    Set<Long> edrpouSet = new HashSet<>();
                    Set<Long> pdvsSet = new HashSet<>();
                    Set<YCompany> savedCompanies = new HashSet<>();
                    Set<YCompany> companies = new HashSet<>();
                    Set<YCompanyRelation> savedCompanyRelation = new HashSet<>();
                    Set<YCompanyRelation> yCompanyRelationSet = new HashSet<>();
                    Set<YCompanyRelationCompany> yCompanyRelationCompaniesSet = new HashSet<>();
                    Set<YCompanyRelationCompany> savedCompanyRelationCompanies = new HashSet<>();
                    Set<YCompany> companiesCreators = new HashSet<>();
                    Set<YINN> inns = new HashSet<>();
                    Set<YPerson> savedPersonSet = new HashSet<>();

                    workPortion.forEach(r -> {
                        if (StringUtils.isNotBlank(r.getInn()))
                            innsSet.add(Long.parseLong(r.getInn()));
                        if (StringUtils.isNotBlank(r.getEdrpou()))
                            edrpouSet.add(Long.parseLong(r.getEdrpou()));
                        if (StringUtils.isNotBlank(r.getEdrpouRelationCompany()))
                            edrpouSet.add(Long.parseLong(r.getEdrpouRelationCompany()));
                        if (StringUtils.isNotBlank(r.getPdv()))
                            pdvsSet.add(Long.parseLong(r.getPdv()));
                    });

                    if (!innsSet.isEmpty()) {
                        List<Long>[] codesListArray = extender.partition(new ArrayList<>(innsSet), searchPortion);
                        for (List<Long> list : codesListArray) {
                            inns.addAll(yinnRepository.findInns(new HashSet<>(list)));
                            savedPersonSet.addAll(ypr.findPeopleInns(new HashSet<>(list)));
                            savedCompanyRelation.addAll(yCompanyRelationRepository.findRelationByInns(new HashSet<>(list)));
                        }
                    }

                    if (!pdvsSet.isEmpty()) {
                        List<Long>[] codesListArray = extender.partition(new ArrayList<>(pdvsSet), searchPortion);
                        for (List<Long> list : codesListArray) {
                            savedCompanies.addAll(companyRepository.findWithPdvCompanies(new HashSet<>(list)));
                            savedCompanyRelation.addAll(yCompanyRelationRepository.findRelationByPdvs(new HashSet<>(list)));
                            savedCompanyRelationCompanies.addAll(yCompanyRelationCompanyRepository.findRelationByPdv(new HashSet<>(list)));
                        }
                    }
                    if (!edrpouSet.isEmpty()) {
                        List<Long>[] codesListArray = extender.partition(new ArrayList<>(edrpouSet), searchPortion);
                        for (List<Long> list : codesListArray) {
                            savedCompanies.addAll(companyRepository.finnByEdrpous(new HashSet<>(list)));
                            savedCompanyRelation.addAll(yCompanyRelationRepository.findRelationByEdrpous(new HashSet<>(list)));
                            savedCompanyRelationCompanies.addAll(yCompanyRelationCompanyRepository.findRelationWithRelationCompanyByEdrpou(new HashSet<>(list)));
                            savedCompanyRelationCompanies.addAll(yCompanyRelationCompanyRepository.findRelationByEdrpou(new HashSet<>(list)));
                        }
                    }

                    workPortion.forEach(r -> {
                        String lastName = UtilString.toUpperCase(r.getLname());
                        String firstName = UtilString.toUpperCase(r.getFname());
                        String patName = UtilString.toUpperCase(r.getPname());

                        YPerson person = new YPerson();
                        person.setLastName(lastName);
                        person.setFirstName(firstName);
                        person.setPatName(patName);

                        if (!StringUtils.isBlank(r.getInn()) && r.getInn().matches(CONTAINS_NUMERAL_REGEX)) {
                            String inn = r.getInn().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                            if (isValidInn(inn, null)) {
                                person = extender.addInn(Long.parseLong(inn), personSet, source, person, inns, savedPersonSet);
                            } else {
                                logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getInn()), "Wrong INN");
                                wrongCounter[0]++;
                            }
                        }

                        person = extender.addPerson(personSet, person, source, true);

                        YCompany company = new YCompany();
                        company.setName(UtilString.toUpperCase(r.getName()));
                        if (!StringUtils.isBlank(r.getEdrpou()) && r.getEdrpou().matches(CONTAINS_NUMERAL_REGEX)) {
                            String edrpou = r.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                            if (isValidEdrpou(edrpou)) {
                                company.setEdrpou(Long.parseLong(edrpou));
                            } else {
                                logError(logger, (counter[0] + 1L), Utils.messageFormat("EDRPOU: {}", r.getEdrpou()), "Wrong EDRPOU");
                                wrongCounter[0]++;
                            }
                        }
                        if (!StringUtils.isBlank(r.getPdv()) && r.getPdv().matches(CONTAINS_NUMERAL_REGEX)) {
                            String pdv = r.getPdv().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                            if (isValidPdv(pdv)) {
                                company.setPdv(Long.parseLong(pdv));
                            } else {
                                logError(logger, (counter[0] + 1L), Utils.messageFormat("PDV: {}", r.getPdv()), "Wrong PDV");
                                wrongCounter[0]++;
                            }
                        }
                        Optional<YCompanyState> state = companyStateRepository.findByState(UtilString.toUpperCase(r.getState()));
                        if (state.isPresent()) company.setState(state.get());

                        if (company.getEdrpou() != null || company.getPdv() != null) {
                            company = extender.addCompany(companies, source, company, savedCompanies);

                            Set<YCAddress> addresses = new HashSet<>();
                            YCAddress address = new YCAddress();
                            address.setAddress(UtilString.toUpperCase(r.getAddress()));
                            addresses.add(address);

                            extender.addCAddresses(company, addresses, source);

                            if (StringUtils.isNotBlank(r.getShortName()))
                                extender.addAltCompany(company, UtilString.toUpperCase(r.getShortName()), "UA", source);

                            if (StringUtils.isNotBlank(r.getNameEn()))
                                extender.addAltCompany(company, UtilString.toUpperCase(r.getNameEn()), "EN", source);

                            Set<YCTag> tags = new HashSet<>();
                            r.getTags().forEach(t -> {
                                YCTag tag = new YCTag();
                                tag.setTagType(tagTypeRepository.findByCode(t.getMkId().toUpperCase()).orElseThrow(() ->
                                        new RuntimeException("Not found tag with code: " + t.getMkId())));
                                tag.setAsOf(extender.stringToDate(t.getMkStart()));
                                tag.setUntil(extender.stringToDate(t.getMkExpire()));
                                tag.setSource(t.getMkSource());
                                if (tag.getUntil() == null) tag.setUntil(LocalDate.of(3500, 1, 1));
                                tags.add(tag);
                            });
                            extender.addTags(company, tags, source);
                        }

                        Optional<YCompanyRole> role = companyRoleRepository.findByRole(UtilString.toUpperCase(r.getTypeRelationPerson()));
                        if (company != null && person != null && role.isPresent())
                            extender.addCompanyRelation(person, company, role.get(), source, yCompanyRelationSet, savedCompanyRelation);

                        YCompany companyCreator = null;
                        if (!StringUtils.isBlank(r.getEdrpouRelationCompany()) && r.getEdrpouRelationCompany().matches(CONTAINS_NUMERAL_REGEX)) {
                            String edrpou = r.getEdrpouRelationCompany().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                            if (isValidEdrpou(edrpou)) {
                                companyCreator = new YCompany();
                                companyCreator.setName(UtilString.toUpperCase(r.getCname()));
                                companyCreator.setEdrpou(Long.parseLong(edrpou));

                                companyCreator = extender.addCompany(companiesCreators, source, companyCreator, savedCompanies);
                            } else {
                                logError(logger, (counter[0] + 1L), Utils.messageFormat("EDRPOU: {}", r.getEdrpouRelationCompany()), "Wrong EDRPOU");
                                wrongCounter[0]++;
                            }
                        }

                        role = companyRoleRepository.findByRole(UtilString.toUpperCase(r.getTypeRelationCompany()));
                        if (company != null && companyCreator != null && role.isPresent())
                            extender.addCompanyRelation(companyCreator, company, role.get(), source, yCompanyRelationCompaniesSet, savedCompanyRelationCompanies);

                        if (!resp.isEmpty()) {
                            counter[0]++;
                            statusChanger.addProcessedVolume(1);
                        }
                    });

                    UUID dispatcherIdFinish = httpClient.get(urlPost, UUID.class);
                    if (Objects.equals(dispatcherId, dispatcherIdFinish)) {

                        if (!personSet.isEmpty()) {
                            emnService.enrichYPersonPackageMonitoringNotification(personSet);
                            ypr.saveAll(personSet);
                        }

                        if (!companies.isEmpty()) {
                            emnService.enrichYCompanyPackageMonitoringNotification(companies);
                            companyRepository.saveAll(companies);
                        }

                        if (!companiesCreators.isEmpty()) {
                            companyRepository.saveAll(companiesCreators);
                        }

                        if (!yCompanyRelationSet.isEmpty())
                            yCompanyRelationRepository.saveAll(yCompanyRelationSet);
                        if (!yCompanyRelationCompaniesSet.isEmpty())
                            yCompanyRelationCompanyRepository.saveAll(yCompanyRelationCompaniesSet);

                        if (!resp.isEmpty()) {
                            httpClient.post(urlDelete, Boolean.class, resp);
                            resp.clear();
                        }

                        emnService.enrichYPersonMonitoringNotification(personSet);
                        emnService.enrichYCompanyMonitoringNotification(companies);
                        emnService.enrichYCompanyMonitoringNotification(companiesCreators);

                        page = page.parallelStream().filter(p -> temp.contains(uuidMap.get(p.getId()))).collect(Collectors.toList());
                    } else {
                        counter[0] -= resp.size();
                        statusChanger.addProcessedVolume(-resp.size());
                    }
                }

                onePage = manualCompanyRepository.findAllByUuid(file, pageRequest);
            }

            logFinish(MANUAL_COMPANY, counter[0]);
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

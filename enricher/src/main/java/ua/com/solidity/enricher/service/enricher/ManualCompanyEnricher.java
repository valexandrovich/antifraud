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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
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
import ua.com.solidity.util.model.YCompanyProcessing;
import ua.com.solidity.util.model.YPersonProcessing;
import ua.com.solidity.util.model.response.YCompanyDispatcherResponse;
import ua.com.solidity.util.model.response.YPersonDispatcherResponse;

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
    @Value("${dispatcher.url.person}")
    private String urlPersonPost;
    @Value("${dispatcher.url.person.delete}")
    private String urlPersonDelete;
    @Value("${dispatcher.url.company}")
    private String urlCompanyPost;
    @Value("${dispatcher.url.company.delete}")
    private String urlCompanyDelete;
    private List<UUID> respPeople;
    private List<UUID> respCompanies;

    @Override
    public void enrich(UUID revision) {
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
            Set<YPerson> peopleSet = new HashSet<>();
            Set<YCompany> companiesSet = new HashSet<>();
            List<ManualCompany> page = onePage.toList();
            Map<Long, UUID> uuidMap = new HashMap<>();

            while (!page.isEmpty()) {
                List<YPersonProcessing> peopleProcessing = page.parallelStream().map(p -> {
                    YPersonProcessing personProcessing = new YPersonProcessing();
                    UUID uuid = UUID.randomUUID();
                    uuidMap.put(p.getId(), uuid);
                    personProcessing.setUuid(uuid);
                    if (StringUtils.isNotBlank(p.getInn()) && p.getInn().matches(ALL_NUMBER_REGEX))
                        personProcessing.setInn(Long.valueOf(p.getInn()));
                    return personProcessing;
                }).collect(Collectors.toList());
                List<YCompanyProcessing> companiesProcessing = page.parallelStream().map(c -> {
                    YCompanyProcessing companyProcessing = new YCompanyProcessing();
                    UUID uuid = UUID.randomUUID();
                    uuidMap.put(c.getId(), uuid);
                    companyProcessing.setUuid(uuid);
                    if (StringUtils.isNotBlank(c.getEdrpou()) && c.getEdrpou().matches(ALL_NUMBER_REGEX))
                        companyProcessing.setEdrpou(Long.valueOf(c.getEdrpou()));
                    if (StringUtils.isNotBlank(c.getPdv()) && c.getPdv().matches(ALL_NUMBER_REGEX))
                        companyProcessing.setPdv(Long.valueOf(c.getPdv()));
                    if (StringUtils.isNotBlank(c.getName()))
                        companyProcessing.setCompanyHash(Objects.hash(c.getName()));
                    return companyProcessing;
                }).collect(Collectors.toList());
                companiesProcessing.addAll(page.parallelStream().map(c -> {
                    YCompanyProcessing companyProcessing = new YCompanyProcessing();
                    UUID uuid = UUID.randomUUID();
                    uuidMap.put(c.getId(), uuid);
                    companyProcessing.setUuid(uuid);
                    companyProcessing.setUuid(uuid);
                    if (StringUtils.isNotBlank(c.getEdrpouRelationCompany()) && c.getEdrpouRelationCompany().matches(ALL_NUMBER_REGEX))
                        companyProcessing.setEdrpou(Long.valueOf(c.getEdrpouRelationCompany()));
                    if (StringUtils.isNotBlank(c.getCname()))
                        companyProcessing.setCompanyHash(Objects.hash(c.getCname()));
                    return companyProcessing;
                }).collect(Collectors.toList()));

                UUID dispatcherId = httpClient.get(urlCompanyPost, UUID.class);

                String url = urlPersonPost + "?id=" + revision;
                YPersonDispatcherResponse response = httpClient.post(url, YPersonDispatcherResponse.class, peopleProcessing);
                respPeople = response.getResp();
                List<UUID> tempPeople = response.getTemp();

                YCompanyDispatcherResponse responseCompanies = httpClient.post(urlCompanyPost, YCompanyDispatcherResponse.class, companiesProcessing);
                respCompanies = responseCompanies.getResp();
                List<UUID> tempCompanies = responseCompanies.getTemp();

                Set<UUID> resp = new HashSet<>();
                Set<UUID> temp = new HashSet<>();
                resp.addAll(respPeople);
                resp.addAll(respCompanies);
                temp.addAll(tempPeople);
                temp.addAll(tempCompanies);

                page = onePage.stream().parallel().filter(p -> resp.contains(uuidMap.get(p.getId())))
                        .collect(Collectors.toList());

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

                page.forEach(r -> {
                    if (StringUtils.isNotBlank(r.getInn()))
                        innsSet.add(Long.parseLong(r.getInn()));
                    if (StringUtils.isNotBlank(r.getEdrpou()))
                        edrpouSet.add(Long.parseLong(r.getEdrpou()));
                    if (StringUtils.isNotBlank(r.getEdrpouRelationCompany()))
                        edrpouSet.add(Long.parseLong(r.getEdrpouRelationCompany()));
                    if (StringUtils.isNotBlank(r.getPdv()))
                        pdvsSet.add(Long.parseLong(r.getPdv()));
                });

                Set<YINN> inns = yinnRepository.findInns(innsSet);

                if (!pdvsSet.isEmpty()) {
                    savedCompanies.addAll(companyRepository.findWithPdvCompanies(pdvsSet));
                    savedCompanyRelation.addAll(yCompanyRelationRepository.findRelationByPdvs(pdvsSet));
                    savedCompanyRelationCompanies.addAll(yCompanyRelationCompanyRepository.findRelationByPdv(pdvsSet));
                }
                if (!edrpouSet.isEmpty()) {
                    savedCompanies.addAll(companyRepository.findWithEdrpouCompanies(edrpouSet));
                    savedCompanyRelation.addAll(yCompanyRelationRepository.findRelationByEdrpous(edrpouSet));
                    savedCompanyRelationCompanies.addAll(yCompanyRelationCompanyRepository.findRelationWithRelationCompanyByEdrpou(edrpouSet));
                    savedCompanyRelationCompanies.addAll(yCompanyRelationCompanyRepository.findRelationByEdrpou(edrpouSet));
                }

                Set<YPerson> savedPersonSet = new HashSet<>();

                if (!inns.isEmpty()) {
                    savedPersonSet = ypr.findPeopleInns(innsSet);
                    savedCompanyRelation.addAll(yCompanyRelationRepository.findRelationByInns(innsSet));
                }

                Set<YPerson> savedPeople = savedPersonSet;

                page.forEach(r -> {
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
                            person = extender.addInn(Long.parseLong(inn), personSet, source, person, inns, savedPeople);
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

                    counter[0]++;
                    statusChanger.addProcessedVolume(1);
                });

                UUID dispatcherIdFinish = httpClient.get(urlCompanyPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {
                    ypr.saveAll(personSet);
                    peopleSet.addAll(personSet);

                    if (!respPeople.isEmpty())
                        httpClient.post(urlPersonDelete, Boolean.class, respPeople);

                    companyRepository.saveAll(companies);
                    companiesSet.addAll(companies);

                    companyRepository.saveAll(companiesCreators);
                    companiesSet.addAll(companiesCreators);

                    if (!respCompanies.isEmpty())
                        httpClient.post(urlCompanyDelete, Boolean.class, respCompanies);

                    yCompanyRelationRepository.saveAll(yCompanyRelationSet);

                    yCompanyRelationCompanyRepository.saveAll(yCompanyRelationCompaniesSet);

                    page = onePage.stream().parallel().filter(p -> temp.contains(uuidMap.get(p.getId()))).collect(Collectors.toList());
                } else {
                    counter[0] -= resp.size();
                    statusChanger.setProcessedVolume(counter[0]);
                }
            }

            emnService.enrichYPersonMonitoringNotification(peopleSet);
            emnService.enrichYCompanyMonitoringNotification(companiesSet);

            onePage = manualCompanyRepository.findAllByUuid(file, pageRequest);
        }

        logFinish(MANUAL_COMPANY, counter[0]);
        logger.finish();

        statusChanger.complete(importedRecords(counter[0]));
    }

    @Override
    @PreDestroy
    public void deleteResp() {
        httpClient.post(urlPersonDelete, Boolean.class, respPeople);
        httpClient.post(urlCompanyDelete, Boolean.class, respCompanies);
    }
}

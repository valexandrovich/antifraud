package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.BASE_DIRECTOR;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.ALL_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.DIRECTOR;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.TAG_TYPE_ID;
import static ua.com.solidity.util.validator.Validator.isValidEdrpou;
import static ua.com.solidity.util.validator.Validator.isValidInn;

import java.util.HashSet;
import java.util.List;
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
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.BaseDirector;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyRelation;
import ua.com.solidity.db.entities.YCompanyRole;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YCompanyRelationRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YCompanyRoleRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.repository.BaseDirectorRepository;
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
public class BaseDirectorEnricher implements Enricher {

    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final BaseDirectorRepository baseDirectorRepository;
    private final ImportSourceRepository isr;
    private final YCompanyRelationRepository yCompanyRelationRepository;
    private final YINNRepository yinnRepository;
    private final YPersonRepository ypr;
    private final YCompanyRepository companyRepository;
    private final YCompanyRoleRepository companyRoleRepository;
    private final HttpClient httpClient;
    private final MonitoringNotificationService emnService;
    private final TagTypeRepository tagTypeRepository;

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
    public void enrich(UUID portion) {
        logStart(BASE_DIRECTOR);

        StatusChanger statusChanger = new StatusChanger(portion, BASE_DIRECTOR, ENRICHER);

        long[] counter = new long[1];
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BaseDirector> onePage = baseDirectorRepository.findAllByPortionId(portion, pageRequest);
        long count = baseDirectorRepository.countAllByPortionId(portion);
        statusChanger.newStage(null, "enriching", count, null);
        String fileName = fileFormatUtil.getLogFileName(portion.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                                                           Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_DIRECTOR, portion));

        ImportSource source = isr.findImportSourceByName(BASE_DIRECTOR);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<BaseDirector> page = onePage.toList();

            while (!page.isEmpty()) {
                List<YPersonProcessing> peopleProcessing = page.parallelStream().map(p -> {
                    YPersonProcessing personProcessing = new YPersonProcessing();
                    personProcessing.setUuid(p.getId());
                    if (StringUtils.isNotBlank(p.getInn()) && p.getInn().matches(ALL_NUMBER_REGEX))
                        personProcessing.setInn(Long.valueOf(p.getInn()));
                    return personProcessing;
                }).collect(Collectors.toList());
                List<YCompanyProcessing> companiesProcessing = page.parallelStream().map(c -> {
                    YCompanyProcessing companyProcessing = new YCompanyProcessing();
                    companyProcessing.setUuid(c.getId());
                    if (StringUtils.isNotBlank(c.getOkpo()) && c.getOkpo().matches(ALL_NUMBER_REGEX))
                        companyProcessing.setEdrpou(Long.valueOf(c.getOkpo()));
                    return companyProcessing;
                }).collect(Collectors.toList());

                UUID dispatcherId = httpClient.get(urlCompanyPost, UUID.class);

                YPersonDispatcherResponse response = httpClient.post(urlPersonPost, YPersonDispatcherResponse.class, peopleProcessing);
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

                page = onePage.stream().parallel().filter(p -> resp.contains(p.getId()))
                        .collect(Collectors.toList());

                Set<YCompanyRelation> yCompanyRelationSet = new HashSet<>();
                Set<Long> peopleCodes = new HashSet<>();
                Set<Long> companiesCodes = new HashSet<>();

                Set<YINN> inns = new HashSet<>();
                Set<YCompany> companies = new HashSet<>();
                Set<YCompanyRelation> companiesRelations = new HashSet<>();

                page.forEach(r -> {
                    if (StringUtils.isNotBlank(r.getInn()))
                        peopleCodes.add(Long.parseLong(r.getInn()));

                    if (StringUtils.isNotBlank(r.getOkpo()))
                        companiesCodes.add(Long.parseLong(r.getOkpo()));
                });

                if (!peopleCodes.isEmpty()) {
                    inns = yinnRepository.findInns(peopleCodes);
                    companiesRelations = yCompanyRelationRepository.findRelationByInns(peopleCodes);
                }
                if (!companiesCodes.isEmpty()) {
                    companies = companyRepository.findWithEdrpouCompanies(companiesCodes);
                    companiesRelations.addAll(yCompanyRelationRepository.findRelationByEdrpous(companiesCodes));
                }
                Set<YPerson> savedPersonSet = new HashSet<>();
                if (!inns.isEmpty())
                    savedPersonSet = ypr.findPeopleInnsForBaseEnricher(peopleCodes);
                Set<YPerson> savedPeople = savedPersonSet;
                Set<YPerson> personSet = new HashSet<>();
                Set<YCompany> companySet = new HashSet<>();

                Set<YCompany> finalCompanies = companies;
                Set<YINN> finalInns = inns;
                Set<YCompanyRelation> finalCompaniesRelations = companiesRelations;
                Optional<TagType> tagType = tagTypeRepository.findByCode(TAG_TYPE_ID);
                page.forEach(r -> {
                    YPerson person = null;
                    if (StringUtils.isNotBlank(r.getInn()) && r.getInn().matches(CONTAINS_NUMERAL_REGEX)) {
                        String inn = r.getInn().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        if (isValidInn(inn, null)) {
                            person = new YPerson();
                            person = extender.addInn(Long.parseLong(inn), personSet, source, person, finalInns, savedPeople);
                            if (person.getId() == null)
                                person.setId(UUID.randomUUID());

                            Set<YTag> tags = new HashSet<>();
                            YTag tag = new YTag();
                            tagType.ifPresent(tag::setTagType);
                            tag.setSource(DIRECTOR);
                            tags.add(tag);

                            extender.addTags(person, tags, source);

                            personSet.add(person);
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getInn()), "Wrong INN");
                            wrongCounter[0]++;
                        }
                    }
                    YCompany company = null;
                    if (StringUtils.isNotBlank(r.getOkpo()) && r.getOkpo().matches(CONTAINS_NUMERAL_REGEX)) {
                        String edrpou = r.getOkpo().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        if (isValidEdrpou(edrpou)) {
                            company = new YCompany();
                            company.setEdrpou(Long.parseLong(edrpou));
                            company = extender.addCompany(companySet, source, company, finalCompanies);
                            companySet.add(company);
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("OKPO: {}", r.getOkpo()), "Wrong OKPO");
                            wrongCounter[0]++;
                        }
                    }
                    Optional<YCompanyRole> role = companyRoleRepository.findByRole(DIRECTOR);
                    if (company != null && person != null && role.isPresent()) {
                        extender.addCompanyRelation(person, company, role.get(), source, yCompanyRelationSet, finalCompaniesRelations);
                    }

                    counter[0]++;
                    statusChanger.addProcessedVolume(1);
                });

                UUID dispatcherIdFinish = httpClient.get(urlCompanyPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {
                    emnService.enrichYPersonPackageMonitoringNotification(personSet);
                    ypr.saveAll(personSet);

                    httpClient.post(urlPersonDelete, Boolean.class, respPeople);

                    companyRepository.saveAll(companySet);

                    emnService.enrichYPersonMonitoringNotification(personSet);
                    emnService.enrichYCompanyMonitoringNotification(companySet);

                    httpClient.post(urlCompanyDelete, Boolean.class, respCompanies);

                    yCompanyRelationRepository.saveAll(yCompanyRelationSet);

                    page = onePage.stream().parallel().filter(p -> temp.contains(p.getId())).collect(Collectors.toList());
                } else {
                    counter[0] -= resp.size();
                    statusChanger.setProcessedVolume(counter[0]);
                }
            }

            onePage = baseDirectorRepository.findAllByPortionId(portion, pageRequest);
        }

        logFinish(BASE_DIRECTOR, counter[0]);
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
